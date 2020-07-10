package com.github.fredrik9000.todolist.notifications

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.github.fredrik9000.todolist.R
import com.github.fredrik9000.todolist.model.TodoDao
import com.github.fredrik9000.todolist.model.TodoDatabase

class GeofenceNotificationJobIntentService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        val notificationId = intent.getIntExtra(GeofenceReceiver.NOTIFICATION_ID, 0)
        NotificationUtil.removeGeofenceNotification(applicationContext, notificationId)

        // TODO: Currently getting the todoitem by geofence notification id, however there is an issue with 5 max pending intents limit.
        //  When this gets solved the notificaiton id by itself wont be unique, so should use the todoitem id instead.
        val todoDao: TodoDao = TodoDatabase.getInstance(applicationContext).todoDao()

        // First clear the notification values, then get the updated item and clear the notification id
        todoDao.clearGeofenceNotificationValues(notificationId)
        val todosWithGeofenceNotificationId = todoDao.getTodoWithGeofenceNotificationId(notificationId)
        todoDao.clearNotificationId(notificationId)
        if (todosWithGeofenceNotificationId.isNotEmpty()) {
            val todo = todosWithGeofenceNotificationId[0]
            todo.geofenceNotificationId = 0 // Finally clearing the geofence notification id
            NotificationUtil.sendNotification(applicationContext, applicationContext.resources.getString(R.string.geofence_notification_title), todosWithGeofenceNotificationId[0], R.drawable.ic_geofence_location_black_24dp, notificationId)
        }
    }

    companion object {
        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, GeofenceNotificationJobIntentService::class.java, 1, work)
        }
    }
}