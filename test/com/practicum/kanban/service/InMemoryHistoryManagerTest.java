package com.practicum.kanban.service;

import com.practicum.kanban.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.List;

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
    void canAddNonRepeatHistory() {
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

        // поменяем задачи в taskManager
        Task getTask = taskManager.getTask(taskId);
        Epic getEpic = taskManager.getEpic(epicId);
        Subtask getSubtask = taskManager.getSubtask(subtaskId);
        // (и история не должна вырасти, повторы)
        list =  taskManager.getHistory();
        assertEquals(3, list.size());

        getTask.setName("Другое имя задачи");
        getEpic.setName("Другое имя эпика");
        getSubtask.setName("Другое имя подзадачи");

        taskManager.updateTask(getTask);
        // а эпик обновлять не будем taskManager.updateEpic(getEpic);
        taskManager.updateSubtask(getSubtask);

        // сравним имена объектов в taskManager и History
        list =  taskManager.getHistory();

        // история без повторов - по-прежменму только три различных ID
        assertEquals(3, list.size());

        String historyName1 = ((Task)list.get(0)).getName();
        String historyName2 = ((Task)list.get(1)).getName();
        String historyName3 = ((Task)list.get(2)).getName();

        String managerName1 = taskManager.getSubtask(subtaskId).getName();
        String managerName2 = taskManager.getEpic(epicId).getName();
        String managerName3 = taskManager.getTask(taskId).getName();

        assertFalse(historyName1.equals(managerName1));
        // эпик мы не обновляли, не должен измениться
        assertTrue(historyName2.equals(managerName2));
        assertFalse(historyName3.equals(managerName3));
    }
}