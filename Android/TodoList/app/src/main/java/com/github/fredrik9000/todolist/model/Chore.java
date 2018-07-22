package com.github.fredrik9000.todolist.model;

import android.support.annotation.NonNull;

public class Chore implements Comparable<Chore> {
    private String description;
    private int priority;

    public Chore(String description, int priority) {
        this.description = description;
        this.priority = priority;
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

    @Override
    public int compareTo(@NonNull Chore chore) {
        return (
            this.priority - chore.priority < 0 ? 1
                : (
                    (this.priority - chore.priority == 0) ? (
                        (this.description.compareTo(chore.description) > 0) ? 1 : -1
                    ) : -1
                )
        );
    }
}
