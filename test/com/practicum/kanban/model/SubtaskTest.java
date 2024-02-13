package com.practicum.kanban.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubtaskTest {
    @Test
    void canCreateSubtask() {
        Subtask subtask = new Subtask();
        Subtask subtask2 = new Subtask(subtask);
        assertTrue(subtask.equals(subtask2), "ошибка конструктора ()");

        subtask = new Subtask("Задача");
        subtask2 = new Subtask(subtask);
        assertTrue(subtask.equals(subtask2), "ошибка конструктора (Имя)");

        subtask = new Subtask("Задача", "Описание");
        subtask2 = new Subtask(subtask);
        assertTrue(subtask.equals(subtask2), "ошибка конструктора (Имя, Описание)");

        subtask = new Subtask("Задача", "Описание", Status.DONE);
        subtask2 = new Subtask(subtask);
        assertTrue(subtask.equals(subtask2), "ошибка конструктора (Имя, Описание, Статус)");

        subtask.setStatus(Status.IN_PROGRESS);
        assertTrue(subtask.equals(subtask2), "ошибка сравнения по статусу");

        subtask.setName("Другое имя");
        assertTrue(subtask.equals(subtask2), "ошибка сравнения по имени");

    }
}