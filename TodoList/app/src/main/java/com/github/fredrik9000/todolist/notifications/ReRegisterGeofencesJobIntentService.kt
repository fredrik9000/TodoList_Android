package com.github.fredrik9000.todolist.notifications

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.github.fredrik9000.todolist.model.TodoDatabase

class ReRegisterGeofencesJobIntentService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        val todoItemsWithGeofencing = TodoDatabase.getInstance(applicationContext).todoDao().getTodosWithGeofence()

        for (todo in todoItemsWithGeofencing) {
            NotificationUtil.addGeofenceNotification(
                applicationContext = applicationContext,
                notificationId = todo.geofenceNotificationId,
                radius = todo.geofenceRadius,
                latitude = todo.geofenceLatitude,
                longitude = todo.geofenceLongitude
            )
        }
    }

    companion object {
        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, ReRegisterGeofencesJobIntentService::class.java, 1, work)
        }
    }
}