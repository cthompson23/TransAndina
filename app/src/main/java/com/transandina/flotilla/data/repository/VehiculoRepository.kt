package com.transandina.flotilla.data.repository

import com.transandina.flotilla.data.model.AsignarMecanicoParams
import com.transandina.flotilla.data.model.PersonaResumen
import com.transandina.flotilla.data.model.ReasignarConductorParams
import com.transandina.flotilla.data.model.Vehiculo
import com.transandina.flotilla.data.model.VehiculoPayload
import com.transandina.flotilla.di.SupabaseProvider
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.Serializable

/** Alta o baja de un vehículo. */
@Serializable
private data class EstadoVehiculoPayload(val activo: Boolean)

/**
 * Único punto de lectura de la tabla `vehiculos`. Las políticas RLS deciden
 * qué filas devuelve Supabase: el conductor ve el vehículo asignado, el
 * mecánico los que tiene a cargo y el encargado toda la flotilla
 * (`vehiculos_select`, migración 202609201200).
 */
class VehiculoRepository {

    suspend fun obtenerVehiculoAsignado(conductorId: String): Vehiculo? {
        return SupabaseProvider.client.postgrest["vehiculos"]
            .select {
                filter {
                    eq("conductor_id", conductorId)
                    eq("activo", true)
                }
            }
            .decodeSingleOrNull()
    }

    suspend fun obtenerVehiculoPorId(id: String): Vehiculo? {
        return SupabaseProvider.client.postgrest["vehiculos"]
            .select {
                filter { eq("id", id) }
            }
            .decodeSingleOrNull()
    }

    /** Vehículos a cargo de un mecánico, ordenados por placa. */
    suspend fun obtenerVehiculosDeMecanico(mecanicoId: String): List<Vehiculo> {
        return SupabaseProvider.client.postgrest["vehiculos"]
            .select {
                filter { eq("mecanico_id", mecanicoId) }
                order("placa", Order.ASCENDING)
            }
            .decodeList()
    }

    /** Toda la flotilla, ordenada por placa. Solo tiene sentido para el encargado y el mecánico. */
    suspend fun obtenerFlotilla(): List<Vehiculo> {
        return SupabaseProvider.client.postgrest["vehiculos"]
            .select {
                order("placa", Order.ASCENDING)
            }
            .decodeList()
    }

    /**
     * Solo el encargado puede insertar (política `vehiculos_insert`).
     * Devuelve la fila creada, porque el formulario necesita el id para
     * asignarle el conductor en el mismo paso.
     */
    suspend fun registrarVehiculo(datos: VehiculoPayload): Vehiculo {
        return SupabaseProvider.client.postgrest["vehiculos"]
            .insert(datos) { select() }
            .decodeSingle()
    }

    /** Solo el encargado puede actualizar (política `vehiculos_update`). */
    suspend fun actualizarVehiculo(id: String, datos: VehiculoPayload) {
        SupabaseProvider.client.postgrest["vehiculos"]
            .update(datos) {
                filter { eq("id", id) }
            }
    }

    /**
     * Da de baja un vehículo o lo reactiva. Un vehículo inactivo deja de
     * aparecer en el vehículo asignado del conductor y no genera alertas,
     * pero conserva su historial.
     */
    suspend fun cambiarActivo(id: String, activo: Boolean) {
        SupabaseProvider.client.postgrest["vehiculos"]
            .update(EstadoVehiculoPayload(activo)) {
                filter { eq("id", id) }
            }
    }

    /**
     * Mecánicos activos (id y nombre) para los selectores. Viene de la
     * función `mecanicos_disponibles`, porque ni el conductor ni el mecánico
     * pueden leer la tabla `usuarios` completa.
     */
    suspend fun obtenerMecanicos(): List<PersonaResumen> {
        return SupabaseProvider.client.postgrest
            .rpc("mecanicos_disponibles")
            .decodeList()
    }

    /**
     * Id y nombre de los conductores y mecánicos de los vehículos que quien
     * pregunta puede ver. Hace falta porque `usuarios_select` solo le
     * devuelve su propia fila a quien no es encargado, y sin esto la lista
     * del mecánico mostraba "Sin conductor" en vehículos que sí lo tienen
     * (migración 202609202000).
     */
    suspend fun obtenerPersonasDeMisVehiculos(): List<PersonaResumen> {
        return SupabaseProvider.client.postgrest
            .rpc("personas_de_mis_vehiculos")
            .decodeList()
    }

    /**
     * Asigna (o quita, con null) el mecánico responsable. Lo pueden hacer el
     * encargado y el conductor del vehículo; quién puede lo decide la función
     * `asignar_mecanico` en Postgres.
     */
    suspend fun asignarMecanico(vehiculoId: String, mecanicoId: String?) {
        SupabaseProvider.client.postgrest.rpc(
            "asignar_mecanico",
            AsignarMecanicoParams(vehiculoId = vehiculoId, mecanicoId = mecanicoId)
        )
    }

    /**
     * Cambia el conductor y guarda el historial en un solo paso. Las reglas
     * (solo encargado, conductor activo y sin otro vehículo) las valida la
     * función `reasignar_conductor` en Postgres.
     */
    suspend fun reasignarConductor(params: ReasignarConductorParams) {
        SupabaseProvider.client.postgrest.rpc("reasignar_conductor", params)
    }
}
