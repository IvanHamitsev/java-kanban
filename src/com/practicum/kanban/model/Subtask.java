package com.practicum.kanban.model;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
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

    private Subtask(int id, String name, String description, Status status, int parentId, long startTime, long duration) {
        this(name, description, status);
        this.setTaskId(id);
        this.setParentId(parentId);
        this.startTime = LocalDateTime.from(Instant.ofEpochMilli(startTime));
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
        long startTime = 0;
        long duration = 0;
        if (this.startTime != null) {
            startTime = Timestamp.valueOf(this.startTime).getTime();
            duration = this.duration.toMinutes();
        }
        return id + "," +
                "SUBTASK," +
                name + "," +
                status + "," +
                startTime  + "," +
                duration  + "," +
                description + "," +
                parentId;
    }

    public static Subtask fromString(String[] values) {
        if (Long.parseLong(values[4]) > 0) {
            return new Subtask(Integer.parseInt(values[0]), values[2], values[6], Status.fromString(values[3]), Integer.parseInt(values[7]),
                    Long.parseLong(values[4]), Long.parseLong(values[5]));
        } else {
            return new Subtask(Integer.parseInt(values[0]), values[2], values[6], Status.fromString(values[3]), Integer.parseInt(values[7]));
        }
    }
}
