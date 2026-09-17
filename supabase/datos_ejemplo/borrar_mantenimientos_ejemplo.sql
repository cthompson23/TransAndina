-- Borra los mantenimientos creados por mantenimientos_ejemplo.sql.
-- Solo toca las filas cuya descripción empieza con "[Ejemplo]"; las fotos
-- asociadas se borran solas (on delete cascade).

delete from public.mantenimientos
where descripcion like '[Ejemplo]%';
