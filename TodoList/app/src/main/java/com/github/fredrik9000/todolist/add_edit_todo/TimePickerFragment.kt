package com.github.fredrik9000.todolist.add_edit_todo

import android.app.Dialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class TimePickerFragment : DialogFragment(), OnTimeSetListener {

    private var listener: OnSelectTimeDialogInteractionListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val calendar = Calendar.getInstance()

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, this, calendar[Calendar.HOUR_OF_DAY], calendar[Calendar.MINUTE], DateFormat.is24HourFormat(activity))
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        listener!!.onSelectTimeDialogInteraction(hourOfDay, minute)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = targetFragment as OnSelectTimeDialogInteractionListener?
        } catch (e: ClassCastException) {
            Log.e(TAG, "onAttach: ClassCastException" + e.message)
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnSelectTimeDialogInteractionListener {
        fun onSelectTimeDialogInteraction(hour: Int, minute: Int)
    }

    companion object {
        private const val TAG: String = "TimePickerFragment"
    }
}