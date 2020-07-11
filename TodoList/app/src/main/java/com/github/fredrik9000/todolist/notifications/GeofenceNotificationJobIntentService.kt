package com.github.fredrik9000.todolist.notifications

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.github.fredrik9000.todolist.R
import com.github.fredrik9000.todolist.model.TodoDao
import com.github.fredrik9000.todolist.model.TodoDatabase

class GeofenceNotificationJobIntentService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        val notificationIdArrayList = intent.getIntegerArrayListExtra(GeofenceReceiver.NOTIFICATION_ID)!!

        // Remove all triggered geofences and clear notification values for these tasks from the database(except the notification id as we still need this)
        NotificationUtil.removeGeofenceList(applicationContext, ArrayList(notificationIdArrayList.map { it.toString() }))
        val todoDao: TodoDao = TodoDatabase.getInstance(applicationContext).todoDao()
        todoDao.clearGeofenceNotificationValues(notificationIdArrayList)

        // Loop through each task and send a notification for each item
        val todoListForTriggeredGeofences = todoDao.getTodoListWithGeofenceNotificationIds(notificationIdArrayList)
        for (todo in todoListForTriggeredGeofences) {
            NotificationUtil.sendNotification(applicationContext, applicationContext.resources.getString(R.string.geofence_notification_title), todo, R.drawable.ic_geofence_location_black_24dp, todo.geofenceNotificationId)
        }

        // Clear the notification ids
        todoDao.clearGeofenceNotificationIds(notificationIdArrayList)
    }

    companion object {
        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, GeofenceNotificationJobIntentService::class.java, 1, work)
        }
    }
}