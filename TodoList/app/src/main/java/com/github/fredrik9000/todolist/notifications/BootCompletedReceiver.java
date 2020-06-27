package com.github.fredrik9000.todolist.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ReRegisterGeofencesJobIntentService.enqueueWork(context, new Intent(context, ReRegisterGeofencesJobIntentService.class));
    }
}
