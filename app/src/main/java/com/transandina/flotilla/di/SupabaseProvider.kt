package com.transandina.flotilla.di

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Punto único de acceso al cliente de Supabase.
 *
 */
object SupabaseProvider {
    val client = createSupabaseClient(
        supabaseUrl = "https://TU-PROYECTO.supabase.co",
        supabaseKey = "TU-ANON-KEY"
    ) {
        install(Auth)
        install(Postgrest)
    }
}
