package com.github.fredrik9000.todolist.todolist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.github.fredrik9000.todolist.R;

import java.util.ArrayList;

public class DeleteTodosDialog extends DialogFragment {

    private static final String TAG = "DeleteTodosDialog";
    private OnDeleteTodosDialogInteractionListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final ArrayList<Integer> selectedPriorities = new ArrayList<>();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.delete_todo_items);
        builder.setMultiChoiceItems(R.array.priorities, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {
                if (isChecked) {
                    // If the user checked the priority, add it to the selected priorities
                    selectedPriorities.add(which);
                } else if (selectedPriorities.contains(which)) {
                    // Else, if the priority is already in the array, remove it
                    selectedPriorities.remove(Integer.valueOf(which));
                }
            }
        })
                // Set the action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDeleteTodosDialogInteraction(selectedPriorities);
                    }
                })
                .setNegativeButton(R.string.cancel, null);
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (OnDeleteTodosDialogInteractionListener) getTargetFragment();
        } catch (ClassCastException e) {
            Log.e(TAG, "onAttach: ClassCastException" + e.getMessage());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface OnDeleteTodosDialogInteractionListener {
        void onDeleteTodosDialogInteraction(ArrayList<Integer> priorities);
    }
}
