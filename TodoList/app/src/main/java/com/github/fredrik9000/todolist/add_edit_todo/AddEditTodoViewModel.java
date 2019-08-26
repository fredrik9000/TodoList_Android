package com.github.fredrik9000.todolist.add_edit_todo;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.github.fredrik9000.todolist.model.Todo;
import com.github.fredrik9000.todolist.model.TodoRepository;

public class AddEditTodoViewModel extends AndroidViewModel {
    private TodoRepository repository;

    public AddEditTodoViewModel(@NonNull Application application) {
        super(application);
        repository = new TodoRepository(application);
    }

    void insert(Todo todo) {
        repository.insert(todo);
    }

    void update(Todo todo) {
        repository.update(todo);
    }
}
