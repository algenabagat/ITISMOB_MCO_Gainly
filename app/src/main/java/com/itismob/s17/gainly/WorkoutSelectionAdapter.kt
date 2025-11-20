package com.itismob.s17.gainly

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// ViewHolder for the adapter
class WorkoutSelectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val workoutNameTv: TextView = itemView.findViewById(R.id.workoutNameTv)
    val workoutDescTv: TextView = itemView.findViewById(R.id.workoutDescTv)
}

// Adapter to display workouts for selection
class WorkoutSelectionAdapter(
    private val workouts: List<Workout>,
    private val onWorkoutSelected: (Workout) -> Unit
) : RecyclerView.Adapter<WorkoutSelectionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutSelectionViewHolder {
        // We can reuse the item_workout layout if it suits, or create a simpler one.
        // Let's assume a simple layout: R.layout.item_workout_selection
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout_selectable, parent, false)
        return WorkoutSelectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutSelectionViewHolder, position: Int) {
        val workout = workouts[position]
        holder.workoutNameTv.text = workout.name
        holder.workoutDescTv.text = workout.description

        holder.itemView.setOnClickListener {
            onWorkoutSelected(workout)
        }
    }

    override fun getItemCount(): Int = workouts.size
}