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

    public void updateTODO(String description, int priority, int notificationId, boolean hasNotification, int notifyYear, int notifyMonth, int notifyDay, int notifyHour, int notifyMinute) {
        this.description = description;
        this.priority = priority;
        this.notificationId = notificationId;
        this.notificationEnabled = hasNotification;
        this.notifyYear = notifyYear;
        this.notifyMonth = notifyMonth;
        this.notifyDay = notifyDay;
        this.notifyHour = notifyHour;
        this.notifyMinute = notifyMinute;
    }

    public int getNotifyYear() {
        return notifyYear;
    }

    public int getNotifyMonth() {
        return notifyMonth;
    }

    public int getNotifyDay() {
        return notifyDay;
    }

    public int getNotifyHour() {
        return notifyHour;
    }

    public int getNotifyMinute() {
        return notifyMinute;
    }

    public boolean isNotificationEnabled() {
        return notificationEnabled;
    }

    public int getPriority() {
        return priority;
    }

    public String getDescription() {
        return description;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public void setNotifyYear(int notifyYear) {
        this.notifyYear = notifyYear;
    }

    public void setNotifyMonth(int notifyMonth) {
        this.notifyMonth = notifyMonth;
    }

    public void setNotifyDay(int notifyDay) {
        this.notifyDay = notifyDay;
    }

    public void setNotifyHour(int notifyHour) {
        this.notifyHour = notifyHour;
    }

    public void setNotifyMinute(int notifyMinute) {
        this.notifyMinute = notifyMinute;
    }

    public void setNotificationEnabled(boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }
}
