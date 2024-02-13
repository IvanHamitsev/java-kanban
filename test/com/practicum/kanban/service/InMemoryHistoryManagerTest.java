package com.practicum.kanban.service;

import com.practicum.kanban.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    static TaskManager taskManager;
    @BeforeAll
    static void prepareManager()
    {
        Managers manager = new Managers();
        taskManager = manager.getDefault();
    }
    @AfterEach
    void clearManager() {
        taskManager.deleteAllTasks();
        taskManager.deleteAllEpics();
    }
    @Test
    void shouldManagerBeReady() {
        assertNotNull(taskManager);
    }
    @Test
    void canAddHistory() {
        Task task = new Task("Задача", "Описание");
        Epic epic = new Epic("Эпик", "Описание");
        Subtask sub = new Subtask("Подзадача", "Описание");

        int taskId = taskManager.addTask(task);
        int epicId = taskManager.addEpic(epic);
        sub.setParentId(epicId);
        int subtaskId = taskManager.addSubtask(sub);

        assertTrue(taskManager.getHistory().isEmpty());

        task = taskManager.getTask(taskId);
        List list =  taskManager.getHistory();

        assertEquals(list.size(), 1, "в истории должен быть один таск");

        epic = taskManager.getEpic(epicId);
        list =  taskManager.getHistory();

        assertEquals(list.size(), 2, "в истории должен быть таск и эпик");

        sub = taskManager.getSubtask(subtaskId);
        list =  taskManager.getHistory();

        assertEquals(list.size(), 3, "в истории должен быть таск, эпик и сабтаск");
    }
}