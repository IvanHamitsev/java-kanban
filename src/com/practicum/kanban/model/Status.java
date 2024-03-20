package com.practicum.kanban.model;

public enum Status {
    NEW("NEW"),
    IN_PROGRESS("IN_PROGRESS"),
    DONE("DONE");

    private String title;

    Status(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return title;
    }

    public static Status fromString(String value) {
        if (value.equals("NEW")) return NEW;
        if (value.equals("IN_PROGRESS")) return IN_PROGRESS;
        if (value.equals("DONE")) return DONE;
        return NEW;
    }
}
