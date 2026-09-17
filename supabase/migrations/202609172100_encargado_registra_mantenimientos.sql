-- PROPUESTA — NO APLICADA. Requiere revisión del equipo antes de correrla.
--
-- Decisión del 17/09/2026: el encargado también puede registrar
-- mantenimientos, sobre cualquier vehículo de la flotilla.
--
-- Hoy `mantenimientos_insert` solo deja insertar al mecánico (cualquier
-- vehículo) y al conductor (solo el suyo). Se recrea la política agregando
-- al encargado. El resto de reglas no cambia:
--   * registrado_por siempre es quien tiene la sesión;
--   * la política restrictiva solo_cuentas_activas (202609171900) sigue
--     bloqueando a las cuentas suspendidas o desactivadas.
--
-- Las fotos no necesitan cambios: fotos_insert exige que el mantenimiento lo
-- haya registrado la misma persona, y el bucket `mantenimientos`
-- (202609152200) ya deja subir al encargado.

drop policy if exists mantenimientos_insert on public.mantenimientos;

create policy mantenimientos_insert on public.mantenimientos
    for insert to authenticated
    with check (
        registrado_por = auth.uid()
        and (
            public.es_encargado()
            or public.rol_actual() = 'mecanico'::public.rol_usuario
            or (
                public.rol_actual() = 'conductor'::public.rol_usuario
                and vehiculo_id in (
                    select vehiculos.id
                    from public.vehiculos
                    where vehiculos.conductor_id = auth.uid()
                )
            )
        )
    );
