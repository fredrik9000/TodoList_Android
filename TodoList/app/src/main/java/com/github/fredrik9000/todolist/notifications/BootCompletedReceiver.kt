package com.github.fredrik9000.todolist.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED
                || intent.action == "android.intent.action.QUICKBOOT_POWERON"
                || intent.action == "com.htc.intent.action.QUICKBOOT_POWERON") {
            ReRegisterGeofencesJobIntentService.enqueueWork(context, Intent(context, ReRegisterGeofencesJobIntentService::class.java))
        }
    }
}