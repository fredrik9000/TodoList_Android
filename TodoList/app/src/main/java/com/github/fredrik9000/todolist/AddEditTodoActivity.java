package com.github.fredrik9000.todolist;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
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
    private CoordinatorLayout coordinatorLayout;
    private int day, month, year, hour, minute;
    private int notificationId;
    private boolean hasNotification = false;
    private Calendar notificationCalendar;

    private TextView notificationTextView;
    private Button removeNotificationButton, addNotificationButton;
    private NumberPicker priorityPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityAddEditTodoBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_add_edit_todo);

        final Intent intent = getIntent();
        final String todoDescription = intent.getStringExtra(TODO_DESCRIPTION);
        int todoPriority = intent.getIntExtra(TODO_PRIORITY, 0);
        hasNotification = intent.getBooleanExtra(HAS_NOTIFICATION, false);

        final FloatingActionButton saveButton = binding.fabSaveTodo;
        final TextInputEditText todoDescriptionET = binding.addTodoEditText;
        notificationTextView = binding.notificationTextView;
        removeNotificationButton = binding.removeNotificationButton;
        addNotificationButton = binding.addNotificationButton;
        priorityPicker = binding.priorityPicker;
        priorityPicker.setMinValue(0);
        priorityPicker.setMaxValue(2);
        priorityPicker.setDisplayedValues(new String[]{getResources().getString(R.string.low_priority), getResources().getString(R.string.medium_priority), getResources().getString(R.string.high_priority)});

        coordinatorLayout = binding.addTodoCoordinatorLayout;

        if (!intent.hasExtra(TODO_ID)) { //Checks if we are adding or editing an item
            saveButton.setEnabled(false);
            saveButton.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
            priorityPicker.setValue(1);
        } else {
            setTitle(R.string.title_activity_edit_todo);
            saveButton.setEnabled(true);
            saveButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            todoDescriptionET.setText(todoDescription);

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
                year = intent.getIntExtra(NOTIFICATION_YEAR, 0);
                month = intent.getIntExtra(NOTIFICATION_MONTH, 0);
                day = intent.getIntExtra(NOTIFICATION_DAY, 0);
                hour = intent.getIntExtra(NOTIFICATION_HOUR, 0);
                minute = intent.getIntExtra(NOTIFICATION_MINUTE, 0);
                notificationId = intent.getIntExtra(NOTIFICATION_ID, 0);

                notificationCalendar = Calendar.getInstance();
                notificationCalendar.set(year, month, day, hour, minute, 0);
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
                    hasNotification = true;
                    notificationTextView.setText(getString(R.string.notification_time, DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, Locale.US).format(notificationCalendar.getTime())));
                    notificationTextView.setVisibility(View.VISIBLE);
                    removeNotificationButton.setVisibility(View.VISIBLE);
                    addNotificationButton.setText(R.string.update_notification);

                    boolean isAlarmRunning = (PendingIntent.getBroadcast(this.getApplicationContext(), notificationId,
                            new Intent("com.my.package.MY_UNIQUE_ACTION"),
                            PendingIntent.FLAG_NO_CREATE) != null);

                    if (!isAlarmRunning) {
                        addNotificationAlarm(todoDescription);
                    }
                }
            } else {
                notificationId = new Random().nextInt();
            }
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
                if (intent.hasExtra(TODO_ID)) { //If we are editing an item we need to send the passed in id back
                    resultIntent.putExtra(TODO_ID, intent.getIntExtra(TODO_ID, 0));
                }
                resultIntent.putExtra(HAS_NOTIFICATION, hasNotification);
                resultIntent.putExtra(NOTIFICATION_YEAR, year);
                resultIntent.putExtra(NOTIFICATION_MONTH, month);
                resultIntent.putExtra(NOTIFICATION_DAY, day);
                resultIntent.putExtra(NOTIFICATION_HOUR, hour);
                resultIntent.putExtra(NOTIFICATION_MINUTE, minute);
                if (hasNotification) {
                    addNotificationAlarm(todoDescriptionET.getText().toString());
                } else {
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
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                Intent intent = new Intent(AddEditTodoActivity.this.getApplicationContext(), MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(AddEditTodoActivity.this.getApplicationContext(), notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
                addNotificationButton.setText(R.string.add_notification);
                notificationTextView.setVisibility(View.GONE);
                removeNotificationButton.setVisibility(View.GONE);
                hasNotification = false;

                Snackbar snackbar = Snackbar.make(
                        coordinatorLayout,
                        R.string.notification_removed,
                        Snackbar.LENGTH_LONG
                ).setAction(R.string.undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        addNotificationAlarm(todoDescription);

                        notificationCalendar = Calendar.getInstance();
                        notificationCalendar.set(year, month, day, hour, minute, 0);
                        Calendar currentTimeCalendar = Calendar.getInstance();
                        if (notificationCalendar.getTimeInMillis() < currentTimeCalendar.getTimeInMillis()) {
                            Toast.makeText(AddEditTodoActivity.this, R.string.invalid_time, Toast.LENGTH_LONG).show();
                            hasNotification = false;
                        } else {
                            hasNotification = true;
                            notificationTextView.setText(getString(R.string.notification_time, DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, Locale.US).format(notificationCalendar.getTime())));
                            notificationTextView.setVisibility(View.VISIBLE);
                            removeNotificationButton.setVisibility(View.VISIBLE);
                            addNotificationButton.setText(R.string.update_notification);
                        }
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

    private void addNotificationAlarm(String todoDescription) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Intent notificationIntent = new Intent(AddEditTodoActivity.this.getApplicationContext(), AlarmReceiver.class);
        notificationIntent.putExtra(AlarmReceiver.TODO_DESCRIPTION, todoDescription);
        PendingIntent broadcast = PendingIntent.getBroadcast(AddEditTodoActivity.this.getApplicationContext(), notificationId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationCalendar.getTimeInMillis(), broadcast);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, notificationCalendar.getTimeInMillis(), broadcast);
        }
    }

    @Override
    public void onSelectDateDialogInteraction(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    @Override
    public void onSelectTimeDialogInteraction(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;

        notificationCalendar = Calendar.getInstance();
        notificationCalendar.set(year, month, day, hour, minute, 0);
        Calendar currentTimeCalendar = Calendar.getInstance();
        if (notificationCalendar.getTimeInMillis() < currentTimeCalendar.getTimeInMillis()) {
            Toast.makeText(this.getApplicationContext(), R.string.invalid_time, Toast.LENGTH_LONG).show();
            hasNotification = false;
        } else {
            hasNotification = true;
            notificationTextView.setText(getString(R.string.notification_time, DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, Locale.US).format(notificationCalendar.getTime())));
            notificationTextView.setVisibility(View.VISIBLE);
            removeNotificationButton.setVisibility(View.VISIBLE);
            addNotificationButton.setText(R.string.update_notification);
        }
    }
}
