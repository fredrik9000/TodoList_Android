package com.github.fredrik9000.todolist.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.github.fredrik9000.todolist.App;
import com.github.fredrik9000.todolist.MainActivity;
import com.github.fredrik9000.todolist.R;
import com.github.fredrik9000.todolist.model.TodoDao;
import com.github.fredrik9000.todolist.model.TodoDatabase;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String TODO_DESCRIPTION = "TODO_DESCRIPTION";
    public static final String NOTIFICATION_ID = "NOTIFICATION_ID";

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent intMain = new Intent(context, MainActivity.class);
        intMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 4754, intMain, PendingIntent.FLAG_ONE_SHOT);

        Notification notification = new NotificationCompat.Builder(context, App.NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Task reminder")
                .setContentText(intent.getStringExtra(TODO_DESCRIPTION))
                .setSmallIcon(R.drawable.ic_notifications_active_black_24dp)
                .setAutoCancel(true)
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setLights(Color.RED, 3000, 3000)
                .setContentIntent(pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .build();

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);

        int notificationId = intent.getIntExtra(NOTIFICATION_ID, 0);
        notificationManagerCompat.notify(notificationId, notification);

        TodoDatabase database = TodoDatabase.getInstance(context.getApplicationContext());
        new DisableNotificationWithIdAsyncTask(database.todoDao()).execute(notificationId);
    }

    private static class DisableNotificationWithIdAsyncTask extends AsyncTask<Integer, Void, Void> {

        private TodoDao todoDao;

        private DisableNotificationWithIdAsyncTask(TodoDao todoDao) {
            this.todoDao = todoDao;
        }

        @Override
        protected Void doInBackground(Integer... notificationIds) {
            todoDao.disableNotificationWithId(notificationIds[0]);
            return null;
        }
    }
}
