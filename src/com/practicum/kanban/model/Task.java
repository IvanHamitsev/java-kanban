package com.practicum.kanban.model;

public class Task {
    protected String name;
    protected String description;
    protected Status status;
    // Идентификатор задачи
    protected Integer id;
    // Статический номер экземпляра для генерации уникального id
    private static int tasksCount = 0;

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
        this.id = in.getTaskId();
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

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getTaskId() {
        return id;
    }

    public void setTaskId(int taskHashNumber) {
        this.id = taskHashNumber;
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
        if (this.id == testingObject.id) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                " '" + name + '\'' +
                " " + status + " }";
    }
}
