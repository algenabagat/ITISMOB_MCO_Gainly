package com.itismob.s17.gainly

data class WorkoutSessionData(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val workoutId: String = "",
    val workoutName: String = "",
    val date: Long = 0,
    val duration: Long = 0,
    val completed: Boolean = false,
    val completedVolume: Double = 0.0,
    val completedSets: Int = 0,
    val completedReps: Int = 0,
    val totalSets: Int = 0,
    val totalVolume: Double = 0.0,
    val notes: String = "",
    val timestamp: Long = 0,
    val exerciseCount: Int = 0,
    val exerciseSessions: List<Map<String, Any>> = emptyList()
)