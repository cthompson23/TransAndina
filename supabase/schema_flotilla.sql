--
-- PostgreSQL database dump
--

\restrict Fs2FLgKvmIGR7XO3hI5ZL19yjdJ7risvkeJ8KT1X4H1A0qVXhaYBu1UZUfPAbmy

-- Dumped from database version 17.6
-- Dumped by pg_dump version 17.11 (Homebrew)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: public; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA public;


--
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON SCHEMA public IS 'standard public schema';


--
-- Name: rol_usuario; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.rol_usuario AS ENUM (
    'conductor',
    'mecanico',
    'encargado'
);


--
-- Name: tipo_alerta; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.tipo_alerta AS ENUM (
    'mantenimiento_proximo',
    'documento_vencimiento',
    'mantenimiento_confirmado',
    'reasignacion'
);


--
-- Name: tipo_mantenimiento; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.tipo_mantenimiento AS ENUM (
    'preventivo',
    'correctivo'
);


--
-- Name: tipo_vehiculo; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.tipo_vehiculo AS ENUM (
    'liviano',
    'pesado',
    'especial'
);


--
-- Name: actualizar_km_vehiculo(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.actualizar_km_vehiculo() RETURNS trigger
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
begin
  update vehiculos set km_actual = new.km where id = new.vehiculo_id;
  return new;
end;
$$;


--
-- Name: crear_alerta_confirmacion(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.crear_alerta_confirmacion() RETURNS trigger
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
begin
  insert into alertas (tipo, vehiculo_id, usuario_id, mensaje)
  values (
    'mantenimiento_confirmado',
    new.vehiculo_id,
    new.registrado_por,
    'Se registró un mantenimiento de categoría ' || new.categoria
  );
  return new;
end;
$$;


--
-- Name: crear_alerta_reasignacion(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.crear_alerta_reasignacion() RETURNS trigger
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
begin
  if new.conductor_id is distinct from old.conductor_id and new.conductor_id is not null then
    insert into alertas (tipo, vehiculo_id, usuario_id, mensaje)
    values (
      'reasignacion',
      new.id,
      new.conductor_id,
      'Se te asignó el vehículo con placa ' || new.placa
    );
  end if;
  return new;
end;
$$;


--
-- Name: es_encargado(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.es_encargado() RETURNS boolean
    LANGUAGE sql STABLE SECURITY DEFINER
    AS $$
  select rol_actual() = 'encargado';
$$;


--
-- Name: proteger_campos_privilegiados_usuario(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.proteger_campos_privilegiados_usuario() RETURNS trigger
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
begin
  if not es_encargado() and (
    new.rol is distinct from old.rol
    or new.activo is distinct from old.activo
  ) then
    raise exception 'No tienes permiso para cambiar tu rol o estado de cuenta';
  end if;
  return new;
end;
$$;


--
-- Name: rol_actual(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.rol_actual() RETURNS public.rol_usuario
    LANGUAGE sql STABLE SECURITY DEFINER
    AS $$
  select rol from usuarios where id = auth.uid();
$$;


--
-- Name: validar_km_incremental(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.validar_km_incremental() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
begin
  if new.km <= (select km_actual from vehiculos where id = new.vehiculo_id) then
    raise exception 'El kilometraje debe ser mayor al último registrado';
  end if;
  return new;
end;
$$;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: alertas; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.alertas (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    tipo public.tipo_alerta NOT NULL,
    vehiculo_id uuid,
    usuario_id uuid,
    mensaje text NOT NULL,
    leida boolean DEFAULT false NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: frecuencias_mantenimiento; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.frecuencias_mantenimiento (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    tipo_vehiculo public.tipo_vehiculo NOT NULL,
    categoria text NOT NULL,
    km_frecuencia numeric,
    dias_frecuencia integer
);


--
-- Name: kilometraje; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.kilometraje (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    vehiculo_id uuid NOT NULL,
    registrado_por uuid NOT NULL,
    fecha date NOT NULL,
    km numeric NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: mantenimiento_fotos; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.mantenimiento_fotos (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    mantenimiento_id uuid NOT NULL,
    storage_path text NOT NULL
);


--
-- Name: mantenimientos; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.mantenimientos (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    vehiculo_id uuid NOT NULL,
    registrado_por uuid NOT NULL,
    tipo public.tipo_mantenimiento NOT NULL,
    categoria text NOT NULL,
    fecha date NOT NULL,
    responsable text,
    descripcion text,
    km numeric NOT NULL,
    costo numeric,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: usuarios; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.usuarios (
    id uuid NOT NULL,
    nombre_completo text NOT NULL,
    cedula text NOT NULL,
    email text NOT NULL,
    telefono text,
    licencia_conducir text,
    rol public.rol_usuario NOT NULL,
    activo boolean DEFAULT true NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: vehiculos; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.vehiculos (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    placa text NOT NULL,
    marca text NOT NULL,
    modelo text NOT NULL,
    anio integer NOT NULL,
    tipo public.tipo_vehiculo NOT NULL,
    capacidad numeric,
    km_actual numeric DEFAULT 0 NOT NULL,
    fecha_marchamo date,
    fecha_revision_tecnica date,
    fecha_seguro date,
    conductor_id uuid,
    activo boolean DEFAULT true NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: alertas alertas_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alertas
    ADD CONSTRAINT alertas_pkey PRIMARY KEY (id);


--
-- Name: frecuencias_mantenimiento frecuencias_mantenimiento_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.frecuencias_mantenimiento
    ADD CONSTRAINT frecuencias_mantenimiento_pkey PRIMARY KEY (id);


--
-- Name: frecuencias_mantenimiento frecuencias_mantenimiento_tipo_vehiculo_categoria_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.frecuencias_mantenimiento
    ADD CONSTRAINT frecuencias_mantenimiento_tipo_vehiculo_categoria_key UNIQUE (tipo_vehiculo, categoria);


--
-- Name: kilometraje kilometraje_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.kilometraje
    ADD CONSTRAINT kilometraje_pkey PRIMARY KEY (id);


--
-- Name: mantenimiento_fotos mantenimiento_fotos_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mantenimiento_fotos
    ADD CONSTRAINT mantenimiento_fotos_pkey PRIMARY KEY (id);


--
-- Name: mantenimientos mantenimientos_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mantenimientos
    ADD CONSTRAINT mantenimientos_pkey PRIMARY KEY (id);


--
-- Name: usuarios usuarios_cedula_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_cedula_key UNIQUE (cedula);


--
-- Name: usuarios usuarios_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_email_key UNIQUE (email);


--
-- Name: usuarios usuarios_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_pkey PRIMARY KEY (id);


--
-- Name: vehiculos vehiculos_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.vehiculos
    ADD CONSTRAINT vehiculos_pkey PRIMARY KEY (id);


--
-- Name: vehiculos vehiculos_placa_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.vehiculos
    ADD CONSTRAINT vehiculos_placa_key UNIQUE (placa);


--
-- Name: idx_alertas_usuario; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_alertas_usuario ON public.alertas USING btree (usuario_id);


--
-- Name: idx_kilometraje_vehiculo; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_kilometraje_vehiculo ON public.kilometraje USING btree (vehiculo_id);


--
-- Name: idx_mantenimientos_fecha; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_mantenimientos_fecha ON public.mantenimientos USING btree (fecha);


--
-- Name: idx_mantenimientos_vehiculo; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_mantenimientos_vehiculo ON public.mantenimientos USING btree (vehiculo_id);


--
-- Name: idx_vehiculos_conductor; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_vehiculos_conductor ON public.vehiculos USING btree (conductor_id);


--
-- Name: kilometraje trg_actualizar_km; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_actualizar_km AFTER INSERT ON public.kilometraje FOR EACH ROW EXECUTE FUNCTION public.actualizar_km_vehiculo();


--
-- Name: mantenimientos trg_alerta_mantenimiento; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_alerta_mantenimiento AFTER INSERT ON public.mantenimientos FOR EACH ROW EXECUTE FUNCTION public.crear_alerta_confirmacion();


--
-- Name: vehiculos trg_alerta_reasignacion; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_alerta_reasignacion AFTER UPDATE ON public.vehiculos FOR EACH ROW EXECUTE FUNCTION public.crear_alerta_reasignacion();


--
-- Name: usuarios trg_proteger_campos_usuario; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_proteger_campos_usuario BEFORE UPDATE ON public.usuarios FOR EACH ROW EXECUTE FUNCTION public.proteger_campos_privilegiados_usuario();


--
-- Name: kilometraje trg_validar_km; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_validar_km BEFORE INSERT ON public.kilometraje FOR EACH ROW EXECUTE FUNCTION public.validar_km_incremental();


--
-- Name: alertas alertas_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alertas
    ADD CONSTRAINT alertas_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id) ON DELETE CASCADE;


--
-- Name: alertas alertas_vehiculo_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alertas
    ADD CONSTRAINT alertas_vehiculo_id_fkey FOREIGN KEY (vehiculo_id) REFERENCES public.vehiculos(id) ON DELETE CASCADE;


--
-- Name: kilometraje kilometraje_registrado_por_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.kilometraje
    ADD CONSTRAINT kilometraje_registrado_por_fkey FOREIGN KEY (registrado_por) REFERENCES public.usuarios(id);


--
-- Name: kilometraje kilometraje_vehiculo_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.kilometraje
    ADD CONSTRAINT kilometraje_vehiculo_id_fkey FOREIGN KEY (vehiculo_id) REFERENCES public.vehiculos(id) ON DELETE CASCADE;


--
-- Name: mantenimiento_fotos mantenimiento_fotos_mantenimiento_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mantenimiento_fotos
    ADD CONSTRAINT mantenimiento_fotos_mantenimiento_id_fkey FOREIGN KEY (mantenimiento_id) REFERENCES public.mantenimientos(id) ON DELETE CASCADE;


--
-- Name: mantenimientos mantenimientos_registrado_por_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mantenimientos
    ADD CONSTRAINT mantenimientos_registrado_por_fkey FOREIGN KEY (registrado_por) REFERENCES public.usuarios(id);


--
-- Name: mantenimientos mantenimientos_vehiculo_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mantenimientos
    ADD CONSTRAINT mantenimientos_vehiculo_id_fkey FOREIGN KEY (vehiculo_id) REFERENCES public.vehiculos(id) ON DELETE CASCADE;


--
-- Name: usuarios usuarios_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_id_fkey FOREIGN KEY (id) REFERENCES auth.users(id) ON DELETE CASCADE;


--
-- Name: vehiculos vehiculos_conductor_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.vehiculos
    ADD CONSTRAINT vehiculos_conductor_id_fkey FOREIGN KEY (conductor_id) REFERENCES public.usuarios(id);


--
-- Name: alertas; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.alertas ENABLE ROW LEVEL SECURITY;

--
-- Name: alertas alertas_select; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY alertas_select ON public.alertas FOR SELECT USING (((usuario_id = auth.uid()) OR public.es_encargado()));


--
-- Name: alertas alertas_update; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY alertas_update ON public.alertas FOR UPDATE USING ((usuario_id = auth.uid()));


--
-- Name: mantenimiento_fotos fotos_insert; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY fotos_insert ON public.mantenimiento_fotos FOR INSERT WITH CHECK ((mantenimiento_id IN ( SELECT mantenimientos.id
   FROM public.mantenimientos
  WHERE (mantenimientos.registrado_por = auth.uid()))));


--
-- Name: mantenimiento_fotos fotos_select; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY fotos_select ON public.mantenimiento_fotos FOR SELECT USING ((mantenimiento_id IN ( SELECT mantenimientos.id
   FROM public.mantenimientos)));


--
-- Name: frecuencias_mantenimiento; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.frecuencias_mantenimiento ENABLE ROW LEVEL SECURITY;

--
-- Name: frecuencias_mantenimiento frecuencias_select; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY frecuencias_select ON public.frecuencias_mantenimiento FOR SELECT USING ((auth.role() = 'authenticated'::text));


--
-- Name: frecuencias_mantenimiento frecuencias_write; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY frecuencias_write ON public.frecuencias_mantenimiento USING (public.es_encargado()) WITH CHECK (public.es_encargado());


--
-- Name: kilometraje; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.kilometraje ENABLE ROW LEVEL SECURITY;

--
-- Name: kilometraje kilometraje_insert; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY kilometraje_insert ON public.kilometraje FOR INSERT WITH CHECK (((registrado_por = auth.uid()) AND (vehiculo_id IN ( SELECT vehiculos.id
   FROM public.vehiculos
  WHERE (vehiculos.conductor_id = auth.uid())))));


--
-- Name: kilometraje kilometraje_select; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY kilometraje_select ON public.kilometraje FOR SELECT USING ((public.es_encargado() OR (vehiculo_id IN ( SELECT vehiculos.id
   FROM public.vehiculos
  WHERE (vehiculos.conductor_id = auth.uid())))));


--
-- Name: mantenimiento_fotos; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.mantenimiento_fotos ENABLE ROW LEVEL SECURITY;

--
-- Name: mantenimientos; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.mantenimientos ENABLE ROW LEVEL SECURITY;

--
-- Name: mantenimientos mantenimientos_delete; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY mantenimientos_delete ON public.mantenimientos FOR DELETE USING (public.es_encargado());


--
-- Name: mantenimientos mantenimientos_insert; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY mantenimientos_insert ON public.mantenimientos FOR INSERT WITH CHECK (((registrado_por = auth.uid()) AND ((public.rol_actual() = 'mecanico'::public.rol_usuario) OR ((public.rol_actual() = 'conductor'::public.rol_usuario) AND (vehiculo_id IN ( SELECT vehiculos.id
   FROM public.vehiculos
  WHERE (vehiculos.conductor_id = auth.uid())))))));


--
-- Name: mantenimientos mantenimientos_select; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY mantenimientos_select ON public.mantenimientos FOR SELECT USING ((public.es_encargado() OR (public.rol_actual() = 'mecanico'::public.rol_usuario) OR (vehiculo_id IN ( SELECT vehiculos.id
   FROM public.vehiculos
  WHERE (vehiculos.conductor_id = auth.uid())))));


--
-- Name: mantenimientos mantenimientos_update; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY mantenimientos_update ON public.mantenimientos FOR UPDATE USING (public.es_encargado());


--
-- Name: usuarios; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.usuarios ENABLE ROW LEVEL SECURITY;

--
-- Name: usuarios usuarios_insert_self; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY usuarios_insert_self ON public.usuarios FOR INSERT WITH CHECK (((id = auth.uid()) AND (rol = ANY (ARRAY['conductor'::public.rol_usuario, 'mecanico'::public.rol_usuario]))));


--
-- Name: usuarios usuarios_select; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY usuarios_select ON public.usuarios FOR SELECT USING (((id = auth.uid()) OR public.es_encargado()));


--
-- Name: usuarios usuarios_update; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY usuarios_update ON public.usuarios FOR UPDATE USING (((id = auth.uid()) OR public.es_encargado()));


--
-- Name: vehiculos; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.vehiculos ENABLE ROW LEVEL SECURITY;

--
-- Name: vehiculos vehiculos_insert; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY vehiculos_insert ON public.vehiculos FOR INSERT WITH CHECK (public.es_encargado());


--
-- Name: vehiculos vehiculos_select; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY vehiculos_select ON public.vehiculos FOR SELECT USING ((public.es_encargado() OR (public.rol_actual() = 'mecanico'::public.rol_usuario) OR (conductor_id = auth.uid())));


--
-- Name: vehiculos vehiculos_update; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY vehiculos_update ON public.vehiculos FOR UPDATE USING (public.es_encargado());


--
-- PostgreSQL database dump complete
--

\unrestrict Fs2FLgKvmIGR7XO3hI5ZL19yjdJ7risvkeJ8KT1X4H1A0qVXhaYBu1UZUfPAbmy

