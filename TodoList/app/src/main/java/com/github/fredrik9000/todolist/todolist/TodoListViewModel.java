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

    void delete(Todo todo) {
        repository.delete(todo);
    }

    LiveData<List<Todo>> getTodoList() {
        return todoList;
    }

    List<Todo> deleteTodosWithPriorities(ArrayList<Integer> priorities, AlarmManager alarmManager) {
        final List<Todo> removedTodoItems = new ArrayList<>();
        for (Todo todo : todoList.getValue()) {
            if (priorities.contains(todo.getPriority())) {
                removedTodoItems.add(todo);
                if (todo.isNotificationEnabled()) {
                    NotificationUtil.removeNotification(application.getApplicationContext(), alarmManager, todo.getNotificationId());
                }

                repository.delete(todo);
            }
        }
        return removedTodoItems;
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
