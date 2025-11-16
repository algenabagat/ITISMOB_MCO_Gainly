package com.itismob.s17.gainly

data class Workout(
    val id: String = "", // For Firebase/SQLite
    val name: String,
    val description: String,
    val exercises: List<Exercise>,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = "user" // For multi-user support later
) {
    val exerciseCount: Int get() = exercises.size

    companion object {
        fun generateId(): String = "workout_${System.currentTimeMillis()}"
    }
}
