package com.transandina.flotilla.data.repository

import com.transandina.flotilla.data.model.EditarMantenimientoPayload
import com.transandina.flotilla.data.model.FotoMantenimiento
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
import kotlin.time.Duration.Companion.hours

/** Bucket privado creado en la migración 202609152200. */
private const val BUCKET_FOTOS = "mantenimientos"

/** Cuánto vale una URL firmada de foto; alcanza de sobra para verla. */
private val DURACION_URL_FOTO = 1.hours

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

    suspend fun obtenerPorId(id: String): Mantenimiento? {
        return SupabaseProvider.client.postgrest["mantenimientos"]
            .select {
                filter { eq("id", id) }
            }
            .decodeSingleOrNull()
    }

    suspend fun obtenerFrecuencias(): List<FrecuenciaMantenimiento> {
        return SupabaseProvider.client.postgrest["frecuencias_mantenimiento"]
            .select {
                order("categoria", Order.ASCENDING)
            }
            .decodeList()
    }

    /**
     * URLs temporales para ver las fotos de varios mantenimientos, agrupadas
     * por mantenimiento. El bucket es privado: cada URL se firma y vence en
     * [DURACION_URL_FOTO]. Si una foto ya no está en el bucket, se omite.
     */
    suspend fun obtenerFotos(mantenimientoIds: List<String>): Map<String, List<String>> {
        if (mantenimientoIds.isEmpty()) return emptyMap()

        val fotos: List<FotoMantenimiento> = SupabaseProvider.client.postgrest["mantenimiento_fotos"]
            .select {
                filter { isIn("mantenimiento_id", mantenimientoIds) }
            }
            .decodeList()
        if (fotos.isEmpty()) return emptyMap()

        val firmadas = SupabaseProvider.client.storage.from(BUCKET_FOTOS)
            .createSignedUrls(DURACION_URL_FOTO, fotos.map { it.rutaArchivo })
            .filter { it.error == null }
            .associate { it.path to it.signedURL }

        return fotos
            .mapNotNull { foto -> firmadas[foto.rutaArchivo]?.let { foto.mantenimientoId to it } }
            .groupBy({ (id, _) -> id }, { (_, url) -> url })
    }

    /** Inserta y devuelve la fila creada, que trae el id para asociar las fotos. */
    suspend fun registrar(datos: NuevoMantenimientoPayload): Mantenimiento {
        return SupabaseProvider.client.postgrest["mantenimientos"]
            .insert(datos) { select() }
            .decodeSingle()
    }

    /** Corrige un registro. Solo el encargado (política `mantenimientos_update`). */
    suspend fun actualizar(id: String, datos: EditarMantenimientoPayload) {
        SupabaseProvider.client.postgrest["mantenimientos"]
            .update(datos) {
                filter { eq("id", id) }
            }
    }

    /**
     * Borra el registro. Solo el encargado (política `mantenimientos_delete`).
     * Las filas de `mantenimiento_fotos` se van solas (la FK es on delete
     * cascade), pero los archivos del bucket no, así que se borran primero
     * para no dejarlos huérfanos. Si esa parte falla, el registro igual se
     * borra: un archivo suelto no se le muestra a nadie.
     */
    suspend fun eliminar(id: String) {
        val fotos: List<FotoMantenimiento> = runCatching {
            SupabaseProvider.client.postgrest["mantenimiento_fotos"]
                .select {
                    filter { eq("mantenimiento_id", id) }
                }
                .decodeList<FotoMantenimiento>()
        }.getOrDefault(emptyList())

        if (fotos.isNotEmpty()) {
            runCatching {
                SupabaseProvider.client.storage.from(BUCKET_FOTOS)
                    .delete(fotos.map { it.rutaArchivo })
            }
        }

        SupabaseProvider.client.postgrest["mantenimientos"]
            .delete {
                filter { eq("id", id) }
            }
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
