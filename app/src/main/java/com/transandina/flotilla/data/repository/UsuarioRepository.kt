package com.transandina.flotilla.data.repository

import com.transandina.flotilla.data.model.RolUsuario
import com.transandina.flotilla.data.model.Usuario
import com.transandina.flotilla.di.SupabaseProvider
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Solo los campos que el propio usuario puede editar de sí mismo.
 * No incluye ni id, ni email, ni rol, ni activo a propósito: esos se
 * manejan aparte (email va ligado al login, rol/activo los protege
 * un trigger en la base de datos — ver proteger_campos_privilegiados_usuario).
 */
@Serializable
data class ActualizarPerfilPayload(
    @SerialName("nombre_completo") val nombreCompleto: String,
    val cedula: String,
    val telefono: String?,
    @SerialName("licencia_conducir") val licenciaConducir: String?
)

/**
 * Fila completa que se inserta al registrarse. `rol` solo puede ser
 * conductor o mecanico aquí — la política usuarios_insert_self en la
 * base de datos rechaza cualquier intento de insertarse como encargado.
 */
@Serializable
data class NuevoUsuarioPayload(
    val id: String,
    @SerialName("nombre_completo") val nombreCompleto: String,
    val cedula: String,
    val email: String,
    val telefono: String?,
    @SerialName("licencia_conducir") val licenciaConducir: String?,
    val rol: RolUsuario
)

class UsuarioRepository {

    suspend fun obtenerPerfil(usuarioId: String): Usuario? {
        return SupabaseProvider.client.postgrest["usuarios"]
            .select {
                filter { eq("id", usuarioId) }
            }
            .decodeSingleOrNull()
    }

    suspend fun crearPerfil(payload: NuevoUsuarioPayload) {
        SupabaseProvider.client.postgrest["usuarios"].insert(payload)
    }

    suspend fun actualizarPerfil(usuarioId: String, datos: ActualizarPerfilPayload) {
        SupabaseProvider.client.postgrest["usuarios"]
            .update(datos) {
                filter { eq("id", usuarioId) }
            }
    }
}
