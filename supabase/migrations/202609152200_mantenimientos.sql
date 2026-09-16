-- PROPUESTA — NO APLICADA. Requiere revisión del equipo antes de correrla.
--
-- Qué falta para la Parte B de la Fase 3 (registro e historial de
-- mantenimientos). Lo que YA existe y no se toca:
--
--   * public.mantenimientos    (id, vehiculo_id, registrado_por, tipo,
--                               categoria, fecha, responsable, descripcion,
--                               km, costo, created_at)
--   * public.mantenimiento_fotos (id, mantenimiento_id, storage_path)
--   * Sus políticas RLS: el mecánico registra sobre cualquier vehículo y el
--     conductor solo sobre el suyo; encargado/mecánico/conductor-dueño leen.
--   * public.frecuencias_mantenimiento, que sirve de catálogo de categorías
--     por tipo de vehículo.
--
-- Faltan dos cosas: la columna `taller` y el bucket de Storage con sus
-- políticas.

-- ---------------------------------------------------------------------------
-- 1. Columna `taller`
-- ---------------------------------------------------------------------------
-- El historial del Figma (51:125) muestra "Taller Central" y "Taller Norte"
-- como una línea propia ("SCD-3421 · Taller Central · ₡ 45 000"), pero la
-- tabla no tiene dónde guardarlo. Hoy solo hay `responsable` (la persona) y
-- `descripcion` (texto libre), y meter el taller en cualquiera de los dos
-- impide filtrar o agrupar por taller en los reportes del encargado
-- (docs/ADAPTACION_MOVIL.md §6, pantalla Reportes).
--
-- Se agrega como text nullable: los mantenimientos ya registrados quedan sin
-- taller, y el formulario puede dejarlo opcional.

alter table public.mantenimientos
    add column if not exists taller text;

comment on column public.mantenimientos.taller is
    'Nombre del taller donde se hizo el servicio. Nullable: los registros anteriores a esta migración no lo tienen.';

-- Más adelante, si el equipo quiere filtrar por taller en los reportes,
-- conviene pasar a una tabla `talleres` y una FK. Por ahora texto libre
-- alcanza y no bloquea esa migración futura.

-- ---------------------------------------------------------------------------
-- 2. Bucket de Storage para la evidencia fotográfica
-- ---------------------------------------------------------------------------
-- `mantenimiento_fotos.storage_path` asume un bucket que no está en el dump
-- (solo trae el esquema public). Se crea privado, con las mismas reglas de
-- visibilidad que la tabla `mantenimientos`.
--
-- Convención de ruta: <vehiculo_id>/<mantenimiento_id>/<uuid>.jpg
-- El primer segmento es el vehículo, para que las políticas puedan decidir
-- con storage.foldername(name).

insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values (
    'mantenimientos',
    'mantenimientos',
    false,
    5242880, -- 5 MB, el mismo tope que valida la app antes de comprimir
    array['image/jpeg', 'image/png']
)
on conflict (id) do nothing;

-- Quién puede SUBIR una foto: el mecánico (cualquier vehículo), el conductor
-- asignado a ese vehículo y el encargado. Mismo criterio que
-- mantenimientos_insert, pero mirando el vehículo del primer segmento.
drop policy if exists mantenimientos_fotos_insert on storage.objects;

create policy mantenimientos_fotos_insert on storage.objects
    for insert to authenticated
    with check (
        bucket_id = 'mantenimientos'
        and (
            public.es_encargado()
            or public.rol_actual() = 'mecanico'::public.rol_usuario
            or (storage.foldername(name))[1]::uuid in (
                select v.id from public.vehiculos v where v.conductor_id = auth.uid()
            )
        )
    );

-- Quién puede VER las fotos: los mismos que pueden ver el vehículo.
drop policy if exists mantenimientos_fotos_select on storage.objects;

create policy mantenimientos_fotos_select on storage.objects
    for select to authenticated
    using (
        bucket_id = 'mantenimientos'
        and (
            public.es_encargado()
            or public.rol_actual() = 'mecanico'::public.rol_usuario
            or (storage.foldername(name))[1]::uuid in (
                select v.id from public.vehiculos v where v.conductor_id = auth.uid()
            )
        )
    );

-- Borrar: solo quien subió el archivo (para poder reintentar una subida a
-- medias) y el encargado.
drop policy if exists mantenimientos_fotos_delete on storage.objects;

create policy mantenimientos_fotos_delete on storage.objects
    for delete to authenticated
    using (
        bucket_id = 'mantenimientos'
        and (owner = auth.uid() or public.es_encargado())
    );

-- ---------------------------------------------------------------------------
-- 3. Pendiente de decisión del equipo: la columna `km`
-- ---------------------------------------------------------------------------
-- `mantenimientos.km` es NOT NULL y el formulario del Figma (49:161) no lo
-- pide. Hay que elegir una de estas tres antes de programar el guardado:
--
--   a) La app manda `vehiculos.km_actual` del momento del registro.
--      No se agrega ningún campo, pero si el servicio se registra días
--      después el kilometraje queda inflado.
--   b) Se agrega un campo "Kilometraje del servicio" al formulario.
--      Es el dato correcto y el que necesita el cálculo del próximo
--      mantenimiento preventivo, pero se aparta del Figma.
--   c) Se hace la columna nullable:
--        alter table public.mantenimientos alter column km drop not null;
--      La más barata, pero deja sin base el cálculo por kilometraje de
--      frecuencias_mantenimiento.
--
-- Esta migración NO toca `km`: la decisión es de producto.

-- ---------------------------------------------------------------------------
-- Notas para la revisión
-- ---------------------------------------------------------------------------
-- 1. Crear el bucket por SQL requiere permisos sobre el esquema storage. Si
--    el proyecto no los da, se crea a mano desde el panel de Supabase
--    (Storage → New bucket → privado, 5 MB, image/jpeg e image/png) y se
--    aplican solo las tres políticas de arriba.
-- 2. `file_size_limit` y `allowed_mime_types` son la red de seguridad del
--    servidor; la app igual valida antes para dar un mensaje claro.
-- 3. No se pone límite de fotos por mantenimiento en la base. La app va a
--    permitir 3, que es un tope de producto, no del esquema.
