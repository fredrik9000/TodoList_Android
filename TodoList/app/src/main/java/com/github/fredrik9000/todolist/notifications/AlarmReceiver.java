package com.github.fredrik9000.todolist.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.github.fredrik9000.todolist.MainActivity;
import com.github.fredrik9000.todolist.R;
import com.github.fredrik9000.todolist.model.TodoDao;
import com.github.fredrik9000.todolist.model.TodoDatabase;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String TODO_DESCRIPTION = "TODO_DESCRIPTION";
    public static final String NOTIFICATION_ID = "NOTIFICATION_ID";
    private static final String NOTIFICATION_CHANNEL_ID = "main_channel";

    @Override
    public void onReceive(Context context, Intent intent) {

        String todoDescription = intent.getStringExtra(TODO_DESCRIPTION);
        Intent intMain = new Intent(context, MainActivity.class);
        intMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 4754, intMain, PendingIntent.FLAG_ONE_SHOT);

        Notification notification;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            notification = builder.setContentTitle("Task reminder")
                    .setContentText(todoDescription)
                    .setAutoCancel(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .build();
        } else {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);

            notification = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle("Task reminder")
                    .setContentText(todoDescription)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true)
                    .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    .setContentIntent(pendingIntent)
                    .build();

        }

        notificationManager.notify(0, notification);
        TodoDatabase database = TodoDatabase.getInstance(context.getApplicationContext());
        new UpdateTodoAsyncTaskAndDisableNotificationWithId(database.todoDao()).execute(intent.getIntExtra(NOTIFICATION_ID, 0));
    }

    private static class UpdateTodoAsyncTaskAndDisableNotificationWithId extends AsyncTask<Integer, Void, Void> {

        private TodoDao todoDao;

        private UpdateTodoAsyncTaskAndDisableNotificationWithId(TodoDao todoDao) {
            this.todoDao = todoDao;
        }

        @Override
        protected Void doInBackground(Integer... notificationIds) {
            todoDao.disableNotificationWithId(notificationIds[0]);
            return null;
        }
    }
}
