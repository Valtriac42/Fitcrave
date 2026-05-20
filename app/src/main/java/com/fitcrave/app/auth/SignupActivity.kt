package com.fitcrave.app.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fitcrave.app.MainActivity
import com.fitcrave.app.data.FitcraveRepository
import com.fitcrave.app.databinding.ActivitySignupBinding
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val repo = FitcraveRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignup.setOnClickListener { attemptSignup() }
        binding.tvGoLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun attemptSignup() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        if (name.isEmpty() || email.isEmpty() || password.length < 6) {
            toast("Fill all fields (password ≥ 6 chars)")
            return
        }
        binding.progress.visibility = View.VISIBLE
        binding.btnSignup.isEnabled = false
        lifecycleScope.launch {
            val result = runCatching { repo.signUp(email, password, name) }
            binding.progress.visibility = View.GONE
            binding.btnSignup.isEnabled = true
            result.onSuccess {
                toast("Account created — check email if confirmation is enabled")
                startActivity(Intent(this@SignupActivity, MainActivity::class.java))
                finish()
            }.onFailure { e ->
                toast(e.message ?: "Signup failed")
            }
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
