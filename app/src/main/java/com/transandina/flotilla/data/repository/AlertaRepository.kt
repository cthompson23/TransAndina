package com.transandina.flotilla.data.repository

import com.transandina.flotilla.data.model.Alerta
import com.transandina.flotilla.data.model.NuevaAlertaPayload
import com.transandina.flotilla.di.SupabaseProvider
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

/**
 * Tabla `alertas`. Cada quien lee las suyas (`alertas_select`) y solo el
 * encargado puede insertar (`alertas_insert`).
 */
class AlertaRepository {

    /** Los avisos guardados de una persona, del más reciente al más antiguo. */
    suspend fun obtenerMisAlertas(usuarioId: String, limite: Long = 50): List<Alerta> {
        return SupabaseProvider.client.postgrest["alertas"]
            .select {
                filter { eq("usuario_id", usuarioId) }
                order("created_at", Order.DESCENDING)
                limit(limite)
            }
            .decodeList()
    }

    /** Inserta todos los avisos en una sola petición. */
    suspend fun enviarAvisos(avisos: List<NuevaAlertaPayload>) {
        if (avisos.isEmpty()) return
        SupabaseProvider.client.postgrest["alertas"].insert(avisos)
    }
}
