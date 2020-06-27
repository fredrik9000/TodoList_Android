package com.github.fredrik9000.todolist.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "todo_table")
public class Todo {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String description;
    private String note;
    private int priority;
    private int notificationId;
    private int notifyYear, notifyMonth, notifyDay, notifyHour, notifyMinute;
    private boolean notificationEnabled;
    private int geofenceNotificationId;
    private double geofenceLatitude, getGeofenceLongitude;
    private int geofenceRadius;
    private boolean geofenceNotificationEnabled;
    private boolean isCompleted;

    public Todo() {
    }

    public Todo(String description, String note, int priority, int notificationId, int geofenceNotificationId, boolean hasNotification, int notifyYear, int notifyMonth, int notifyDay, int notifyHour, int notifyMinute, boolean hasGeofenceNotification, double geofenceLatitude, double getGeofenceLongitude, int geofenceRadius, boolean isCompleted) {
        this.description = description;
        this.note = note;
        this.priority = priority;
        this.notificationId = notificationId;
        this.geofenceNotificationId = geofenceNotificationId;
        this.notificationEnabled = hasNotification;
        this.notifyYear = notifyYear;
        this.notifyMonth = notifyMonth;
        this.notifyDay = notifyDay;
        this.notifyHour = notifyHour;
        this.notifyMinute = notifyMinute;
        this.isCompleted = isCompleted;
        this.geofenceNotificationEnabled = hasGeofenceNotification;
        this.geofenceLatitude = geofenceLatitude;
        this.getGeofenceLongitude = getGeofenceLongitude;
        this.geofenceRadius = geofenceRadius;
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

    public boolean isGeofenceNotificationEnabled() {
        return geofenceNotificationEnabled;
    }

    public void setGeofenceNotificationEnabled(boolean geofenceNotificationEnabled) {
        this.geofenceNotificationEnabled = geofenceNotificationEnabled;
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public int getGeofenceNotificationId() {
        return geofenceNotificationId;
    }

    public void setGeofenceNotificationId(int geofenceNotificationId) {
        this.geofenceNotificationId = geofenceNotificationId;
    }

    public double getGeofenceLatitude() {
        return geofenceLatitude;
    }

    public void setGeofenceLatitude(double geofenceLatitude) {
        this.geofenceLatitude = geofenceLatitude;
    }

    public double getGetGeofenceLongitude() {
        return getGeofenceLongitude;
    }

    public void setGetGeofenceLongitude(double getGeofenceLongitude) {
        this.getGeofenceLongitude = getGeofenceLongitude;
    }

    public int getGeofenceRadius() {
        return geofenceRadius;
    }

    public void setGeofenceRadius(int geofenceRadius) {
        this.geofenceRadius = geofenceRadius;
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
