package com.github.fredrik9000.todolist.add_edit_todo;

import android.app.AlarmManager;
import android.app.Application;
import android.os.Bundle;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.SavedStateHandle;

import com.github.fredrik9000.todolist.R;
import com.github.fredrik9000.todolist.model.Todo;
import com.github.fredrik9000.todolist.model.TodoRepository;
import com.github.fredrik9000.todolist.notifications.NotificationUtil;

import java.util.Calendar;
import java.util.Random;

public class AddEditTodoViewModel extends AndroidViewModel {

    private static final String DESCRIPTION_STATE = "DESCRIPTION";
    private static final String NOTE_STATE = "NOTE";
    private static final String PRIORITY_STATE = "PRIORITY";
    private static final String HAS_NOTIFICATION_STATE = "HAS_NOTIFICATION";
    private static final String HAS_GEOFENCE_NOTIFICATION_STATE = "HAS_GEOFENCE_NOTIFICATION";
    private static final String NOTIFICATION_UPDATE_STATE_STATE = "NOTIFICATION_UPDATE_STATE";
    private static final String GEOFENCE_NOTIFICATION_UPDATE_STATE_STATE = "GEOFENCE_NOTIFICATION_UPDATE_STATE";
    private static final String NOTIFICATION_ID_STATE = "NOTIFICATION_ID";
    private static final String NOTIFICATION_YEAR_STATE = "NOTIFICATION_YEAR";
    private static final String NOTIFICATION_MONTH_STATE = "NOTIFICATION_MONTH";
    private static final String NOTIFICATION_DAY_STATE = "NOTIFICATION_DAY";
    private static final String NOTIFICATION_HOUR_STATE = "NOTIFICATION_HOUR";
    private static final String NOTIFICATION_MINUTE_STATE = "NOTIFICATION_MINUTE";
    private static final String GEOFENCE_NOTIFICATION_ID_STATE = "GEOFENCE_NOTIFICATION_ID";
    private static final String GEOFENCE_NOTIFICATION_LONGITUDE_STATE = "GEOFENCE_NOTIFICATION_LONGITUDE";
    private static final String GEOFENCE_NOTIFICATION_LATITUDE_STATE = "GEOFENCE_NOTIFICATION_LATITUDE";
    private static final String GEOFENCE_NOTIFICATION_RADIUS_STATE = "GEOFENCE_NOTIFICATION_RADIUS";

    // Keeps track of whether the user has added a new, or removed an existing notification.
    // Notifications will only be scheduled when the task is saved.
    private NotificationUpdateState notificationUpdateState = NotificationUpdateState.NOT_UPDATED;
    private NotificationUpdateState geofenceNotificationUpdateState = NotificationUpdateState.NOT_UPDATED;

    private Application application;
    private TodoRepository repository;
    private SavedStateHandle savedStateHandle;

    private long lastClickedUndoTime = 0;
    private static final int MINIMUM_TIME_BETWEEN_UNDOS_IN_MILLISECONDS = 1000;

    private int todoId = -1;
    private String description = "";
    private String note = "";
    private int dayTemp, day, monthTemp, month, yearTemp, year, hourTemp, hour, minuteTemp, minute;
    private int notificationId;
    private int geofenceNotificationId;
    private boolean hasNotification = false;
    private boolean hasGeofenceNotification = false;
    private double geofenceLatitude, geofenceLongitude;
    private int geofenceRadius;
    private int priority = 1; // Default to medium priority (0=low, 1=medium, 2=high)


    public AddEditTodoViewModel(@NonNull Application application, SavedStateHandle savedStateHandle) {
        super(application);
        repository = new TodoRepository(application);
        this.application = application;
        this.savedStateHandle = savedStateHandle;
    }

    boolean isUndoDoubleClicked() {
        return SystemClock.elapsedRealtime() - lastClickedUndoTime < MINIMUM_TIME_BETWEEN_UNDOS_IN_MILLISECONDS;
    }

    void updateLastClickedUndoTime() {
        lastClickedUndoTime = SystemClock.elapsedRealtime();
    }

    void setupNotificationState(Bundle args) {
        if (hasNotification) {
            if (savedStateHandleContainsValues()) {
                setNotificationValuesFromSavedState();
            } else if (args != null) {
                setNotificationValuesFromArguments(args);
            }

            if (isNotificationExpired()) {
                clearNotificationValues();
                generateNewNotificationId();
            }
        } else {
            // Tasks without notification will be given a generated notification id for later use
            // If the task is saved without a notification it wont be included
            generateNewNotificationId();
        }
    }

    void setNotificationValuesFromSavedState() {
        year = savedStateHandle.get(NOTIFICATION_YEAR_STATE);
        month = savedStateHandle.get(NOTIFICATION_MONTH_STATE);
        day = savedStateHandle.get(NOTIFICATION_DAY_STATE);
        hour = savedStateHandle.get(NOTIFICATION_HOUR_STATE);
        minute = savedStateHandle.get(NOTIFICATION_MINUTE_STATE);
        notificationId = savedStateHandle.get(NOTIFICATION_ID_STATE);
    }

    void setNotificationValuesFromArguments(Bundle args) {
        year = args.getInt(AddEditTodoFragment.ARGUMENT_NOTIFICATION_YEAR);
        month = args.getInt(AddEditTodoFragment.ARGUMENT_NOTIFICATION_MONTH);
        day = args.getInt(AddEditTodoFragment.ARGUMENT_NOTIFICATION_DAY);
        hour = args.getInt(AddEditTodoFragment.ARGUMENT_NOTIFICATION_HOUR);
        minute = args.getInt(AddEditTodoFragment.ARGUMENT_NOTIFICATION_MINUTE);
        notificationId = args.getInt(AddEditTodoFragment.ARGUMENT_NOTIFICATION_ID);
    }

    void setupGeofenceNotificationState(Bundle args) {
        if (hasGeofenceNotification) {
            if ((savedStateHandleContainsValues())) {
                setGeofenceNotificationValuesFromSavedState();
            } else if (args != null) {
                setGeofenceNotificationValuesFromArguments(args);
            }
        } else {
            // Tasks without notification will be given a generated notification id for later use
            // If the task is saved without a notification it wont be included
            generateNewGeofenceNotificationId();
        }
    }

    void setGeofenceNotificationValuesFromSavedState() {
        geofenceNotificationId = savedStateHandle.get(GEOFENCE_NOTIFICATION_ID_STATE);
        geofenceLatitude = savedStateHandle.get(GEOFENCE_NOTIFICATION_LATITUDE_STATE);
        geofenceLongitude = savedStateHandle.get(GEOFENCE_NOTIFICATION_LONGITUDE_STATE);
        geofenceRadius = savedStateHandle.get(GEOFENCE_NOTIFICATION_RADIUS_STATE);

    }

    void setGeofenceNotificationValuesFromArguments(Bundle args) {
        geofenceNotificationId = args.getInt(AddEditTodoFragment.ARGUMENT_GEOFENCE_NOTIFICATION_ID);
        geofenceLatitude = args.getDouble(AddEditTodoFragment.ARGUMENT_GEOFENCE_LATITUDE);
        geofenceLongitude = args.getDouble(AddEditTodoFragment.ARGUMENT_GEOFENCE_LONGITUDE);
        geofenceRadius = args.getInt(AddEditTodoFragment.ARGUMENT_GEOFENCE_RADIUS);
    }

    void clearNotificationValues() {
        year = 0;
        month = 0;
        day = 0;
        hour = 0;
        minute = 0;
        hasNotification = false;
    }

    void setTemporaryNotificationDateValues(int year, int month, int day) {
        this.yearTemp = year;
        this.monthTemp = month;
        this.dayTemp = day;
    }

    void setTemporaryNotificationTimeValues(int hour, int minute) {
        this.hourTemp = hour;
        this.minuteTemp = minute;
    }

    void setFinallySelectedNotificationValues() {
        this.year = yearTemp;
        this.month = monthTemp;
        this.day = dayTemp;
        this.hour = hourTemp;
        this.minute = minuteTemp;
        hasNotification = true;
        notificationUpdateState = NotificationUpdateState.ADDED_NOTIFICATION;
    }

    // TODO: fix unique notification id retrieval.
    void generateNewNotificationId() {
        notificationId = new Random().nextInt();
    }

    void generateNewGeofenceNotificationId() {
        geofenceNotificationId = new Random().nextInt();
    }

    Calendar createNotificationCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, 0);
        return calendar;
    }

    Calendar createTemporaryNotificationCalendar() {
        Calendar notificationCalendar = Calendar.getInstance();
        notificationCalendar.set(yearTemp, monthTemp, dayTemp, hourTemp, minuteTemp, 0);
        return notificationCalendar;
    }

    boolean isNotificationExpired() {
        Calendar notificationCalendar = createNotificationCalendar();
        Calendar currentTimeCalendar = Calendar.getInstance();
        return notificationCalendar.getTimeInMillis() < currentTimeCalendar.getTimeInMillis();
    }

    void togglePriorityValue() {
        if (priority == 0) {
            priority = 1;
        } else if (priority == 1) {
            priority = 2;
        } else {
            priority = 0;
        }
    }

    String getLabelForCurrentPriority() {
        switch (priority) {
            case 0:
                return application.getApplicationContext().getString(R.string.low_priority);
            case 2:
                return application.getApplicationContext().getString(R.string.high_priority);
            case 1:
            default:
                return application.getApplicationContext().getString(R.string.medium_priority);
        }
    }

    int getColorForCurrentPriority() {
        switch (priority) {
            case 0:
                return application.getApplicationContext().getResources().getColor(R.color.low_priority);
            case 2:
                return application.getApplicationContext().getResources().getColor(R.color.high_priority);
            case 1:
            default:
                return application.getApplicationContext().getResources().getColor(R.color.medium_priority);
        }
    }

    void saveTodoItem(AlarmManager alarmManager, String description, String note) {
        if (notificationUpdateState == NotificationUpdateState.ADDED_NOTIFICATION) {
            NotificationUtil.addNotification(application.getApplicationContext(), alarmManager, notificationId, description, year, month, day, hour, minute);
        } else if (notificationUpdateState == NotificationUpdateState.REMOVED_NOTIFICATION) {
            NotificationUtil.removeNotification(application.getApplicationContext(), alarmManager, notificationId);
        }

        if (geofenceNotificationUpdateState == NotificationUpdateState.ADDED_NOTIFICATION) {
            NotificationUtil.addGeofenceNotification(application.getApplicationContext(), geofenceNotificationId, description, geofenceRadius, geofenceLatitude, geofenceLongitude);
        } else if (geofenceNotificationUpdateState == NotificationUpdateState.REMOVED_NOTIFICATION) {
            NotificationUtil.removeGeofenceNotification(application.getApplicationContext(), geofenceNotificationId);
        }

        if (!hasNotification) {
            notificationId = 0;
        }

        if (!hasGeofenceNotification) {
            geofenceNotificationId = 0;
        }

        Todo todo = createTodoItem(description, note);

        if (todoId == -1) {
            insert(todo);
        } else {
            todo.setId(todoId);
            update(todo);
        }
    }

    private void insert(Todo todo) {
        repository.insert(todo);
    }

    private void update(Todo todo) {
        repository.update(todo);
    }

    private Todo createTodoItem(String description, String note) {
        return new Todo(description, note, priority, notificationId, geofenceNotificationId, hasNotification, year, month, day, hour, minute, hasGeofenceNotification, geofenceLatitude, geofenceLongitude, geofenceRadius, false);
    }

    void saveState() {
        savedStateHandle.set(DESCRIPTION_STATE, description);
        savedStateHandle.set(NOTE_STATE, note);
        savedStateHandle.set(PRIORITY_STATE, priority);
        savedStateHandle.set(NOTIFICATION_YEAR_STATE, year);
        savedStateHandle.set(NOTIFICATION_MONTH_STATE, month);
        savedStateHandle.set(NOTIFICATION_DAY_STATE, day);
        savedStateHandle.set(NOTIFICATION_HOUR_STATE, hour);
        savedStateHandle.set(NOTIFICATION_MINUTE_STATE, minute);
        savedStateHandle.set(NOTIFICATION_ID_STATE, notificationId);
        savedStateHandle.set(GEOFENCE_NOTIFICATION_ID_STATE, geofenceNotificationId);
        savedStateHandle.set(GEOFENCE_NOTIFICATION_LATITUDE_STATE, geofenceLatitude);
        savedStateHandle.set(GEOFENCE_NOTIFICATION_LONGITUDE_STATE, geofenceLongitude);
        savedStateHandle.set(GEOFENCE_NOTIFICATION_RADIUS_STATE, geofenceRadius);
        savedStateHandle.set(HAS_NOTIFICATION_STATE, hasNotification);
        savedStateHandle.set(HAS_GEOFENCE_NOTIFICATION_STATE, hasGeofenceNotification);
        savedStateHandle.set(NOTIFICATION_UPDATE_STATE_STATE, notificationUpdateState);
        savedStateHandle.set(GEOFENCE_NOTIFICATION_UPDATE_STATE_STATE, geofenceNotificationUpdateState);
    }

    void setValuesFromArgumentsOrSavedState(Bundle args) {
        todoId = args.getInt(AddEditTodoFragment.ARGUMENT_TODO_ID);

        if (savedStateHandle.contains(DESCRIPTION_STATE)) {
            description = savedStateHandle.get(DESCRIPTION_STATE);
            note = savedStateHandle.get(NOTE_STATE);
            priority = savedStateHandle.get(PRIORITY_STATE);
            hasNotification = savedStateHandle.get(HAS_NOTIFICATION_STATE);
            hasGeofenceNotification = savedStateHandle.get(HAS_GEOFENCE_NOTIFICATION_STATE);
            notificationUpdateState = savedStateHandle.get(NOTIFICATION_UPDATE_STATE_STATE);
            geofenceNotificationUpdateState = savedStateHandle.get(GEOFENCE_NOTIFICATION_UPDATE_STATE_STATE);
        } else {
            description = args.getString(AddEditTodoFragment.ARGUMENT_DESCRIPTION);
            note = args.getString(AddEditTodoFragment.ARGUMENT_NOTE);
            priority = args.getInt(AddEditTodoFragment.ARGUMENT_PRIORITY);
            hasNotification = args.getBoolean(AddEditTodoFragment.ARGUMENT_HAS_NOTIFICATION);
            hasGeofenceNotification = args.getBoolean(AddEditTodoFragment.ARGUMENT_HAS_GEOFENCE_NOTIFICATION);
        }
    }

    boolean savedStateHandleContainsValues() {
        return savedStateHandle != null && savedStateHandle.contains(DESCRIPTION_STATE);
    }

    boolean isDescriptionEmpty() {
        return description.length() == 0;
    }

    String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    String getNote() {
        return note;
    }

    void setNote(String note) {
        this.note = note;
    }

    boolean hasNotification() {
        return hasNotification;
    }

    void setHasNotification(boolean hasNotification) {
        this.hasNotification = hasNotification;
    }

    boolean hasGeofenceNotification() {
        return hasGeofenceNotification;
    }

    void setHasGeofenceNotification(boolean hasGeofenceNotification) {
        this.hasGeofenceNotification = hasGeofenceNotification;
    }

    NotificationUpdateState getNotificationUpdateState() {
        return notificationUpdateState;
    }

    void setNotificationUpdateState(NotificationUpdateState notificationUpdateState) {
        this.notificationUpdateState = notificationUpdateState;
    }

    NotificationUpdateState getGeofenceNotificationUpdateState() {
        return geofenceNotificationUpdateState;
    }

    void setGeofenceNotificationUpdateState(NotificationUpdateState geofenceNotificationUpdateState) {
        this.geofenceNotificationUpdateState = geofenceNotificationUpdateState;
    }

    double getGeofenceLatitude() {
        return geofenceLatitude;
    }

    public void setGeofenceLongitude(double geofenceLongitude) {
        this.geofenceLongitude = geofenceLongitude;
    }

    double getGeofenceLongitude() {
        return geofenceLongitude;
    }

    public void setGeofenceRadius(int geofenceRadius) {
        this.geofenceRadius = geofenceRadius;
    }

    int getGeofenceRadius() {
        return geofenceRadius;
    }

    public void setGeofenceLatitude(double geofenceLatitude) {
        this.geofenceLatitude = geofenceLatitude;
    }
}
