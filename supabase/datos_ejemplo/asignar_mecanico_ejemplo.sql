-- Datos de ejemplo, NO es una migración.
--
-- Reparte los vehículos activos que no tienen mecánico entre los mecánicos
-- activos, en orden de placa, para poder demostrar el rol sin ir uno por uno.
-- Requiere la migración 202609201200_mecanico.sql.
--
-- Los avisos ("Quedaste a cargo del vehículo con placa X") los crea el
-- trigger trg_alerta_mecanico, igual que si se hiciera desde la app.
--
-- Para deshacerlo:
--   update public.vehiculos set mecanico_id = null;

do $$
declare
    v_mecanicos uuid[];
    v_total int;
    v_fila record;
    v_indice int := 0;
begin
    select array_agg(id order by nombre_completo)
    into v_mecanicos
    from public.usuarios
    where rol = 'mecanico' and estado = 'activo';

    v_total := coalesce(array_length(v_mecanicos, 1), 0);
    if v_total = 0 then
        raise notice 'No hay mecánicos activos: no se asignó nada.';
        return;
    end if;

    for v_fila in
        select id, placa
        from public.vehiculos
        where activo and mecanico_id is null
        order by placa
    loop
        update public.vehiculos
        set mecanico_id = v_mecanicos[(v_indice % v_total) + 1]
        where id = v_fila.id;
        v_indice := v_indice + 1;
    end loop;

    raise notice 'Vehículos asignados: %, entre % mecánico(s).', v_indice, v_total;
end
$$;

-- Comprobación:
--   select v.placa, u.nombre_completo as mecanico
--   from public.vehiculos v
--   left join public.usuarios u on u.id = v.mecanico_id
--   where v.activo
--   order by v.placa;
