package com.itismob.s17.gainly

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SelectedExercisesAdapter(private var exercises: MutableList<Exercise>,private val onRemoveClick: (Exercise) -> Unit) : RecyclerView.Adapter<SelectedExercisesAdapter.SelectedExerciseViewHolder>() {
    inner class SelectedExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val exerciseNameTv: TextView = itemView.findViewById(R.id.exerciseNameTv)
        private val removeExerciseBtn: ImageButton = itemView.findViewById(R.id.removeExerciseBtn)

        fun bind(exercise: Exercise) {
            exerciseNameTv.text = exercise.name
            removeExerciseBtn.setOnClickListener {
                onRemoveClick(exercise)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.selected_exercise_item, parent, false)
        return SelectedExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: SelectedExerciseViewHolder, position: Int) {
        val exercise = exercises[position]
        holder.bind(exercise)
    }

    override fun getItemCount(): Int {
        return exercises.size
    }
}
