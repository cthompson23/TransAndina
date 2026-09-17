-- DATOS DE EJEMPLO — solo para probar Historial, Alertas y Reportes.
-- No es una migración: no cambia el esquema, solo agrega filas.
--
-- Qué hace
-- --------
-- Por cada vehículo activo inserta 8 mantenimientos repartidos en los
-- últimos 250 días, con talleres, responsables y costos variados. El último
-- cambio de aceite cambia de un vehículo a otro para que se vean los tres
-- estados en Flotilla y en Alertas:
--   vehículo 1, 4, 7...  → al día
--   vehículo 2, 5, 8...  → próximo (el cambio de aceite toca en ~15 días)
--   vehículo 3, 6, 9...  → atrasado
-- El kilometraje de cada servicio se calcula hacia atrás desde el
-- `km_actual` del vehículo, así que siempre es coherente con la fecha.
-- Si un vehículo tiene km_actual = 0, sus servicios quedan en 0 km.
--
-- Todas las filas llevan "[Ejemplo]" al inicio de la descripción, para
-- poder borrarlas después con borrar_mantenimientos_ejemplo.sql.
--
-- Cómo usarlo
-- -----------
-- Requiere las migraciones 202609152200 (columna taller) y 202609171910
-- (categorías). Copiar todo en el SQL Editor de Supabase y ejecutar.
-- Se puede correr una sola vez; si ya hay datos de ejemplo, se detiene.

do $$
declare
    v record;
    indice int := 0;
    total int := 0;
    encargado_id uuid;
    quien uuid;
    base numeric;
    factor numeric;
    talleres text[] := array['Taller Central', 'Taller Norte', 'Taller Sur'];
    responsables text[] := array['Marco Ureña', 'Ana Castro', 'Luis Mora'];
    dias_aceite int;
    km_aceite numeric;
begin
    if exists (select 1 from public.mantenimientos where descripcion like '[Ejemplo]%') then
        raise exception 'Ya hay datos de ejemplo. Bórralos primero con borrar_mantenimientos_ejemplo.sql';
    end if;

    select id into encargado_id
    from public.usuarios
    where rol = 'encargado' and estado = 'activo'
    order by created_at
    limit 1;

    if encargado_id is null then
        raise exception 'Se necesita al menos un encargado activo en public.usuarios';
    end if;

    for v in select * from public.vehiculos where activo order by placa loop
        -- Queda como "registrado por" el conductor asignado; si no hay, el encargado.
        quien := coalesce(v.conductor_id, encargado_id);
        base := v.km_actual;
        factor := case v.tipo when 'pesado' then 1.8 when 'especial' then 2.5 else 1 end;

        case indice % 3
            when 0 then dias_aceite := 20;  km_aceite := 400;   -- al día
            when 1 then dias_aceite := 165; km_aceite := 3300;  -- próximo por fecha
            else        dias_aceite := 200; km_aceite := 4000;  -- atrasado por fecha
        end case;

        insert into public.mantenimientos
            (vehiculo_id, registrado_por, tipo, categoria, fecha, km, responsable, taller, costo, descripcion)
        values
            (v.id, quien, 'preventivo', 'Cambio de aceite', current_date - 250,
                greatest(base - 4900, 0), responsables[1 + indice % 3], talleres[1 + indice % 3],
                round(42000 * factor), '[Ejemplo] Aceite sintético y filtro'),
            (v.id, quien, 'preventivo', 'Revisión general', current_date - 170,
                greatest(base - 3500, 0), responsables[1 + (indice + 1) % 3], talleres[1 + (indice + 1) % 3],
                round(85000 * factor), '[Ejemplo] Revisión de 30 puntos'),
            (v.id, quien, 'preventivo', 'Frenos', current_date - 150,
                greatest(base - 3000, 0), responsables[1 + (indice + 2) % 3], talleres[1 + (indice + 2) % 3],
                round(78500 * factor), '[Ejemplo] Cambio de pastillas delanteras'),
            (v.id, quien, 'correctivo', 'Llantas', current_date - 120,
                greatest(base - 2400, 0), responsables[1 + indice % 3], talleres[1 + indice % 3],
                round(320000 * factor), '[Ejemplo] Reemplazo de dos llantas por desgaste'),
            (v.id, quien, 'preventivo', 'Alineación y balanceo', current_date - 90,
                greatest(base - 1800, 0), responsables[1 + (indice + 1) % 3], talleres[1 + (indice + 1) % 3],
                round(32000 * factor), '[Ejemplo] Alineación computarizada'),
            (v.id, quien, 'correctivo', 'Otro', current_date - 45,
                greatest(base - 900, 0), responsables[1 + (indice + 2) % 3], talleres[1 + (indice + 2) % 3],
                round(96000 * factor), '[Ejemplo] Cambio de batería'),
            (v.id, quien, 'preventivo', 'Cambio de aceite', current_date - dias_aceite,
                greatest(base - km_aceite, 0), responsables[1 + indice % 3], talleres[1 + indice % 3],
                round((45000 + indice * 1500) * factor), '[Ejemplo] Aceite sintético y filtro'),
            (v.id, quien, 'correctivo', 'Frenos', current_date - 10,
                greatest(base - 150, 0), responsables[1 + (indice + 1) % 3], talleres[1 + (indice + 1) % 3],
                round(18500 * factor), '[Ejemplo] Ajuste de freno de mano');

        indice := indice + 1;
        total := total + 8;
    end loop;

    -- Cada insert disparó trg_alerta_mantenimiento y creó un aviso
    -- "Se registró un mantenimiento…". Con datos de ejemplo solo harían ruido
    -- en las alertas de los conductores. now() es la hora de inicio de esta
    -- transacción, igual para todas esas filas.
    delete from public.alertas
    where tipo = 'mantenimiento_confirmado'
      and created_at = now();

    raise notice 'Se insertaron % mantenimientos de ejemplo en % vehículos', total, indice;
end
$$;

-- Para revisar lo insertado:
--   select v.placa, m.fecha, m.tipo, m.categoria, m.km, m.taller, m.costo
--   from public.mantenimientos m
--   join public.vehiculos v on v.id = m.vehiculo_id
--   where m.descripcion like '[Ejemplo]%'
--   order by v.placa, m.fecha;
