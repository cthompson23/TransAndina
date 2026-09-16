-- PROPUESTA — NO APLICADA. Requiere revisión del equipo antes de correrla.
--
-- Problema
-- --------
-- RegistroViewModel.registrar() hace dos pasos:
--   1. authRepository.registrarUsuario(email, password)  -> auth.signUpWith(Email)
--   2. usuarioRepository.crearPerfil(...)                -> INSERT en public.usuarios
--
-- El paso 2 solo corre si el paso 1 dejó una sesión iniciada, porque necesita
-- el id del usuario (authRepository.usuarioActualId()) y porque la política
-- usuarios_insert_self exige auth.uid() = id.
--
-- Si el proyecto de Supabase tiene activada la confirmación de correo,
-- signUp NO devuelve sesión: usuarioActualId() es null, el ViewModel corta con
-- el mensaje "Cuenta creada. Revisa tu correo...", y la fila en public.usuarios
-- nunca se crea. Resultado: la persona confirma el correo, inicia sesión y
-- queda sin perfil y sin rol; la app no sabe qué mostrarle.
--
-- Solución propuesta
-- ------------------
-- Que la fila de public.usuarios la cree la propia base de datos con un trigger
-- AFTER INSERT sobre auth.users, leyendo los datos de raw_user_meta_data (lo
-- que el cliente manda como `data` en signUp). Así el perfil existe desde el
-- momento del registro, haya o no confirmación de correo.
--
-- El rol se fuerza a conductor o mecanico: si llega cualquier otra cosa
-- (incluido "encargado"), se usa conductor. La cuenta de encargado la sigue
-- creando el equipo a mano, igual que hoy (docs/ADAPTACION_MOVIL.md §6 y §7).

create or replace function public.crear_perfil_usuario()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    rol_solicitado text;
    rol_final text;
begin
    rol_solicitado := coalesce(new.raw_user_meta_data ->> 'rol', 'conductor');

    -- Nunca se acepta 'encargado' por esta vía, venga como venga el metadata.
    if rol_solicitado in ('conductor', 'mecanico') then
        rol_final := rol_solicitado;
    else
        rol_final := 'conductor';
    end if;

    insert into public.usuarios (
        id,
        nombre_completo,
        cedula,
        email,
        telefono,
        licencia_conducir,
        rol,
        activo
    )
    values (
        new.id,
        coalesce(new.raw_user_meta_data ->> 'nombre_completo', ''),
        coalesce(new.raw_user_meta_data ->> 'cedula', ''),
        new.email,
        nullif(new.raw_user_meta_data ->> 'telefono', ''),
        case
            when rol_final = 'conductor'
                then nullif(new.raw_user_meta_data ->> 'licencia_conducir', '')
            else null
        end,
        rol_final::public.rol_usuario,
        true
    )
    -- Si la app alcanzó a insertar el perfil (cuando no hay confirmación de
    -- correo, signUp sí devuelve sesión), este trigger no pisa esa fila.
    on conflict (id) do nothing;

    return new;
end;
$$;

comment on function public.crear_perfil_usuario() is
    'Crea la fila de public.usuarios al registrarse, a partir de raw_user_meta_data. El rol solo puede ser conductor o mecanico.';

drop trigger if exists crear_perfil_al_registrarse on auth.users;

create trigger crear_perfil_al_registrarse
    after insert on auth.users
    for each row
    execute function public.crear_perfil_usuario();

-- Notas para la revisión
-- ----------------------
-- 1. `security definer` es necesario: el trigger corre sin sesión y tiene que
--    saltarse las políticas RLS de public.usuarios. Por eso se fija
--    search_path = public, para que nadie pueda secuestrar los nombres.
-- 2. El tipo del rol se asume `public.rol_usuario` (enum con conductor,
--    mecanico, encargado). Si en la base la columna es text con un CHECK,
--    hay que quitar el cast `::public.rol_usuario`.
-- 3. `on conflict (id) do nothing` hace que el trigger sea inofensivo en los
--    proyectos donde hoy sí funciona el flujo actual.
-- 4. Para los usuarios que YA quedaron sin perfil por este bug hace falta un
--    repaso aparte: revisar auth.users sin fila en public.usuarios y decidir
--    si se crean a mano o se les pide registrarse de nuevo.
