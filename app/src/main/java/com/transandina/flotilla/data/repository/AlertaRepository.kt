package com.transandina.flotilla.data.repository

import com.transandina.flotilla.data.model.NuevaAlertaPayload
import com.transandina.flotilla.di.SupabaseProvider
import io.github.jan.supabase.postgrest.postgrest

/** Escritura en `alertas`. Solo el encargado puede insertar (`alertas_insert`). */
class AlertaRepository {

    /** Inserta todos los avisos en una sola petición. */
    suspend fun enviarAvisos(avisos: List<NuevaAlertaPayload>) {
        if (avisos.isEmpty()) return
        SupabaseProvider.client.postgrest["alertas"].insert(avisos)
    }
}
