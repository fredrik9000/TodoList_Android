package com.github.fredrik9000.todolist;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.github.fredrik9000.todolist.model.Todo;
import com.github.fredrik9000.todolist.model.TodoRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class TodoListViewModel extends AndroidViewModel {
    private LiveData<List<Todo>> todoList;
    private Application application;
    private TodoRepository repository;

    public TodoListViewModel(@NonNull Application application) {
        super(application);
        repository = new TodoRepository(application);
        todoList = repository.getAllTodos();
        this.application = application;
    }

    public void addTodo(Todo todo) {
        repository.insert(todo);
    }

    public void removeTodo(Todo todo) {
        repository.delete(todo);
    }

    public void updateTodo(Todo todo) {
        repository.update(todo);
    }

    public LiveData<List<Todo>> getTodoList() {
        return todoList;
    }

    public List<Todo> removeTodosWithPriorities(ArrayList<Integer> priorities, AlarmManager alarmManager) {
        final List<Todo> removedTodoItems = new ArrayList<>();
        for (Iterator<Todo> iterator = todoList.getValue().listIterator(); iterator.hasNext(); ) {
            Todo todo = iterator.next();
            if (priorities.contains(todo.getPriority())) {
                removedTodoItems.add(todo);
                if (todo.isNotificationEnabled()) {
                    removeNotification(alarmManager, todo);
                }

                repository.delete(todo);
            }
        }
        return removedTodoItems;
    }

    public void addTodoItems(List<Todo> todoListItems, AlarmManager alarmManager) {
        for (Iterator<Todo> iterator = todoListItems.listIterator(); iterator.hasNext(); ) {
            Todo todo = iterator.next();
            if (todo.isNotificationEnabled()) {
                addNotification(alarmManager, todo);
            }
        }
        repository.insertTodoItems(todoListItems);
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
