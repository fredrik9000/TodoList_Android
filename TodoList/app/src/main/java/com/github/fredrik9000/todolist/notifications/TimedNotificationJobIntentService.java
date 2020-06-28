package com.github.fredrik9000.todolist.notifications;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.github.fredrik9000.todolist.R;
import com.github.fredrik9000.todolist.model.Todo;
import com.github.fredrik9000.todolist.model.TodoDao;
import com.github.fredrik9000.todolist.model.TodoDatabase;

import java.util.List;

public class TimedNotificationJobIntentService extends JobIntentService {

    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, TimedNotificationJobIntentService.class, 1, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        int notificationId = intent.getIntExtra(TimedNotificationAlarmReceiver.NOTIFICATION_ID, 0);
        TodoDao todoDao = TodoDatabase.getInstance(getApplicationContext()).todoDao();

        // First clear the notification values, then get the updated item and clear the notification id
        todoDao.clearTimedNotificationValues(notificationId);
        List<Todo> todosWithNotificationId = todoDao.getTodoWithNotificationId(notificationId);
        todoDao.clearNotificationId(notificationId);

        if (todosWithNotificationId != null && todosWithNotificationId.size() > 0) {
            Todo todo = todosWithNotificationId.get(0);
            todo.setNotificationId(0); // Finally clearing the notification id
            NotificationUtil.sendNotification(getApplicationContext(), getApplicationContext().getResources().getString(R.string.timed_notification_title), todosWithNotificationId.get(0), R.drawable.ic_notifications_active_black_24dp, notificationId);
        }
    }
}
