package com.github.fredrik9000.todolist.todolist;

import android.app.AlarmManager;
import android.app.Application;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.github.fredrik9000.todolist.model.Todo;
import com.github.fredrik9000.todolist.model.TodoRepository;
import com.github.fredrik9000.todolist.notifications.NotificationUtil;

import java.util.ArrayList;
import java.util.List;

public class TodoListViewModel extends AndroidViewModel {
    private MutableLiveData<String> searchValueLiveData = new MutableLiveData<>();
    private LiveData<List<Todo>> todoList;
    private Application application;
    private TodoRepository repository;
    private long lastClickedUndoTime = 0;
    private static final int MINIMUM_TIME_BETWEEN_UNDOS_IN_MILLISECONDS = 1000;
    private static final int MINIMUM_SEARCH_LENGTH = 2;

    public TodoListViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
        repository = new TodoRepository(application);
        todoList = Transformations.switchMap(searchValueLiveData, new Function<String, LiveData<List<Todo>>>() {
            @Override
            public LiveData<List<Todo>> apply(String value) {
                if (value != null && value.length() >= MINIMUM_SEARCH_LENGTH) {
                    return repository.getTodosWithText(value);
                } else {
                    return repository.getAllTodos();
                }
            }
        });
        searchValueLiveData.setValue(null); // Sets the todoList live data to contain all todos.
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

    void searchTodoList(String searchValue) {
        searchValueLiveData.setValue(searchValue);
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

    boolean isUndoDoubleClicked() {
        return SystemClock.elapsedRealtime() - lastClickedUndoTime < MINIMUM_TIME_BETWEEN_UNDOS_IN_MILLISECONDS;
    }

    void updateLastClickedUndoTime() {
        lastClickedUndoTime = SystemClock.elapsedRealtime();
    }
}
