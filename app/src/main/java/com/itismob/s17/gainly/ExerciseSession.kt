package com.itismob.s17.gainly

data class ExerciseSession(
    val exerciseName: String,
    val sets: List<ExerciseSet>,
    var completed: Boolean = false
) {
    fun getTotalVolume(): Double {
        return sets.sumOf { it.weight * it.reps }
    }

//    currently not in use
//    fun getMaxWeight(): Double {
//        return sets.maxOfOrNull { it.weight } ?: 0.0
//    }
//
//    fun getAllSetsCompleted(): Boolean {
//        return sets.all { it.completed }
//    }
}