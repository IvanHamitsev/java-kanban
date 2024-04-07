package com.practicum.kanban.model;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

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

    public Epic(String name, String description, LocalDateTime startTime, Duration duration) {
        super(name, description, startTime, duration);
    }

    public Epic(String name, Status status) {
        super(name, status);
    }

    public Epic(String name, String description, Status status) {
        super(name, description, status);
    }

    public Epic(Epic in) {
        super(in);
        if (null != this.startTime) {
            this.endTime = this.startTime.plus(this.duration);
        }
        in.getSubtasks().values().stream()
                .forEach(subtask -> {
                    // добавить в this Map копию subtask
                    Subtask newSubtask = subtask.copy();
                    this.subtasks.put(newSubtask.getTaskId(), newSubtask);
                });
    }

    private Epic(int id, String name, String description, Status status) {
        this(name, description, status);
        this.setTaskId(id);
    }

    private Epic(String name, String description, LocalDateTime startTime, long duration) {
        this(name, description);
        this.startTime = startTime;
        this.duration = Duration.ofMinutes(duration);
        this.endTime = startTime.plus(this.duration);
    }

    private Epic(int id, String name, String description, Status status, LocalDateTime startTime, long duration) {
        this(name, description, startTime, duration);
        this.setTaskId(id);
        this.setStatus(status);
    }

    public HashMap<Integer, Subtask> getSubtasks() {
        return subtasks;
    }

    public void insertSubtask(Subtask subtask) {
        subtasks.put(subtask.getTaskId(), subtask);
    }

    @Override
    public Optional<LocalDateTime> getEndTime() {
        return Optional.ofNullable(endTime);
    }

    // применяется при удалении всех подзадач
    public void clearTime() {
        this.startTime = null;
        this.duration = null;
        this.endTime = null;
    }

    // расширяющее обновление времени Epic - простое сравнение со старыми значениями
    public void expandingTimeUpdate(LocalDateTime start, LocalDateTime finish) {
        // возможно эпик ещё не получил времени старта/окончания
        if (this.startTime == null) {
            this.startTime = start;
            this.endTime = finish;

        } else {
            if (start.isBefore(this.startTime)) {
                this.startTime = start;
            }
            if (finish.isAfter(this.endTime)) {
                this.endTime = finish;
            }
        }
        this.duration = Duration.between(this.startTime, this.endTime);
    }

    // сужающее обновление времени Epic требует перебора всех подзадач
    public void reduceTimeUpdate() {
        // значения времени будут пересчитаны, начать с null значений
        clearTime();
        // перебираем все подзадачи с помощью stream
        this.subtasks.values().stream()
                // взять первое notNull значение времени подзадачи как начальное
                .peek((Subtask s) -> {
                    if (s.getStartTime().isPresent() && this.startTime == null) {
                        this.startTime = s.getStartTime().get();
                        this.endTime = this.startTime.plus(s.getDuration().get());
                    }
                })
                // проверить время старта/окончания подзадачи на предмент возможно актуального для Epic
                .forEach((Subtask s) -> {
                    if (s.getStartTime().isPresent() && s.getStartTime().get().isBefore(this.startTime)) {
                        this.startTime = s.getStartTime().get();
                    }
                    if (s.getEndTime().isPresent() && s.getEndTime().get().isAfter(this.endTime)) {
                        this.endTime = s.getEndTime().get();
                    }
                });
        // не забываем, что возможно не осталось задач, привязаных ко времени
        if (this.startTime != null) {
            this.duration = Duration.between(this.startTime, this.endTime);
        }
    }

    @Override
    public Epic copy() {
        return new Epic(this);
    }

    @Override
    public String toString() {
        String startTime = "0";
        long duration = 0;
        if (this.startTime != null) {
            startTime = this.startTime.toString();
            duration = this.duration.toMinutes();

            //LocalDateTime ldt = LocalDateTime.parse(startTime);
            //System.out.println(ldt);
        }
        return "EPIC," +
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

            //LocalDateTime ldt = LocalDateTime.parse(startTime);
            //System.out.println(ldt);
        }
        return id + "," +
                "EPIC," +
                name + "," +
                status + "," +
                startTime + "," +
                duration + "," +
                description + ",";
    }

    public static Epic fromStrings(String[] values) {
        if (values[4].equals("0")) {
            return new Epic(Integer.parseInt(values[0]), values[2], values[6], Status.fromString(values[3]));
        } else {
            return new Epic(Integer.parseInt(values[0]), values[2], values[6], Status.fromString(values[3]), LocalDateTime.parse(values[4]), Long.parseLong(values[5]));
        }
    }
}
