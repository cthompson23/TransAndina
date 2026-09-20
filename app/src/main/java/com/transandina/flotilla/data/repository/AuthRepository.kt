package com.transandina.flotilla.data.repository

import com.transandina.flotilla.di.SupabaseProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

/**
 * Envuelve las llamadas de autenticación de Supabase.
 * Sin lógica adicional: la validación de credenciales la hace el propio
 * servicio de Auth, no la reinventamos aquí.
 */
class AuthRepository {

    suspend fun iniciarSesion(email: String, password: String) {
        SupabaseProvider.client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }
    suspend fun registrarUsuario(email: String, password: String) {
        SupabaseProvider.client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    /**
     * Crea la cuenta de Auth de otra persona sin tocar la sesión actual y
     * devuelve su id. Si Supabase exige confirmar el correo, `signUpWith`
     * devuelve el usuario; si no, deja la sesión en el cliente temporal.
     */
    suspend fun crearCuentaParaOtraPersona(email: String, password: String): String {
        val clienteTemporal = SupabaseProvider.crearClienteTemporal()
        try {
            val usuario = clienteTemporal.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            return usuario?.id
                ?: clienteTemporal.auth.currentUserOrNull()?.id
                ?: throw IllegalStateException("Supabase no devolvió el id de la cuenta nueva")
        } finally {
            clienteTemporal.close()
        }
    }

    suspend fun cerrarSesion() {
        SupabaseProvider.client.auth.signOut()
    }

    suspend fun enviarCorreoRecuperacion(email: String) {
        SupabaseProvider.client.auth.resetPasswordForEmail(
            email = email,
            redirectUrl = "transandina://reset-password"
        )
    }

    suspend fun actualizarContrasena(nuevaPassword: String) {
        SupabaseProvider.client.auth.updateUser {
            password = nuevaPassword
        }
    }

    fun usuarioActualId(): String? =
        SupabaseProvider.client.auth.currentUserOrNull()?.id

    fun usuarioActualEmail(): String? =
        SupabaseProvider.client.auth.currentUserOrNull()?.email
}
