package com.fitcrave.app.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Maps to a `profiles` table:
 *   id uuid primary key references auth.users(id),
 *   full_name text,
 *   email text,
 *   created_at timestamptz default now()
 */
@Serializable
data class Profile(
    @SerialName("id") val id: String,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("email") val email: String? = null
)

/**
 * Maps to a `daily_stats` table:
 *   id bigserial,
 *   user_id uuid references auth.users(id),
 *   day_date date,
 *   workouts_completed int default 0,
 *   kcal int default 0,
 *   minutes int default 0
 */
@Serializable
data class DailyStats(
    @SerialName("user_id") val userId: String,
    @SerialName("day_date") val dayDate: String,
    @SerialName("workouts_completed") val workoutsCompleted: Int = 0,
    @SerialName("kcal") val kcal: Int = 0,
    @SerialName("minutes") val minutes: Int = 0
)

/**
 * Maps to a `workouts` table:
 *   id bigserial,
 *   user_id uuid,
 *   day_number int,
 *   completed boolean default false,
 *   created_at timestamptz default now()
 */
@Serializable
data class WorkoutRow(
    @SerialName("user_id") val userId: String,
    @SerialName("day_number") val dayNumber: Int,
    @SerialName("completed") val completed: Boolean = false
)

data class Exercise(
    val name: String,
    val sets: Int,
    val reps: Int
)

data class Meal(
    val name: String,
    val items: String,
    val kcal: Int
)
