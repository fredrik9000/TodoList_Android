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

    @Query("UPDATE todo_table SET notificationEnabled = 0, notificationId = 0, notifyYear = 0, notifyMonth = 0, notifyDay = 0, notifyHour = 0, notifyMinute = 0 WHERE notificationId = :notificationId")
    void disableNotificationWithId(int notificationId);

    @Delete
    void delete(Todo todo);

    @Query("SELECT * FROM todo_table ORDER BY isCompleted ASC, priority DESC, description ASC")
    LiveData<List<Todo>> getAllTodos();
}
