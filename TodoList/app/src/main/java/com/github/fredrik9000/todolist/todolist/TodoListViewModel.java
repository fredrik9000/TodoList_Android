package com.github.fredrik9000.todolist.todolist;

import android.app.AlarmManager;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.github.fredrik9000.todolist.model.Todo;
import com.github.fredrik9000.todolist.model.TodoRepository;
import com.github.fredrik9000.todolist.notifications.NotificationUtil;

import java.util.ArrayList;
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

    void insert(Todo todo) {
        repository.insert(todo);
    }

    void update(Todo todo) {
        repository.update(todo);
    }

    LiveData<List<Todo>> getTodoList() {
        return todoList;
    }

    List<Todo> deleteAllTodoItems(AlarmManager alarmManager) {
        final List<Todo> removedTodoItems = new ArrayList<>();
        for (Todo todo : todoList.getValue()) {
            removedTodoItems.add(todo);
            deleteTodo(alarmManager, todo);
        }
        return removedTodoItems;
    }

    List<Todo> deleteCompletedTodoItems(AlarmManager alarmManager) {
        final List<Todo> removedTodoItems = new ArrayList<>();
        for (Todo todo : todoList.getValue()) {
            if (todo.isCompleted()) {
                removedTodoItems.add(todo);
                deleteTodo(alarmManager, todo);
            }
        }
        return removedTodoItems;
    }

    void deleteTodo(AlarmManager alarmManager, Todo todo) {
        if (todo.isNotificationEnabled()) {
            NotificationUtil.removeNotification(application.getApplicationContext(), alarmManager, todo.getNotificationId());
        }

        repository.delete(todo);
    }

    void insertTodoItems(List<Todo> todoListItems, AlarmManager alarmManager) {
        for (Todo todo : todoListItems) {
            if (todo.isNotificationEnabled()) {
                NotificationUtil.addNotification(application.getApplicationContext(), alarmManager, todo.getNotificationId(), todo.getDescription(), todo.getNotifyYear(), todo.getNotifyMonth(), todo.getNotifyDay(), todo.getNotifyHour(), todo.getNotifyMinute());
            }
        }
        repository.insertTodoItems(todoListItems);
    }
}
