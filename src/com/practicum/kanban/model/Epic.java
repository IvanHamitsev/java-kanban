package com.practicum.kanban.model;

import java.util.HashMap;

public class Epic extends Task {
    private HashMap<Integer, Subtask> subtasks = new HashMap<>();

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

    // Закрытый конструктор для клонирования
    protected Epic(String name, String description, Integer taskHashNumber, Status status) {
        super(name, description, taskHashNumber, status);
    }

    public HashMap<Integer, Subtask> getSubtasks() {
        return subtasks;
    }

    public void insertSubtask(Subtask subtask) {
        subtasks.put(subtask.getTaskId(), subtask);
    }

    @Override
    public Object clone() {
        Epic newEpic = new Epic(this.name, this.description, this.id, this.status);
        newEpic.subtasks = new HashMap<>();
        for (Subtask subtask : this.subtasks.values()) {
            Subtask newSub = (Subtask) subtask.clone();
            // установить ссылки в своих подзадачах
            newSub.setParentId(newSub.getTaskId());
            newEpic.subtasks.put(newSub.getTaskId(), newSub);
        }
        return newEpic;
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
