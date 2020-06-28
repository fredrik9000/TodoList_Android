package com.github.fredrik9000.todolist.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TimedNotificationAlarmReceiver extends BroadcastReceiver {

    public static final String TODO_DESCRIPTION = "TODO_DESCRIPTION";
    public static final String NOTIFICATION_ID = "NOTIFICATION_ID";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent timedNotificationJobIntentServiceIntent = new Intent(context, TimedNotificationJobIntentService.class);
        timedNotificationJobIntentServiceIntent.putExtra(NOTIFICATION_ID, intent.getIntExtra(NOTIFICATION_ID, 0));
        TimedNotificationJobIntentService.enqueueWork(context, timedNotificationJobIntentServiceIntent);
    }
}
