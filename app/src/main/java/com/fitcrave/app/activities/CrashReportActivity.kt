package com.fitcrave.app.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fitcrave.app.FitcraveApp
import com.fitcrave.app.auth.LoginActivity
import com.fitcrave.app.databinding.ActivityCrashReportBinding

class CrashReportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityCrashReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val file = FitcraveApp.crashFile(this)
        val text = if (file.exists()) file.readText() else "(no crash file)"
        binding.tvCrash.text = text

        binding.btnCopy.setOnClickListener {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("Fitcrave crash", text))
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        binding.btnDismiss.setOnClickListener {
            file.delete()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
