-- Datos de demostración, NO es una migración.
--
-- Deja la flotilla lista para enseñar la app: documentos con fechas variadas
-- (para que salgan alertas críticas y próximas), historial de kilometraje de
-- los últimos 6 meses (para la gráfica) y mantenimientos que producen los
-- tres estados del semáforo: atrasado, próximo y al día.
--
-- No toca las fotos: el bucket guarda los archivos fuera de Postgres, así que
-- una fila de `mantenimiento_fotos` sin archivo se vería como imagen rota.
-- Las fotos se registran desde la app, que es además lo que conviene
-- demostrar en vivo.
--
-- Se puede correr varias veces: primero borra lo que dejó la corrida
-- anterior (sus mantenimientos llevan la descripción "Demo: ...").
--
-- Requiere: 202609171910 (frecuencias) y 202609201200 (mecánico).
-- Para revertirlo: borrar_demostracion.sql

do $$
declare
    -- now() es constante dentro de la transacción y es lo que queda en
    -- alertas.created_at, así que sirve de marca de "esta corrida".
    -- clock_timestamp() no: avanza, y dejaría fuera las filas ya insertadas.
    v_inicio timestamptz := now();
    v_encargado uuid;
    v_vehiculo record;
    v_frecuencia record;
    v_indice int := 0;
    v_estado int;
    v_registrador uuid;
    v_responsable text;
    v_km numeric;
    v_fecha date;
    v_paso numeric;
    v_talleres text[] := array['Taller Central', 'Taller Norte', 'Lubricentro del Valle', 'Servicio Andino'];
    v_total_mant int := 0;
begin
    select id into v_encargado
    from public.usuarios
    where rol = 'encargado' and estado = 'activo'
    order by created_at
    limit 1;

    if v_encargado is null then
        raise exception 'No hay ningún encargado activo: cree uno antes de correr la demostración.';
    end if;

    if not exists (select 1 from public.frecuencias_mantenimiento) then
        raise exception 'La tabla frecuencias_mantenimiento está vacía: corra antes la migración 202609171910.';
    end if;

    -- Limpia la corrida anterior (las fotos se van por la FK en cascada).
    delete from public.mantenimientos where descripcion like 'Demo:%';

    for v_vehiculo in
        select * from public.vehiculos where activo order by placa
    loop
        v_estado := v_indice % 3;
        v_registrador := coalesce(v_vehiculo.conductor_id, v_encargado);

        select u.nombre_completo into v_responsable
        from public.usuarios u
        where u.id = v_vehiculo.mecanico_id;
        v_responsable := coalesce(v_responsable, 'Encargado de taller');

        -- -------------------------------------------------------------
        -- 1. Documentos: uno vencido, uno por vencer, uno al día
        -- -------------------------------------------------------------
        if v_estado = 0 then
            -- Crítico: el marchamo venció hace 6 días.
            update public.vehiculos set
                fecha_marchamo = current_date - 6,
                fecha_revision_tecnica = current_date + 45,
                fecha_seguro = current_date + 210,
                fecha_permiso_carga = current_date + 300
            where id = v_vehiculo.id;
        elsif v_estado = 1 then
            -- Crítico por proximidad: la revisión técnica vence en 5 días.
            update public.vehiculos set
                fecha_marchamo = current_date + 95,
                fecha_revision_tecnica = current_date + 5,
                fecha_seguro = current_date + 160,
                fecha_permiso_carga = current_date + 240
            where id = v_vehiculo.id;
        else
            -- Al día, con el seguro entrando en "próximo" (12 días).
            update public.vehiculos set
                fecha_marchamo = current_date + 180,
                fecha_revision_tecnica = current_date + 120,
                fecha_seguro = current_date + 12,
                fecha_permiso_carga = current_date + 330
            where id = v_vehiculo.id;
        end if;

        -- -------------------------------------------------------------
        -- 2. Historial de kilometraje de los últimos 6 meses
        -- -------------------------------------------------------------
        -- Los triggers se apagan a propósito: `trg_validar_km` exige que cada
        -- lectura sea mayor a la última y `trg_actualizar_km` movería
        -- km_actual. Aquí se está sembrando historia hacia atrás, no
        -- registrando lecturas nuevas.
        if (select count(*) from public.kilometraje k where k.vehiculo_id = v_vehiculo.id) < 3 then
            v_paso := greatest(round(v_vehiculo.km_actual * 0.02), 150);

            alter table public.kilometraje disable trigger trg_validar_km;
            alter table public.kilometraje disable trigger trg_actualizar_km;

            insert into public.kilometraje (vehiculo_id, registrado_por, fecha, km)
            select
                v_vehiculo.id,
                v_registrador,
                (date_trunc('month', current_date) - (interval '1 month' * g) + interval '5 days')::date,
                greatest(v_vehiculo.km_actual - (g * v_paso), 0)
            from generate_series(6, 1, -1) as g;

            insert into public.kilometraje (vehiculo_id, registrado_por, fecha, km)
            values (v_vehiculo.id, v_registrador, current_date, v_vehiculo.km_actual);

            alter table public.kilometraje enable trigger trg_validar_km;
            alter table public.kilometraje enable trigger trg_actualizar_km;
        end if;

        -- -------------------------------------------------------------
        -- 3. Mantenimientos: el último de cada categoría define el semáforo
        -- -------------------------------------------------------------
        for v_frecuencia in
            select * from public.frecuencias_mantenimiento
            where tipo_vehiculo = v_vehiculo.tipo
            order by categoria
        loop
            if v_estado = 0 then
                -- Atrasado: se pasó de kilómetros y de días.
                v_km := v_vehiculo.km_actual - coalesce(v_frecuencia.km_frecuencia, 5000) - 1500;
                v_fecha := current_date - coalesce(v_frecuencia.dias_frecuencia, 180) - 25;
            elsif v_estado = 1 then
                -- Próximo: le faltan unos 600 km.
                v_km := v_vehiculo.km_actual - coalesce(v_frecuencia.km_frecuencia, 5000) + 600;
                v_fecha := current_date - greatest(coalesce(v_frecuencia.dias_frecuencia, 180) - 10, 5);
            else
                -- Al día: recién atendido.
                v_km := v_vehiculo.km_actual - 300;
                v_fecha := current_date - 7;
            end if;

            insert into public.mantenimientos
                (vehiculo_id, registrado_por, tipo, categoria, fecha, km, responsable, descripcion, costo, taller)
            values (
                v_vehiculo.id,
                v_registrador,
                'preventivo',
                v_frecuencia.categoria,
                v_fecha,
                greatest(v_km, 0),
                v_responsable,
                'Demo: ' || v_frecuencia.categoria || ' de rutina',
                (25000 + (random() * 60000))::numeric(10, 0),
                v_talleres[1 + (v_indice % array_length(v_talleres, 1))]
            );
            v_total_mant := v_total_mant + 1;
        end loop;

        -- Dos servicios correctivos viejos, para que el historial y los
        -- reportes tengan de dónde sacar costos y rangos de fechas.
        insert into public.mantenimientos
            (vehiculo_id, registrado_por, tipo, categoria, fecha, km, responsable, descripcion, costo, taller)
        select
            v_vehiculo.id,
            v_registrador,
            'correctivo',
            (array['Frenos', 'Sistema eléctrico', 'Suspensión', 'Llantas'])[1 + ((v_indice + g) % 4)],
            (current_date - (interval '1 month' * (g * 2 + 1)))::date,
            greatest(v_vehiculo.km_actual - (g * 4200), 0),
            v_responsable,
            'Demo: reparación correctiva',
            (48000 + (random() * 180000))::numeric(10, 0),
            v_talleres[1 + ((v_indice + g) % array_length(v_talleres, 1))]
        from generate_series(1, 2) as g;
        v_total_mant := v_total_mant + 2;

        v_indice := v_indice + 1;
    end loop;

    -- Los avisos de "mantenimiento registrado" que disparó esta siembra son
    -- ruido: en la demo interesan los que se generan en vivo.
    delete from public.alertas
    where tipo = 'mantenimiento_confirmado'
      and created_at >= v_inicio;

    -- Un aviso de gerencia, para que el centro de alertas no salga vacío.
    delete from public.alertas where tipo = 'gerencia' and mensaje like 'Demo:%';
    insert into public.alertas (tipo, usuario_id, mensaje)
    select 'gerencia', u.id, 'Demo: recuerden registrar el kilometraje antes del viernes.'
    from public.usuarios u
    where u.estado = 'activo';

    raise notice 'Listo: % vehículos, % mantenimientos.', v_indice, v_total_mant;
end
$$;

-- Comprobación rápida:
--   select v.placa, v.km_actual,
--          v.fecha_marchamo, v.fecha_revision_tecnica, v.fecha_seguro,
--          (select count(*) from public.mantenimientos m where m.vehiculo_id = v.id) as mantenimientos,
--          (select count(*) from public.kilometraje k where k.vehiculo_id = v.id) as lecturas
--   from public.vehiculos v
--   where v.activo
--   order by v.placa;
