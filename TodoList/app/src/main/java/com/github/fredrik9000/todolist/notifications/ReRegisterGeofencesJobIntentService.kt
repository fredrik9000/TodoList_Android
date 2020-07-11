package com.github.fredrik9000.todolist.notifications

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.github.fredrik9000.todolist.model.Todo
import com.github.fredrik9000.todolist.model.TodoDatabase

class ReRegisterGeofencesJobIntentService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        val todoItemsWithGeofencing: MutableList<Todo> = TodoDatabase.getInstance(applicationContext).todoDao().getTodosWithGeofence()
        for (todo in todoItemsWithGeofencing) {
            NotificationUtil.addGeofenceNotification(
                    applicationContext,
                    todo.geofenceNotificationId,
                    todo.geofenceRadius,
                    todo.geofenceLatitude,
                    todo.geofenceLongitude)
        }
    }

    companion object {
        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, ReRegisterGeofencesJobIntentService::class.java, 1, work)
        }
    }
}