package com.itismob.s17.gainly

data class Exercise(
    val name: String,
    val description: String,
    val imageResId: Int = R.drawable.gainly_logo, // Default image
    val targetMuscle: String,
    var sets: Int = 3, // Default sets, modifiable by user
    var reps: Int = 10 // Default reps, modifiable by user
)