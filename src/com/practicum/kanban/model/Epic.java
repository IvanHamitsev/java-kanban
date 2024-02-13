package com.practicum.kanban.model;

import java.util.HashMap;

public class Epic extends Task {
    private HashMap<Integer, Subtask> subtasks = new HashMap<>();
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
            subtasks.put(subtask.getTaskId(), subtask);
        }
    }

    public HashMap<Integer, Subtask> getSubtasks() {
        return subtasks;
    }

    public void insertSubtask(Subtask subtask) {
        subtasks.put(subtask.getTaskId(), subtask);
    }

    @Override
    public String toString() {
        String res = "Epic{" +
                "id=" + id +
                " '" + name + "' ";
        for (Task task : subtasks.values()) {
            res = res.concat(task.toString());
        }
        res = res.concat(" " + status + " }\n");
        return res;
    }
}
