package com.fitcrave.app.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.fitcrave.app.auth.LoginActivity
import com.fitcrave.app.data.FitcraveRepository
import com.fitcrave.app.databinding.FragmentProfileBinding
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val repo = FitcraveRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rowLogout.setOnClickListener {
            lifecycleScope.launch {
                runCatching { repo.signOut() }
                val i = Intent(requireContext(), LoginActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(i)
                requireActivity().finish()
            }
        }

        lifecycleScope.launch {
            val profile = runCatching { repo.fetchProfile() }.getOrNull()
            binding.tvProfileName.text = profile?.fullName ?: "Athlete"
            binding.tvProfileEmail.text = profile?.email ?: repo.currentUserEmail ?: ""

            val completed = runCatching { repo.fetchWeeklyWorkoutDays() }.getOrDefault(emptySet())
            binding.tvTotalWorkouts.text = completed.size.toString()
            binding.tvStreak.text = computeStreak(completed).toString()

            val today = runCatching { repo.fetchTodayStats() }.getOrNull()
            binding.tvTotalKcal.text = (today?.kcal ?: 0).toString()
        }
    }

    private fun computeStreak(days: Set<Int>): Int {
        if (days.isEmpty()) return 0
        var streak = 0
        for (d in 7 downTo 1) {
            if (days.contains(d)) streak++ else break
        }
        return streak
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
