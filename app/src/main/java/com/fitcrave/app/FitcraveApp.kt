package com.fitcrave.app

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import com.fitcrave.app.data.SupabaseProvider
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FitcraveApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Install the crash handler FIRST so anything that fails next gets captured.
        val prev = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                writeCrash(this, thread, throwable)
            } catch (_: Throwable) { /* ignore */ }
            Log.e(TAG, "Uncaught on ${thread.name}", throwable)
            prev?.uncaughtException(thread, throwable)
        }

        try {
            SupabaseProvider.init(this)
        } catch (t: Throwable) {
            Log.e(TAG, "Supabase init crashed (caught) — app continues", t)
        }
    }

    companion object {
        private const val TAG = "FitcraveApp"
        const val CRASH_FILE = "last_crash.txt"

        fun crashFile(ctx: Context): File = File(ctx.filesDir, CRASH_FILE)

        private fun writeCrash(ctx: Context, thread: Thread, throwable: Throwable) {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            val ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            pw.println("Fitcrave crash report")
            pw.println("=====================")
            pw.println("When : $ts")
            pw.println("Build: ${BuildConfig.VERSION_NAME} (code ${BuildConfig.VERSION_CODE}) ${BuildConfig.BUILD_TYPE}")
            pw.println("Device: ${Build.MANUFACTURER} ${Build.MODEL}  Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})  ${Build.SUPPORTED_ABIS.joinToString(",")}")
            pw.println("Thread: ${thread.name}")
            pw.println()
            throwable.printStackTrace(pw)
            pw.flush()
            crashFile(ctx).writeText(sw.toString())
        }
    }
}
