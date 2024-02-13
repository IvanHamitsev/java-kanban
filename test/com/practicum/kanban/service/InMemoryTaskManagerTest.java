package com.practicum.kanban.service;

import com.practicum.kanban.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
public class InMemoryTaskManagerTest {
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
    void canAddTask() {
        Task task = new Task("Задача", "Описание");
        int taskId = taskManager.addTask(task);
        assertNotNull(taskManager.getTask(taskId));
    }
    @Test
    void canAddEpic() {
        Epic epic = new Epic("Эпик", "Описание");
        int taskId = taskManager.addEpic(epic);
        assertNotNull(taskManager.getEpic(taskId));
    }
    @Test
    void canAddSubtask() {
        Epic epic = new Epic("Эпик", "Описание");
        Subtask sub = new Subtask("Подзадача", "Описание");

        int epicId = taskManager.addEpic(epic);
        sub.setParentId(epicId);
        int subtaskId = taskManager.addSubtask(sub);

        assertNotNull(taskManager.getSubtask(subtaskId));
    }
    @Test
    void managerShouldBeClearBeforeUse() {
        Map allTasks = taskManager.getTaskList();
        Map allEpics = taskManager.getEpicList();
        assertTrue(allTasks.isEmpty());
        assertTrue(allEpics.isEmpty());
    }
    @Test
    void shouldNotAddEpicAsTask() {
        Epic epic = new Epic("Эпик", "Описание");
        int taskId = taskManager.addTask(epic);
        assertNull(taskManager.getTask(taskId));
    }
    @Test
    void shouldNotAddSubtaskAsTask() {
        Subtask subtask = new Subtask("Подзадача", "Описание");
        int taskId = taskManager.addTask(subtask);
        assertNull(taskManager.getTask(taskId));
    }

    @Test
    void shouldNotAddSubtaskAsEpic() {
        Epic epic = new Epic("Эпик", "Описание");
        Subtask sub = new Subtask("Подзадача", "Описание");

        int epicId = taskManager.addEpic(epic);
        sub.setParentId(sub.getTaskId());
        int subtaskId = taskManager.addSubtask(sub);

        assertNull(taskManager.getSubtask(subtaskId));
    }
}
