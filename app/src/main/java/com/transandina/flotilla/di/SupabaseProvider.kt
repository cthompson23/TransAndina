package com.transandina.flotilla.di

import com.transandina.flotilla.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Punto único de acceso al cliente de Supabase.
 *
 */
object SupabaseProvider {
    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY
    ) {
        install(Auth){
            scheme = "transandina"
            host = "reset-password"
        }
        install(Postgrest)
    }
}
