package com.fitcrave.app.data

import android.content.Context
import com.fitcrave.app.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

/**
 * Centralized Supabase client. Init from FitcraveApp.onCreate.
 * Replace SUPABASE_URL and SUPABASE_ANON_KEY in app/build.gradle.kts
 * (or wire them through local.properties) before running the app.
 */
object SupabaseProvider {
    lateinit var client: SupabaseClient
        private set

    fun init(context: Context) {
        if (this::client.isInitialized) return
        client = createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
        }
    }
}
