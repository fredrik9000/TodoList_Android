package com.github.fredrik9000.todolist.model

import android.app.Application
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TodoRepository(application: Application) {

    private val todoDao: TodoDao

    init {
        val database = TodoDatabase.getInstance(application)
        todoDao = database.todoDao()
    }

    suspend fun insert(todo: Todo) = withContext(Dispatchers.IO) {
        todoDao.insert(todo)
    }

    suspend fun insertTodoItems(todoList: MutableList<Todo>) = withContext(Dispatchers.IO) {
        todoDao.insert(todoList)
    }

    suspend fun update(todo: Todo) = withContext(Dispatchers.IO) {
        todoDao.update(todo)
    }

    suspend fun delete(todo: Todo) = withContext(Dispatchers.IO) {
        todoDao.delete(todo)
    }

    fun getAllTodos(): LiveData<MutableList<Todo>> {
        return todoDao.getAllTodos()
    }

    fun getTodosWithText(searchValue: String?): LiveData<MutableList<Todo>> {
        return todoDao.getTodosWithText(searchValue)
    }
}