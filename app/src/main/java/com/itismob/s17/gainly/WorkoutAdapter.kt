package com.itismob.s17.gainly

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WorkoutAdapter(
    private var workouts: List<Workout> = emptyList(),
    private val onExerciseClick: (Exercise) -> Unit = {},
    private val onStartWorkout: (Workout) -> Unit = {}
) : RecyclerView.Adapter<WorkoutViewHolder>() {

    fun updateWorkouts(newWorkouts: List<Workout>) {
        workouts = newWorkouts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val workout = workouts[position]

        // workout info
        holder.workoutNameTv.text = workout.name
        holder.workoutDescriptionTv.text = workout.description
        holder.exerciseCountTv.text = "${workout.exerciseCount} exercises"

        // clear views
        holder.exercisesLayout.removeAllViews()

        // add ALL exercises (no limit)
        workout.exercises.forEach { exercise ->
            val exerciseView = TextView(holder.itemView.context).apply {
                text = "• ${exercise.name} (${exercise.sets}x${exercise.reps})"
                setTextColor(holder.itemView.context.getColor(android.R.color.black))
                textSize = 12f
                setOnClickListener {
                    onExerciseClick(exercise)
                }
            }
            holder.exercisesLayout.addView(exerciseView)
        }

        // Set click listener for start button
        holder.startBtn.setOnClickListener {
            onStartWorkout(workout)
        }

        // TODO: Implement options button functionality
        holder.optionsBtn.setOnClickListener {
            // Show menu for editing workout, deleting, etc.
        }
    }

    override fun getItemCount(): Int = workouts.size
}