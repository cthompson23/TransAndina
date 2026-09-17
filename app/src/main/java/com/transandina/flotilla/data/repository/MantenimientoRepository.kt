package com.transandina.flotilla.data.repository

import com.transandina.flotilla.data.model.FrecuenciaMantenimiento
import com.transandina.flotilla.data.model.Mantenimiento
import com.transandina.flotilla.data.model.NuevaFotoMantenimientoPayload
import com.transandina.flotilla.data.model.NuevoMantenimientoPayload
import com.transandina.flotilla.di.SupabaseProvider
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import java.util.UUID

/** Bucket privado creado en la migración 202609152200. */
private const val BUCKET_FOTOS = "mantenimientos"

/**
 * Lectura y registro de `mantenimientos`, su catálogo
 * `frecuencias_mantenimiento` y sus fotos. Qué filas vuelven y quién puede
 * insertar lo deciden las políticas RLS (`mantenimientos_select`,
 * `mantenimientos_insert`).
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

    /** Inserta y devuelve la fila creada, que trae el id para asociar las fotos. */
    suspend fun registrar(datos: NuevoMantenimientoPayload): Mantenimiento {
        return SupabaseProvider.client.postgrest["mantenimientos"]
            .insert(datos) { select() }
            .decodeSingle()
    }

    /**
     * Sube una foto JPEG y la asocia al mantenimiento. La ruta sigue la
     * convención de las políticas del bucket: `<vehiculo>/<mantenimiento>/<uuid>.jpg`.
     */
    suspend fun subirFoto(vehiculoId: String, mantenimientoId: String, jpeg: ByteArray) {
        val ruta = "$vehiculoId/$mantenimientoId/${UUID.randomUUID()}.jpg"
        SupabaseProvider.client.storage.from(BUCKET_FOTOS).upload(ruta, jpeg) {
            contentType = ContentType.Image.JPEG
        }
        SupabaseProvider.client.postgrest["mantenimiento_fotos"]
            .insert(NuevaFotoMantenimientoPayload(mantenimientoId = mantenimientoId, rutaArchivo = ruta))
    }
}
