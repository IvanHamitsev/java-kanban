public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");
        TaskManager manager = new TaskManager();

        Task task1 = new Task("Задача 1", "Описание1");
        Task task2 = new Task("Задача 2", "Описание2", Status.DONE);

        Epic epic1 = new Epic("Эпик1", "ЭпикОписание1");
        Epic epic2 = new Epic("Эпик2", "ЭпикОписание2");

        Subtask sub1 = new Subtask("Подзад1", "ПодзадОписание1");
        Subtask sub2 = new Subtask("Подзад2", "ПодзадОписание2", Status.DONE);
        Subtask sub3 = new Subtask("Подзад3", "ПодзадОписание3", Status.DONE);

        // некрасиво, но надо правильно подготовить subtask, чтобы не делать epic1.addSubtask(sub1) в обход manager
        sub1.setParentHashNumber(epic1.getTaskHashNumber());
        sub2.setParentHashNumber(epic1.getTaskHashNumber());
        sub3.setParentHashNumber(epic2.getTaskHashNumber());

        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(epic1);
        manager.addTask(epic2);
        manager.addTask(sub1);
        manager.addTask(sub2);
        manager.addTask(sub3);

        System.out.println(manager);

        System.out.println(manager.getTask(557041917));

        // попробуем целиком удалить эпик
        //manager.delTask(557041914);

        // удалить подзадачи, влияя на статус эпика
        manager.delTask(557041916);
        manager.delTask(557041918);

        System.out.println(manager);
    }
}
