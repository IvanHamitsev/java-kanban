class Task implements Cloneable {
    String name;
    String description;
    Status status;
    // Возложим задачу контроля уникальности генерации номера задачи на сам класс Task.
    // Пусть помнит число созданных задач и генерит из него taskHashNumber
    private static int tasksCount = 0;
    private int taskHashNumber;
    public Task() {
        if (tasksCount == 0) {
            // начнём с непростого числа (зачем?)
            tasksCount = this.hashCode();
        } else {
            tasksCount++;
        }
        // начнём с простейшего принципа генерации уникального taskHashNumber
        taskHashNumber = tasksCount;
    }

    // Обычные конструкторы при создании новой задачи
    public Task(String name) {
        this();
        this.name = name;
        this.status = Status.NEW;
    }
    public Task(String name, String description) {
        this(name);
        this.description = description;
    }

    public Task(String name, String description, Status status) {
        this(name, description);
        this.status = status;
    }

    // Особый конструктор, только при клонировании. Только он умеет создать экземпляр Task с идентичным taskHashNumber
    protected Task(String name, String description, int taskHashNumber, Status status) {
        // всё равно надо увеличивать счётчик созданных задач
        this();
        this.name = name;
        this.description = description;
        this.taskHashNumber = taskHashNumber;
        this.status = status;
    }

    public int getTaskHashNumber() {
        return taskHashNumber;
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
        // начнём с простой гипотезы - каждая новая задача уникальна
        if (this.taskHashNumber == testingObject.taskHashNumber) {
            return true;
        }
        return false;
    }
    @Override
    protected Object clone() {
        Task task = new Task(this.name, this.description, this.taskHashNumber, this.status);
        return task;
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", taskHashNumber=" + taskHashNumber +
                " "+status +" }\n";
    }
}
