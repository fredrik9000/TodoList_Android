package com.github.fredrik9000.todolist.model;

import android.support.annotation.NonNull;

public class Chore implements Comparable<Chore> {
    private String title;
    private int priority;

    public Chore(String title, int priority) {
        this.title = title;
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public int compareTo(@NonNull Chore chore) {
        return (
            this.priority - chore.priority < 0 ? 1
                : (
                    (this.priority - chore.priority == 0) ? (
                        (this.title.compareTo(chore.title) > 0) ? 1 : -1
                    ) : -1
                )
        );
    }
}
