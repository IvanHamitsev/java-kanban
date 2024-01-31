class Task implements Cloneable {
    String name;
    String description;
    // Возложим задачу контроля уникальности генерации номера задачи на сам класс Task.
    // Пусть помнит число созданных задач и генерит из него taskHashNumber
    private static int tasksCount = 0;
    int taskHashNumber;
    Status status;

    public Task() {
        if (tasksCount == 0) {
            // начнём с непростого числа
            tasksCount = this.hashCode();
        } else {
            tasksCount++;
        }
        // начнём с простейшего принципа генерации уникального taskHashNumber
        taskHashNumber = tasksCount;
    }

    // Обычный конструктор при создании новой задачи
    public Task(String name) {
        this();
        this.name = name;
        this.status = Status.NEW;
    }

    // Особый конструктор при клонировании
    public Task(String name, String description, int taskHashNumber, Status status) {
        // всё равно надо увеличивать счётчик созданных задач
        this();
        this.name = name;
        this.description = description;
        this.taskHashNumber = taskHashNumber;
        this.status = status;
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
}
