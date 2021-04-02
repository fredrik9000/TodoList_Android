package com.github.fredrik9000.todolist.add_edit_todo

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import java.util.*

class DatePickerFragment : DialogFragment(), OnDateSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Uses the current date as the default date in the picker
        with(Calendar.getInstance()) {
            return DatePickerDialog(requireActivity(), this@DatePickerFragment, this[Calendar.YEAR], this[Calendar.MONTH], this[Calendar.DAY_OF_MONTH]).apply {
                datePicker.minDate = System.currentTimeMillis() - 1000
            }
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, day: Int) {
        setFragmentResult(DATE_PICKER_FRAGMENT_REQUEST_KEY, bundleOf(BUNDLE_YEAR_KEY to year, BUNDLE_MONTH_KEY to month, BUNDLE_DAY_KEY to day))
    }

    companion object {
        const val DATE_PICKER_FRAGMENT_REQUEST_KEY = "DATE_PICKER_FRAGMENT_REQUEST"
        const val BUNDLE_YEAR_KEY = "year"
        const val BUNDLE_MONTH_KEY = "month"
        const val BUNDLE_DAY_KEY = "day"
    }
}