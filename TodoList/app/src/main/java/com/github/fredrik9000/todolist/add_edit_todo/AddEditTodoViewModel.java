package com.github.fredrik9000.todolist.add_edit_todo;

import android.app.AlarmManager;
import android.app.Application;
import android.os.Bundle;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.github.fredrik9000.todolist.R;
import com.github.fredrik9000.todolist.model.Todo;
import com.github.fredrik9000.todolist.model.TodoRepository;
import com.github.fredrik9000.todolist.notifications.NotificationUtil;

import java.util.Calendar;
import java.util.Random;

public class AddEditTodoViewModel extends AndroidViewModel {
    private Application application;
    private TodoRepository repository;
    private long lastClickedUndoTime = 0;
    private static final int MINIMUM_TIME_BETWEEN_UNDOS_IN_MILLISECONDS = 1000;
    int todoId = -1;
    int dayTemp, day, monthTemp, month, yearTemp, year, hour, minute;
    int notificationId;
    boolean hasNotification = false;
    int priority = 1; // Default to medium priority (0=low, 1=medium, 2=high)

    // notificationUpdateState keeps track of whether the user has added a new, or removed an existing notification.
    // Notifications will only be scheduled when the task is saved.
    NotificationUpdateState notificationUpdateState = NotificationUpdateState.NOT_UPDATED;

    public AddEditTodoViewModel(@NonNull Application application) {
        super(application);
        repository = new TodoRepository(application);
        this.application = application;
    }

    boolean isUndoDoubleClicked() {
        return SystemClock.elapsedRealtime() - lastClickedUndoTime < MINIMUM_TIME_BETWEEN_UNDOS_IN_MILLISECONDS;
    }

    void updateLastClickedUndoTime() {
        lastClickedUndoTime = SystemClock.elapsedRealtime();
    }

    void setNotificationValuesFromBundle(Bundle bundle) {
        year = bundle.getInt(AddEditTodoFragment.NOTIFICATION_YEAR_SAVED_STATE);
        month = bundle.getInt(AddEditTodoFragment.NOTIFICATION_MONTH_SAVED_STATE);
        day = bundle.getInt(AddEditTodoFragment.NOTIFICATION_DAY_SAVED_STATE);
        hour = bundle.getInt(AddEditTodoFragment.NOTIFICATION_HOUR_SAVED_STATE);
        minute = bundle.getInt(AddEditTodoFragment.NOTIFICATION_MINUTE_SAVED_STATE);
        notificationId = bundle.getInt(AddEditTodoFragment.NOTIFICATION_ID_SAVED_STATE);
    }

    void setNotificationValuesFromArguments(AddEditTodoFragmentArgs args) {
        year = args.getNotificationYear();
        month = args.getNotificationMonth();
        day = args.getNotificationDay();
        hour = args.getNotificationHour();
        minute = args.getNotificationMinute();
        notificationId = args.getNotificationId();
    }

    void clearNotificationValues() {
        year = 0;
        month = 0;
        day = 0;
        hour = 0;
        minute = 0;
        hasNotification = false;
    }

    void setTemporaryNotificationDateValues(int year, int month, int day) {
        this.yearTemp = year;
        this.monthTemp = month;
        this.dayTemp = day;
    }

    void setFinallySelectedNotificationValues(int hour, int minute) {
        this.year = yearTemp;
        this.month = monthTemp;
        this.day = dayTemp;
        this.hour = hour;
        this.minute = minute;

        hasNotification = true;
        notificationUpdateState = NotificationUpdateState.ADDED_NOTIFICATION;
    }

    // This is not optimal as the notification id might not be unique. TODO: fix unique notification id retrieval.
    void generateNewNotificationId() {
        notificationId = new Random().nextInt();
    }

    Calendar createNotificationCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, 0);
        return calendar;
    }

    boolean isNotificationExpired() {
        Calendar notificationCalendar = createNotificationCalendar();
        Calendar currentTimeCalendar = Calendar.getInstance();
        return notificationCalendar.getTimeInMillis() < currentTimeCalendar.getTimeInMillis();
    }

    public String getLabelForCurrentPriority() {
        switch (priority) {
            case 0:
                return application.getApplicationContext().getString(R.string.low_priority);
            case 2:
                return application.getApplicationContext().getString(R.string.high_priority);
            case 1:
            default:
                return application.getApplicationContext().getString(R.string.medium_priority);
        }
    }

    public int getColorForCurrentPriority() {
        switch (priority) {
            case 0:
                return application.getApplicationContext().getResources().getColor(R.color.low_priority);
            case 2:
                return application.getApplicationContext().getResources().getColor(R.color.high_priority);
            case 1:
            default:
                return application.getApplicationContext().getResources().getColor(R.color.medium_priority);
        }
    }

    public int convertPriorityLabelToValue(String todoPriority) {
        if (todoPriority.equals(application.getApplicationContext().getString(R.string.low_priority)))
        {
            return 0;
        } else if (todoPriority.equals(application.getApplicationContext().getString(R.string.medium_priority)))
        {
            return 1;
        } else if (todoPriority.equals(application.getApplicationContext().getString(R.string.high_priority)))
        {
            return 2;
        }
        return 1;
    }

    void saveTodoItem(AlarmManager alarmManager, String description, String note, int priority) {
        if (notificationUpdateState == NotificationUpdateState.ADDED_NOTIFICATION) {
            NotificationUtil.addNotification(application.getApplicationContext(), alarmManager, notificationId, description, year, month, day, hour, minute);
        } else if (notificationUpdateState == NotificationUpdateState.REMOVED_NOTIFICATION) {
            NotificationUtil.removeNotification(application.getApplicationContext(), alarmManager, notificationId);
        }

        if (!hasNotification) {
            notificationId = 0;
        }

        Todo todo = createTodoItem(description, note, priority);

        if (todoId == -1) {
            insert(todo);
        } else {
            todo.setId(todoId);
            update(todo);
        }
    }

    private void insert(Todo todo) {
        repository.insert(todo);
    }

    private void update(Todo todo) {
        repository.update(todo);
    }

    private Todo createTodoItem(String description, String note, int priority) {
        return new Todo(description, note, priority, notificationId, hasNotification, year, month, day, hour, minute, false);
    }
}
