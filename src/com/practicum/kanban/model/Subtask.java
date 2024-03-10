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

    public Subtask(Subtask in) {
        super(in);
        this.parentId = in.getParentId();
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
        return "Subtask{" +
                "id=" + id +
                ", parentId=" + parentId +
                " '" + name + '\'' +
                ", " + status +
                " } ";
    }
}
