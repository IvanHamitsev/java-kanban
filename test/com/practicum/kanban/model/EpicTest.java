package com.practicum.kanban.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EpicTest {
    @Test
    void canCreateEpic() {
        Epic epic = new Epic();
        Epic subtask2 = new Epic(epic);
        assertTrue(epic.equals(subtask2), "ошибка конструктора ()");

        epic = new Epic("Эпик");
        subtask2 = new Epic(epic);
        assertTrue(epic.equals(subtask2), "ошибка конструктора (Имя)");

        epic = new Epic("Эпик", "Описание");
        subtask2 = new Epic(epic);
        assertTrue(epic.equals(subtask2), "ошибка конструктора (Имя, Описание)");

        epic = new Epic("Эпик", "Описание", Status.DONE);
        subtask2 = new Epic(epic);
        assertTrue(epic.equals(subtask2), "ошибка конструктора (Имя, Описание, Статус)");

        epic.setStatus(Status.IN_PROGRESS);
        assertTrue(epic.equals(subtask2), "ошибка сравнения по статусу");

        epic.setName("Другое имя");
        assertTrue(epic.equals(subtask2), "ошибка сравнения по имени");
    }
}