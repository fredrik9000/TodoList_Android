package com.github.fredrik9000.todolist.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TodoDao {
    @Insert
    fun insert(todo: Todo)

    @Insert
    fun insert(todoList: MutableList<Todo>)

    @Update
    fun update(todo: Todo)

    @Query("UPDATE todo_table SET notificationEnabled = 0, notifyYear = 0, notifyMonth = 0, notifyDay = 0, notifyHour = 0, notifyMinute = 0 WHERE notificationId = :notificationId")
    fun clearTimedNotificationValues(notificationId: Int)

    @Query("UPDATE todo_table SET geofenceNotificationEnabled = 0, geofenceLatitude = 0, geofenceLongitude = 0, geofenceRadius = 0 WHERE geofenceNotificationId = :geofenceNotificationId")
    fun clearGeofenceNotificationValues(geofenceNotificationId: Int)

    @Query("UPDATE todo_table SET notificationId = 0 WHERE notificationId = :notificationId")
    fun clearNotificationId(notificationId: Int)

    @Query("UPDATE todo_table SET geofenceNotificationId = 0 WHERE geofenceNotificationId = :geofenceNotificationId")
    fun clearGeofenceNotificationId(geofenceNotificationId: Int)

    @Query("SELECT * FROM todo_table WHERE notificationId = :notificationId LIMIT 1")
    fun getTodoWithNotificationId(notificationId: Int): MutableList<Todo>

    @Query("SELECT * FROM todo_table WHERE geofenceNotificationId = :geofenceNotificationId LIMIT 1")
    fun getTodoWithGeofenceNotificationId(geofenceNotificationId: Int): MutableList<Todo>

    @Delete
    fun delete(todo: Todo)

    @Query("SELECT * FROM todo_table ORDER BY isCompleted ASC, priority DESC, description ASC")
    fun getAllTodos(): LiveData<MutableList<Todo>>

    @Query("SELECT * FROM todo_table WHERE description LIKE '%' || :searchValue || '%' ORDER BY isCompleted ASC, priority DESC, description ASC")
    fun getTodosWithText(searchValue: String?): LiveData<MutableList<Todo>>

    @Query("SELECT * FROM todo_table WHERE geofenceNotificationEnabled = 1")
    fun getTodosWithGeofence(): MutableList<Todo>
}