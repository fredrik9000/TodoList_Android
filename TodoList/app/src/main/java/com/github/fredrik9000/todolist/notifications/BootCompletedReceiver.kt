package com.github.fredrik9000.todolist.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ReRegisterGeofencesJobIntentService.enqueueWork(context, Intent(context, ReRegisterGeofencesJobIntentService::class.java))
    }
}