package com.practicum.kanban.model;

import java.time.LocalDateTime;
import java.util.HashMap;

public class Epic extends Task {
    private HashMap<Integer, Subtask> subtasks = new HashMap<>();

    protected LocalDateTime endTime;

    public Epic() {
        super();
    }

    public Epic(String name) {
        super(name);
    }

    public Epic(String name, String description) {
        super(name, description);
    }

    public Epic(String name, Status status) {
        super(name, status);
    }

    public Epic(String name, String description, Status status) {
        super(name, description, status);
    }

    public Epic(Epic in) {
        super(in);
        for (Subtask subtask : in.getSubtasks().values()) {
            // добавить в наш Map копию subtask
            Subtask newSubtask = subtask.copy();
            subtasks.put(newSubtask.getTaskId(), newSubtask);
        }
    }

    private Epic(int id, String name, String description, Status status) {
        this(name, description, status);
        this.setTaskId(id);
    }

    public HashMap<Integer, Subtask> getSubtasks() {
        return subtasks;
    }

    public void insertSubtask(Subtask subtask) {
        subtasks.put(subtask.getTaskId(), subtask);
    }

    @Override
    public Epic copy() {
        return new Epic(this);
    }

    @Override
    public String toString() {
        return id + "," +
                "EPIC," +
                name + "," +
                status + "," +
                description + ",";
    }

    public static Epic fromStrings(String[] values) {
        return new Epic(Integer.parseInt(values[0]), values[2], values[4], Status.fromString(values[3]));
    }
}
