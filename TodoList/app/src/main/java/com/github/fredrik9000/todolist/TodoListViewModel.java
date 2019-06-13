package com.github.fredrik9000.todolist;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.github.fredrik9000.todolist.model.Todo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;

public class TodoListViewModel extends AndroidViewModel {
    private ArrayList<Todo> todoList;
    private static final String SHARED_PREFERENCES_TODOS_KEY = "TODOS";
    private Application application;

    public TodoListViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
    }

    public ArrayList<Todo> getTodoList() {
        if (todoList == null) {
            SharedPreferences appSharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(application.getApplicationContext());

            if (appSharedPrefs.contains(SHARED_PREFERENCES_TODOS_KEY)) {
                Type type = new TypeToken<ArrayList<Todo>>() {
                }.getType();
                String json = appSharedPrefs.getString(SHARED_PREFERENCES_TODOS_KEY, "");
                todoList = new Gson().fromJson(json, type);
            } else {
                todoList = new ArrayList<>();
            }
        }
        return todoList;
    }


    public void saveTodoList() {
        SharedPreferences appSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(application.getApplicationContext());
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
        prefsEditor.putString(SHARED_PREFERENCES_TODOS_KEY, new Gson().toJson(todoList));
        prefsEditor.apply();
    }

    public void addTodo(Todo todo) {
        if (todoList == null) {
            todoList = new ArrayList<>();
        }
        todoList.add(todo);
    }

    public void removeNotification(AlarmManager alarmManager, Todo todo) {
        Intent intent = new Intent(application.getApplicationContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(application.getApplicationContext(), todo.getNotificationId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    public void addNotification(AlarmManager alarmManager, Todo todo) {
        Intent notificationIntent = new Intent(application.getApplicationContext(), AlarmReceiver.class);
        notificationIntent.putExtra(AlarmReceiver.TODO_DESCRIPTION, todo.getDescription());
        PendingIntent broadcast = PendingIntent.getBroadcast(application.getApplicationContext(), todo.getNotificationId(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar notificationCalendar = Calendar.getInstance();
        notificationCalendar.set(todo.getNotifyYear(), todo.getNotifyMonth(), todo.getNotifyDay(), todo.getNotifyHour(), todo.getNotifyMinute(), 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationCalendar.getTimeInMillis(), broadcast);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, notificationCalendar.getTimeInMillis(), broadcast);
        }
    }
}
