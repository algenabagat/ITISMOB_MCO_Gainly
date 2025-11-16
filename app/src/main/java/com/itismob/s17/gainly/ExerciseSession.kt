package com.itismob.s17.gainly

data class ExerciseSession(
    val exerciseId: String,
    val exerciseName: String,
    val targetMuscle: String,
    val sets: List<SetRecord>,
    val targetReps: Int,
    val targetWeight: Double,
    var completed: Boolean = false,
) {
    fun getTotalVolume(): Double = sets.sumOf { it.getVolume() }
    fun getBestSet(): SetRecord? = sets.maxByOrNull { it.weight }
}
