package com.transandina.flotilla.data.repository

import com.transandina.flotilla.data.model.KilometrajeHistorico
import com.transandina.flotilla.data.model.RegistroKilometraje
import com.transandina.flotilla.di.SupabaseProvider
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

/**
 * Lectura y escritura de la tabla `kilometraje`. El vehículo se pide a
 * [VehiculoRepository]: aquí no se consulta `vehiculos`.
 */
class KilometrajeRepository {

    /** Historial del vehículo, de la lectura más vieja a la más reciente. */
    suspend fun obtenerHistorial(vehiculoId: String): List<KilometrajeHistorico> {
        return SupabaseProvider.client.postgrest["kilometraje"]
            .select {
                filter { eq("vehiculo_id", vehiculoId) }
                order("fecha", Order.ASCENDING)
            }
            .decodeList()
    }

    suspend fun registrarKilometraje(registro: RegistroKilometraje) {
        // La validación de "km mayor al último registrado" y la actualización
        // de vehiculos.km_actual las hacen los triggers trg_validar_km y
        // trg_actualizar_km (ver supabase/schema_flotilla.sql). Aquí solo
        // insertamos y dejamos que el error del trigger suba tal cual.
        SupabaseProvider.client.postgrest["kilometraje"]
            .insert(registro)
    }
}
