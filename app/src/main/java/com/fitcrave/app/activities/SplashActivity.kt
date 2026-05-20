package com.fitcrave.app.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fitcrave.app.MainActivity
import com.fitcrave.app.auth.LoginActivity
import com.fitcrave.app.data.SupabaseProvider
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            delay(600)
            val signedIn = runCatching {
                SupabaseProvider.client?.auth?.currentUserOrNull() != null
            }.getOrDefault(false)
            val next = if (signedIn) MainActivity::class.java else LoginActivity::class.java
            startActivity(Intent(this@SplashActivity, next))
            finish()
        }
    }
}
