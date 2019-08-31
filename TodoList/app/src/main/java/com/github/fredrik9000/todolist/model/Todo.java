package com.github.fredrik9000.todolist.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "todo_table")
public class Todo {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String description;
    private int priority, notificationId, notifyYear, notifyMonth, notifyDay, notifyHour, notifyMinute;
    private boolean notificationEnabled;
    private boolean isCompleted;

    public Todo() {
    }

    public Todo(String description, int priority, int notificationId, boolean hasNotification, int notifyYear, int notifyMonth, int notifyDay, int notifyHour, int notifyMinute, boolean isCompleted) {
        this.description = description;
        this.priority = priority;
        this.notificationId = notificationId;
        this.notificationEnabled = hasNotification;
        this.notifyYear = notifyYear;
        this.notifyMonth = notifyMonth;
        this.notifyDay = notifyDay;
        this.notifyHour = notifyHour;
        this.notifyMinute = notifyMinute;
        this.isCompleted = isCompleted;
    }

    public int getNotifyYear() {
        return notifyYear;
    }

    public void setNotifyYear(int notifyYear) {
        this.notifyYear = notifyYear;
    }

    public int getNotifyMonth() {
        return notifyMonth;
    }

    public void setNotifyMonth(int notifyMonth) {
        this.notifyMonth = notifyMonth;
    }

    public int getNotifyDay() {
        return notifyDay;
    }

    public void setNotifyDay(int notifyDay) {
        this.notifyDay = notifyDay;
    }

    public int getNotifyHour() {
        return notifyHour;
    }

    public void setNotifyHour(int notifyHour) {
        this.notifyHour = notifyHour;
    }

    public int getNotifyMinute() {
        return notifyMinute;
    }

    public void setNotifyMinute(int notifyMinute) {
        this.notifyMinute = notifyMinute;
    }

    public boolean isNotificationEnabled() {
        return notificationEnabled;
    }

    public void setNotificationEnabled(boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
