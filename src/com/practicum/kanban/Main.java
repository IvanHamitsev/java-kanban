package com.practicum.kanban;

import com.practicum.kanban.model.*;
import com.practicum.kanban.service.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("Поехали!");

        TaskManager taskManager = Managers.getDefault();

        Task task1 = new Task("Задача1", "Описание1");
        Task task2 = new Task("Задача2", "Описание2", Status.DONE);

        Epic epic1 = new Epic("Эпик1", "ЭпикОписание1");
        Epic epic2 = new Epic("Эпик2", "ЭпикОписание2");
        Epic epic3 = new Epic("Эпик3", "ЭпикОписание3");

        Subtask sub1 = new Subtask("Подзад1", "ПодзадОписание1");
        Subtask sub2 = new Subtask("Подзад2", "ПодзадОписание2", Status.DONE);
        Subtask sub3 = new Subtask("Подзад3", "ПодзадОписание3", Status.IN_PROGRESS);
        Subtask sub4 = new Subtask("Подзад4", "ПодзадОписание4", Status.DONE);
        Subtask sub5 = new Subtask("Подзад5", "ПодзадОписание5", Status.DONE);
        Subtask sub6 = new Subtask("Подзад6", "ПодзадОписание6", Status.DONE);

        int task1Id = taskManager.addTask(task1);
        int epic1Id = taskManager.addEpic(epic1);
        int epic2Id = taskManager.addEpic(epic2);
        int task2Id = taskManager.addTask(task2);
        int epic3Id = taskManager.addEpic(epic3);

        // надо подготовить subtask
        sub1.setParentId(epic1Id);
        sub2.setParentId(epic1Id);
        sub3.setParentId(epic2Id);
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
        taskManager.getTask(task2Id);
        taskManager.getSubtask(sub2Id);
        taskManager.getSubtask(sub2Id);
        taskManager.getEpic(epic2Id);
        System.out.println("История после запросов с повтором");
        for (Task task : taskManager.getHistory()) {
            System.out.println(task);
        }
        // спросим то же самое и в другом порядке
        taskManager.getEpic(epic2Id);
        taskManager.getSubtask(sub2Id);
        taskManager.getTask(task2Id);
        System.out.println();
        System.out.println("История после запросов в другом порядке");
        for (Task task : taskManager.getHistory()) {
            System.out.println(task);
        }

        // удалить задачу
        taskManager.deleteTask(task1Id);
        // удалить целиком все задачи
        taskManager.deleteAllTasks();

        // удалить подзадачи, влияя на статус эпика
        taskManager.deleteSubtask(sub1Id);
        taskManager.deleteSubtask(sub3Id);

        System.out.println();
        System.out.println(taskManager);

        // поменяем статус подзадачи
        sub5.setStatus(Status.IN_PROGRESS);
        sub6.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubtask(sub5);
        taskManager.updateSubtask(sub6);

        System.out.println();
        System.out.println(taskManager.getEpic(epic2Id));

        // удалить подзадачи эпика
        taskManager.deleteSubtasks(epic2Id);

        // удалить последнюю подзадачу эпика
        taskManager.deleteSubtask(sub2Id);

        System.out.println();
        System.out.println(taskManager);

        System.out.println();
        System.out.println("История после удалений");
        for (Task task : taskManager.getHistory()) {
            System.out.println(task);
        }
    }
}
