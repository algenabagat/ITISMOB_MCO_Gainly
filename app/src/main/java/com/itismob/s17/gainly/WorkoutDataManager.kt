package com.itismob.s17.gainly

object WorkoutDataManager {
    val workouts = mutableListOf<Workout>()
    val workoutSessions = mutableListOf<WorkoutSession>()

    fun getWorkout(position: Int): Workout = workouts[position]

    fun getWorkoutById(id: String): Workout? = workouts.find { it.id == id }

    fun addWorkout(workout: Workout) {
        workouts.add(0, workout)
    }

    fun startWorkoutSession(workoutPosition: Int): WorkoutSession {
        val workout = workouts[workoutPosition]

        val exerciseSessions = workout.exercises.map { exercise ->
            ExerciseSession(
                exerciseId = exercise.id,
                exerciseName = exercise.name,
                targetMuscle = exercise.targetMuscle,
                sets = List(exercise.defaultSets) { setIndex ->
                    SetRecord(
                        setNumber = setIndex + 1,
                        weight = 0.0,
                        reps = exercise.defaultReps
                    )
                }.toMutableList(),
                targetReps = exercise.defaultReps,
                targetWeight = 0.0,
                completed = false
            )
        }.toMutableList()

        val session = WorkoutSession(
            id = WorkoutSession.generateId(),
            workoutId = workout.id,
            workoutName = workout.name,
            exerciseSessions = exerciseSessions,
            completed = false,
            duration = 0L
        )

        workoutSessions.add(session)
        return session
    }
    fun saveWorkoutSession(session: WorkoutSession) {
        val existingSession = workoutSessions.find { it.id == session.id }
        if (existingSession != null) {
            val index = workoutSessions.indexOf(existingSession)
            workoutSessions[index] = session
        } else {
            workoutSessions.add(session)
        }
    }

}
