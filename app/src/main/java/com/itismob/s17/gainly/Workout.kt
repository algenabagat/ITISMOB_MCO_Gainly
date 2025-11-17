package com.itismob.s17.gainly

import java.io.Serializable

data class Workout(
    val name: String,
    val description: String,
    val exercises: List<Exercise>,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = ""
) {

    constructor() : this("", "", "", emptyList(), false,System.currentTimeMillis(), "")
    val exerciseCount: Int get() = exercises.size

    companion object {
        fun generateId(): String = "workout_${System.currentTimeMillis()}"
    }
}
