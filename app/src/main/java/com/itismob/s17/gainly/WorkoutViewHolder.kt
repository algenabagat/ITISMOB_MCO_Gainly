package com.itismob.s17.gainly

import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val workoutNameTv: TextView = itemView.findViewById(R.id.workoutNameTv)
    val workoutDescriptionTv: TextView = itemView.findViewById(R.id.workoutDescriptionTv)
    val exerciseCountTv: TextView = itemView.findViewById(R.id.exerciseCountTv)
    val exercisesLayout: LinearLayout = itemView.findViewById(R.id.exercisesLayout)
    val startBtn: Button = itemView.findViewById(R.id.startBtn)
    val optionsBtn: ImageButton = itemView.findViewById(R.id.optionsBtn)
    val favoriteBtn: ImageButton = itemView.findViewById(R.id.favoriteBtn)
}