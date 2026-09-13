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

    suspend fun cerrarSesion() {
        SupabaseProvider.client.auth.signOut()
    }

    fun usuarioActualId(): String? =
        SupabaseProvider.client.auth.currentUserOrNull()?.id
}
