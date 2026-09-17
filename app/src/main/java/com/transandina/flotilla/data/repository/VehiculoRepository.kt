package com.transandina.flotilla.data.repository

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
 * qué filas devuelve Supabase: el conductor solo ve el vehículo asignado,
 * el mecánico y el encargado ven toda la flotilla (`vehiculos_select`).
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

    /** Toda la flotilla, ordenada por placa. Solo tiene sentido para el encargado y el mecánico. */
    suspend fun obtenerFlotilla(): List<Vehiculo> {
        return SupabaseProvider.client.postgrest["vehiculos"]
            .select {
                order("placa", Order.ASCENDING)
            }
            .decodeList()
    }

    /** Solo el encargado puede insertar (política `vehiculos_insert`). */
    suspend fun registrarVehiculo(datos: VehiculoPayload) {
        SupabaseProvider.client.postgrest["vehiculos"].insert(datos)
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
     * Cambia el conductor y guarda el historial en un solo paso. Las reglas
     * (solo encargado, conductor activo y sin otro vehículo) las valida la
     * función `reasignar_conductor` en Postgres.
     */
    suspend fun reasignarConductor(params: ReasignarConductorParams) {
        SupabaseProvider.client.postgrest.rpc("reasignar_conductor", params)
    }
}
