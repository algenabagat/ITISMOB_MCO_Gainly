package com.itismob.s17.gainly

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SelectedExercisesAdapter(private var exercises: MutableList<Exercise>,private val onRemoveClick: (Exercise) -> Unit) : RecyclerView.Adapter<SelectedExercisesAdapter.SelectedExerciseViewHolder>() {

    // This ViewHolder holds the views for a single item.
    inner class SelectedExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val exerciseNameTv: TextView = itemView.findViewById(R.id.exerciseNameTv)
        private val removeExerciseBtn: ImageButton = itemView.findViewById(R.id.removeExerciseBtn)

        fun bind(exercise: Exercise) {
            exerciseNameTv.text = exercise.name
            // Set a click listener for the remove button
            removeExerciseBtn.setOnClickListener {
                onRemoveClick(exercise)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedExerciseViewHolder {
        // Inflate the layout for a single item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.selected_exercise_item, parent, false)
        return SelectedExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: SelectedExerciseViewHolder, position: Int) {
        // Bind the data to the views in the ViewHolder
        val exercise = exercises[position]
        holder.bind(exercise)
    }

    override fun getItemCount(): Int {
        // Return the total number of items in the list
        return exercises.size
    }

    // A helper function to update the list of exercises and refresh the RecyclerView
    fun updateExercises(newExercises: List<Exercise>) {
        exercises.clear()
        exercises.addAll(newExercises)
        notifyDataSetChanged() // This is a simple way to refresh the list
    }
}
