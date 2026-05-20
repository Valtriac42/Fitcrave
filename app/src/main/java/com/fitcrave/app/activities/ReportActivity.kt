package com.fitcrave.app.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fitcrave.app.data.FitcraveRepository
import com.fitcrave.app.databinding.ActivityReportBinding
import kotlinx.coroutines.launch

class ReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportBinding
    private val repo = FitcraveRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnOpenWeb.setOnClickListener {
            startActivity(Intent(this, WebViewActivity::class.java))
        }

        lifecycleScope.launch {
            val days = runCatching { repo.fetchWeeklyWorkoutDays() }.getOrDefault(emptySet())
            binding.tvRepWorkouts.text = days.size.toString()
            val stats = runCatching { repo.fetchTodayStats() }.getOrNull()
            binding.tvRepKcal.text = (stats?.kcal ?: 0).toString()
            binding.tvRepMinutes.text = (stats?.minutes ?: 0).toString()
        }
    }
}
