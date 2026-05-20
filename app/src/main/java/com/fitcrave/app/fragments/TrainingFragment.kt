package com.fitcrave.app.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.fitcrave.app.R
import com.fitcrave.app.activities.DietActivity
import com.fitcrave.app.activities.ReportActivity
import com.fitcrave.app.activities.WorkoutActivity
import com.fitcrave.app.data.FitcraveRepository
import com.fitcrave.app.databinding.FragmentTrainingBinding
import kotlinx.coroutines.launch
import java.util.Calendar

class TrainingFragment : Fragment() {

    private var _binding: FragmentTrainingBinding? = null
    private val binding get() = _binding!!
    private val repo = FitcraveRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrainingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardWorkout.setOnClickListener { openWorkout() }
        binding.btnStartWorkout.setOnClickListener { openWorkout() }
        binding.cardDiet.setOnClickListener { openDiet() }
        binding.btnStartDiet.setOnClickListener { openDiet() }
        binding.cardReport.setOnClickListener {
            startActivity(Intent(requireContext(), ReportActivity::class.java))
        }

        binding.swipeRefresh.setOnRefreshListener { refresh() }
        refresh()
    }

    private fun openWorkout() {
        startActivity(Intent(requireContext(), WorkoutActivity::class.java))
    }

    private fun openDiet() {
        startActivity(Intent(requireContext(), DietActivity::class.java))
    }

    private fun refresh() {
        // highlight today's weekday on the weekly chip row
        val cal = Calendar.getInstance()
        val today = ((cal.get(Calendar.DAY_OF_WEEK) + 5) % 7) + 1 // make Monday = 1, Sunday = 7

        val chips = listOf(
            binding.chip1, binding.chip2, binding.chip3,
            binding.chip4, binding.chip5, binding.chip6, binding.chip7
        )
        chips.forEachIndexed { idx, chip ->
            chip.setBackgroundResource(
                if (idx + 1 == today) R.drawable.bg_weekly_chip_active
                else R.drawable.bg_weekly_chip
            )
        }

        lifecycleScope.launch {
            val stats = runCatching { repo.fetchTodayStats() }.getOrNull()
            binding.tvWorkoutsCount.text = (stats?.workoutsCompleted ?: 0).toString()
            binding.tvKcal.text = (stats?.kcal ?: 0).toString()
            binding.tvMinutes.text = (stats?.minutes ?: 0).toString()

            val completedDays = runCatching { repo.fetchWeeklyWorkoutDays() }.getOrDefault(emptySet())
            val workoutDay = completedDays.maxOrNull() ?: 0
            binding.tvWorkoutDay.text = "DAY $workoutDay"
            binding.tvDietDay.text = "DAY $workoutDay"

            binding.swipeRefresh.isRefreshing = false
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
