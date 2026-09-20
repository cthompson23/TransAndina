-- Quita lo que dejó demostracion.sql.
--
-- Borra los mantenimientos de demostración (los que tienen descripción
-- "Demo: ...") con sus alertas, y el aviso de gerencia de demostración.
--
-- NO borra el historial de kilometraje ni las fechas de documentos: son
-- datos que el equipo puede querer conservar. Si también se quieren limpiar:
--
--   delete from public.kilometraje;   -- todo el historial, de todos
--   update public.vehiculos set fecha_marchamo = null,
--                               fecha_revision_tecnica = null,
--                               fecha_seguro = null,
--                               fecha_permiso_carga = null;

do $$
declare
    v_mantenimientos int;
    v_alertas int;
begin
    delete from public.alertas a
    using public.mantenimientos m
    where a.tipo = 'mantenimiento_confirmado'
      and a.vehiculo_id = m.vehiculo_id
      and m.descripcion like 'Demo:%';
    get diagnostics v_alertas = row_count;

    delete from public.mantenimientos where descripcion like 'Demo:%';
    get diagnostics v_mantenimientos = row_count;

    delete from public.alertas where tipo = 'gerencia' and mensaje like 'Demo:%';

    raise notice 'Borrados: % mantenimientos y % alertas.', v_mantenimientos, v_alertas;
end
$$;
