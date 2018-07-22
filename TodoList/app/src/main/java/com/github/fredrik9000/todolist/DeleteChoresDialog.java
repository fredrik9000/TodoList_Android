package com.github.fredrik9000.todolist;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

public class DeleteChoresDialog extends DialogFragment {

    private OnDeleteChoresDialogInteractionListener mListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delete_chores_popup, container, false);
        TextView txtClose = view.findViewById(R.id.closeDialog);
        txtClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });
        Button deleteButton = view.findViewById(R.id.deleteChoresPopupButton);
        final CheckBox lowPriorityCB = view.findViewById(R.id.deleteLowPriorityChoresCheckBox);
        final CheckBox mediumPriorityCB = view.findViewById(R.id.deleteMediumPriorityChoresCheckBox);
        final CheckBox highPriorityCB = view.findViewById(R.id.deleteHighPriorityChoresCheckBox);
        CheckBox[] checkBoxes = {lowPriorityCB, mediumPriorityCB, highPriorityCB};

        lowPriorityCB.setOnCheckedChangeListener(new DeleteChoresDialog.PriorityCheckBoxChanged(checkBoxes, deleteButton));
        mediumPriorityCB.setOnCheckedChangeListener(new DeleteChoresDialog.PriorityCheckBoxChanged(checkBoxes, deleteButton));
        highPriorityCB.setOnCheckedChangeListener(new DeleteChoresDialog.PriorityCheckBoxChanged(checkBoxes, deleteButton));

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean[] priorities = new boolean[3];
                if (lowPriorityCB.isChecked()) {
                    priorities[0] = true;
                }
                if (mediumPriorityCB.isChecked()) {
                    priorities[1] = true;
                }
                if (highPriorityCB.isChecked()) {
                    priorities[2] = true;
                }
                mListener.onDeleteChoresDialogInteraction(priorities);
                getDialog().dismiss();
            }
        });

        return view;
    }

    class PriorityCheckBoxChanged implements CheckBox.OnCheckedChangeListener
    {
        private CheckBox[] checkBoxes;
        private Button deleteButton;
        PriorityCheckBoxChanged(CheckBox[] checkBoxes, Button deleteButton) {
            this.checkBoxes = checkBoxes;
            this.deleteButton = deleteButton;
        }
        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            boolean isAnyChecked = false;

            for (CheckBox checkBox : checkBoxes) {
                if (checkBox.isChecked()) {
                    isAnyChecked = true;
                    break;
                }
            }

            deleteButton.setEnabled(isAnyChecked);
        }
    }

    public interface OnDeleteChoresDialogInteractionListener {
        void onDeleteChoresDialogInteraction(boolean[] priorities);
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
