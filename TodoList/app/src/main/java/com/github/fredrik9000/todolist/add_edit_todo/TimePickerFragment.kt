package com.github.fredrik9000.todolist.add_edit_todo

import android.app.Dialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import java.util.*

class TimePickerFragment : DialogFragment(), OnTimeSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Uses the current time as the default values for the picker
        with(Calendar.getInstance()) {
            return TimePickerDialog(activity, this@TimePickerFragment, this[Calendar.HOUR_OF_DAY], this[Calendar.MINUTE], DateFormat.is24HourFormat(activity))
        }
    }

    override fun onTimeSet(view: TimePicker?, hour: Int, minute: Int) {
        setFragmentResult(TIME_PICKER_FRAGMENT_REQUEST_KEY, bundleOf(BUNDLE_HOUR_KEY to hour, BUNDLE_MINUTE_KEY to minute))
    }

    companion object {
        const val TIME_PICKER_FRAGMENT_REQUEST_KEY = "TIME_PICKER_FRAGMENT_REQUEST"
        const val BUNDLE_HOUR_KEY = "hour"
        const val BUNDLE_MINUTE_KEY = "minute"
    }
}