package com.practicum.kanban.model;

import java.time.Duration;
import java.time.LocalDateTime;

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

    public Subtask(String name, String description, LocalDateTime startTime, Duration duration) {
        super(name, description);
        this.startTime = startTime;
        this.duration = duration;
    }

    public Subtask(String name, String description, LocalDateTime startTime, long duration) {
        super(name, description);
        this.startTime = startTime;
        this.duration = Duration.ofMinutes(duration);
    }

    private Subtask(int id, String name, String description, Status status, LocalDateTime startTime, long duration, int parentId) {
        this(name, description, status);
        this.setTaskId(id);
        this.setParentId(parentId);
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

    private Subtask(int id, String name, String description, Status status, int parentId, LocalDateTime startTime, long duration) {
        this(name, description, status);
        this.setTaskId(id);
        this.setParentId(parentId);
        this.startTime = startTime;
        this.duration = Duration.ofMinutes(duration);
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
        String startTime = "0";
        long duration = 0;
        if (this.startTime != null) {
            startTime = this.startTime.toString();
            duration = this.duration.toMinutes();
        }
        return "SUBTASK," +
                name + "," +
                status + "," +
                startTime + "," +
                duration;
    }

    public String toFileString() {
        String startTime = "0";
        long duration = 0;
        if (this.startTime != null) {
            startTime = this.startTime.toString();
            duration = this.duration.toMinutes();
        }
        return id + "," +
                "SUBTASK," +
                name + "," +
                status + "," +
                startTime + "," +
                duration + "," +
                description + "," +
                parentId;
    }

    public static Subtask fromString(String[] values) {
        if ((null != values) && (5 <= values.length)) {
            if (values[4].equals("0")) {
                return new Subtask(Integer.parseInt(values[0]), values[2], values[6], Status.fromString(values[3]), Integer.parseInt(values[7]));
            } else {
                return new Subtask(Integer.parseInt(values[0]), values[2], values[6], Status.fromString(values[3]), Integer.parseInt(values[7]),
                        LocalDateTime.parse(values[4]), Long.parseLong(values[5]));
            }
        } else {
            throw new IllegalArgumentException("Параметры " + values +
                    " не могут быть использованы в качестве описания новой Subtask");
        }
    }
}
