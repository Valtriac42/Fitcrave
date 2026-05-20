package com.fitcrave.app.data

import com.fitcrave.app.data.models.DailyStats
import com.fitcrave.app.data.models.Profile
import com.fitcrave.app.data.models.WorkoutRow
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Thin wrapper around the Supabase client. Every call is a suspend function so callers must
 * be in a coroutine scope (lifecycleScope / viewModelScope).
 *
 * All read methods return null/empty on failure (table missing, network error, etc.) so the
 * UI never crashes — the user just sees zeros until the backend is reachable.
 */
class FitcraveRepository {

    private val client get() = SupabaseProvider.client

    val currentUserId: String?
        get() = client?.auth?.currentUserOrNull()?.id

    val currentUserEmail: String?
        get() = client?.auth?.currentUserOrNull()?.email

    suspend fun signUp(email: String, password: String, fullName: String) {
        val c = SupabaseProvider.requireClient()
        c.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        currentUserId?.let { uid ->
            runCatching {
                c.from("profiles").insert(
                    Profile(id = uid, fullName = fullName, email = email)
                )
            }
        }
    }

    suspend fun signIn(email: String, password: String) {
        val c = SupabaseProvider.requireClient()
        c.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signOut() {
        client?.auth?.signOut()
    }

    suspend fun fetchProfile(): Profile? {
        val c = client ?: return null
        val uid = currentUserId ?: return null
        return runCatching {
            c.from("profiles")
                .select {
                    filter { eq("id", uid) }
                    limit(1)
                }
                .decodeSingleOrNull<Profile>()
        }.getOrNull()
    }

    suspend fun fetchTodayStats(): DailyStats? {
        val c = client ?: return null
        val uid = currentUserId ?: return null
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        return runCatching {
            c.from("daily_stats")
                .select {
                    filter {
                        eq("user_id", uid)
                        eq("day_date", today)
                    }
                    limit(1)
                }
                .decodeSingleOrNull<DailyStats>()
        }.getOrNull()
    }

    suspend fun fetchWeeklyWorkoutDays(): Set<Int> {
        val c = client ?: return emptySet()
        val uid = currentUserId ?: return emptySet()
        return runCatching {
            c.from("workouts")
                .select {
                    filter {
                        eq("user_id", uid)
                        eq("completed", true)
                    }
                }
                .decodeList<WorkoutRow>()
                .map { it.dayNumber }
                .toSet()
        }.getOrElse { emptySet() }
    }

    suspend fun markWorkoutComplete(dayNumber: Int) {
        val c = client ?: return
        val uid = currentUserId ?: return
        runCatching {
            c.from("workouts").insert(
                WorkoutRow(userId = uid, dayNumber = dayNumber, completed = true)
            )
        }
    }
}
