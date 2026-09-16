package com.transandina.flotilla.data.repository

import com.transandina.flotilla.data.model.Vehiculo
import com.transandina.flotilla.di.SupabaseProvider
import io.github.jan.supabase.postgrest.postgrest

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
}
