package com.fitcrave.app

import android.app.Application
import android.util.Log
import com.fitcrave.app.data.SupabaseProvider

class FitcraveApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Don't let a single library failure kill the app on launch — log and continue.
        try {
            SupabaseProvider.init(this)
        } catch (t: Throwable) {
            Log.e("FitcraveApp", "Supabase init crashed (caught) — app continues", t)
        }

        // Last-ditch handler so we get *some* signal if anything else goes wrong later.
        val prev = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("FitcraveApp", "Uncaught on ${thread.name}", throwable)
            prev?.uncaughtException(thread, throwable)
        }
    }
}
