-- PROPUESTA — NO APLICADA. Requiere revisión del equipo antes de correrla.
--
-- Cambios de base de datos para la vista del encargado
-- (docs/ADAPTACION_MOVIL.md §6 y §8). Decisiones tomadas con el equipo:
--
--   1. Estado de cuenta de tres valores: activo, suspendido, desactivado.
--      Suspendido es temporal; desactivado es permanente y libera el vehículo.
--   2. Una cuenta que no está activa no puede leer ni escribir datos.
--   3. "Permiso de carga" es una fecha de vencimiento más en `vehiculos`.
--   4. El encargado puede enviar alertas de gerencia.
--   5. Historial de reasignaciones con fecha efectiva y motivo.
--   6. El encargado puede crear el perfil de otro encargado (Registrar
--      administrador). La cuenta de Auth la crea la app con un cliente
--      secundario, sin cerrar la sesión del encargado actual.
--
-- No depende de 202609151900 ni de 202609152200: se puede correr antes o
-- después de ellas. Se ejecuta completa en el SQL Editor de Supabase.

-- ---------------------------------------------------------------------------
-- 1. Estado de cuenta
-- ---------------------------------------------------------------------------

do $$
begin
    create type public.estado_cuenta as enum ('activo', 'suspendido', 'desactivado');
exception
    when duplicate_object then null;
end
$$;

alter table public.usuarios
    add column if not exists estado public.estado_cuenta not null default 'activo';

comment on column public.usuarios.estado is
    'activo: uso normal. suspendido: sin acceso, reversible. desactivado: sin acceso, permanente.';

-- Quien hoy tiene activo = false pasa a suspendido, que es reversible.
update public.usuarios
set estado = 'suspendido'
where activo = false
  and estado = 'activo';

-- `activo` se conserva porque la app todavía lo lee (Usuario.activo), pero
-- deja de ser editable a mano: el trigger de abajo lo recalcula desde
-- `estado` en cada actualización.
comment on column public.usuarios.activo is
    'Espejo de estado = activo. Lo mantiene el trigger trg_proteger_campos_usuario; no escribirlo directamente.';

create or replace function public.proteger_campos_privilegiados_usuario()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    if not es_encargado() and (
        new.rol is distinct from old.rol
        or new.estado is distinct from old.estado
    ) then
        raise exception 'No tienes permiso para cambiar tu rol o estado de cuenta';
    end if;

    -- Evita que un encargado se bloquee a sí mismo por accidente.
    if new.id = auth.uid() and new.estado is distinct from old.estado then
        raise exception 'No puedes cambiar el estado de tu propia cuenta';
    end if;

    if old.estado = 'desactivado' and new.estado <> 'desactivado' then
        raise exception 'Una cuenta desactivada no se puede reactivar';
    end if;

    new.activo := (new.estado = 'activo');
    return new;
end;
$$;

-- El trigger trg_proteger_campos_usuario ya existe y apunta a esta función;
-- no hace falta recrearlo.

-- ---------------------------------------------------------------------------
-- 2. Las cuentas que no están activas pierden el acceso
-- ---------------------------------------------------------------------------
-- rol_actual() devuelve null si la cuenta no está activa. Con eso, todas las
-- políticas que preguntan por el rol (encargado, mecánico) ya bloquean a las
-- cuentas suspendidas o desactivadas.

create or replace function public.rol_actual()
returns public.rol_usuario
language sql
stable
security definer
set search_path = public
as $$
    select rol from usuarios where id = auth.uid() and estado = 'activo';
$$;

-- coalesce: sin él, un usuario sin perfil devuelve null y `not es_encargado()`
-- también es null, lo que en un IF se comporta como falso.
create or replace function public.es_encargado()
returns boolean
language sql
stable
security definer
set search_path = public
as $$
    select coalesce(rol_actual() = 'encargado', false);
$$;

-- Las políticas del conductor no usan rol_actual() (comparan conductor_id
-- con auth.uid()), así que se agrega una política RESTRICTIVA por tabla. Las
-- restrictivas se combinan con AND sobre las que ya existen.
--
-- Un usuario sin fila en `usuarios` (recién registrado) cuenta como activo,
-- para no bloquear el registro.
create or replace function public.cuenta_activa()
returns boolean
language sql
stable
security definer
set search_path = public
as $$
    select not exists (
        select 1 from usuarios where id = auth.uid() and estado <> 'activo'
    );
$$;

drop policy if exists solo_cuentas_activas on public.vehiculos;
create policy solo_cuentas_activas on public.vehiculos
    as restrictive for all to authenticated
    using (public.cuenta_activa())
    with check (public.cuenta_activa());

drop policy if exists solo_cuentas_activas on public.kilometraje;
create policy solo_cuentas_activas on public.kilometraje
    as restrictive for all to authenticated
    using (public.cuenta_activa())
    with check (public.cuenta_activa());

drop policy if exists solo_cuentas_activas on public.mantenimientos;
create policy solo_cuentas_activas on public.mantenimientos
    as restrictive for all to authenticated
    using (public.cuenta_activa())
    with check (public.cuenta_activa());

drop policy if exists solo_cuentas_activas on public.mantenimiento_fotos;
create policy solo_cuentas_activas on public.mantenimiento_fotos
    as restrictive for all to authenticated
    using (public.cuenta_activa())
    with check (public.cuenta_activa());

drop policy if exists solo_cuentas_activas on public.alertas;
create policy solo_cuentas_activas on public.alertas
    as restrictive for all to authenticated
    using (public.cuenta_activa())
    with check (public.cuenta_activa());

drop policy if exists solo_cuentas_activas on public.frecuencias_mantenimiento;
create policy solo_cuentas_activas on public.frecuencias_mantenimiento
    as restrictive for all to authenticated
    using (public.cuenta_activa())
    with check (public.cuenta_activa());

-- En `usuarios`, la persona suspendida sí puede leer SU fila: la app la
-- necesita para mostrarle "Tu cuenta está suspendida" y cerrar la sesión.
drop policy if exists solo_cuentas_activas_select on public.usuarios;
create policy solo_cuentas_activas_select on public.usuarios
    as restrictive for select to authenticated
    using (id = auth.uid() or public.cuenta_activa());

drop policy if exists solo_cuentas_activas_update on public.usuarios;
create policy solo_cuentas_activas_update on public.usuarios
    as restrictive for update to authenticated
    using (public.cuenta_activa())
    with check (public.cuenta_activa());

-- ---------------------------------------------------------------------------
-- 3. Documentos: permiso de carga
-- ---------------------------------------------------------------------------

alter table public.vehiculos
    add column if not exists fecha_permiso_carga date;

comment on column public.vehiculos.fecha_permiso_carga is
    'Vencimiento del permiso de carga. Nullable, igual que las otras fechas de documentos.';

-- ---------------------------------------------------------------------------
-- 4. Alertas de gerencia
-- ---------------------------------------------------------------------------
-- Las alertas de documentos y de mantenimiento próximo/atrasado NO se guardan:
-- la app las calcula a partir de las fechas, el kilometraje y
-- frecuencias_mantenimiento. En esta tabla quedan solo los avisos que
-- ocurren una vez (reasignación, mantenimiento registrado, gerencia).

alter type public.tipo_alerta add value if not exists 'gerencia';

drop policy if exists alertas_insert on public.alertas;
create policy alertas_insert on public.alertas
    for insert to authenticated
    with check (public.es_encargado());

-- ---------------------------------------------------------------------------
-- 5. Historial de reasignaciones
-- ---------------------------------------------------------------------------

create table if not exists public.reasignaciones (
    id uuid primary key default gen_random_uuid(),
    vehiculo_id uuid not null references public.vehiculos (id) on delete cascade,
    conductor_anterior_id uuid references public.usuarios (id) on delete set null,
    conductor_nuevo_id uuid references public.usuarios (id) on delete set null,
    fecha_efectiva date not null default current_date,
    motivo text,
    registrado_por uuid references public.usuarios (id) on delete set null,
    created_at timestamp with time zone not null default now()
);

comment on table public.reasignaciones is
    'Historial de cambios de conductor. Solo se escribe desde reasignar_conductor() y desde el trigger de cuentas desactivadas.';

create index if not exists idx_reasignaciones_vehiculo
    on public.reasignaciones (vehiculo_id, created_at desc);

alter table public.reasignaciones enable row level security;

drop policy if exists reasignaciones_select on public.reasignaciones;
create policy reasignaciones_select on public.reasignaciones
    for select to authenticated
    using (
        public.es_encargado()
        or conductor_anterior_id = auth.uid()
        or conductor_nuevo_id = auth.uid()
    );

drop policy if exists solo_cuentas_activas on public.reasignaciones;
create policy solo_cuentas_activas on public.reasignaciones
    as restrictive for all to authenticated
    using (public.cuenta_activa())
    with check (public.cuenta_activa());

-- Sin política de INSERT a propósito: la única forma de escribir es la
-- función de abajo, que cambia el vehículo y guarda el historial juntos.
--
-- La fecha efectiva es informativa: el cambio se aplica en el momento.
create or replace function public.reasignar_conductor(
    p_vehiculo_id uuid,
    p_conductor_nuevo_id uuid,
    p_fecha_efectiva date,
    p_motivo text
)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
    v_conductor_anterior uuid;
begin
    if not es_encargado() then
        raise exception 'Solo el encargado puede reasignar vehículos';
    end if;

    select conductor_id into v_conductor_anterior
    from vehiculos
    where id = p_vehiculo_id
    for update;

    if not found then
        raise exception 'El vehículo no existe';
    end if;

    if v_conductor_anterior is not distinct from p_conductor_nuevo_id then
        raise exception 'El vehículo ya está asignado a ese conductor';
    end if;

    if p_conductor_nuevo_id is not null then
        if not exists (
            select 1 from usuarios
            where id = p_conductor_nuevo_id
              and rol = 'conductor'
              and estado = 'activo'
        ) then
            raise exception 'El nuevo conductor debe tener rol conductor y la cuenta activa';
        end if;

        -- La app asume un solo vehículo activo por conductor
        -- (VehiculoRepository.obtenerVehiculoAsignado usa decodeSingleOrNull).
        if exists (
            select 1 from vehiculos
            where conductor_id = p_conductor_nuevo_id
              and activo
        ) then
            raise exception 'Ese conductor ya tiene un vehículo asignado';
        end if;
    end if;

    update vehiculos
    set conductor_id = p_conductor_nuevo_id
    where id = p_vehiculo_id;

    insert into reasignaciones (
        vehiculo_id,
        conductor_anterior_id,
        conductor_nuevo_id,
        fecha_efectiva,
        motivo,
        registrado_por
    )
    values (
        p_vehiculo_id,
        v_conductor_anterior,
        p_conductor_nuevo_id,
        coalesce(p_fecha_efectiva, current_date),
        nullif(trim(p_motivo), ''),
        auth.uid()
    );
end;
$$;

revoke execute on function public.reasignar_conductor(uuid, uuid, date, text) from public, anon;
grant execute on function public.reasignar_conductor(uuid, uuid, date, text) to authenticated;

-- Antes solo se avisaba al conductor nuevo. Ahora también al anterior,
-- porque pierde el acceso al vehículo.
create or replace function public.crear_alerta_reasignacion()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    if new.conductor_id is distinct from old.conductor_id then
        if new.conductor_id is not null then
            insert into alertas (tipo, vehiculo_id, usuario_id, mensaje)
            values (
                'reasignacion',
                new.id,
                new.conductor_id,
                'Se te asignó el vehículo con placa ' || new.placa
            );
        end if;

        if old.conductor_id is not null then
            insert into alertas (tipo, vehiculo_id, usuario_id, mensaje)
            values (
                'reasignacion',
                new.id,
                old.conductor_id,
                'Ya no tienes asignado el vehículo con placa ' || new.placa
            );
        end if;
    end if;
    return new;
end;
$$;

-- Al desactivar una cuenta se libera su vehículo y queda en el historial.
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
    end if;
    return new;
end;
$$;

drop trigger if exists trg_liberar_vehiculo_al_desactivar on public.usuarios;
create trigger trg_liberar_vehiculo_al_desactivar
    after update of estado on public.usuarios
    for each row
    execute function public.liberar_vehiculo_al_desactivar();

-- ---------------------------------------------------------------------------
-- 6. Registrar administrador
-- ---------------------------------------------------------------------------
-- Flujo en la app:
--   a) Un cliente de Supabase secundario (sin guardar sesión) hace signUp con
--      el correo y la contraseña del nuevo encargado. La sesión del encargado
--      actual no cambia.
--   b) Con el id que devuelve ese signUp, el cliente principal hace un UPSERT
--      en `usuarios` con rol = 'encargado'.
--
-- Es UPSERT y no INSERT para que siga funcionando si algún día se aplica el
-- trigger de 202609151900, que crea la fila antes. El upsert necesita pasar
-- las políticas de INSERT y de UPDATE; usuarios_update ya permite al
-- encargado, así que solo falta esta:

drop policy if exists usuarios_insert_encargado on public.usuarios;
create policy usuarios_insert_encargado on public.usuarios
    for insert to authenticated
    with check (public.es_encargado());

-- ---------------------------------------------------------------------------
-- Notas para la revisión
-- ---------------------------------------------------------------------------
-- 1. Supabase Auth sigue dejando iniciar sesión a una cuenta suspendida (la
--    contraseña es válida). La base no le devuelve datos, y la app revisa
--    `estado` después del login para mostrar el mensaje y cerrar la sesión.
-- 2. `alter type ... add value` funciona dentro de la transacción del SQL
--    Editor porque este script no usa el valor 'gerencia' en ningún lado.
-- 3. Las políticas de Storage de 202609152200 no pasan por cuenta_activa():
--    un conductor suspendido podría seguir leyendo las fotos de su vehículo si
--    conoce la ruta. Se acepta por ahora; si se quiere cerrar, se agrega
--    `and public.cuenta_activa()` a esas políticas después de correr esta
--    migración.
-- 4. Para comprobar que quedó bien:
--      select estado, activo, count(*) from public.usuarios group by 1, 2;
--      select policyname, permissive, cmd from pg_policies
--      where schemaname = 'public' order by tablename, policyname;
