package com.github.fredrik9000.todolist.notifications;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.github.fredrik9000.todolist.model.Todo;
import com.github.fredrik9000.todolist.model.TodoDatabase;

import java.util.List;

public class ReRegisterGeofencesJobIntentService extends JobIntentService {

    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, ReRegisterGeofencesJobIntentService.class, 1, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        List<Todo> todoItemsWithGeofencing = TodoDatabase.getInstance(getApplicationContext()).todoDao().getTodosWithGeofence();

        for (Todo todo : todoItemsWithGeofencing) {
            NotificationUtil.addGeofenceNotification(
                    getApplicationContext(),
                    todo.getGeofenceNotificationId(),
                    todo.getDescription(),
                    todo.getGeofenceRadius(),
                    todo.getGeofenceLatitude(),
                    todo.getGetGeofenceLongitude());
        }
    }
}
