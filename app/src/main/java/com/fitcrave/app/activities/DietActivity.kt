package com.fitcrave.app.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.fitcrave.app.data.FitcraveRepository
import com.fitcrave.app.data.models.Meal
import com.fitcrave.app.databinding.ActivityDietBinding
import com.fitcrave.app.ui.MealAdapter
import kotlinx.coroutines.launch

class DietActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDietBinding
    private val repo = FitcraveRepository()

    private val sampleMeals = listOf(
        Meal("Breakfast", "Oats, banana, whey protein shake", 420),
        Meal("Snack", "Greek yogurt + almonds", 220),
        Meal("Lunch", "Grilled chicken, brown rice, broccoli", 650),
        Meal("Pre-workout", "Coffee, apple, peanut butter", 180),
        Meal("Dinner", "Salmon, sweet potato, salad", 580),
        Meal("Late snack", "Cottage cheese + berries", 200)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDietBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.rvMeals.layoutManager = LinearLayoutManager(this)
        binding.rvMeals.adapter = MealAdapter(sampleMeals)

        lifecycleScope.launch {
            val completed = runCatching { repo.fetchWeeklyWorkoutDays() }.getOrDefault(emptySet())
            val day = ((completed.maxOrNull() ?: 0) + 1).coerceAtMost(7)
            binding.tvDietDayHeader.text = "DAY $day"
        }
    }
}
