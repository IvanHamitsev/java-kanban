package com.practicum.kanban;

import com.practicum.kanban.model.*;
import com.practicum.kanban.service.*;

import java.time.Duration;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        System.out.println("Поехали!");

        TaskManager taskManager = Managers.getFileTaskManager("kanban.csv");

        LocalDateTime dateTime = LocalDateTime.of(2024, 4, 6, 12, 0);

        Task task1 = new Task("Задача1", "Описание1", dateTime, Duration.ofMinutes(16));
        // время задачи пересекается
        Task task2 = new Task("Задача2", "Описание2", dateTime.plus(Duration.ofMinutes(15)), Duration.ofMinutes(15));
        // начнём через пару часиков: делов то на минуту
        Task task3 = new Task("Задача3", "Описание3", dateTime.plus(Duration.ofHours(2)), Duration.ofMinutes(1));

        Epic epic1 = new Epic("Эпик1", "ЭпикОписание1");
        Epic epic2 = new Epic("Эпик2", "ЭпикОписание2");
        Epic epic3 = new Epic("Эпик3", "ЭпикОписание3");

        Subtask sub1 = new Subtask("Подзад1", "ПодзадОписание1", dateTime.plus(Duration.ofHours(3)), Duration.ofMinutes(60));
        Subtask sub2 = new Subtask("Подзад2", "ПодзадОписание2", dateTime.plus(Duration.ofHours(4)), Duration.ofMinutes(60));
        Subtask sub3 = new Subtask("Подзад3", "ПодзадОписание3", dateTime.plus(Duration.ofHours(5)), Duration.ofMinutes(60));
        Subtask sub4 = new Subtask("Подзад4", "ПодзадОписание4", dateTime.plus(Duration.ofHours(6)), Duration.ofMinutes(60));
        Subtask sub5 = new Subtask("Подзад5", "ПодзадОписание5", dateTime.plus(Duration.ofHours(7)), Duration.ofMinutes(60));
        Subtask sub6 = new Subtask("Подзад6", "ПодзадОписание6", dateTime.plus(Duration.ofHours(8)), Duration.ofMinutes(60));

        int task1Id = taskManager.addTask(task1);
        int task2Id = taskManager.addTask(task2);
        int task3Id = taskManager.addTask(task3);

        int epic1Id = taskManager.addEpic(epic1);
        int epic2Id = taskManager.addEpic(epic2);
        int epic3Id = taskManager.addEpic(epic3);

        // надо подготовить subtask
        sub1.setParentId(epic1Id);
        sub2.setParentId(epic1Id);
        sub3.setParentId(epic1Id);

        sub4.setParentId(epic2Id);
        sub5.setParentId(epic2Id);
        sub6.setParentId(epic2Id);

        int sub1Id = taskManager.addSubtask(sub1);
        int sub2Id = taskManager.addSubtask(sub2);
        int sub3Id = taskManager.addSubtask(sub3);
        int sub4Id = taskManager.addSubtask(sub4);
        int sub5Id = taskManager.addSubtask(sub5);
        int sub6Id = taskManager.addSubtask(sub6);

        // спросим задачи, подзадачи, эпик
        taskManager.getEpic(epic1Id);
        taskManager.getTask(task1Id);
        taskManager.getTask(task2Id);
        taskManager.getSubtask(sub1Id);
        taskManager.getSubtask(sub2Id);
        taskManager.getSubtask(sub1Id);
        taskManager.getEpic(epic2Id);

        System.out.println("История после запросов с повтором");
        for (Task task : taskManager.getHistory()) {
            System.out.println(task);
        }

        // поменяем статус подзадачи
        sub5.setStatus(Status.IN_PROGRESS);
        sub6.setStatus(Status.DONE);
        taskManager.updateSubtask(sub5);
        taskManager.updateSubtask(sub6);

        System.out.println("Эпик поменял статус");
        System.out.println(taskManager.getEpic(epic2Id));

        System.out.println("Список задач в порядке приоритета");
        System.out.println(taskManager.getPrioritizedTasks());
    }
}
