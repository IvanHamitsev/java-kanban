package com.practicum.kanban;

import com.practicum.kanban.model.*;
import com.practicum.kanban.service.TaskManager;

public class Main {
    public static void main(String[] args) throws CloneNotSupportedException {
        System.out.println("Поехали!");
        TaskManager manager = new TaskManager();

        Task task1 = new Task("Задача1", "Описание1");
        Task task2 = new Task("Задача2", "Описание2", Status.DONE);

        Epic epic1 = new Epic("Эпик1", "ЭпикОписание1");
        Epic epic2 = new Epic("Эпик2", "ЭпикОписание2");

        Subtask sub1 = new Subtask("Подзад1", "ПодзадОписание1");
        Subtask sub2 = new Subtask("Подзад2", "ПодзадОписание2", Status.DONE);
        Subtask sub3 = new Subtask("Подзад3", "ПодзадОписание3", Status.IN_PROGRESS);
        Subtask sub4 = new Subtask("Подзад4", "ПодзадОписание4", Status.DONE);
        Subtask sub5 = new Subtask("Подзад5", "ПодзадОписание5", Status.DONE);
        Subtask sub6 = new Subtask("Подзад6", "ПодзадОписание6", Status.DONE);

        // надо подготовить subtask
        sub1.setParentId(epic1.getTaskId());
        sub2.setParentId(epic1.getTaskId());
        sub3.setParentId(epic2.getTaskId());
        sub6.setParentId(epic2.getTaskId());

        manager.addTask(task1);
        manager.addEpic(epic1);
        manager.addEpic(epic2);
        manager.addTask(task2);

        manager.addSubtask(sub1);
        manager.addSubtask(sub2);
        manager.addSubtask(sub3);

        // другой способ добавить подзадачу
        manager.addSubtask(epic2.getTaskId(), sub4);
        manager.addSubtask(epic2.getTaskId(), sub5);
        manager.addSubtask(epic2.getTaskId(), sub6);

        // добавить идентичную подзадачу
        manager.addSubtask(epic2.getTaskId(), sub1);

        System.out.println(manager.getTask(task2.getTaskId()));
        System.out.println(manager);

        // удалить задачу
        manager.deleteTask(task1.getTaskId());
        // удалить целиком все задачи
        manager.deleteAllTasks();

        // удалить подзадачи, влияя на статус эпика
        manager.deleteSubtask(epic1.getTaskId(), sub1.getTaskId());
        manager.deleteSubtask(sub1.getTaskId()); // теперь подзадача sub1 найдётся в epic2
        manager.deleteSubtask(sub3);

        System.out.println(manager);

        // поменяем статус подзадачи
        sub5.setStatus(Status.IN_PROGRESS);
        sub6.setStatus(Status.IN_PROGRESS);
        // вот так не получится - у sub5 не заполнен parentId
        manager.updateSubtask(sub5);
        // вот так получится - у sub6 заполнен parentId
        manager.updateSubtask(sub6);

        System.out.println(manager.getEpic(epic2.getTaskId()));

        // целиком удалить эпик
        manager.deleteEpic(100); // попробуем неверный id
        manager.deleteEpic(epic1.getTaskId()); // верный id

        // удалить подзадачи эпика
        manager.deleteSubtasks(epic2.getTaskId());

        System.out.println(manager);
    }
}
