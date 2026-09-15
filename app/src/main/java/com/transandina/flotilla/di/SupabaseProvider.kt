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
        supabaseUrl = "https://jeqaibzvjfamqnssycln.supabase.co",
        supabaseKey = "sb_publishable_rvqQIYtaXlOh8XeuLu7D1Q_HBSdV4Mm"
    ) {
        install(Auth){
            scheme = "transandina"
            host = "reset-password"
        }
        install(Postgrest)
    }
}
