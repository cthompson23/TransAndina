package com.transandina.flotilla.data.repository

import com.transandina.flotilla.data.model.Vehiculo
import com.transandina.flotilla.di.SupabaseProvider
import io.github.jan.supabase.postgrest.postgrest

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
}
