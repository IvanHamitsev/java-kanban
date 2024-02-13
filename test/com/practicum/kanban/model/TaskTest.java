package com.practicum.kanban.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskTest {
    @Test
    void canCreateTask() {
        Task task = new Task();
        Task task2 = new Task(task);
        assertTrue(task.equals(task2), "ошибка конструктора ()");

        task = new Task("Задача");
        task2 = new Task(task);
        assertTrue(task.equals(task2), "ошибка конструктора (Имя)");

        task = new Task("Задача", "Описание");
        task2 = new Task(task);
        assertTrue(task.equals(task2), "ошибка конструктора (Имя, Описание)");

        task = new Task("Задача", "Описание", Status.DONE);
        task2 = new Task(task);
        assertTrue(task.equals(task2), "ошибка конструктора (Имя, Описание, Статус)");

        task.setStatus(Status.IN_PROGRESS);
        assertTrue(task.equals(task2), "ошибка сравнения по статусу");

        task.setName("Другое имя");
        assertTrue(task.equals(task2), "ошибка сравнения по имени");

    }

}