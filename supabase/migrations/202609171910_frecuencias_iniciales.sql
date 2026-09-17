-- PROPUESTA — NO APLICADA. Requiere revisión del equipo antes de correrla.
--
-- `frecuencias_mantenimiento` está vacía en la base (consulta del
-- 17/09/2026), así que la app no tiene con qué estimar el próximo
-- mantenimiento ni qué categorías ofrecer en el formulario.
--
-- Esta tabla será el catálogo único de categorías: el formulario de
-- mantenimiento (Figma 49:161) las lee de aquí según el tipo de vehículo.
--
-- Cálculo del próximo mantenimiento (lo hace la app): para cada categoría,
-- el último mantenimiento de esa categoría + km_frecuencia o + dias_frecuencia,
-- lo que ocurra primero.
--
-- LOS VALORES SON UNA PROPUESTA. Ajustarlos a lo que diga el Figma o el
-- equipo antes de correr el script. "Otro" no tiene frecuencia: sirve para
-- los correctivos que no encajan en ninguna categoría.

insert into public.frecuencias_mantenimiento (tipo_vehiculo, categoria, km_frecuencia, dias_frecuencia)
values
    ('liviano',  'Cambio de aceite',      5000,  180),
    ('liviano',  'Frenos',                20000, 365),
    ('liviano',  'Llantas',               10000, 180),
    ('liviano',  'Alineación y balanceo', 10000, 180),
    ('liviano',  'Revisión general',      10000, 365),
    ('liviano',  'Otro',                  null,  null),

    ('pesado',   'Cambio de aceite',      10000, 90),
    ('pesado',   'Frenos',                15000, 180),
    ('pesado',   'Llantas',               20000, 180),
    ('pesado',   'Alineación y balanceo', 20000, 180),
    ('pesado',   'Revisión general',      20000, 180),
    ('pesado',   'Otro',                  null,  null),

    ('especial', 'Cambio de aceite',      5000,  90),
    ('especial', 'Frenos',                10000, 180),
    ('especial', 'Llantas',               10000, 180),
    ('especial', 'Alineación y balanceo', 10000, 180),
    ('especial', 'Revisión general',      10000, 180),
    ('especial', 'Otro',                  null,  null)
on conflict (tipo_vehiculo, categoria) do nothing;
