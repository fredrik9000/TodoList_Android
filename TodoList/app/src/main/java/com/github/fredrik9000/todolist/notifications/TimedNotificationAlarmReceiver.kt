package com.github.fredrik9000.todolist.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TimedNotificationAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        with(Intent(context, TimedNotificationJobIntentService::class.java)) {
            this.putExtra(NOTIFICATION_ID, intent.getIntExtra(NOTIFICATION_ID, 0))
            TimedNotificationJobIntentService.enqueueWork(context, this)
        }
    }

    companion object {
        const val NOTIFICATION_ID = "NOTIFICATION_ID"
    }
}