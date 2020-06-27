package com.github.fredrik9000.todolist.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.github.fredrik9000.todolist.R;
import com.github.fredrik9000.todolist.model.TodoDao;
import com.github.fredrik9000.todolist.model.TodoDatabase;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String TODO_DESCRIPTION = "TODO_DESCRIPTION";
    public static final String NOTIFICATION_ID = "NOTIFICATION_ID";

    @Override
    public void onReceive(Context context, Intent intent) {
        int notificationId = intent.getIntExtra(NOTIFICATION_ID, 0);
        NotificationUtil.sendNotification(context, context.getResources().getString(R.string.timed_notification_title), intent.getStringExtra(TODO_DESCRIPTION), R.drawable.ic_notifications_active_black_24dp, notificationId);
        new DisableNotificationWithIdAsyncTask(TodoDatabase.getInstance(context.getApplicationContext()).todoDao()).execute(notificationId);
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
