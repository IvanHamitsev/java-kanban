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
    static void prepareManager() {
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
    void canAddTask() {
        Task task = new Task("Задача", "Описание");
        int taskId = taskManager.addTask(task);

        assertTrue(taskId > 0);
        Task getTask = taskManager.getTask(taskId);

        assertNotNull(getTask);
        assertTrue(task.equals(getTask));
    }

    @Test
    void canAddEpic() {
        Epic epic = new Epic("Эпик", "Описание");
        int taskId = taskManager.addEpic(epic);

        assertTrue(taskId > 0);
        Epic getEpic = taskManager.getEpic(taskId);

        assertNotNull(getEpic);
        assertTrue(epic.equals(getEpic));
    }

    @Test
    void canAddSubtask() {
        Epic epic = new Epic("Эпик", "Описание");
        Subtask sub = new Subtask("Подзадача", "Описание");
        int epicId = taskManager.addEpic(epic);

        assertTrue(epicId > 0);
        sub.setParentId(epicId);
        int subtaskId = taskManager.addSubtask(sub);

        assertTrue(subtaskId > 0);
        Subtask getSubtask = taskManager.getSubtask(subtaskId);
        assertNotNull(getSubtask);
        assertTrue(sub.equals(getSubtask));
    }

    @Test
    void managerShouldBeClearBeforeUse() {
        Map allTasks = taskManager.getTaskList();
        Map allEpics = taskManager.getEpicList();
        assertTrue(allTasks.isEmpty());
        assertTrue(allEpics.isEmpty());
    }

    @Test
    void correctEqualsMetodForTaskEpicSubtask() {
        Task task1 = new Task("Задача1", "Описание1");
        Task task2 = new Task("Задача2", "Описание2");

        assertFalse(task1.equals(task2));
        // приравняем ID
        task2.setTaskId(task1.getTaskId());
        assertTrue(task1.equals(task2));

        Epic epic1 = new Epic("Эпик1", "Описание1");
        Epic epic2 = new Epic("Эпик2", "Описание2");

        assertFalse(epic1.equals(epic2));
        // приравняем ID
        epic2.setTaskId(epic1.getTaskId());
        assertTrue(epic1.equals(epic2));

        Subtask subtask1 = new Subtask("Подзадача1", "Описание1");
        Subtask subtask2 = new Subtask("Подзадача2", "Описание2");

        assertFalse(subtask1.equals(subtask2));
        // приравняем ID
        subtask2.setTaskId(subtask1.getTaskId());
        assertTrue(subtask1.equals(subtask2));
    }

    @Test
    void canFindAllTypesOfTasks() {
        Task task1 = new Task("Задача1", "Описание1");
        Subtask subtask1 = new Subtask("Подзадача1", "Описание1");
        Epic epic1 = new Epic("Эпик1", "Описание1");

        int taskId = taskManager.addTask(task1);
        int epicId = taskManager.addEpic(epic1);

        subtask1.setParentId(epicId);

        int subtaskId = taskManager.addSubtask(subtask1);

        Task getTask = taskManager.getTask(taskId);
        Epic getEpic = taskManager.getEpic(epicId);
        Subtask getSubtask = taskManager.getSubtask(subtaskId);

        assertNotNull(getTask);
        assertNotNull(getEpic);
        assertNotNull(getSubtask);

        assertTrue(getTask.equals(task1));
        assertTrue(getEpic.equals(getEpic));
        assertTrue(getSubtask.equals(subtask1));
    }

    @Test
    void shouldNotAddEpicAsTask() {
        Epic epic = new Epic("Эпик", "Описание");
        int taskId = taskManager.addTask(epic);
        assertFalse(taskId > 0);
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
