-- PROPUESTA — NO APLICADA. Requiere revisión del equipo antes de correrla.
--
-- Rol mecánico (decisiones del 20/09/2026, con el enunciado y la rúbrica del
-- curso a la vista):
--
--   1. Un vehículo tiene UN mecánico responsable; un mecánico puede tener
--      VARIOS vehículos a cargo. Se resuelve con una columna, no con una
--      tabla intermedia.
--   2. El mecánico solo ve, registra y recibe alertas de LOS VEHÍCULOS QUE
--      TIENE ASIGNADOS. Hoy ve toda la flotilla; eso se cierra aquí.
--   3. La asignación la puede hacer el encargado (desde el formulario del
--      vehículo) y también el conductor del vehículo (desde su detalle).
--      Como `vehiculos_update` es solo del encargado, el conductor pasa por
--      la función `asignar_mecanico`.
--   4. El mecánico sí puede registrar kilometraje de sus vehículos: si no,
--      no podría registrar un servicio con más kilómetros que la última
--      lectura guardada (misma razón que 202609172200 para el encargado).
--
-- Depende de 202609152200 (columna `taller` y bucket) y de 202609171900
-- (estado de cuenta, cuenta_activa()). Se ejecuta completa en el SQL Editor.

-- ---------------------------------------------------------------------------
-- 1. Mecánico responsable del vehículo
-- ---------------------------------------------------------------------------

alter table public.vehiculos
    add column if not exists mecanico_id uuid references public.usuarios (id);

comment on column public.vehiculos.mecanico_id is
    'Mecánico responsable del mantenimiento de esta unidad. Nullable: un vehículo puede no tener mecánico asignado.';

create index if not exists idx_vehiculos_mecanico
    on public.vehiculos (mecanico_id);

-- ---------------------------------------------------------------------------
-- 2. Catálogo de mecánicos para los selectores de la app
-- ---------------------------------------------------------------------------
-- `usuarios_select` solo le devuelve su propia fila al conductor y al
-- mecánico, así que ninguno de los dos puede armar la lista para elegir. En
-- vez de abrir la tabla entera, esta función devuelve únicamente id y nombre
-- de los mecánicos activos. Sin correo, ni cédula, ni teléfono.

create or replace function public.mecanicos_disponibles()
returns table (id uuid, nombre_completo text)
language sql
stable
security definer
set search_path = public
as $$
    select u.id, u.nombre_completo
    from usuarios u
    where u.rol = 'mecanico'
      and u.estado = 'activo'
      and cuenta_activa()
    order by u.nombre_completo;
$$;

revoke execute on function public.mecanicos_disponibles() from public, anon;
grant execute on function public.mecanicos_disponibles() to authenticated;

-- ---------------------------------------------------------------------------
-- 3. Asignar el mecánico (encargado o conductor del vehículo)
-- ---------------------------------------------------------------------------
-- El encargado también puede hacerlo con un UPDATE normal desde el
-- formulario del vehículo; esta función existe para el conductor, que no
-- tiene permiso de UPDATE sobre `vehiculos`. Las dos rutas terminan en el
-- mismo UPDATE, así que el trigger de avisos de abajo se dispara igual.

create or replace function public.asignar_mecanico(
    p_vehiculo_id uuid,
    p_mecanico_id uuid
)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    v_conductor uuid;
begin
    if not cuenta_activa() then
        raise exception 'Tu cuenta no está activa';
    end if;

    select conductor_id into v_conductor
    from vehiculos
    where id = p_vehiculo_id
    for update;

    if not found then
        raise exception 'El vehículo no existe';
    end if;

    if not es_encargado() and v_conductor is distinct from auth.uid() then
        raise exception 'Solo el encargado o el conductor asignado pueden cambiar el mecánico';
    end if;

    if p_mecanico_id is not null and not exists (
        select 1 from usuarios
        where id = p_mecanico_id
          and rol = 'mecanico'
          and estado = 'activo'
    ) then
        raise exception 'El mecánico elegido debe tener rol mecánico y la cuenta activa';
    end if;

    update vehiculos
    set mecanico_id = p_mecanico_id
    where id = p_vehiculo_id;
end;
$$;

revoke execute on function public.asignar_mecanico(uuid, uuid) from public, anon;
grant execute on function public.asignar_mecanico(uuid, uuid) to authenticated;

-- ---------------------------------------------------------------------------
-- 4. Avisos al mecánico
-- ---------------------------------------------------------------------------
-- "El mecánico debe recibir notificaciones acerca de los carros asignados a
-- él": las de documentos y mantenimiento próximo las calcula la app sobre
-- sus vehículos; las que ocurren una sola vez se guardan aquí.

create or replace function public.crear_alerta_mecanico()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    if new.mecanico_id is distinct from old.mecanico_id then
        if new.mecanico_id is not null then
            insert into alertas (tipo, vehiculo_id, usuario_id, mensaje)
            values (
                'reasignacion',
                new.id,
                new.mecanico_id,
                'Quedaste a cargo del mantenimiento del vehículo con placa ' || new.placa
            );
        end if;

        if old.mecanico_id is not null then
            insert into alertas (tipo, vehiculo_id, usuario_id, mensaje)
            values (
                'reasignacion',
                new.id,
                old.mecanico_id,
                'Ya no tienes a cargo el vehículo con placa ' || new.placa
            );
        end if;
    end if;
    return new;
end;
$$;

drop trigger if exists trg_alerta_mecanico on public.vehiculos;
create trigger trg_alerta_mecanico
    after update of mecanico_id on public.vehiculos
    for each row
    execute function public.crear_alerta_mecanico();

-- Antes, la confirmación de "mantenimiento registrado" le llegaba solo a
-- quien lo registró. Ahora también al conductor y al mecánico del vehículo,
-- que son quienes necesitan enterarse. `distinct` evita repetir el aviso
-- cuando el que registra es uno de ellos.
create or replace function public.crear_alerta_confirmacion()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    v_placa text;
    v_conductor uuid;
    v_mecanico uuid;
begin
    select placa, conductor_id, mecanico_id
    into v_placa, v_conductor, v_mecanico
    from vehiculos
    where id = new.vehiculo_id;

    insert into alertas (tipo, vehiculo_id, usuario_id, mensaje)
    select
        'mantenimiento_confirmado',
        new.vehiculo_id,
        d.destinatario,
        'Se registró un mantenimiento de categoría ' || new.categoria ||
            ' en el vehículo ' || coalesce(v_placa, 'sin placa')
    from (
        select distinct unnest(array[new.registrado_por, v_conductor, v_mecanico]) as destinatario
    ) d
    where d.destinatario is not null;

    return new;
end;
$$;

-- Al desactivar una cuenta de mecánico, sus vehículos quedan sin mecánico.
-- (La parte del conductor viene de 202609171900 y no cambia.)
create or replace function public.liberar_vehiculo_al_desactivar()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    if new.estado = 'desactivado' and old.estado <> 'desactivado' then
        insert into reasignaciones (
            vehiculo_id,
            conductor_anterior_id,
            conductor_nuevo_id,
            motivo,
            registrado_por
        )
        select v.id, new.id, null, 'Cuenta del conductor desactivada', auth.uid()
        from vehiculos v
        where v.conductor_id = new.id;

        update vehiculos
        set conductor_id = null
        where conductor_id = new.id;

        update vehiculos
        set mecanico_id = null
        where mecanico_id = new.id;
    end if;
    return new;
end;
$$;

-- ---------------------------------------------------------------------------
-- 5. El mecánico queda limitado a sus vehículos
-- ---------------------------------------------------------------------------
-- Hasta hoy, `rol_actual() = 'mecanico'` le daba acceso a toda la flotilla.
-- Se cambia por la pertenencia real: el vehículo es suyo si `mecanico_id`
-- es él. De paso, las políticas de mantenimientos y kilometraje dejan de
-- preguntar por el rol y preguntan por el vehículo, que es lo que importa.

drop policy if exists vehiculos_select on public.vehiculos;
create policy vehiculos_select on public.vehiculos
    for select to authenticated
    using (
        public.es_encargado()
        or conductor_id = auth.uid()
        or mecanico_id = auth.uid()
    );

drop policy if exists mantenimientos_select on public.mantenimientos;
create policy mantenimientos_select on public.mantenimientos
    for select to authenticated
    using (
        public.es_encargado()
        or vehiculo_id in (
            select v.id
            from public.vehiculos v
            where v.conductor_id = auth.uid()
               or v.mecanico_id = auth.uid()
        )
    );

drop policy if exists mantenimientos_insert on public.mantenimientos;
create policy mantenimientos_insert on public.mantenimientos
    for insert to authenticated
    with check (
        registrado_por = auth.uid()
        and (
            public.es_encargado()
            or vehiculo_id in (
                select v.id
                from public.vehiculos v
                where v.conductor_id = auth.uid()
                   or v.mecanico_id = auth.uid()
            )
        )
    );

drop policy if exists kilometraje_select on public.kilometraje;
create policy kilometraje_select on public.kilometraje
    for select to authenticated
    using (
        public.es_encargado()
        or vehiculo_id in (
            select v.id
            from public.vehiculos v
            where v.conductor_id = auth.uid()
               or v.mecanico_id = auth.uid()
        )
    );

drop policy if exists kilometraje_insert on public.kilometraje;
create policy kilometraje_insert on public.kilometraje
    for insert to authenticated
    with check (
        registrado_por = auth.uid()
        and (
            public.es_encargado()
            or vehiculo_id in (
                select v.id
                from public.vehiculos v
                where v.conductor_id = auth.uid()
                   or v.mecanico_id = auth.uid()
            )
        )
    );

-- Las fotos del bucket siguen el mismo criterio: la carpeta raíz es el id
-- del vehículo (convención de 202609152200).
drop policy if exists mantenimientos_fotos_insert on storage.objects;
create policy mantenimientos_fotos_insert on storage.objects
    for insert to authenticated
    with check (
        bucket_id = 'mantenimientos'
        and (
            public.es_encargado()
            or (storage.foldername(name))[1]::uuid in (
                select v.id
                from public.vehiculos v
                where v.conductor_id = auth.uid()
                   or v.mecanico_id = auth.uid()
            )
        )
    );

drop policy if exists mantenimientos_fotos_select on storage.objects;
create policy mantenimientos_fotos_select on storage.objects
    for select to authenticated
    using (
        bucket_id = 'mantenimientos'
        and (
            public.es_encargado()
            or (storage.foldername(name))[1]::uuid in (
                select v.id
                from public.vehiculos v
                where v.conductor_id = auth.uid()
                   or v.mecanico_id = auth.uid()
            )
        )
    );

-- ---------------------------------------------------------------------------
-- Notas para la revisión
-- ---------------------------------------------------------------------------
-- 1. Después de correrla, ningún mecánico ve vehículos hasta que el
--    encargado (o el conductor) le asigne al menos uno. Es el efecto
--    buscado, pero conviene asignar uno antes de la demo:
--      update public.vehiculos set mecanico_id = (
--          select id from public.usuarios where rol = 'mecanico' and estado = 'activo' limit 1
--      ) where placa = 'LA-PLACA-QUE-SEA';
--    Ese update dispara el aviso al mecánico, igual que desde la app.
-- 2. `mecanicos_disponibles()` la puede llamar cualquier sesión autenticada
--    y activa. Devuelve nombre e id, nada más.
-- 3. Para comprobar que quedó bien:
--      select policyname, cmd, qual from pg_policies
--      where schemaname = 'public' and tablename in
--        ('vehiculos', 'mantenimientos', 'kilometraje') order by tablename;
--      select count(*) from public.mecanicos_disponibles();
