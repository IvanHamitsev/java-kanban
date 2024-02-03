package com.practicum.kanban.model;

public class Subtask extends Task {
    // id эпика, к которому принадлежит подзадача
    private Integer parentId;

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

    protected Subtask(String name, String description, Integer taskHashNumber, Status status) {
        super(name, description, taskHashNumber, status);
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    @Override
    public Object clone() {
        //Subtask newSub = (Subtask) this.clone();
        Subtask newSub = new Subtask(this.name, this.description, this.getTaskId(), this.status);
        // ссылку на эпик заполнять не надо, их проставит при создании или добавлении Epic
        newSub.parentId = null;
        return newSub;
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
