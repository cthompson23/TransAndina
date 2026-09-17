package com.transandina.flotilla.data.repository

import com.transandina.flotilla.data.model.EstadoCuenta
import com.transandina.flotilla.data.model.RolUsuario
import com.transandina.flotilla.data.model.Usuario
import com.transandina.flotilla.di.SupabaseProvider
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
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

/** Cambio de estado de cuenta que hace el encargado. */
@Serializable
data class CambioEstadoPayload(
    val estado: EstadoCuenta
)

class UsuarioRepository {

    /**
     * Todas las cuentas, ordenadas por nombre. La política `usuarios_select`
     * solo se las devuelve completas al encargado.
     */
    suspend fun obtenerUsuarios(): List<Usuario> {
        return SupabaseProvider.client.postgrest["usuarios"]
            .select {
                order("nombre_completo", Order.ASCENDING)
            }
            .decodeList()
    }

    /**
     * Solo el encargado puede cambiar el estado de otra cuenta. Las reglas
     * (no reactivar una cuenta desactivada, no cambiar la propia, liberar el
     * vehículo) las aplican los triggers de `usuarios`.
     */
    suspend fun cambiarEstado(usuarioId: String, estado: EstadoCuenta) {
        SupabaseProvider.client.postgrest["usuarios"]
            .update(CambioEstadoPayload(estado)) {
                filter { eq("id", usuarioId) }
            }
    }

    /**
     * Crea (o completa, si un trigger ya la creó) la fila de otra persona.
     * Lo usa el encargado al registrar un administrador; lo permiten las
     * políticas `usuarios_insert_encargado` y `usuarios_update`.
     */
    suspend fun guardarPerfilDeOtraPersona(payload: NuevoUsuarioPayload) {
        SupabaseProvider.client.postgrest["usuarios"]
            .upsert(payload) {
                onConflict = "id"
            }
    }

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
