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

public class AddChoreActivity extends AppCompatActivity implements DatePickerFragment.OnSelectDateDialogInteractionListener, TimePickerFragment.OnSelectTimeDialogInteractionListener {

    public static final String CHORE_DESCRIPTION = "CHORE_DESCRIPTION";
    public static final String CHORE_PRIORITY = "CHORE_PRIORITY";
    public static final String CHORE_POSITION = "CHORE_POSITION";
    public static final String CHORE_NOTIFICATION_ID = "CHORE_NOTIFICATION_ID";
    public static final String NOTIFICATION_YEAR = "NOTIFICATION_YEAR";
    public static final String NOTIFICATION_MONTH = "NOTIFICATION_MONTH";
    public static final String NOTIFICATION_DAY = "NOTIFICATION_DAY";
    public static final String NOTIFICATION_HOUR = "NOTIFICATION_HOUR";
    public static final String NOTIFICATION_MINUTE = "NOTIFICATION_MINUTE";
    public static final String HAS_NOTIFICATION = "HAS_NOTIFICATION";

    int dayFinal, monthFinal, yearFinal, hourFinal, minuteFinal;
    int notificationId;
    boolean hasNotification = false;

    TextView notificationTextView;
    Button removeNotificationButton, addNotificationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_chore);

        final FloatingActionButton saveChoreButton = findViewById(R.id.fabSaveChore);
        saveChoreButton.setEnabled(false);
        saveChoreButton.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
        final EditText choreDescriptionET = findViewById(R.id.addChoreEditText);
        notificationTextView = findViewById(R.id.notificationTextView);
        removeNotificationButton = findViewById(R.id.removeNotificationButton);
        addNotificationButton = findViewById(R.id.addNotificationButton);

        notificationId = new Random().nextInt();

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
                resultIntent.putExtra(CHORE_NOTIFICATION_ID, notificationId);
                resultIntent.putExtra(HAS_NOTIFICATION, hasNotification);
                if (hasNotification) {
                    resultIntent.putExtra(NOTIFICATION_YEAR, yearFinal);
                    resultIntent.putExtra(NOTIFICATION_MONTH, monthFinal);
                    resultIntent.putExtra(NOTIFICATION_DAY, dayFinal);
                    resultIntent.putExtra(NOTIFICATION_HOUR, hourFinal);
                    resultIntent.putExtra(NOTIFICATION_MINUTE, minuteFinal);
                }
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        removeNotificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
                Intent intent = new Intent(AddChoreActivity.this, AlarmReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(AddChoreActivity.this.getApplicationContext(), notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.cancel(pendingIntent);
                addNotificationButton.setText("add notification");
                notificationTextView.setVisibility(View.GONE);
                removeNotificationButton.setVisibility(View.GONE);
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

    @Override
    public void onSelectDateDialogInteraction(int year, int month, int day) {
        yearFinal = year;
        monthFinal = month;
        dayFinal = day;
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    @Override
    public void onSelectTimeDialogInteraction(int hour, int minute) {
        hourFinal = hour;
        minuteFinal = minute;

        Calendar notificationCalendar = Calendar.getInstance();
        notificationCalendar.set(yearFinal, monthFinal, dayFinal, hourFinal, minuteFinal, 0);
        Calendar currentTimeCalendar = Calendar.getInstance();
        if (notificationCalendar.getTimeInMillis() < currentTimeCalendar.getTimeInMillis()) {
            Toast.makeText(this.getApplicationContext(), "Invalid Time, select a future date and time", Toast.LENGTH_LONG).show();
        } else {
            AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);;

            Intent intent = new Intent(this, AlarmReceiver.class);
            PendingIntent broadcast = PendingIntent.getBroadcast(this.getApplicationContext(), notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationCalendar.getTimeInMillis(), broadcast);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, notificationCalendar.getTimeInMillis(), broadcast);
            }

            notificationTextView.setText("notify: " + DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, Locale.US).format(notificationCalendar.getTime()));
            notificationTextView.setVisibility(View.VISIBLE);
            removeNotificationButton.setVisibility(View.VISIBLE);
            addNotificationButton.setText("update notification");
        }
    }
}
