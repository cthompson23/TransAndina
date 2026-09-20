-- PROPUESTA — NO APLICADA. Requiere revisión del equipo antes de correrla.
--
-- Corrección del 20/09/2026: en la lista de vehículos del mecánico, las
-- tarjetas decían "Sin conductor" aunque el vehículo sí tuviera uno.
--
-- Por qué pasaba
-- --------------
-- La pantalla resuelve los nombres leyendo la tabla `usuarios`, y
-- `usuarios_select` solo le devuelve su propia fila a quien no es encargado.
-- Sin nombre para ese id, la tarjeta caía en "Sin conductor", que es
-- información equivocada, no solo un dato faltante.
--
-- Qué se hace
-- -----------
-- Una función que devuelve id y nombre de las personas ligadas a los
-- vehículos que quien pregunta YA puede ver: el conductor y el mecánico de
-- cada uno. No abre la tabla `usuarios` (sin correo, cédula ni teléfono) y
-- no muestra a nadie de otro vehículo.

create or replace function public.personas_de_mis_vehiculos()
returns table (id uuid, nombre_completo text)
language sql
stable
security definer
set search_path = public
as $$
    select distinct u.id, u.nombre_completo
    from usuarios u
    join vehiculos v
      on u.id = v.conductor_id
      or u.id = v.mecanico_id
    where cuenta_activa()
      and (
          es_encargado()
          or v.conductor_id = auth.uid()
          or v.mecanico_id = auth.uid()
      );
$$;

revoke execute on function public.personas_de_mis_vehiculos() from public, anon;
grant execute on function public.personas_de_mis_vehiculos() to authenticated;

-- ---------------------------------------------------------------------------
-- Notas para la revisión
-- ---------------------------------------------------------------------------
-- 1. A diferencia de `mecanicos_disponibles()`, esta no filtra por estado de
--    la cuenta de la otra persona: si al conductor lo suspendieron, su nombre
--    sigue apareciendo en el vehículo que todavía tiene asignado.
-- 2. Para comprobarla, con la sesión de un mecánico con vehículos a cargo:
--      select * from public.personas_de_mis_vehiculos();
