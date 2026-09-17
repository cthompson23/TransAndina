package com.transandina.flotilla.di

import com.transandina.flotilla.BuildConfig
import io.github.jan.supabase.SupabaseClient
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

    /**
     * Cliente desechable que no lee ni guarda sesión en el teléfono. Sirve
     * para crear la cuenta de otra persona (Registrar administrador) sin
     * cerrar la sesión del encargado que está usando la app. Quien lo crea
     * debe cerrarlo con `close()` al terminar.
     */
    fun crearClienteTemporal(): SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY
    ) {
        install(Auth) {
            autoLoadFromStorage = false
            autoSaveToStorage = false
            alwaysAutoRefresh = false
            enableLifecycleCallbacks = false
        }
    }
}
