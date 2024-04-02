package com.practicum.kanban.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

public class Task {
    protected String name;
    protected String description;
    protected Status status;
    protected LocalDateTime startTime;
    protected Duration duration;
    // Идентификатор задачи
    protected Integer id;
    // Статический номер экземпляра для генерации уникального id
    protected static int tasksCount = 0;

    public Task() {
        // автоинкрементное поле даст уникальность экземпляров
        tasksCount++;
        // простейшая генерация уникального ID
        id = tasksCount;
    }

    // Доступные конструкторы
    public Task(String name) {
        this();
        this.name = name;
        this.status = Status.NEW;
    }

    public Task(String name, String description) {
        this(name);
        this.description = description;
    }

    public Task(String name, Status status) {
        this(name);
        this.status = status;
    }

    public Task(String name, String description, Status status) {
        this(name, description);
        this.status = status;
    }

    public Task(Task in) {
        this.name = in.getName();
        this.description = in.getDescription();
        this.status = in.getStatus();
        this.setTaskId(in.getTaskId());
    }

    private Task(int id, String name, String description, Status status) {
        this(name, description, status);
        this.setTaskId(id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public Optional<LocalDateTime> getStartTime() {
        return Optional.ofNullable(startTime);
    }

    public Optional<LocalDateTime> getEndTime() {
        Optional<LocalDateTime> result = Optional.empty();
        if ((startTime != null) && (duration != null)) {
            result = Optional.of(startTime.plus(duration));
        }
        return result;
    }

    public Optional<Duration> getDuration() {
        return Optional.ofNullable(duration);
    }

    // сеттеры времени начала задачи и окончания объеденины в один, поскольку по отдельности они бессмысленны
    public void setTime(LocalDateTime start, Duration duration) {
        this.startTime = start;
        this.duration = duration;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getTaskId() {
        return id;
    }

    public void setTaskId(int id) {
        if (tasksCount < id) {
            tasksCount = id;
        }
        this.id = id;
    }

    public Task copy() {
        return new Task(this);
    }

    @Override
    public boolean equals(Object obj) {
        // сначала простое - равенство ссылок
        if (obj == (Object) this) {
            return true;
        }
        if ((obj == null) || (obj.getClass() != this.getClass())) {
            return false;
        }
        Task testingObject = (Task) obj;
        // Считаем, что идентичность идентификаторов означает идентичность задач
        if (this.getTaskId() == testingObject.getTaskId()) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return id + "," +
                "TASK," +
                name + "," +
                status + "," +
                description + ",";
    }

    public static Task fromStrings(String[] values) {
        return new Task(Integer.parseInt(values[0]), values[2], values[4], Status.fromString(values[3]));
    }
}
