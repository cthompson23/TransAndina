package com.transandina.flotilla.data.repository

import com.transandina.flotilla.data.model.FrecuenciaMantenimiento
import com.transandina.flotilla.data.model.Mantenimiento
import com.transandina.flotilla.di.SupabaseProvider
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

/**
 * Lectura de `mantenimientos` y de su catálogo `frecuencias_mantenimiento`.
 * Qué filas vuelven lo decide `mantenimientos_select`: el encargado y el
 * mecánico ven todas; el conductor, solo las de su vehículo.
 */
class MantenimientoRepository {

    /** Todos los mantenimientos visibles, del más reciente al más antiguo. */
    suspend fun obtenerTodos(): List<Mantenimiento> {
        return SupabaseProvider.client.postgrest["mantenimientos"]
            .select {
                order("fecha", Order.DESCENDING)
            }
            .decodeList()
    }

    /** Historial de un vehículo, del más reciente al más antiguo. */
    suspend fun obtenerPorVehiculo(vehiculoId: String): List<Mantenimiento> {
        return SupabaseProvider.client.postgrest["mantenimientos"]
            .select {
                filter { eq("vehiculo_id", vehiculoId) }
                order("fecha", Order.DESCENDING)
            }
            .decodeList()
    }

    suspend fun obtenerFrecuencias(): List<FrecuenciaMantenimiento> {
        return SupabaseProvider.client.postgrest["frecuencias_mantenimiento"]
            .select {
                order("categoria", Order.ASCENDING)
            }
            .decodeList()
    }
}
