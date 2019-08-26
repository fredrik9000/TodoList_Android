package com.github.fredrik9000.todolist.add_edit_todo;


import android.app.AlarmManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.SystemClock;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.github.fredrik9000.todolist.R;
import com.github.fredrik9000.todolist.databinding.FragmentAddEditTodoBinding;
import com.github.fredrik9000.todolist.model.Todo;
import com.github.fredrik9000.todolist.notifications.NotificationUtil;
import com.google.android.material.snackbar.Snackbar;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

public class AddEditTodoFragment extends Fragment implements DatePickerFragment.OnSelectDateDialogInteractionListener, TimePickerFragment.OnSelectTimeDialogInteractionListener {

    private static final String DESCRIPTION_SAVED_STATE = "TODO_DESCRIPTION";
    private static final String PRIORITY_SAVED_STATE = "TODO_PRIORITY";
    private static final String NOTIFICATION_YEAR_SAVED_STATE = "NOTIFICATION_YEAR";
    private static final String NOTIFICATION_MONTH_SAVED_STATE = "NOTIFICATION_MONTH";
    private static final String NOTIFICATION_DAY_SAVED_STATE = "NOTIFICATION_DAY";
    private static final String NOTIFICATION_HOUR_SAVED_STATE = "NOTIFICATION_HOUR";
    private static final String NOTIFICATION_MINUTE_SAVED_STATE = "NOTIFICATION_MINUTE";
    private static final String NOTIFICATION_ID_SAVED_STATE = "NOTIFICATION_ID";
    private static final String HAS_NOTIFICATION_SAVED_STATE = "HAS_NOTIFICATION";
    private static final String HAS_ADDED_NOTIFICATION_SAVED_STATE = "HAS_ADDED_NOTIFICATION";
    private static final String HAS_REMOVED_NOTIFICATION_SAVED_STATE = "HAS_REMOVED_NOTIFICATION";

    private FragmentAddEditTodoBinding binding;
    private AddEditTodoViewModel addEditTodoViewModel;

    private int todoId = -1;
    private int dayTemp, day, monthTemp, month, yearTemp, year, hour, minute;
    private int notificationId;
    private boolean hasNotification = false;
    private boolean hasAddedNotification = false;
    private boolean hasRemovedNotification = false;
    private long lastClickedUndoTime = 0;
    private static final int MINIMUM_TIME_BETWEEN_UNDOS_IN_MILLISECONDS = 1000;

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

        addEditTodoViewModel = ViewModelProviders.of(this).get(AddEditTodoViewModel.class);

        NumberPicker priorityPicker = binding.priorityPicker;
        priorityPicker.setMinValue(0);
        priorityPicker.setMaxValue(2);
        priorityPicker.setDisplayedValues(new String[]{getResources().getString(R.string.low_priority), getResources().getString(R.string.medium_priority), getResources().getString(R.string.high_priority)});

        AddEditTodoFragmentArgs args = AddEditTodoFragmentArgs.fromBundle(getArguments());
        todoId = args.getId();

        int todoPriority;
        String todoDescription;

        if (savedInstanceState != null) {
            todoDescription = savedInstanceState.getString(DESCRIPTION_SAVED_STATE);
            todoPriority = savedInstanceState.getInt(PRIORITY_SAVED_STATE);
            hasNotification = savedInstanceState.getBoolean(HAS_NOTIFICATION_SAVED_STATE);
            hasAddedNotification = savedInstanceState.getBoolean(HAS_ADDED_NOTIFICATION_SAVED_STATE);
            hasRemovedNotification = savedInstanceState.getBoolean(HAS_REMOVED_NOTIFICATION_SAVED_STATE);
        } else {
            todoDescription = args.getDescription();
            todoPriority = args.getPriority();
            hasNotification = args.getHasNotification();
        }

        if (todoDescription.length() == 0) {
            binding.saveTodoButton.setEnabled(false);
            binding.saveTodoButton.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
        } else {
            binding.saveTodoButton.setEnabled(true);
            binding.saveTodoButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            binding.todoDescriptionEditText.setText(todoDescription);
        }

        switch (todoPriority) {
            case 0:
                priorityPicker.setValue(0);
                break;
            case 1:
                priorityPicker.setValue(1);
                break;
            case 2:
                priorityPicker.setValue(2);
        }

        setupNotificationState(savedInstanceState);

        binding.todoDescriptionEditText.addTextChangedListener(descriptionTextWatcher);
        binding.saveTodoButton.setOnClickListener(saveButtonListener);
        binding.removeNotificationButton.setOnClickListener(removeNotificationButtonListener);
        binding.addNotificationButton.setOnClickListener(addNotificationButtonListener);
    }

    private View.OnClickListener saveButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (hasAddedNotification) {
                AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                NotificationUtil.addNotification(getActivity().getApplicationContext(), alarmManager, notificationId, binding.todoDescriptionEditText.getText().toString(), year, month, day, hour, minute);
            } else if (hasRemovedNotification) {
                AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                NotificationUtil.removeNotification(getActivity().getApplicationContext(), alarmManager, notificationId);
            }

            if (!hasNotification) {
                notificationId = 0;
            }

            Todo todo = createTodoItem();

            if (todoId == -1) {
                addEditTodoViewModel.insert(todo);
            } else {
                todo.setId(todoId);
                addEditTodoViewModel.update(todo);
            }

            NavController controller = Navigation.findNavController(getView());
            controller.navigateUp();
        }
    };

    private View.OnClickListener removeNotificationButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            displayNotificationNotAddedState();
            hasNotification = false;
            hasRemovedNotification = true;
            final boolean currentHasAddedNotification = hasAddedNotification;
            hasAddedNotification = false;

            Snackbar snackbar = Snackbar.make(
                    binding.addEditTodoCoordinatorLayout,
                    R.string.notification_removed,
                    Snackbar.LENGTH_LONG
            ).setAction(R.string.undo, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isUndoDoubleClicked()) {
                        return;
                    }
                    lastClickedUndoTime = SystemClock.elapsedRealtime();
                    // When undoing, set hasAddedNotification to what it was previously.
                    // This is because one could be undoing either a newly added or an already existing notification.
                    hasAddedNotification = currentHasAddedNotification;
                    hasRemovedNotification = false;
                    hasNotification = true;
                    displayNotificationAddedState(createNotificationCalendar());

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
            datePickerFragment.show(getFragmentManager(), "datePicker");
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
                binding.saveTodoButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private void setupNotificationState(@Nullable Bundle savedInstanceState) {
        if (hasNotification) {
            if (savedInstanceState != null) {
                year = savedInstanceState.getInt(NOTIFICATION_YEAR_SAVED_STATE);
                month = savedInstanceState.getInt(NOTIFICATION_MONTH_SAVED_STATE);
                day = savedInstanceState.getInt(NOTIFICATION_DAY_SAVED_STATE);
                hour = savedInstanceState.getInt(NOTIFICATION_HOUR_SAVED_STATE);
                minute = savedInstanceState.getInt(NOTIFICATION_MINUTE_SAVED_STATE);
                notificationId = savedInstanceState.getInt(NOTIFICATION_ID_SAVED_STATE);
                displayNotificationAddedState(createNotificationCalendar());
            } else {
                AddEditTodoFragmentArgs args = AddEditTodoFragmentArgs.fromBundle(getArguments());
                year = args.getNotificationYear();
                month = args.getNotificationMonth();
                day = args.getNotificationDay();
                hour = args.getNotificationHour();
                minute = args.getNotificationMinute();
                notificationId = args.getNotificationId();

                // Reset expired notification when first creating the activity
                Calendar notificationCalendar = createNotificationCalendar();
                Calendar currentTimeCalendar = Calendar.getInstance();

                if (notificationCalendar.getTimeInMillis() < currentTimeCalendar.getTimeInMillis()) {
                    year = 0;
                    month = 0;
                    day = 0;
                    hour = 0;
                    minute = 0;
                    hasNotification = false;
                    notificationId = new Random().nextInt();
                } else {
                    displayNotificationAddedState(notificationCalendar);
                }
            }
        } else {
            // Tasks without notification will be given a generated notification id for later use
            // If the task is saved without a notification it wont be included
            notificationId = new Random().nextInt();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PRIORITY_SAVED_STATE, binding.priorityPicker.getValue());
        outState.putString(DESCRIPTION_SAVED_STATE, binding.todoDescriptionEditText.getText().toString());
        outState.putInt(NOTIFICATION_YEAR_SAVED_STATE, year);
        outState.putInt(NOTIFICATION_MONTH_SAVED_STATE, month);
        outState.putInt(NOTIFICATION_DAY_SAVED_STATE, day);
        outState.putInt(NOTIFICATION_HOUR_SAVED_STATE, hour);
        outState.putInt(NOTIFICATION_MINUTE_SAVED_STATE, minute);
        outState.putInt(NOTIFICATION_ID_SAVED_STATE, notificationId);
        outState.putBoolean(HAS_NOTIFICATION_SAVED_STATE, hasNotification);
        outState.putBoolean(HAS_ADDED_NOTIFICATION_SAVED_STATE, hasAddedNotification);
        outState.putBoolean(HAS_REMOVED_NOTIFICATION_SAVED_STATE, hasRemovedNotification);
    }

    @Override
    public void onSelectDateDialogInteraction(int year, int month, int day) {
        this.yearTemp = year;
        this.monthTemp = month;
        this.dayTemp = day;
        TimePickerFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.setTargetFragment(AddEditTodoFragment.this, 2);
        timePickerFragment.show(getFragmentManager(), "timePicker");
    }

    @Override
    public void onSelectTimeDialogInteraction(int hour, int minute) {
        Calendar notificationCalendar = Calendar.getInstance();
        notificationCalendar.set(yearTemp, monthTemp, dayTemp, hour, minute, 0);
        Calendar currentTimeCalendar = Calendar.getInstance();
        if (notificationCalendar.getTimeInMillis() < currentTimeCalendar.getTimeInMillis()) {
            Toast.makeText(getActivity().getApplicationContext(), R.string.invalid_time, Toast.LENGTH_LONG).show();
        } else {
            hasNotification = true;
            hasAddedNotification = true;
            displayNotificationAddedState(notificationCalendar);
            this.year = yearTemp;
            this.month = monthTemp;
            this.day = dayTemp;
            this.hour = hour;
            this.minute = minute;
        }
    }

    private Todo createTodoItem() {
        return new Todo(binding.todoDescriptionEditText.getText().toString(), binding.priorityPicker.getValue(), notificationId, hasNotification, year, month, day, hour, minute);
    }

    private boolean isUndoDoubleClicked() {
        return SystemClock.elapsedRealtime() - lastClickedUndoTime < MINIMUM_TIME_BETWEEN_UNDOS_IN_MILLISECONDS;
    }

    private void displayNotificationAddedState(Calendar notificationCalendar) {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(binding.addEditTodoConstraintLayout);

        // When both buttons are showing, show each button on each side of the centered vertical guideline, moved towards the center
        constraintSet.connect(R.id.addNotificationButton, ConstraintSet.END, R.id.centered_vertical_guideline, ConstraintSet.END, (int)getResources().getDimension(R.dimen.notification_buttons_space_divided_by_2));
        constraintSet.setHorizontalBias(R.id.addNotificationButton, 1.0f);
        constraintSet.applyTo(binding.addEditTodoConstraintLayout);

        binding.notificationTextView.setText(Html.fromHtml("<b>" + getString(R.string.notification_pretext) + "</b> "
                + getString(R.string.notification_time, DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, Locale.US).format(notificationCalendar.getTime()))));
        binding.notificationTextView.setVisibility(View.VISIBLE);
        binding.removeNotificationButton.setVisibility(View.VISIBLE);
        binding.addNotificationButton.setText(R.string.update_notification);
    }

    private void displayNotificationNotAddedState() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(binding.addEditTodoConstraintLayout);

        // When only the add button is showing, center it horizontally
        constraintSet.connect(R.id.addNotificationButton, ConstraintSet.END, R.id.addEditTodoConstraintLayout, ConstraintSet.END, 0);
        constraintSet.setHorizontalBias(R.id.addNotificationButton, 0.5f);
        constraintSet.applyTo(binding.addEditTodoConstraintLayout);

        binding.addNotificationButton.setText(R.string.add_notification);
        binding.notificationTextView.setVisibility(View.GONE);
        binding.removeNotificationButton.setVisibility(View.GONE);
    }

    private Calendar createNotificationCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, 0);
        return calendar;
    }
}
