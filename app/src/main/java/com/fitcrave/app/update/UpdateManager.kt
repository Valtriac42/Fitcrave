package com.fitcrave.app.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.fitcrave.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class ReleaseInfo(
    val tagName: String,
    val versionName: String,
    val apkUrl: String,
    val notes: String,
    val sizeBytes: Long
)

object UpdateManager {

    private const val LATEST_API =
        "https://api.github.com/repos/Valtriac42/Fitcrave/releases/latest"

    /** Network call — must be invoked off the main thread. */
    suspend fun fetchLatest(): ReleaseInfo? = withContext(Dispatchers.IO) {
        runCatching {
            val conn = (URL(LATEST_API).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "Fitcrave-Android/${BuildConfig.VERSION_NAME}")
                connectTimeout = 10_000
                readTimeout = 15_000
            }
            val json = conn.inputStream.bufferedReader().use { it.readText() }
            val obj = JSONObject(json)
            val tag = obj.optString("tag_name", "")
            val notes = obj.optString("body", "")
            val assets = obj.optJSONArray("assets") ?: return@runCatching null
            // Prefer an asset literally named fitcrave.apk; otherwise first .apk.
            var apkUrl = ""
            var apkSize = 0L
            for (i in 0 until assets.length()) {
                val a = assets.getJSONObject(i)
                val name = a.optString("name", "")
                if (name.equals("fitcrave.apk", ignoreCase = true)) {
                    apkUrl = a.optString("browser_download_url", "")
                    apkSize = a.optLong("size", 0L)
                    break
                }
                if (apkUrl.isEmpty() && name.endsWith(".apk", ignoreCase = true)) {
                    apkUrl = a.optString("browser_download_url", "")
                    apkSize = a.optLong("size", 0L)
                }
            }
            if (apkUrl.isBlank() || tag.isBlank()) null
            else ReleaseInfo(
                tagName = tag,
                versionName = tag.removePrefix("v"),
                apkUrl = apkUrl,
                notes = notes,
                sizeBytes = apkSize
            )
        }.getOrNull()
    }

    /** Semantic-ish comparison. true if `latest` > `current`. */
    fun isNewer(current: String, latest: String): Boolean {
        fun parts(s: String) =
            s.removePrefix("v").split('.').mapNotNull { it.filter(Char::isDigit).toIntOrNull() }
        val c = parts(current)
        val l = parts(latest)
        val n = maxOf(c.size, l.size)
        for (i in 0 until n) {
            val ci = c.getOrElse(i) { 0 }
            val li = l.getOrElse(i) { 0 }
            if (li != ci) return li > ci
        }
        return false
    }

    private fun updatesDir(context: Context): File =
        File(context.getExternalFilesDir(null), "updates").apply { mkdirs() }

    /**
     * Enqueue the APK download via Android's DownloadManager. Returns the download id.
     * Caller should register the supplied receiver to learn when the download completes.
     */
    fun enqueueDownload(
        context: Context,
        info: ReleaseInfo,
        onComplete: (File?) -> Unit
    ): Long {
        val outFile = File(updatesDir(context), "fitcrave-${info.versionName}.apk")
        if (outFile.exists()) outFile.delete()

        val req = DownloadManager.Request(Uri.parse(info.apkUrl))
            .setTitle("Fitcrave ${info.tagName}")
            .setDescription("Downloading update…")
            .setMimeType("application/vnd.android.package-archive")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationUri(Uri.fromFile(outFile))
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val id = dm.enqueue(req)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, intent: Intent) {
                val finishedId =
                    intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                if (finishedId != id) return
                runCatching { c.unregisterReceiver(this) }
                val status = queryStatus(dm, id)
                if (status == DownloadManager.STATUS_SUCCESSFUL && outFile.exists()) {
                    onComplete(outFile)
                } else {
                    onComplete(null)
                }
            }
        }
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(receiver, filter)
        }
        return id
    }

    private fun queryStatus(dm: DownloadManager, id: Long): Int {
        val q = DownloadManager.Query().setFilterById(id)
        dm.query(q).use { c ->
            if (c.moveToFirst()) {
                val ix = c.getColumnIndex(DownloadManager.COLUMN_STATUS)
                if (ix >= 0) return c.getInt(ix)
            }
        }
        return DownloadManager.STATUS_FAILED
    }

    /**
     * Launches Android's system installer for the downloaded APK. On API 26+, asks the user
     * to enable "Install unknown apps" for Fitcrave if they haven't already.
     */
    fun launchInstall(context: Context, apk: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
        ) {
            val settings = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                .setData(Uri.parse("package:${context.packageName}"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(settings)
            return
        }
        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", apk
        )
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(intent)
    }
}
