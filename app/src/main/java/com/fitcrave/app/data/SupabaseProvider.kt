package com.fitcrave.app.data

import android.content.Context
import android.util.Log
import com.fitcrave.app.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Lazy-and-defensive Supabase init. Any failure here is logged and `client` stays null
 * — the app will still load (with "no backend" behavior) instead of crashing on start.
 */
object SupabaseProvider {
    private const val TAG = "SupabaseProvider"

    @JvmStatic
    var client: SupabaseClient? = null
        private set

    fun init(@Suppress("UNUSED_PARAMETER") context: Context) {
        if (client != null) return
        try {
            val url = BuildConfig.SUPABASE_URL.trim()
            val key = BuildConfig.SUPABASE_ANON_KEY.trim()
            if (url.isBlank() || key.isBlank() || url.startsWith("https://YOUR_PROJECT")) {
                Log.w(TAG, "Supabase not configured — running without backend")
                return
            }
            client = createSupabaseClient(supabaseUrl = url, supabaseKey = key) {
                install(Auth)
                install(Postgrest)
            }
            Log.i(TAG, "Supabase initialized for $url")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to init Supabase — app will continue without backend", t)
            client = null
        }
    }

    /** Throws only at the call site if Supabase isn't available — never on app start. */
    fun requireClient(): SupabaseClient =
        client ?: error("Supabase is not configured. Check local.properties / BuildConfig.")
}
