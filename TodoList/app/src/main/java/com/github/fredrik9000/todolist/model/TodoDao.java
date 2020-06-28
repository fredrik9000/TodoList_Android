package com.github.fredrik9000.todolist.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TodoDao {

    @Insert
    void insert(Todo todo);

    @Insert
    void insert(List<Todo> todoList);

    @Update
    void update(Todo todo);

    @Query("UPDATE todo_table SET notificationEnabled = 0, notifyYear = 0, notifyMonth = 0, notifyDay = 0, notifyHour = 0, notifyMinute = 0 WHERE notificationId = :notificationId")
    void clearTimedNotificationValues(int notificationId);

    @Query("UPDATE todo_table SET geofenceNotificationEnabled = 0, geofenceLatitude = 0, getGeofenceLongitude = 0, geofenceRadius = 0 WHERE geofenceNotificationId = :geofenceNotificationId")
    void clearGeofenceNotificationValues(int geofenceNotificationId);

    @Query("UPDATE todo_table SET notificationId = 0 WHERE notificationId = :notificationId")
    void clearNotificationId(int notificationId);

    @Query("UPDATE todo_table SET geofenceNotificationId = 0 WHERE geofenceNotificationId = :geofenceNotificationId")
    void clearGeofenceNotificationId(int geofenceNotificationId);

    @Query("SELECT * FROM todo_table WHERE notificationId = :notificationId LIMIT 1")
    List<Todo> getTodoWithNotificationId(int notificationId);

    @Query("SELECT * FROM todo_table WHERE geofenceNotificationId = :geofenceNotificationId LIMIT 1")
    List<Todo> getTodoWithGeofenceNotificationId(int geofenceNotificationId);

    @Delete
    void delete(Todo todo);

    @Query("SELECT * FROM todo_table ORDER BY isCompleted ASC, priority DESC, description ASC")
    LiveData<List<Todo>> getAllTodos();

    @Query("SELECT * FROM todo_table WHERE description LIKE '%' || :searchValue || '%' ORDER BY isCompleted ASC, priority DESC, description ASC")
    LiveData<List<Todo>> getTodosWithText(String searchValue);

    @Query("SELECT * FROM todo_table WHERE geofenceNotificationEnabled = 1")
    List<Todo> getTodosWithGeofence();
}
