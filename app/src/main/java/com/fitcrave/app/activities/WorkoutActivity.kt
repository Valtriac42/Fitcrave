package com.fitcrave.app.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitcrave.app.data.FitcraveRepository
import com.fitcrave.app.data.models.Exercise
import com.fitcrave.app.databinding.ActivityWorkoutBinding
import com.fitcrave.app.ui.ExerciseAdapter
import kotlinx.coroutines.launch

class WorkoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkoutBinding
    private val repo = FitcraveRepository()

    private val sampleExercises = listOf(
        Exercise("Bench Press", 4, 8),
        Exercise("Incline Dumbbell Press", 3, 10),
        Exercise("Lat Pulldown", 4, 10),
        Exercise("Seated Row", 3, 12),
        Exercise("Squat", 4, 8),
        Exercise("Leg Curl", 3, 12),
        Exercise("Plank", 3, 60)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.rvExercises.layoutManager = LinearLayoutManager(this)
        binding.rvExercises.adapter = ExerciseAdapter(sampleExercises)

        lifecycleScope.launch {
            val completed = runCatching { repo.fetchWeeklyWorkoutDays() }.getOrDefault(emptySet())
            val next = (completed.maxOrNull() ?: 0) + 1
            binding.tvDayHeader.text = "DAY ${next.coerceAtMost(7)}"
        }

        binding.btnComplete.setOnClickListener {
            lifecycleScope.launch {
                val completed = runCatching { repo.fetchWeeklyWorkoutDays() }.getOrDefault(emptySet())
                val next = ((completed.maxOrNull() ?: 0) + 1).coerceAtMost(7)
                runCatching { repo.markWorkoutComplete(next) }
                    .onSuccess { Toast.makeText(this@WorkoutActivity, "Day $next complete!", Toast.LENGTH_SHORT).show(); finish() }
                    .onFailure { Toast.makeText(this@WorkoutActivity, "Failed: ${it.message}", Toast.LENGTH_SHORT).show() }
            }
        }
    }
}
