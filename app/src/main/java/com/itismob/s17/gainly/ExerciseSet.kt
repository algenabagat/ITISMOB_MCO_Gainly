package com.itismob.s17.gainly

data class ExerciseSet(
    val setNumber: Int,
    var weight: Double,
    var reps: Int,
    var completed: Boolean = false,
    val previousWeight: Double = 0.0
) //{ Not used currently
//    fun getVolume(): Double {
//        return weight * reps
//    }
//}