package com.github.fredrik9000.todolist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import java.util.ArrayList;

public class DeleteChoresDialog extends DialogFragment {

    private ArrayList mSelectedItems;
    private OnDeleteChoresDialogInteractionListener mListener;

    public interface OnDeleteChoresDialogInteractionListener {
        void onDeleteChoresDialogInteraction(ArrayList<Integer> priorities);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mSelectedItems = new ArrayList();  // Where we track the selected items
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.delete_todo_items);
        builder.setMultiChoiceItems(R.array.priorities, null, new DialogInterface.OnMultiChoiceClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int which, boolean isChecked)
            {
                if (isChecked) {
                    // If the user checked the item, add it to the selected items
                    mSelectedItems.add(which);
                } else if (mSelectedItems.contains(which)) {
                    // Else, if the item is already in the array, remove it
                    mSelectedItems.remove(Integer.valueOf(which));
                }
            }
        })
        // Set the action buttons
       .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                mListener.onDeleteChoresDialogInteraction(mSelectedItems);
            }
        })
        .setNegativeButton(R.string.cancel,null);
    return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDeleteChoresDialogInteractionListener) {
            mListener = (OnDeleteChoresDialogInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnDeleteChoresDialogInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
