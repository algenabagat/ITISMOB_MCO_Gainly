package com.itismob.s17.gainly

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WorkoutAdapter(private var workouts: List<Workout> = emptyList()) : RecyclerView.Adapter<WorkoutViewHolder>() {

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

        // add exercises (max 3)
        val exercisesToShow = workout.exercises.take(3)
        exercisesToShow.forEach { exercise ->
            val exerciseView = TextView(holder.itemView.context).apply {
                text = "• $exercise"
                setTextColor(holder.itemView.context.getColor(android.R.color.black))
                textSize = 12f
            }
            holder.exercisesLayout.addView(exerciseView)
        }

        // more than 3 exercises will show "..."
        if (workout.exercises.size > 3) {
            val moreView = TextView(holder.itemView.context).apply {
                text = "..."
                setTextColor(holder.itemView.context.getColor(android.R.color.black))
                textSize = 12f
            }
            holder.exercisesLayout.addView(moreView)
        }
    }

    override fun getItemCount(): Int = workouts.size
}