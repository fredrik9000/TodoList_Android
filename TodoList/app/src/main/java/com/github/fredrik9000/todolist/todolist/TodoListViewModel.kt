package com.github.fredrik9000.todolist.todolist

import android.app.AlarmManager
import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.*
import com.github.fredrik9000.todolist.model.Todo
import com.github.fredrik9000.todolist.model.TodoRepository
import com.github.fredrik9000.todolist.notifications.NotificationUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class TodoListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TodoRepository = TodoRepository(application)
    private val searchValueLiveData: MutableLiveData<String?> = MutableLiveData()
    private val todoList: LiveData<MutableList<Todo>>
    private var lastClickedUndoTime: Long = 0
    var isSearching = false

    init {
        todoList = Transformations.switchMap(searchValueLiveData) { value ->
            if (value != null && value.length >= MINIMUM_SEARCH_LENGTH) {
                isSearching = true
                repository.getTodosWithText(value)
            } else {
                isSearching = false
                repository.getAllTodos()
            }
        }

        searchValueLiveData.value = null // Sets the todoList live data to contain all todos.
    }

    val isUndoDoubleClicked
        get() = SystemClock.elapsedRealtime() - lastClickedUndoTime < MINIMUM_TIME_BETWEEN_UNDOS_IN_MILLISECONDS

    fun updateLastClickedUndoTime() {
        lastClickedUndoTime = SystemClock.elapsedRealtime()
    }

    fun insertTodo(todo: Todo, alarmManager: AlarmManager) {
        if (todo.notificationEnabled) {
            NotificationUtil.addNotification(getApplication<Application>().applicationContext,
                    alarmManager,
                    todo.notificationId, todo.notifyYear, todo.notifyMonth, todo.notifyDay, todo.notifyHour, todo.notifyMinute)
        }

        if (todo.geofenceNotificationEnabled) {
            NotificationUtil.addGeofenceNotification(getApplication<Application>().applicationContext, todo.geofenceNotificationId, todo.geofenceRadius, todo.geofenceLatitude, todo.geofenceLongitude)
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(todo)
        }
    }

    fun update(todo: Todo) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(todo)
        }
    }

    fun getTodoList(): LiveData<MutableList<Todo>> {
        return todoList
    }

    fun searchTodoList(searchValue: String?) {
        searchValueLiveData.value = searchValue
    }

    fun deleteAllTodoItems(alarmManager: AlarmManager): MutableList<Todo> {
        val removedTodoItems: MutableList<Todo> = ArrayList()
        for (todo in todoList.value!!) {
            removedTodoItems.add(todo)
            deleteTodo(alarmManager, todo)
        }
        return removedTodoItems
    }

    fun deleteAllCompletedTodoItems(alarmManager: AlarmManager): MutableList<Todo> {
        val removedTodoItems: MutableList<Todo> = ArrayList()
        for (todo in todoList.value!!) {
            if (todo.isCompleted) {
                removedTodoItems.add(todo)
                deleteTodo(alarmManager, todo)
            }
        }
        return removedTodoItems
    }

    fun deleteTodo(alarmManager: AlarmManager, todo: Todo) {
        if (todo.notificationEnabled) {
            NotificationUtil.removeNotification(getApplication<Application>().applicationContext, alarmManager, todo.notificationId)
        }

        if (todo.geofenceNotificationEnabled) {
            NotificationUtil.removeGeofence(getApplication<Application>().applicationContext, todo.geofenceNotificationId)
        }

        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(todo)
        }
    }

    fun insertTodoItems(todoListItems: MutableList<Todo>, alarmManager: AlarmManager) {
        for (todo in todoListItems) {
            if (todo.notificationEnabled) {
                NotificationUtil.addNotification(getApplication<Application>().applicationContext, alarmManager, todo.notificationId, todo.notifyYear, todo.notifyMonth, todo.notifyDay, todo.notifyHour, todo.notifyMinute)
            }

            if (todo.geofenceNotificationEnabled) {
                NotificationUtil.addGeofenceNotification(getApplication<Application>().applicationContext, todo.notificationId, todo.geofenceRadius, todo.geofenceLatitude, todo.geofenceLongitude)
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            repository.insertTodoItems(todoListItems)
        }
    }

    companion object {
        private const val MINIMUM_TIME_BETWEEN_UNDOS_IN_MILLISECONDS = 1000
        private const val MINIMUM_SEARCH_LENGTH = 2
    }
}