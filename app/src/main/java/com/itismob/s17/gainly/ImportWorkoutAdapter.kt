package com.itismob.s17.gainly

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ImportWorkoutAdapter(
    private var workouts: List<Workout>,
    private val onWorkoutClick: (Workout) -> Unit,
    private val onWorkoutSelected: (Workout, Boolean) -> Unit
) : RecyclerView.Adapter<ImportWorkoutAdapter.ViewHolder>() {

    private val selectedWorkouts = mutableSetOf<String>()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.workoutNameTextView)
        val createdByTextView: TextView = itemView.findViewById(R.id.createdByTextView)
        val exerciseCountTextView: TextView = itemView.findViewById(R.id.exerciseCountTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.import_workout_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workout = workouts[position]

        holder.nameTextView.text = workout.name
        holder.createdByTextView.text = "By: ${workout.createdBy}"
        holder.exerciseCountTextView.text = "${workout.exercises.size} exercises"

        val isSelected = selectedWorkouts.contains(workout.id)

        holder.itemView.setOnClickListener {
            if (isSelected) {
                selectedWorkouts.remove(workout.id)
                onWorkoutSelected(workout, false)
            } else {
                selectedWorkouts.add(workout.id)
                onWorkoutSelected(workout, true)
            }
            notifyItemChanged(position)

            onWorkoutClick(workout)
        }
    }

    fun getSelectedWorkouts(): List<Workout> {
        return workouts.filter { selectedWorkouts.contains(it.id) }
    }

    override fun getItemCount(): Int = workouts.size
}