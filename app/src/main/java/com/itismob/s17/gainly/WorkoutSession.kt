package com.itismob.s17.gainly

import java.util.*

data class WorkoutSession(
    val id: String = UUID.randomUUID().toString(),
    val workoutName: String,
    val date: Long = System.currentTimeMillis(),
    val exerciseSessions: List<ExerciseSession>,
    var duration: Long = 0, // in seconds
    var completed: Boolean = false,
    val notes: String = ""
) {
    fun getTotalVolume(): Double {
        return exerciseSessions.sumOf { it.getTotalVolume() }
    }
}