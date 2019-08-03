package com.github.fredrik9000.todolist.model;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

public class TodoRepository {
    private TodoDao todoDao;
    private LiveData<List<Todo>> allTodos;

    public TodoRepository(Application application) {
        TodoDatabase database = TodoDatabase.getInstance(application);
        todoDao = database.todoDao();
        allTodos = todoDao.getAllTodos();
    }

    public void insert(Todo todo) {
        new InsertTodoAsyncTask(todoDao).execute(todo);
    }

    public void insertTodoItems(List<Todo> todoList) {
        new InsertTodoListAsyncTask(todoDao, todoList).execute();
    }

    public void update(Todo todo) {
        new UpdateTodoAsyncTask(todoDao).execute(todo);
    }

    public void delete(Todo todo) {
        new DeleteTodoAsyncTask(todoDao).execute(todo);
    }

    public LiveData<List<Todo>> getAllTodos() {
        return allTodos;
    }

    private static class InsertTodoAsyncTask extends AsyncTask<Todo, Void, Void> {

        private TodoDao todoDao;

        private InsertTodoAsyncTask(TodoDao todoDao) {
            this.todoDao = todoDao;
        }

        @Override
        protected Void doInBackground(Todo... todos) {
            todoDao.insert(todos[0]);
            return null;
        }
    }

    private static class InsertTodoListAsyncTask extends AsyncTask<Void, Void, Void> {

        private TodoDao todoDao;
        private List<Todo> todoList;

        private InsertTodoListAsyncTask(TodoDao todoDao, List<Todo> todoList) {
            this.todoDao = todoDao;
            this.todoList = todoList;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            todoDao.insert(todoList);
            return null;
        }
    }

    private static class UpdateTodoAsyncTask extends AsyncTask<Todo, Void, Void> {

        private TodoDao todoDao;

        private UpdateTodoAsyncTask(TodoDao todoDao) {
            this.todoDao = todoDao;
        }

        @Override
        protected Void doInBackground(Todo... todos) {
            todoDao.update(todos[0]);
            return null;
        }
    }

    private static class DeleteTodoAsyncTask extends AsyncTask<Todo, Void, Void> {

        private TodoDao todoDao;

        private DeleteTodoAsyncTask(TodoDao todoDao) {
            this.todoDao = todoDao;
        }

        @Override
        protected Void doInBackground(Todo... todos) {
            todoDao.delete(todos[0]);
            return null;
        }
    }
}
