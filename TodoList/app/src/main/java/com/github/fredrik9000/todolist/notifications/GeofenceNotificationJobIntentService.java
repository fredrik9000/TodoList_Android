package com.github.fredrik9000.todolist.notifications;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.github.fredrik9000.todolist.R;
import com.github.fredrik9000.todolist.model.Todo;
import com.github.fredrik9000.todolist.model.TodoDao;
import com.github.fredrik9000.todolist.model.TodoDatabase;

import java.util.List;

public class GeofenceNotificationJobIntentService extends JobIntentService {

    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, GeofenceNotificationJobIntentService.class, 1, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        int notificationId = intent.getIntExtra(GeofenceReceiver.NOTIFICATION_ID, 0);
        NotificationUtil.removeGeofenceNotification(getApplicationContext(), notificationId);

        // TODO: Currently getting the todoitem by geofence notification id, however there is an issue with 5 max pending intents limit.
        //  When this gets solved the notificaiton id by itself wont be unique, so should use the todoitem id instead.
        TodoDao todoDao = TodoDatabase.getInstance(getApplicationContext()).todoDao();

        // First clear the notification values, then get the updated item and clear the notification id
        todoDao.clearGeofenceNotificationValues(notificationId);
        List<Todo> todosWithGeofenceNotificationId = todoDao.getTodoWithGeofenceNotificationId(notificationId);
        todoDao.clearNotificationId(notificationId);
        if (todosWithGeofenceNotificationId != null && todosWithGeofenceNotificationId.size() > 0) {
            Todo todo = todosWithGeofenceNotificationId.get(0);
            todo.setGeofenceNotificationId(0); // Finally clearing the geofence notification id
            NotificationUtil.sendNotification(getApplicationContext(), getApplicationContext().getResources().getString(R.string.geofence_notification_title), todosWithGeofenceNotificationId.get(0), R.drawable.ic_baseline_location_searching_black_24dp, notificationId);
        }
    }
}