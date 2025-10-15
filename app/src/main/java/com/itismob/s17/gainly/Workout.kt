package com.itismob.s17.gainly

data class Workout(
    val name: String,
    val description: String,
    val exercises: List<Exercise>,
    val isFavorite: Boolean = false,
    val exerciseCount: Int = exercises.size
)