package com.github.fredrik9000.todolist.add_edit_todo;

import android.app.AlarmManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.github.fredrik9000.todolist.R;
import com.github.fredrik9000.todolist.databinding.FragmentAddEditTodoBinding;
import com.google.android.material.snackbar.Snackbar;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddEditTodoFragment extends Fragment implements DatePickerFragment.OnSelectDateDialogInteractionListener, TimePickerFragment.OnSelectTimeDialogInteractionListener {

    private FragmentAddEditTodoBinding binding;
    private AddEditTodoViewModel addEditTodoViewModel;

    private static final String DESCRIPTION_SAVED_STATE = "TODO_DESCRIPTION";
    private static final String NOTE_SAVED_STATE = "TODO_PRIORITY";
    private static final String PRIORITY_SAVED_STATE = "TODO_PRIORITY";
    static final String NOTIFICATION_YEAR_SAVED_STATE = "NOTIFICATION_YEAR";
    static final String NOTIFICATION_MONTH_SAVED_STATE = "NOTIFICATION_MONTH";
    static final String NOTIFICATION_DAY_SAVED_STATE = "NOTIFICATION_DAY";
    static final String NOTIFICATION_HOUR_SAVED_STATE = "NOTIFICATION_HOUR";
    static final String NOTIFICATION_MINUTE_SAVED_STATE = "NOTIFICATION_MINUTE";
    static final String NOTIFICATION_ID_SAVED_STATE = "NOTIFICATION_ID";
    private static final String HAS_NOTIFICATION_SAVED_STATE = "HAS_NOTIFICATION";
    private static final String NOTIFICATION_UPDATE_STATE_SAVED_STATE = "NOTIFICATION_UPDATE_STATE";

    public AddEditTodoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_edit_todo, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addEditTodoViewModel = new ViewModelProvider(this).get(AddEditTodoViewModel.class);
        AddEditTodoFragmentArgs args = AddEditTodoFragmentArgs.fromBundle(getArguments());
        addEditTodoViewModel.todoId = args.getId();

        String todoDescription;
        String todoNote;

        if (savedInstanceState != null) {
            todoDescription = savedInstanceState.getString(DESCRIPTION_SAVED_STATE);
            todoNote = savedInstanceState.getString(NOTE_SAVED_STATE);
            addEditTodoViewModel.priority = savedInstanceState.getInt(PRIORITY_SAVED_STATE);
            addEditTodoViewModel.hasNotification = savedInstanceState.getBoolean(HAS_NOTIFICATION_SAVED_STATE);
            addEditTodoViewModel.notificationUpdateState = (NotificationUpdateState) savedInstanceState.getSerializable(NOTIFICATION_UPDATE_STATE_SAVED_STATE);
        } else {
            todoDescription = args.getDescription();
            todoNote = args.getNote();
            addEditTodoViewModel.priority = args.getPriority();
            addEditTodoViewModel.hasNotification = args.getHasNotification();
        }

        if (todoDescription.length() == 0) {
            binding.saveTodoButton.setEnabled(false);
            binding.saveTodoButton.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
        } else {
            binding.saveTodoButton.setEnabled(true);
            binding.saveTodoButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this.getContext(), R.color.colorAccent)));
            binding.todoDescriptionEditText.setText(todoDescription);
        }

        binding.todoNoteEditText.setText(todoNote);

        setupPriorityPicker();
        setupNotificationState(savedInstanceState);

        binding.todoDescriptionEditText.addTextChangedListener(descriptionTextWatcher);
        binding.saveTodoButton.setOnClickListener(saveButtonListener);
        binding.removeNotificationButton.setOnClickListener(removeNotificationButtonListener);
        binding.addNotificationButton.setOnClickListener(addNotificationButtonListener);
    }

    private void setupPriorityPicker() {
        binding.priorityPickerButton.setText(addEditTodoViewModel.getLabelForCurrentPriority());
        binding.priorityPickerButton.setTextColor(addEditTodoViewModel.getColorForCurrentPriority());
        binding.priorityPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (addEditTodoViewModel.priority == 0) {
                    addEditTodoViewModel.priority = 1;
                } else if (addEditTodoViewModel.priority == 1) {
                    addEditTodoViewModel.priority = 2;
                } else {
                    addEditTodoViewModel.priority = 0;
                }
                binding.priorityPickerButton.setText(addEditTodoViewModel.getLabelForCurrentPriority());
                binding.priorityPickerButton.setTextColor(addEditTodoViewModel.getColorForCurrentPriority());
            }
        });
    }

    private void setupNotificationState(@Nullable Bundle savedInstanceState) {
        if (addEditTodoViewModel.hasNotification) {
            if (savedInstanceState != null) {
                addEditTodoViewModel.setNotificationValuesFromBundle(savedInstanceState);
                displayNotificationAddedState(addEditTodoViewModel.createNotificationCalendar());
            } else {
                AddEditTodoFragmentArgs args = AddEditTodoFragmentArgs.fromBundle(getArguments());
                addEditTodoViewModel.setNotificationValuesFromArguments(args);

                if (addEditTodoViewModel.isNotificationExpired()) {
                    addEditTodoViewModel.clearNotificationValues();
                    addEditTodoViewModel.generateNewNotificationId();
                } else {
                    displayNotificationAddedState(addEditTodoViewModel.createNotificationCalendar());
                }
            }
        } else {
            // Tasks without notification will be given a generated notification id for later use
            // If the task is saved without a notification it wont be included
            addEditTodoViewModel.generateNewNotificationId();
        }
    }

    private View.OnClickListener saveButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
            addEditTodoViewModel.saveTodoItem(alarmManager, binding.todoDescriptionEditText.getText().toString(), binding.todoNoteEditText.getText().toString(), addEditTodoViewModel.convertPriorityLabelToValue(binding.priorityPickerButton.getText().toString()));
            NavController controller = Navigation.findNavController(getView());
            controller.navigateUp();
        }
    };

    private View.OnClickListener removeNotificationButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            displayNotificationNotAddedState();
            addEditTodoViewModel.hasNotification = false;
            final NotificationUpdateState tempNotificationUpdateState = addEditTodoViewModel.notificationUpdateState;
            addEditTodoViewModel.notificationUpdateState = NotificationUpdateState.REMOVED_NOTIFICATION;

            Snackbar snackbar = Snackbar.make(
                    binding.addEditTodoCoordinatorLayout,
                    R.string.notification_removed,
                    Snackbar.LENGTH_LONG
            ).setAction(R.string.undo, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (addEditTodoViewModel.isUndoDoubleClicked()) {
                        return;
                    }

                    addEditTodoViewModel.updateLastClickedUndoTime();

                    // When undoing, set the notification update state to what it was previously.
                    addEditTodoViewModel.notificationUpdateState = tempNotificationUpdateState;
                    addEditTodoViewModel.hasNotification = true;
                    displayNotificationAddedState(addEditTodoViewModel.createNotificationCalendar());

                    Snackbar snackbar2 = Snackbar.make(
                            binding.addEditTodoCoordinatorLayout,
                            R.string.undo_successful,
                            Snackbar.LENGTH_SHORT
                    );
                    snackbar2.show();
                }
            });
            snackbar.show();
        }
    };

    private View.OnClickListener addNotificationButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            DatePickerFragment datePickerFragment = new DatePickerFragment();
            datePickerFragment.setTargetFragment(AddEditTodoFragment.this, 1);
            datePickerFragment.show(getParentFragmentManager(), "datePicker");
        }
    };

    private TextWatcher descriptionTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (charSequence.toString().trim().length() == 0) {
                binding.saveTodoButton.setEnabled(false);
                binding.saveTodoButton.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
            } else {
                binding.saveTodoButton.setEnabled(true);
                binding.saveTodoButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(AddEditTodoFragment.this.getContext(), R.color.colorAccent)));
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private void displayNotificationAddedState(Calendar notificationCalendar) {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(binding.addEditTodoConstraintLayout);

        // When both buttons are showing, show each button on each side of the centered vertical guideline, moved towards the center
        constraintSet.connect(R.id.add_notification_button, ConstraintSet.END, R.id.centered_vertical_guideline, ConstraintSet.END, (int)getResources().getDimension(R.dimen.notification_buttons_space_divided_by_2));
        constraintSet.setHorizontalBias(R.id.add_notification_button, 1.0f);
        constraintSet.applyTo(binding.addEditTodoConstraintLayout);

        binding.notificationTextView.setText(HtmlCompat.fromHtml("<b>" + getString(R.string.notification_pretext) + "</b> "
                + DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, Locale.US).format(notificationCalendar.getTime()), HtmlCompat.FROM_HTML_MODE_LEGACY));
        binding.notificationTextView.setVisibility(View.VISIBLE);
        binding.removeNotificationButton.setVisibility(View.VISIBLE);
        binding.addNotificationButton.setText(R.string.update_notification);
    }

    private void displayNotificationNotAddedState() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(binding.addEditTodoConstraintLayout);

        // When only the add button is showing, center it horizontally
        constraintSet.connect(R.id.add_notification_button, ConstraintSet.END, R.id.add_edit_todo_constraint_layout, ConstraintSet.END, 0);
        constraintSet.setHorizontalBias(R.id.add_notification_button, 0.5f);
        constraintSet.applyTo(binding.addEditTodoConstraintLayout);

        binding.addNotificationButton.setText(R.string.add_notification);
        binding.notificationTextView.setVisibility(View.GONE);
        binding.removeNotificationButton.setVisibility(View.GONE);
    }

    @Override
    public void onSelectDateDialogInteraction(int year, int month, int day) {
        addEditTodoViewModel.setTemporaryNotificationDateValues(year, month, day);
        TimePickerFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.setTargetFragment(AddEditTodoFragment.this, 2);
        timePickerFragment.show(getParentFragmentManager(), "timePicker");
    }

    @Override
    public void onSelectTimeDialogInteraction(int hour, int minute) {
        Calendar notificationCalendar = Calendar.getInstance();
        notificationCalendar.set(addEditTodoViewModel.yearTemp, addEditTodoViewModel.monthTemp, addEditTodoViewModel.dayTemp, hour, minute, 0);
        Calendar currentTimeCalendar = Calendar.getInstance();
        if (notificationCalendar.getTimeInMillis() < currentTimeCalendar.getTimeInMillis()) {
            Toast.makeText(getActivity().getApplicationContext(), R.string.invalid_time, Toast.LENGTH_LONG).show();
        } else {
            addEditTodoViewModel.setFinallySelectedNotificationValues(hour, minute);
            displayNotificationAddedState(notificationCalendar);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PRIORITY_SAVED_STATE, addEditTodoViewModel.priority);
        outState.putString(DESCRIPTION_SAVED_STATE, binding.todoDescriptionEditText.getText().toString());
        outState.putString(NOTE_SAVED_STATE, binding.todoNoteEditText.getText().toString());
        outState.putInt(NOTIFICATION_YEAR_SAVED_STATE, addEditTodoViewModel.year);
        outState.putInt(NOTIFICATION_MONTH_SAVED_STATE, addEditTodoViewModel.month);
        outState.putInt(NOTIFICATION_DAY_SAVED_STATE, addEditTodoViewModel.day);
        outState.putInt(NOTIFICATION_HOUR_SAVED_STATE, addEditTodoViewModel.hour);
        outState.putInt(NOTIFICATION_MINUTE_SAVED_STATE, addEditTodoViewModel.minute);
        outState.putInt(NOTIFICATION_ID_SAVED_STATE, addEditTodoViewModel.notificationId);
        outState.putBoolean(HAS_NOTIFICATION_SAVED_STATE, addEditTodoViewModel.hasNotification);
        outState.putSerializable(NOTIFICATION_UPDATE_STATE_SAVED_STATE, addEditTodoViewModel.notificationUpdateState);
    }
}
