-- PROPUESTA — NO APLICADA. Requiere revisión del equipo antes de correrla.
--
-- Decisión del 17/09/2026: el encargado también puede registrar el
-- kilometraje de cualquier vehículo.
--
-- Por qué
-- -------
-- Hoy `kilometraje_insert` solo deja registrar al conductor asignado. Eso
-- deja dos huecos:
--   * un vehículo SIN conductor (recién registrado o liberado por una
--     reasignación) no puede actualizar nunca su kilometraje, y sin
--     kilometraje las alertas de mantenimiento por km de esa unidad no
--     sirven;
--   * si el conductor olvida registrarlo, nadie más puede hacerlo, y el
--     encargado tampoco puede registrar un mantenimiento con un kilometraje
--     mayor al que quedó guardado.
--
-- Qué NO cambia
-- -------------
--   * `registrado_por` sigue siendo quien tiene la sesión, así que en la
--     tabla queda quién hizo cada lectura.
--   * El trigger `trg_validar_km` sigue exigiendo que cada lectura sea mayor
--     a la anterior: nadie puede devolver el odómetro, ni el encargado.
--   * El conductor conserva su permiso sobre el vehículo asignado.
--   * El mecánico sigue sin poder registrar kilometraje, mientras el equipo
--     no defina ese rol.
--   * La política restrictiva solo_cuentas_activas (202609171900) sigue
--     bloqueando a las cuentas suspendidas o desactivadas.

drop policy if exists kilometraje_insert on public.kilometraje;

create policy kilometraje_insert on public.kilometraje
    for insert to authenticated
    with check (
        registrado_por = auth.uid()
        and (
            public.es_encargado()
            or vehiculo_id in (
                select vehiculos.id
                from public.vehiculos
                where vehiculos.conductor_id = auth.uid()
            )
        )
    );
