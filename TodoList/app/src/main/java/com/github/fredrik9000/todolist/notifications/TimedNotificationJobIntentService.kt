package com.github.fredrik9000.todolist.notifications

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import com.github.fredrik9000.todolist.R
import com.github.fredrik9000.todolist.model.TodoDao
import com.github.fredrik9000.todolist.model.TodoDatabase

class TimedNotificationJobIntentService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        val notificationId = intent.getIntExtra(TimedNotificationAlarmReceiver.NOTIFICATION_ID, 0)
        val todoDao: TodoDao = TodoDatabase.getInstance(applicationContext).todoDao()

        // First clear the notification values, then get the updated item and clear the notification id
        todoDao.clearTimedNotificationValues(notificationId)
        val todosWithNotificationId = todoDao.getTodoWithNotificationId(notificationId)
        todoDao.clearNotificationId(notificationId)
        if (todosWithNotificationId.isNotEmpty()) {
            val todo = todosWithNotificationId[0]
            todo.notificationId = 0 // Finally clearing the notification id
            NotificationUtil.sendNotification(
                context = applicationContext,
                title = applicationContext.resources.getString(R.string.timed_notification_title),
                todo = todosWithNotificationId[0],
                icon = R.drawable.ic_notifications_active_black_24dp,
                notificationId = notificationId
            )
        }
    }

    companion object {
        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, TimedNotificationJobIntentService::class.java, 1, work)
        }
    }
}