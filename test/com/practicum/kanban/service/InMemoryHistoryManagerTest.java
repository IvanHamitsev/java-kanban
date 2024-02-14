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
        taskManager = Managers.getDefault();
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

        taskManager.getTask(taskId);
        List list =  taskManager.getHistory();

        assertEquals(list.size(), 1, "в истории должен быть один таск");

        taskManager.getEpic(epicId);
        list =  taskManager.getHistory();

        assertEquals(list.size(), 2, "в истории должен быть таск и эпик");

        taskManager.getSubtask(subtaskId);
        list =  taskManager.getHistory();

        assertEquals(list.size(), 3, "в истории должен быть таск, эпик и сабтаск");

        // поменяем задачи в taskManager (ещё +3 элемента в истории)
        Task getTask = taskManager.getTask(taskId);
        Epic getEpic = taskManager.getEpic(epicId);
        Subtask getSubtask = taskManager.getSubtask(subtaskId);

        getTask.setName("Другое имя задачи");
        getEpic.setName("Другое имя эпика");
        getSubtask.setName("Другое имя подзадачи");

        taskManager.updateTask(getTask);
        taskManager.updateEpic(getEpic);
        taskManager.updateSubtask(getSubtask);

        // проверим, что имена объектов в taskManager и History теперь разные
        list =  taskManager.getHistory();

        assertEquals(list.size(), 6);
        String historyName4 = ((Task)list.get(3)).getName();
        String historyName5 = ((Task)list.get(4)).getName();
        String historyName6 = ((Task)list.get(5)).getName();

        String managerName1 = taskManager.getSubtask(subtaskId).getName();
        String managerName2 = taskManager.getEpic(epicId).getName();
        String managerName3 = taskManager.getTask(taskId).getName();

        assertFalse(historyName4.equals(managerName1));
        assertFalse(historyName5.equals(managerName2));
        assertFalse(historyName6.equals(managerName3));
    }
}