package com.github.fredrik9000.todolist.model

import android.app.Application
import androidx.lifecycle.LiveData

class TodoRepository(application: Application) {

    private val todoDao: TodoDao

    init {
        val database = TodoDatabase.getInstance(application)
        todoDao = database.todoDao()
    }

    fun insert(todo: Todo) {
        todoDao.insert(todo)
    }

    fun insertTodoItems(todoList: MutableList<Todo>) {
        todoDao.insert(todoList)
    }

    fun update(todo: Todo) {
        todoDao.update(todo)
    }

    fun delete(todo: Todo) {
        todoDao.delete(todo)
    }

    fun getAllTodos(): LiveData<MutableList<Todo>> {
        return todoDao.getAllTodos()
    }

    fun getTodosWithText(searchValue: String?): LiveData<MutableList<Todo>> {
        return todoDao.getTodosWithText(searchValue)
    }
}