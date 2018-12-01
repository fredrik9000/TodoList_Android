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

import java.util.Calendar;

public class AddChoreActivity extends AppCompatActivity implements DatePickerFragment.OnSelectDateDialogInteractionListener, TimePickerFragment.OnSelectTimeDialogInteractionListener {

    public static final String CHORE_DESCRIPTION = "CHORE_DESCRIPTION";
    public static final String CHORE_PRIORITY = "CHORE_PRIORITY";
    public static final String CHORE_POSITION = "CHORE_POSITION";

    int dayFinal, monthFinal, yearFinal, hourFinal, minuteFinal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_chore);

        final FloatingActionButton saveChoreButton = findViewById(R.id.fabSaveChore);
        saveChoreButton.setEnabled(false);
        saveChoreButton.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
        final EditText choreDescriptionET = findViewById(R.id.addChoreEditText);
        final TextView notificationTextView = findViewById(R.id.notificationTextView);
        final Button addNotificationButton = findViewById(R.id.addNotificationButton);

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
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
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

        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.set(yearFinal, monthFinal, dayFinal, hourFinal, minuteFinal, 0);

        System.out.println(calendar.toString());

        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent broadcast = PendingIntent.getBroadcast(this.getApplicationContext(), (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), broadcast);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), broadcast);
        }
    }
}
