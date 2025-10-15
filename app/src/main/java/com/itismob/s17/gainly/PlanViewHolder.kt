package com.itismob.s17.gainly

import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val planNameTv: TextView = itemView.findViewById(R.id.planNameTv)
    val dateTv: TextView = itemView.findViewById(R.id.dateTv)
    val timeTv: TextView = itemView.findViewById(R.id.timeTv)
    val startPlanBtn: Button = itemView.findViewById(R.id.startPlanBtn)
    val optionsPlanBtn: ImageButton = itemView.findViewById(R.id.optionsPlanBtn)
}