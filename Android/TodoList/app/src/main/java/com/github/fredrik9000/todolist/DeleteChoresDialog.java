package com.github.fredrik9000.todolist;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.github.fredrik9000.todolist.model.Chore;

import java.util.Iterator;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DeleteChoresDialog.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DeleteChoresDialog#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DeleteChoresDialog extends DialogFragment {

    private TextView txtClose;
    private Button deleteChoresButton;
    private CheckBox deleteLowPriorityCB;
    private CheckBox deleteMediumPriorityCB;
    private CheckBox deleteHighPriorityCB;
    private OnFragmentInteractionListener mListener;

    public DeleteChoresDialog() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delete_chores_popup, container, false);
        txtClose = view.findViewById(R.id.closeDialog);
        txtClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });
        deleteChoresButton = view.findViewById(R.id.deleteChoresPopupButton);
        deleteLowPriorityCB = view.findViewById(R.id.deleteLowPriorityChoresCheckBox);
        deleteMediumPriorityCB = view.findViewById(R.id.deleteMediumPriorityChoresCheckBox);
        deleteHighPriorityCB = view.findViewById(R.id.deleteHighPriorityChoresCheckBox);
        deleteLowPriorityCB.setOnCheckedChangeListener(new DeleteChoresDialog.priorityCheckBoxChanged());
        deleteMediumPriorityCB.setOnCheckedChangeListener(new DeleteChoresDialog.priorityCheckBoxChanged());
        deleteHighPriorityCB.setOnCheckedChangeListener(new DeleteChoresDialog.priorityCheckBoxChanged());

        deleteChoresButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean[] priorities = new boolean[3];
                if (deleteLowPriorityCB.isChecked()) {
                    priorities[0] = true;
                }
                if (deleteMediumPriorityCB.isChecked()) {
                    priorities[1] = true;
                }
                if (deleteHighPriorityCB.isChecked()) {
                    priorities[2] = true;
                }
                mListener.onFragmentInteraction(priorities);
                getDialog().dismiss();
            }
        });

        return view;
    }

    class priorityCheckBoxChanged implements CheckBox.OnCheckedChangeListener
    {
        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            if(deleteLowPriorityCB.isChecked() || deleteMediumPriorityCB.isChecked() || deleteHighPriorityCB.isChecked()) {
                deleteChoresButton.setEnabled(true);
            } else {
                deleteChoresButton.setEnabled(false);
            }
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(boolean[] priorities);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
