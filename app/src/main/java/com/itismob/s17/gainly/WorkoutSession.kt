package com.itismob.s17.gainly

data class WorkoutSession(
    val id: String = "",
    val workoutId: String, // Reference to template
    val workoutName: String,
    val date: Long = System.currentTimeMillis(),
    val exerciseSessions: MutableList<ExerciseSession>,
    var duration: Long = 0, // in seconds
    var completed: Boolean = false,
    val notes: String = ""
) {
    val totalVolume: Double get() = exerciseSessions.sumOf { it.getTotalVolume() }
    val exerciseCount: Int get() = exerciseSessions.size

    companion object {
        fun generateId(): String = "session_${System.currentTimeMillis()}"
    }
}
