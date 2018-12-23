package com.github.fredrik9000.todolist;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

public class AddTODOActivity extends AppCompatActivity implements DatePickerFragment.OnSelectDateDialogInteractionListener, TimePickerFragment.OnSelectTimeDialogInteractionListener {

    public static final String CHORE_DESCRIPTION = "CHORE_DESCRIPTION";
    public static final String CHORE_PRIORITY = "CHORE_PRIORITY";
    public static final String NOTIFICATION_YEAR = "NOTIFICATION_YEAR";
    public static final String NOTIFICATION_MONTH = "NOTIFICATION_MONTH";
    public static final String NOTIFICATION_DAY = "NOTIFICATION_DAY";
    public static final String NOTIFICATION_HOUR = "NOTIFICATION_HOUR";
    public static final String NOTIFICATION_MINUTE = "NOTIFICATION_MINUTE";
    public static final String NOTIFICATION_ID = "NOTIFICATION_ID";
    public static final String HAS_NOTIFICATION = "HAS_NOTIFICATION";
    public static final String CHORE_POSITION = "CHORE_POSITION";

    private int day, month, year, hour, minute;
    private int notificationId;
    private boolean hasNotification = false;
    private Calendar notificationCalendar;

    private TextView notificationTextView;
    private Button removeNotificationButton, addNotificationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_chore);

        Intent intent = getIntent();
        String choreDescription = intent.getStringExtra(CHORE_DESCRIPTION);
        int chorePriority = intent.getIntExtra(CHORE_PRIORITY, 0);
        final int chorePosition = intent.getIntExtra(CHORE_POSITION, 0);
        hasNotification = intent.getBooleanExtra(HAS_NOTIFICATION, false);

        final FloatingActionButton saveChoreButton = findViewById(R.id.fabSaveChore);
        final EditText choreDescriptionET = findViewById(R.id.addChoreEditText);
        notificationTextView = findViewById(R.id.notificationTextView);
        removeNotificationButton = findViewById(R.id.removeNotificationButton);
        addNotificationButton = findViewById(R.id.addNotificationButton);

        if (choreDescription == null) { //description doubles as a check for task being created
            saveChoreButton.setEnabled(false);
            saveChoreButton.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
        } else {
            setTitle(R.string.title_activity_edit_chore);
            saveChoreButton.setEnabled(true);
            saveChoreButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            choreDescriptionET.setText(choreDescription);

            switch (chorePriority) {
                case 0:
                    ((RadioGroup)findViewById(R.id.priorityRadioGroup)).check(R.id.lowPriority);
                    break;
                case 1:
                    ((RadioGroup)findViewById(R.id.priorityRadioGroup)).check(R.id.mediumPriority);
                    break;
                case 2:
                    ((RadioGroup)findViewById(R.id.priorityRadioGroup)).check(R.id.highPriority);
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

                    if (!isAlarmRunning)
                    {
                        addNotificationAlarm(choreDescription);
                    }
                }
            } else {
                notificationId = new Random().nextInt();
            }
        }

        choreDescriptionET.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().trim().length()==0){
                    saveChoreButton.setEnabled(false);
                    saveChoreButton.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                } else {
                    saveChoreButton.setEnabled(true);
                    saveChoreButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        saveChoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int priorityRBID = ((RadioGroup)findViewById(R.id.priorityRadioGroup)).getCheckedRadioButtonId();
                Intent resultIntent = new Intent();
                resultIntent.putExtra(CHORE_DESCRIPTION, choreDescriptionET.getText().toString());
                int priority;
                switch (priorityRBID) {
                    case R.id.lowPriority:
                        priority = 0;
                        break;
                    case R.id.mediumPriority:
                        priority = 1;
                        break;
                    case R.id.highPriority:
                        priority = 2;
                        break;
                    default:
                        priority = -1;
                }
                resultIntent.putExtra(CHORE_PRIORITY, priority);
                resultIntent.putExtra(CHORE_POSITION, chorePosition);
                resultIntent.putExtra(HAS_NOTIFICATION, hasNotification);
                resultIntent.putExtra(NOTIFICATION_YEAR, year);
                resultIntent.putExtra(NOTIFICATION_MONTH, month);
                resultIntent.putExtra(NOTIFICATION_DAY, day);
                resultIntent.putExtra(NOTIFICATION_HOUR, hour);
                resultIntent.putExtra(NOTIFICATION_MINUTE, minute);
                resultIntent.putExtra(NOTIFICATION_ID, notificationId);
                if (hasNotification) {
                    addNotificationAlarm(choreDescriptionET.getText().toString());
                }
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        removeNotificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
                Intent intent = new Intent(AddTODOActivity.this.getApplicationContext(), MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(AddTODOActivity.this.getApplicationContext(), notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
                addNotificationButton.setText(R.string.add_notification);
                notificationTextView.setVisibility(View.GONE);
                removeNotificationButton.setVisibility(View.GONE);
                hasNotification = false;
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

    private void addNotificationAlarm(String choreDescription) {
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

        Intent notificationIntent = new Intent(AddTODOActivity.this.getApplicationContext(), AlarmReceiver.class);
        notificationIntent.putExtra(AlarmReceiver.CHORE_DESCRIPTION, choreDescription);
        PendingIntent broadcast = PendingIntent.getBroadcast(AddTODOActivity.this.getApplicationContext(), notificationId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

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
