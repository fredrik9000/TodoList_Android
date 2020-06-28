package com.github.fredrik9000.todolist.notifications;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.navigation.NavDeepLinkBuilder;

import com.github.fredrik9000.todolist.App;
import com.github.fredrik9000.todolist.R;
import com.github.fredrik9000.todolist.add_edit_todo.AddEditTodoFragment;
import com.github.fredrik9000.todolist.model.Todo;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class NotificationUtil {

    public static final String TAG = "NotificationUtil";

    public static void removeNotification(Context applicationContext, AlarmManager alarmManager, int notificationId) {
        Intent notificationIntent = new Intent(applicationContext, TimedNotificationAlarmReceiver.class);
        PendingIntent pendingIntent = getNotificationPendingIntent(applicationContext, notificationId, notificationIntent);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    public static void addNotification(Context applicationContext, AlarmManager alarmManager, int notificationId, String description, int year, int month, int day, int hour, int minute) {
        Intent notificationIntent = new Intent(applicationContext, TimedNotificationAlarmReceiver.class);
        notificationIntent.putExtra(TimedNotificationAlarmReceiver.TODO_DESCRIPTION, description);
        notificationIntent.putExtra(TimedNotificationAlarmReceiver.NOTIFICATION_ID, notificationId);
        PendingIntent pendingIntent = getNotificationPendingIntent(applicationContext, notificationId, notificationIntent);

        Calendar notificationCalendar = Calendar.getInstance();
        notificationCalendar.set(year, month, day, hour, minute, 0);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationCalendar.getTimeInMillis(), pendingIntent);
    }

    public static void removeGeofenceNotification(final Context applicationContext, int geofenceNotificationId) {
        GeofencingClient geofencingClient = LocationServices.getGeofencingClient(applicationContext);

        geofencingClient.removeGeofences(getNotificationPendingIntent(applicationContext, geofenceNotificationId, new Intent(applicationContext, GeofenceReceiver.class)))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.e(TAG, applicationContext.getResources().getString(R.string.geofence_removed));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, applicationContext.getResources().getString(R.string.geofence_removal_failed));
                    }
                });
    }

    @SuppressLint("MissingPermission")
    public static void addGeofenceNotification(final Context applicationContext, int notificationId, String description, int radius, double latitude, double longitude) {
        Intent notificationIntent = new Intent(applicationContext, GeofenceReceiver.class);
        notificationIntent.putExtra(GeofenceReceiver.TODO_DESCRIPTION, description);
        notificationIntent.putExtra(GeofenceReceiver.NOTIFICATION_ID, notificationId);

        PendingIntent geofencePendingIntent = getNotificationPendingIntent(applicationContext, notificationId, notificationIntent);

        List<Geofence> geofenceList = new ArrayList<>();
        float radiusFloat = (float) radius;
        geofenceList.add(new Geofence.Builder()
                .setRequestId(Integer.toString(notificationId))
                .setCircularRegion(latitude, longitude, radiusFloat)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build());

        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(0) // Don't trigger geofence if already inside it
                .addGeofences(geofenceList)
                .build();

        // TODO: Android restricts to 5 pending intents for geofences, need to handle this
        GeofencingClient geofencingClient = LocationServices.getGeofencingClient(applicationContext);
        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.e(TAG, applicationContext.getResources().getString(R.string.geofence_added));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, applicationContext.getResources().getString(R.string.adding_geofence_failed));
                    }
                });
    }

    private static PendingIntent getNotificationPendingIntent(Context applicationContext, int notificationId, Intent notificationIntent) {
        // Using FLAG_UPDATE_CURRENT so that we get the same pending intent back when adding and removing
        return PendingIntent.getBroadcast(applicationContext, notificationId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void sendNotification(Context context, String title, Todo todo, int icon, int notificationId) {
        PendingIntent pendingIntent = new NavDeepLinkBuilder(context)
                .setGraph(R.navigation.navigation_graph)
                .setDestination(R.id.addEditTodoFragment)
                .setArguments(AddEditTodoFragment.createBundleForTodoItem(todo))
                .createPendingIntent();

        Notification notification = new NotificationCompat.Builder(context, App.NOTIFICATION_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(todo.getDescription())
                .setSmallIcon(icon)
                .setAutoCancel(true)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setLights(Color.RED, 3000, 3000)
                .setContentIntent(pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .build();

        NotificationManagerCompat.from(context).notify(notificationId, notification);
    }
}
