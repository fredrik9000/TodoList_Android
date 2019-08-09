package com.github.fredrik9000.todolist;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import com.github.fredrik9000.todolist.databinding.ActivityAddEditTodoBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

public class AddEditTodoActivity extends AppCompatActivity implements DatePickerFragment.OnSelectDateDialogInteractionListener, TimePickerFragment.OnSelectTimeDialogInteractionListener {

    public static final String TODO_DESCRIPTION = "TODO_DESCRIPTION";
    public static final String TODO_PRIORITY = "TODO_PRIORITY";
    public static final String TODO_ID = "TODO_ID";
    public static final String NOTIFICATION_YEAR = "NOTIFICATION_YEAR";
    public static final String NOTIFICATION_MONTH = "NOTIFICATION_MONTH";
    public static final String NOTIFICATION_DAY = "NOTIFICATION_DAY";
    public static final String NOTIFICATION_HOUR = "NOTIFICATION_HOUR";
    public static final String NOTIFICATION_MINUTE = "NOTIFICATION_MINUTE";
    public static final String NOTIFICATION_ID = "NOTIFICATION_ID";
    public static final String HAS_NOTIFICATION = "HAS_NOTIFICATION";
    public static final String HAS_ADDED_NOTIFICATION = "HAS_ADDED_NOTIFICATION";
    public static final String HAS_REMOVED_NOTIFICATION = "HAS_REMOVED_NOTIFICATION";

    private CoordinatorLayout coordinatorLayout;
    private TextInputEditText todoDescriptionET;
    private TextView notificationTextView;
    private Button removeNotificationButton, addNotificationButton;
    private NumberPicker priorityPicker;

    private int dayTemp, day, monthTemp, month, yearTemp, year, hour, minute;
    private int notificationId;
    private boolean hasNotification = false;
    private boolean hasAddedNotification = false;
    private boolean hasRemovedNotification = false;
    private long lastClickedUndoTime = 0;
    private static final int MINIMUM_TIME_BETWEEN_UNDOS_IN_MILLISECONDS = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityAddEditTodoBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_add_edit_todo);

        final Intent intent = getIntent();
        int todoPriority;
        final String todoDescription;

        if (savedInstanceState != null) {
            todoDescription = savedInstanceState.getString(TODO_DESCRIPTION);
            todoPriority = savedInstanceState.getInt(TODO_PRIORITY, 1);
            hasNotification = savedInstanceState.getBoolean(HAS_NOTIFICATION);
            hasAddedNotification = savedInstanceState.getBoolean(HAS_ADDED_NOTIFICATION);
            hasRemovedNotification = savedInstanceState.getBoolean(HAS_REMOVED_NOTIFICATION);
        } else {
            todoDescription = intent.getStringExtra(TODO_DESCRIPTION);
            todoPriority = intent.getIntExtra(TODO_PRIORITY, 1);
            hasNotification = intent.getBooleanExtra(HAS_NOTIFICATION, false);
        }

        final FloatingActionButton saveButton = binding.fabSaveTodo;
        todoDescriptionET = binding.todoDescriptionEditText;
        notificationTextView = binding.notificationTextView;
        removeNotificationButton = binding.removeNotificationButton;
        addNotificationButton = binding.addNotificationButton;
        priorityPicker = binding.priorityPicker;
        priorityPicker.setMinValue(0);
        priorityPicker.setMaxValue(2);
        priorityPicker.setDisplayedValues(new String[]{getResources().getString(R.string.low_priority), getResources().getString(R.string.medium_priority), getResources().getString(R.string.high_priority)});

        coordinatorLayout = binding.addTodoCoordinatorLayout;

        if (intent.hasExtra(TODO_ID)) { //Checks if we are adding or editing an item
            setTitle(R.string.title_activity_edit_todo);
        }

        if (todoDescription == null || todoDescription.length() == 0) {
            saveButton.setEnabled(false);
            saveButton.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
        } else {
            saveButton.setEnabled(true);
            saveButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            todoDescriptionET.setText(todoDescription);
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

        if (hasNotification) {
            if (savedInstanceState != null) {
                year = savedInstanceState.getInt(NOTIFICATION_YEAR, 0);
                month = savedInstanceState.getInt(NOTIFICATION_MONTH, 0);
                day = savedInstanceState.getInt(NOTIFICATION_DAY, 0);
                hour = savedInstanceState.getInt(NOTIFICATION_HOUR, 0);
                minute = savedInstanceState.getInt(NOTIFICATION_MINUTE, 0);
                notificationId = savedInstanceState.getInt(NOTIFICATION_ID, 0);
                displayNotificationAddedState(createNotificationCalendar());
            } else {
                year = intent.getIntExtra(NOTIFICATION_YEAR, 0);
                month = intent.getIntExtra(NOTIFICATION_MONTH, 0);
                day = intent.getIntExtra(NOTIFICATION_DAY, 0);
                hour = intent.getIntExtra(NOTIFICATION_HOUR, 0);
                minute = intent.getIntExtra(NOTIFICATION_MINUTE, 0);
                notificationId = intent.getIntExtra(NOTIFICATION_ID, 0);

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

        todoDescriptionET.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() == 0) {
                    saveButton.setEnabled(false);
                    saveButton.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                } else {
                    saveButton.setEnabled(true);
                    saveButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int priority = priorityPicker.getValue();
                Intent resultIntent = new Intent();
                resultIntent.putExtra(TODO_DESCRIPTION, todoDescriptionET.getText().toString());
                resultIntent.putExtra(TODO_PRIORITY, priority);

                // If we are editing an existing task we need to send the passed in id back
                if (intent.hasExtra(TODO_ID)) {
                    resultIntent.putExtra(TODO_ID, intent.getIntExtra(TODO_ID, 0));
                }

                resultIntent.putExtra(HAS_NOTIFICATION, hasNotification);
                resultIntent.putExtra(NOTIFICATION_YEAR, year);
                resultIntent.putExtra(NOTIFICATION_MONTH, month);
                resultIntent.putExtra(NOTIFICATION_DAY, day);
                resultIntent.putExtra(NOTIFICATION_HOUR, hour);
                resultIntent.putExtra(NOTIFICATION_MINUTE, minute);

                if (hasAddedNotification) {
                    addNotificationAlarm(todoDescriptionET.getText().toString());
                } else if (hasRemovedNotification) {
                    removeNotificationAlarm();
                }

                if (!hasNotification) {
                    notificationId = 0;
                }

                resultIntent.putExtra(NOTIFICATION_ID, notificationId);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        removeNotificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayNotificationNotAddedState();
                hasNotification = false;
                hasRemovedNotification = true;
                final boolean currentHasAddedNotification = hasAddedNotification;
                hasAddedNotification = false;

                Snackbar snackbar = Snackbar.make(
                        coordinatorLayout,
                        R.string.notification_removed,
                        Snackbar.LENGTH_LONG
                ).setAction(R.string.undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (isUndoDoubleClicked()){
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
                                coordinatorLayout,
                                R.string.undo_successful,
                                Snackbar.LENGTH_SHORT
                        );
                        snackbar2.show();
                    }
                });
                snackbar.show();
            }
        });

        addNotificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment datePickerFragment = new DatePickerFragment();
                datePickerFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });
    }

    private void displayNotificationAddedState(Calendar notificationCalendar) {
        notificationTextView.setText(getString(R.string.notification_time, DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, Locale.US).format(notificationCalendar.getTime())));
        notificationTextView.setVisibility(View.VISIBLE);
        removeNotificationButton.setVisibility(View.VISIBLE);
        addNotificationButton.setText(R.string.update_notification);
    }

    private void displayNotificationNotAddedState() {
        addNotificationButton.setText(R.string.add_notification);
        notificationTextView.setVisibility(View.GONE);
        removeNotificationButton.setVisibility(View.GONE);
    }

    private Calendar createNotificationCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, 0);
        return calendar;
    }

    private void removeNotificationAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent notificationIntent = new Intent(AddEditTodoActivity.this.getApplicationContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(AddEditTodoActivity.this.getApplicationContext(), notificationId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    private void addNotificationAlarm(String todoDescription) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent notificationIntent = new Intent(AddEditTodoActivity.this.getApplicationContext(), AlarmReceiver.class);
        notificationIntent.putExtra(AlarmReceiver.TODO_DESCRIPTION, todoDescription);
        notificationIntent.putExtra(AlarmReceiver.NOTIFICATION_ID, notificationId);
        PendingIntent broadcast = PendingIntent.getBroadcast(AddEditTodoActivity.this.getApplicationContext(), notificationId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar notificationCalendar = createNotificationCalendar();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationCalendar.getTimeInMillis(), broadcast);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, notificationCalendar.getTimeInMillis(), broadcast);
        }
    }

    private boolean isUndoDoubleClicked() {
        return SystemClock.elapsedRealtime() - lastClickedUndoTime < MINIMUM_TIME_BETWEEN_UNDOS_IN_MILLISECONDS;
    }

    @Override
    public void onSelectDateDialogInteraction(int year, int month, int day) {
        this.yearTemp = year;
        this.monthTemp = month;
        this.dayTemp = day;
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    @Override
    public void onSelectTimeDialogInteraction(int hour, int minute) {
        Calendar notificationCalendar = Calendar.getInstance();
        notificationCalendar.set(yearTemp, monthTemp, dayTemp, hour, minute, 0);
        Calendar currentTimeCalendar = Calendar.getInstance();
        if (notificationCalendar.getTimeInMillis() < currentTimeCalendar.getTimeInMillis()) {
            Toast.makeText(this.getApplicationContext(), R.string.invalid_time, Toast.LENGTH_LONG).show();
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(TODO_PRIORITY, priorityPicker.getValue());
        outState.putString(TODO_DESCRIPTION, todoDescriptionET.getText().toString());
        outState.putInt(NOTIFICATION_YEAR, year);
        outState.putInt(NOTIFICATION_MONTH, month);
        outState.putInt(NOTIFICATION_DAY, day);
        outState.putInt(NOTIFICATION_HOUR, hour);
        outState.putInt(NOTIFICATION_MINUTE, minute);
        outState.putInt(NOTIFICATION_ID, notificationId);
        outState.putBoolean(HAS_NOTIFICATION, hasNotification);
        outState.putBoolean(HAS_ADDED_NOTIFICATION, hasAddedNotification);
        outState.putBoolean(HAS_REMOVED_NOTIFICATION, hasRemovedNotification);
    }
}
