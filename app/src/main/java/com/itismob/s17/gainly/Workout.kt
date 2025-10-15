package com.itismob.s17.gainly

data class Workout(
    val name: String,
    val description: String,
    val exercises: List<Exercise>,
    val exerciseCount: Int = exercises.size
)