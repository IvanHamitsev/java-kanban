package com.practicum.kanban.model;

public class Subtask extends Task {
    // id эпика, к которому принадлежит подзадача
    private Integer parentId;

    public Subtask() {
        super();
    }

    public Subtask(String name) {
        super(name);
    }

    public Subtask(String name, String description) {
        super(name, description);
    }

    public Subtask(String name, Status status) {
        super(name, status);
    }

    public Subtask(String name, String description, Status status) {
        super(name, description, status);
    }

    private Subtask(int id, String name, String description, Status status, int parentId) {
        this(name, description, status);
        this.setTaskId(id);
        this.setParentId(parentId);
    }

    public Subtask(Subtask in) {
        super(in);
        this.setParentId(in.getParentId());
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    @Override
    public Subtask copy() {
        return new Subtask(this);
    }

    @Override
    public String toString() {
        return id + "," +
                "SUBTASK," +
                name + "," +
                status + "," +
                description + "," +
                parentId;
    }

    public static Subtask fromString(String[] values) {
        return new Subtask(Integer.parseInt(values[0]), values[2], values[4], Status.fromString(values[3]), Integer.parseInt(values[5]));
    }
}
