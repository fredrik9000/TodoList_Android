package com.github.fredrik9000.todolist.todolist

import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.os.SystemClock
import androidx.lifecycle.*
import com.github.fredrik9000.todolist.model.Todo
import com.github.fredrik9000.todolist.model.TodoRepository
import com.github.fredrik9000.todolist.notifications.NotificationUtil
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
            deleteTodo(alarmManager = alarmManager, todo = todo)
        }
        return removedTodoItems
    }

    fun deleteAllCompletedTodoItems(alarmManager: AlarmManager): MutableList<Todo> {
        val removedTodoItems: MutableList<Todo> = ArrayList()
        for (todo in todoList.value!!) {
            if (todo.isCompleted) {
                removedTodoItems.add(todo)
                deleteTodo(alarmManager = alarmManager, todo = todo)
            }
        }
        return removedTodoItems
    }

    fun deleteTodo(alarmManager: AlarmManager, todo: Todo) {
        if (todo.notificationEnabled) {
            NotificationUtil.removeNotification(
                applicationContext = getApplication<Application>().applicationContext,
                alarmManager = alarmManager,
                notificationId = todo.notificationId
            )
        }

        if (todo.geofenceNotificationEnabled) {
            NotificationUtil.removeGeofence(
                applicationContext = getApplication<Application>().applicationContext,
                notificationId = todo.geofenceNotificationId
            )
        }

        viewModelScope.launch {
            repository.delete(todo)
        }
    }

    fun insertTodoItems(todoListItems: MutableList<Todo>, alarmManager: AlarmManager) {
        for (todo in todoListItems) {
            addNotificationForTodoItem(todo = todo, alarmManager = alarmManager)
        }

        viewModelScope.launch {
            repository.insertTodoItems(todoListItems)
        }
    }

    fun insertTodo(todo: Todo, alarmManager: AlarmManager) {
        addNotificationForTodoItem(todo = todo, alarmManager = alarmManager)

        viewModelScope.launch {
            repository.insert(todo)
        }
    }

    private fun addNotificationForTodoItem(todo: Todo, alarmManager: AlarmManager) {
        if (todo.notificationEnabled) {
            NotificationUtil.addNotification(
                applicationContext = getApplication<Application>().applicationContext,
                alarmManager = alarmManager,
                notificationId = todo.notificationId,
                year = todo.notifyYear,
                month = todo.notifyMonth,
                day = todo.notifyDay,
                hour = todo.notifyHour,
                minute = todo.notifyMinute
            )
        }

        if (todo.geofenceNotificationEnabled) {
            NotificationUtil.addGeofenceNotification(
                applicationContext = getApplication<Application>().applicationContext,
                notificationId = todo.geofenceNotificationId,
                radius = todo.geofenceRadius,
                latitude = todo.geofenceLatitude,
                longitude = todo.geofenceLongitude
            )
        }
    }

    fun updatedCompleted(isChecked: Boolean, todoItem: Todo, context: Context) {
        // If a notification is active for the completed task, remove it.
        if (isChecked) {
            if (todoItem.notificationEnabled) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                NotificationUtil.removeNotification(
                    applicationContext = context,
                    alarmManager = alarmManager,
                    notificationId = todoItem.notificationId
                )
            }
            if (todoItem.geofenceNotificationEnabled) {
                NotificationUtil.removeGeofence(
                    applicationContext = context,
                    notificationId = todoItem.geofenceNotificationId
                )
            }
        }

        viewModelScope.launch {
            repository.update(
                Todo(
                    id = todoItem.id,
                    title = todoItem.title,
                    description = todoItem.description,
                    priority = todoItem.priority,
                    notificationId = 0,
                    geofenceNotificationId = 0,
                    notificationEnabled = false,
                    notifyYear = 0,
                    notifyMonth = 0,
                    notifyDay = 0,
                    notifyHour = 0,
                    notifyMinute = 0,
                    geofenceNotificationEnabled = false,
                    geofenceLatitude = 0.0,
                    geofenceLongitude = 0.0,
                    geofenceRadius = 0,
                    isCompleted = isChecked
                )
            )
        }
    }

    fun isNotificationExpired(todo: Todo): Boolean {
        return Calendar.getInstance().also {
            it[todo.notifyYear, todo.notifyMonth, todo.notifyDay, todo.notifyHour, todo.notifyMinute] = 0
        }.timeInMillis < Calendar.getInstance().timeInMillis
    }

    companion object {
        private const val MINIMUM_TIME_BETWEEN_UNDOS_IN_MILLISECONDS = 1000
        private const val MINIMUM_SEARCH_LENGTH = 2
    }
}