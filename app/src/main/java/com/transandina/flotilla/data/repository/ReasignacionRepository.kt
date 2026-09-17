package com.transandina.flotilla.data.repository

import com.transandina.flotilla.data.model.Reasignacion
import com.transandina.flotilla.di.SupabaseProvider
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

/**
 * Lectura del historial `reasignaciones`. No hay escritura aquí: la única
 * forma de reasignar es [VehiculoRepository.reasignarConductor].
 */
class ReasignacionRepository {

    suspend fun obtenerPorVehiculo(vehiculoId: String, limite: Long = 10): List<Reasignacion> {
        return SupabaseProvider.client.postgrest["reasignaciones"]
            .select {
                filter { eq("vehiculo_id", vehiculoId) }
                order("created_at", Order.DESCENDING)
                limit(limite)
            }
            .decodeList()
    }

    /** Reasignaciones de toda la flotilla desde una fecha ISO ("2026-08-01"). */
    suspend fun obtenerDesde(fechaIso: String): List<Reasignacion> {
        return SupabaseProvider.client.postgrest["reasignaciones"]
            .select {
                filter { gte("created_at", fechaIso) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList()
    }
}
