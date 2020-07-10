package com.github.fredrik9000.todolist.add_edit_todo

import android.app.AlarmManager
import android.app.Application
import android.os.Bundle
import android.os.SystemClock
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.github.fredrik9000.todolist.R
import com.github.fredrik9000.todolist.model.Todo
import com.github.fredrik9000.todolist.model.TodoRepository
import com.github.fredrik9000.todolist.notifications.NotificationUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class AddEditTodoViewModel(application: Application, private val savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {

    // Keeps track of whether the user has added a new, or removed an existing notification.
    // Notifications will only be scheduled when the task is saved.
    var notificationUpdateState: NotificationUpdateState = NotificationUpdateState.NOT_UPDATED
    var geofenceNotificationUpdateState: NotificationUpdateState = NotificationUpdateState.NOT_UPDATED

    private val repository: TodoRepository = TodoRepository(application)
    private var lastClickedUndoTime: Long = 0
    private var todoId = TODO_ID_NOT_SET

    var title: String = ""
    var description: String = ""
    var hasNotification = false
    var hasGeofenceNotification = false
    var geofenceLatitude = 0.0
    var geofenceLongitude = 0.0
    var geofenceRadius = GeofenceRadiusFragment.DEFAULT_RADIUS_IN_METERS

    private var priority = PRIORITY_MEDIUM
    private var dayTemp = 0
    private var day = 0
    private var monthTemp = 0
    private var month = 0
    private var yearTemp = 0
    private var year = 0
    private var hourTemp = 0
    private var hour = 0
    private var minuteTemp = 0
    private var minute = 0
    private var notificationId = 0
    private var geofenceNotificationId = 0

    private val savedStateHandleContainsValues
        get() = savedStateHandle.contains(TITLE_STATE)

    val hasActiveTimedNotification
        get() = hasNotification && !isNotificationExpired

    private val isNotificationExpired
        get() = createNotificationCalendar().timeInMillis < Calendar.getInstance().timeInMillis

    val isUndoDoubleClicked
        get() = SystemClock.elapsedRealtime() - lastClickedUndoTime < MINIMUM_TIME_BETWEEN_UNDOS_IN_MILLISECONDS

    fun updateLastClickedUndoTime() {
        lastClickedUndoTime = SystemClock.elapsedRealtime()
    }

    private fun setupNotificationState(args: Bundle?) {
        if (hasNotification) {
            if (savedStateHandleContainsValues) {
                setNotificationValuesFromSavedState()
            } else args?.let {
                setNotificationValuesFromArguments(it)
            }

            if (isNotificationExpired) {
                clearNotificationValues()
                generateNewNotificationId()
            }
        } else {
            // Tasks without notification will be given a generated notification id for later use
            // If the task is saved without a notification it wont be included
            generateNewNotificationId()
        }
    }

    private fun setNotificationValuesFromSavedState() {
        year = savedStateHandle.get(NOTIFICATION_YEAR_STATE)!!
        month = savedStateHandle.get(NOTIFICATION_MONTH_STATE)!!
        day = savedStateHandle.get(NOTIFICATION_DAY_STATE)!!
        hour = savedStateHandle.get(NOTIFICATION_HOUR_STATE)!!
        minute = savedStateHandle.get(NOTIFICATION_MINUTE_STATE)!!
        notificationId = savedStateHandle.get(NOTIFICATION_ID_STATE)!!
    }

    private fun setNotificationValuesFromArguments(args: Bundle) {
        year = args.getInt(AddEditTodoFragment.ARGUMENT_NOTIFICATION_YEAR)
        month = args.getInt(AddEditTodoFragment.ARGUMENT_NOTIFICATION_MONTH)
        day = args.getInt(AddEditTodoFragment.ARGUMENT_NOTIFICATION_DAY)
        hour = args.getInt(AddEditTodoFragment.ARGUMENT_NOTIFICATION_HOUR)
        minute = args.getInt(AddEditTodoFragment.ARGUMENT_NOTIFICATION_MINUTE)
        notificationId = args.getInt(AddEditTodoFragment.ARGUMENT_NOTIFICATION_ID)
    }

    private fun setupGeofenceNotificationState(args: Bundle?) {
        if (hasGeofenceNotification) {
            if (savedStateHandleContainsValues) {
                setGeofenceNotificationValuesFromSavedState()
            } else args?.let {
                setGeofenceNotificationValuesFromArguments(it)
            }
        } else {
            // Tasks without notification will be given a generated notification id for later use
            // If the task is saved without a notification it wont be included
            generateNewGeofenceNotificationId()
        }
    }

    private fun setGeofenceNotificationValuesFromSavedState() {
        geofenceNotificationId = savedStateHandle.get(GEOFENCE_NOTIFICATION_ID_STATE)!!
        geofenceLatitude = savedStateHandle.get(GEOFENCE_NOTIFICATION_LATITUDE_STATE)!!
        geofenceLongitude = savedStateHandle.get(GEOFENCE_NOTIFICATION_LONGITUDE_STATE)!!
        geofenceRadius = savedStateHandle.get(GEOFENCE_NOTIFICATION_RADIUS_STATE)!!
    }

    private fun setGeofenceNotificationValuesFromArguments(args: Bundle) {
        geofenceNotificationId = args.getInt(AddEditTodoFragment.ARGUMENT_GEOFENCE_NOTIFICATION_ID)
        geofenceLatitude = args.getDouble(AddEditTodoFragment.ARGUMENT_GEOFENCE_LATITUDE)
        geofenceLongitude = args.getDouble(AddEditTodoFragment.ARGUMENT_GEOFENCE_LONGITUDE)
        geofenceRadius = args.getInt(AddEditTodoFragment.ARGUMENT_GEOFENCE_RADIUS)
    }

    private fun clearNotificationValues() {
        year = 0
        month = 0
        day = 0
        hour = 0
        minute = 0
        hasNotification = false
    }

    fun setTemporaryNotificationDateValues(year: Int, month: Int, day: Int) {
        yearTemp = year
        monthTemp = month
        dayTemp = day
    }

    fun setTemporaryNotificationTimeValues(hour: Int, minute: Int) {
        hourTemp = hour
        minuteTemp = minute
    }

    fun setFinallySelectedNotificationValues() {
        year = yearTemp
        month = monthTemp
        day = dayTemp
        hour = hourTemp
        minute = minuteTemp
        hasNotification = true
        notificationUpdateState = NotificationUpdateState.ADDED_NOTIFICATION
    }

    // TODO: fix unique notification id retrieval.
    private fun generateNewNotificationId() {
        notificationId = Random().nextInt()
    }

    private fun generateNewGeofenceNotificationId() {
        geofenceNotificationId = Random().nextInt()
    }

    fun createNotificationCalendar(): Calendar {
        val calendar = Calendar.getInstance()
        calendar[year, month, day, hour, minute] = 0
        return calendar
    }

    fun createTemporaryNotificationCalendar(): Calendar {
        val notificationCalendar = Calendar.getInstance()
        notificationCalendar[yearTemp, monthTemp, dayTemp, hourTemp, minuteTemp] = 0
        return notificationCalendar
    }

    fun togglePriorityValue() {
        priority = when (priority) {
            PRIORITY_LOW -> {
                PRIORITY_MEDIUM
            }
            PRIORITY_MEDIUM -> {
                PRIORITY_HIGH
            }
            else -> {
                PRIORITY_LOW
            }
        }
    }

    fun getLabelForCurrentPriority(): String? {
        return when (priority) {
            PRIORITY_LOW -> getApplication<Application>().applicationContext.getString(R.string.low_priority)
            PRIORITY_MEDIUM -> getApplication<Application>().applicationContext.getString(R.string.medium_priority)
            PRIORITY_HIGH -> getApplication<Application>().applicationContext.getString(R.string.high_priority)
            else -> getApplication<Application>().applicationContext.getString(R.string.medium_priority)
        }
    }

    fun getColorForCurrentPriority(): Int {
        return when (priority) {
            PRIORITY_LOW -> ContextCompat.getColor(getApplication<Application>().applicationContext, R.color.low_priority)
            PRIORITY_MEDIUM -> ContextCompat.getColor(getApplication<Application>().applicationContext, R.color.medium_priority)
            PRIORITY_HIGH -> ContextCompat.getColor(getApplication<Application>().applicationContext, R.color.high_priority)
            else -> ContextCompat.getColor(getApplication<Application>().applicationContext, R.color.medium_priority)
        }
    }

    fun saveTodoItem(alarmManager: AlarmManager, title: String, description: String) {
        if (notificationUpdateState == NotificationUpdateState.ADDED_NOTIFICATION) {
            NotificationUtil.addNotification(getApplication<Application>().applicationContext, alarmManager, notificationId, title, year, month, day, hour, minute)
        } else if (notificationUpdateState == NotificationUpdateState.REMOVED_NOTIFICATION) {
            NotificationUtil.removeNotification(getApplication<Application>().applicationContext, alarmManager, notificationId)
        }

        if (geofenceNotificationUpdateState == NotificationUpdateState.ADDED_NOTIFICATION) {
            NotificationUtil.addGeofenceNotification(getApplication<Application>().applicationContext, geofenceNotificationId, title, geofenceRadius, geofenceLatitude, geofenceLongitude)
        } else if (geofenceNotificationUpdateState == NotificationUpdateState.REMOVED_NOTIFICATION) {
            NotificationUtil.removeGeofenceNotification(getApplication<Application>().applicationContext, geofenceNotificationId)
        }

        if (!hasNotification) {
            notificationId = 0
        }

        if (!hasGeofenceNotification) {
            geofenceNotificationId = 0
        }

        val todo = createTodoItem(title, description)
        if (todoId == TODO_ID_NOT_SET) {
            insert(todo)
        } else {
            todo.id = todoId
            update(todo)
        }
    }

    private fun insert(todo: Todo) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(todo)
        }
    }

    private fun update(todo: Todo) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(todo)
        }
    }

    private fun createTodoItem(title: String, description: String): Todo {
        return Todo(0,
                title,
                description,
                priority,
                notificationId,
                geofenceNotificationId,
                hasNotification,
                year,
                month,
                day,
                hour,
                minute,
                hasGeofenceNotification,
                geofenceLatitude,
                geofenceLongitude,
                geofenceRadius,
                false)
    }

    fun saveState() {
        savedStateHandle.set(TITLE_STATE, title)
        savedStateHandle.set(DESCRIPTION_STATE, description)
        savedStateHandle.set(PRIORITY_STATE, priority)
        savedStateHandle.set(NOTIFICATION_YEAR_STATE, year)
        savedStateHandle.set(NOTIFICATION_MONTH_STATE, month)
        savedStateHandle.set(NOTIFICATION_DAY_STATE, day)
        savedStateHandle.set(NOTIFICATION_HOUR_STATE, hour)
        savedStateHandle.set(NOTIFICATION_MINUTE_STATE, minute)
        savedStateHandle.set(NOTIFICATION_ID_STATE, notificationId)
        savedStateHandle.set(GEOFENCE_NOTIFICATION_ID_STATE, geofenceNotificationId)
        savedStateHandle.set(GEOFENCE_NOTIFICATION_LATITUDE_STATE, geofenceLatitude)
        savedStateHandle.set(GEOFENCE_NOTIFICATION_LONGITUDE_STATE, geofenceLongitude)
        savedStateHandle.set(GEOFENCE_NOTIFICATION_RADIUS_STATE, geofenceRadius)
        savedStateHandle.set(HAS_NOTIFICATION_STATE, hasNotification)
        savedStateHandle.set(HAS_GEOFENCE_NOTIFICATION_STATE, hasGeofenceNotification)
        savedStateHandle.set(NOTIFICATION_UPDATE_STATE_STATE, notificationUpdateState)
        savedStateHandle.set(GEOFENCE_NOTIFICATION_UPDATE_STATE_STATE, geofenceNotificationUpdateState)
    }

    fun setValuesFromArgumentsOrSavedState(args: Bundle) {
        todoId = args.getInt(AddEditTodoFragment.ARGUMENT_TODO_ID)
        if (savedStateHandleContainsValues) {
            title = savedStateHandle.get(TITLE_STATE)!!
            description = savedStateHandle.get(DESCRIPTION_STATE)!!
            priority = savedStateHandle.get(PRIORITY_STATE)!!
            hasNotification = savedStateHandle.get(HAS_NOTIFICATION_STATE)!!
            hasGeofenceNotification = savedStateHandle.get(HAS_GEOFENCE_NOTIFICATION_STATE)!!
            notificationUpdateState = savedStateHandle.get(NOTIFICATION_UPDATE_STATE_STATE)!!
            geofenceNotificationUpdateState = savedStateHandle.get(GEOFENCE_NOTIFICATION_UPDATE_STATE_STATE)!!
        } else {
            title = args.getString(AddEditTodoFragment.ARGUMENT_TITLE)!!
            description = args.getString(AddEditTodoFragment.ARGUMENT_DESCRIPTION)!!
            priority = args.getInt(AddEditTodoFragment.ARGUMENT_PRIORITY)
            hasNotification = args.getBoolean(AddEditTodoFragment.ARGUMENT_HAS_NOTIFICATION)
            hasGeofenceNotification = args.getBoolean(AddEditTodoFragment.ARGUMENT_HAS_GEOFENCE_NOTIFICATION)
        }

        setupNotificationState(args)
        setupGeofenceNotificationState(args)
    }

    companion object {
        private const val TITLE_STATE: String = "TITLE"
        private const val DESCRIPTION_STATE: String = "DESCRIPTION"
        private const val PRIORITY_STATE: String = "PRIORITY"
        private const val HAS_NOTIFICATION_STATE: String = "HAS_NOTIFICATION"
        private const val HAS_GEOFENCE_NOTIFICATION_STATE: String = "HAS_GEOFENCE_NOTIFICATION"
        private const val NOTIFICATION_UPDATE_STATE_STATE: String = "NOTIFICATION_UPDATE_STATE"
        private const val GEOFENCE_NOTIFICATION_UPDATE_STATE_STATE: String = "GEOFENCE_NOTIFICATION_UPDATE_STATE"
        private const val NOTIFICATION_ID_STATE: String = "NOTIFICATION_ID"
        private const val NOTIFICATION_YEAR_STATE: String = "NOTIFICATION_YEAR"
        private const val NOTIFICATION_MONTH_STATE: String = "NOTIFICATION_MONTH"
        private const val NOTIFICATION_DAY_STATE: String = "NOTIFICATION_DAY"
        private const val NOTIFICATION_HOUR_STATE: String = "NOTIFICATION_HOUR"
        private const val NOTIFICATION_MINUTE_STATE: String = "NOTIFICATION_MINUTE"
        private const val GEOFENCE_NOTIFICATION_ID_STATE: String = "GEOFENCE_NOTIFICATION_ID"
        private const val GEOFENCE_NOTIFICATION_LONGITUDE_STATE: String = "GEOFENCE_NOTIFICATION_LONGITUDE"
        private const val GEOFENCE_NOTIFICATION_LATITUDE_STATE: String = "GEOFENCE_NOTIFICATION_LATITUDE"
        private const val GEOFENCE_NOTIFICATION_RADIUS_STATE: String = "GEOFENCE_NOTIFICATION_RADIUS"

        private const val TODO_ID_NOT_SET = -1
        private const val MINIMUM_TIME_BETWEEN_UNDOS_IN_MILLISECONDS = 1000

        const val PRIORITY_LOW = 0
        const val PRIORITY_MEDIUM = 1
        const val PRIORITY_HIGH = 2
    }
}