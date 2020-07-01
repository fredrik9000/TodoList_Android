package com.github.fredrik9000.todolist.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TimedNotificationAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val timedNotificationJobIntentServiceIntent = Intent(context, TimedNotificationJobIntentService::class.java)
        timedNotificationJobIntentServiceIntent.putExtra(NOTIFICATION_ID, intent.getIntExtra(NOTIFICATION_ID, 0))
        TimedNotificationJobIntentService.enqueueWork(context, timedNotificationJobIntentServiceIntent)
    }

    companion object {
        const val TODO_DESCRIPTION: String = "TODO_DESCRIPTION"
        const val NOTIFICATION_ID: String = "NOTIFICATION_ID"
    }
}