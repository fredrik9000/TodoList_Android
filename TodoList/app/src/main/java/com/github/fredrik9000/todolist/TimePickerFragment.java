package com.github.fredrik9000.todolist;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    private OnSelectTimeDialogInteractionListener mListener;

    public interface OnSelectTimeDialogInteractionListener {
        void onSelectTimeDialogInteraction(int hour, int minute);
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar datetime = Calendar.getInstance();
        Calendar c = Calendar.getInstance();
        datetime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        datetime.set(Calendar.MINUTE, minute);
        if (datetime.getTimeInMillis() >= c.getTimeInMillis()) {
            mListener.onSelectTimeDialogInteraction(hourOfDay, minute);
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "Invalid Time, select a future date and time", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSelectTimeDialogInteractionListener) {
            mListener = (OnSelectTimeDialogInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSelectTimeDialogInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}