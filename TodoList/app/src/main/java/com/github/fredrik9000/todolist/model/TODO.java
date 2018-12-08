package com.github.fredrik9000.todolist.model;

import android.support.annotation.NonNull;

public class TODO implements Comparable<TODO> {
    private String description;
    private int priority, notificationId, notifyYear, notifyMonth, notifyDay, notifyHour, notifyMinute;
    private boolean hasNotification;

    public TODO() {}

    public void updateTODO(String description, int priority, int notificationId, boolean hasNotification, int notifyYear, int notifyMonth, int notifyDay, int notifyHour, int notifyMinute) {
        this.description = description;
        this.priority = priority;
        this.notificationId = notificationId;
        this.hasNotification = hasNotification;
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

    public boolean getHasNotification() {
        return hasNotification;
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

    @Override
    public int compareTo(@NonNull TODO TODO) {
        return (
            this.priority - TODO.priority < 0 ? 1
                : (
                    (this.priority - TODO.priority == 0) ? (
                        (this.description.compareTo(TODO.description) > 0) ? 1 : -1
                    ) : -1
                )
        );
    }
}
