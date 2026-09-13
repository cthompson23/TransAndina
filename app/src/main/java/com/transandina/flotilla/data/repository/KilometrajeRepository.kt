package com.transandina.flotilla.data.repository

import com.transandina.flotilla.data.model.RegistroKilometraje
import com.transandina.flotilla.data.model.Vehiculo
import com.transandina.flotilla.di.SupabaseProvider
import io.github.jan.supabase.postgrest.postgrest

/**
 * Nota: la sintaxis exacta del builder (`filter { eq(...) }`) puede variar
 * ligeramente según la versión del SDK supabase-kt que agregues en Gradle.
 * Revisa la doc de la versión instalada si el autocompletado no coincide.
 */
class KilometrajeRepository {

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

    suspend fun registrarKilometraje(registro: RegistroKilometraje) {
        // La validación de "km mayor al último registrado" y la actualización
        // de vehiculos.km_actual las hace la base de datos (triggers en
        // schema_flotilla.sql). Aquí solo insertamos.
        SupabaseProvider.client.postgrest["kilometraje"]
            .insert(registro)
    }
}
