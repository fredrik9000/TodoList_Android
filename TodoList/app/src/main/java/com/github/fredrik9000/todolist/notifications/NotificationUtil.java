package com.github.fredrik9000.todolist.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class NotificationUtil {
    public static void removeNotification(Context applicationContext, AlarmManager alarmManager, int notificationId) {
        Intent notificationIntent = new Intent(applicationContext, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(applicationContext, notificationId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    public static void addNotification(Context applicationContext, AlarmManager alarmManager, int notificationId, String description, int year, int month, int day, int hour, int minute) {
        Intent notificationIntent = new Intent(applicationContext, AlarmReceiver.class);
        notificationIntent.putExtra(AlarmReceiver.TODO_DESCRIPTION, description);
        notificationIntent.putExtra(AlarmReceiver.NOTIFICATION_ID, notificationId);
        PendingIntent broadcast = PendingIntent.getBroadcast(applicationContext, notificationId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar notificationCalendar = Calendar.getInstance();
        notificationCalendar.set(year, month, day, hour, minute, 0);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationCalendar.getTimeInMillis(), broadcast);
    }
}
