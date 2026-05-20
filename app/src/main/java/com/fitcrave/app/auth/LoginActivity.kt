package com.fitcrave.app.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fitcrave.app.MainActivity
import com.fitcrave.app.data.FitcraveRepository
import com.fitcrave.app.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val repo = FitcraveRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener { attemptLogin() }
        binding.tvGoSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun attemptLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        if (email.isEmpty() || password.isEmpty()) {
            toast("Enter email and password")
            return
        }
        binding.progress.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false
        lifecycleScope.launch {
            val result = runCatching { repo.signIn(email, password) }
            binding.progress.visibility = View.GONE
            binding.btnLogin.isEnabled = true
            result.onSuccess {
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }.onFailure { e ->
                toast(e.message ?: "Login failed")
            }
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
