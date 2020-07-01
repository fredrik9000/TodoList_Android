package com.github.fredrik9000.todolist.add_edit_todo

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class DatePickerFragment : DialogFragment(), OnDateSetListener {

    private var listener: OnSelectDateDialogInteractionListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val calendar = Calendar.getInstance()

        // Create a new instance of DatePickerDialog and return it
        return DatePickerDialog(requireActivity(), this, calendar[Calendar.YEAR], calendar[Calendar.MONTH], calendar[Calendar.DAY_OF_MONTH]).apply {
            datePicker.minDate = System.currentTimeMillis() - 1000
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, day: Int) {
        listener!!.onSelectDateDialogInteraction(year, month, day)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = targetFragment as OnSelectDateDialogInteractionListener?
        } catch (e: ClassCastException) {
            Log.e(TAG, "onAttach: ClassCastException : " + e.message)
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnSelectDateDialogInteractionListener {
        fun onSelectDateDialogInteraction(year: Int, month: Int, day: Int)
    }

    companion object {
        private const val TAG: String = "DatePickerFragment"
    }
}