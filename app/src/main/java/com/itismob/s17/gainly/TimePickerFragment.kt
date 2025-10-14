package com.itismob.s17.gainly

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.text.format.DateFormat
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.Calendar

class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    private var listener: OnTimeSelectedListener? = null

    interface OnTimeSelectedListener {
        fun onTimeSelected(hour: Int, minute: Int)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? OnTimeSelectedListener
        if (listener == null) {
            throw ClassCastException("$context must implement OnTimeSelectedListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker.
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        // Create a new instance of TimePickerDialog and return it.
        return TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity))
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        // Do something with the time the user picks.
        listener?.onTimeSelected(hourOfDay, minute)
    }
}