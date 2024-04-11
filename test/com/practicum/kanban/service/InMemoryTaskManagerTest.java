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

        int taskId = taskManager.addTask(null);
        assertTrue(taskId < 0);

        Task task = new Task("Задача", "Описание");
        taskId = taskManager.addTask(task);

        assertTrue(taskId > 0);
        Task getTask = taskManager.getTask(taskId);

        assertNotNull(getTask);
        assertTrue(task.equals(getTask));
    }

    @Test
    void canAddEpic() {
        int taskId = taskManager.addEpic(null);
        assertTrue(taskId < 0);

        Epic epic = new Epic("Эпик", "Описание");
        taskId = taskManager.addEpic(epic);

        assertTrue(taskId > 0);
        Epic getEpic = taskManager.getEpic(taskId);

        assertNotNull(getEpic);
        assertTrue(epic.equals(getEpic));
    }

    @Test
    void canAddSubtask() {

        int subtaskId = taskManager.addSubtask(null);
        assertTrue(subtaskId < 0);

        Epic epic = new Epic("Эпик", "Описание");
        Subtask sub = new Subtask("Подзадача", "Описание");
        int epicId = taskManager.addEpic(epic);

        assertTrue(epicId > 0);
        sub.setParentId(epicId);
        subtaskId = taskManager.addSubtask(sub);

        assertTrue(subtaskId > 0);
        Subtask getSubtask = taskManager.getSubtask(subtaskId);
        assertNotNull(getSubtask);
        assertTrue(sub.equals(getSubtask));

        // наличие у полученной подзадачи правильной ссылки на эпик
        assertEquals(getSubtask.getParentId(), sub.getParentId(), "Неверная ссылка на эпик " + getSubtask.getParentId() + " != " + sub.getParentId());
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
    void shouldNotAddSubtaskWithNoEpic() {
        Epic epic = new Epic("Эпик", "Описание");
        Subtask sub = new Subtask("Подзадача", "Описание");

        int epicId = taskManager.addEpic(epic);
        sub.setParentId(sub.getTaskId());
        int subtaskId = taskManager.addSubtask(sub);

        assertNull(taskManager.getSubtask(subtaskId));
    }

    @Test
    void shouldCorrectCalcEpicStatus() {
        Epic epic = new Epic("Эпик", "Описание");
        Subtask sub1 = new Subtask("Подзадача1", "Описание", Status.NEW);
        Subtask sub2 = new Subtask("Подзадача2", "Описание", Status.IN_PROGRESS);
        Subtask sub3 = new Subtask("Подзадача3", "Описание", Status.DONE);

        int epicId = taskManager.addEpic(epic);
        assertTrue(taskManager.getEpic(epicId).getStatus() == Status.NEW,
                "Статус пустого эпика должен быть NEW");

        sub1.setParentId(epicId);
        sub2.setParentId(epicId);
        sub3.setParentId(epicId);

        taskManager.addSubtask(sub1);
        assertTrue(taskManager.getEpic(epicId).getStatus() == Status.NEW,
                "Статус эпика с подзадачей NEW должен быть NEW");

        taskManager.addSubtask(sub2);
        assertTrue(taskManager.getEpic(epicId).getStatus() == Status.IN_PROGRESS,
                "Статус эпика с подзадачей IN_PROGRESS должен подняться до IN_PROGRESS");

        taskManager.addSubtask(sub3);
        assertTrue(taskManager.getEpic(epicId).getStatus() == Status.IN_PROGRESS,
                "Статус DONE только одной подзадачи эпика оставляет статус эпика в IN_PROGRESS");

        // переводим все подзадачи в статус DONE
        sub1.setStatus(Status.DONE);
        sub2.setStatus(Status.DONE);
        taskManager.updateSubtask(sub1);
        taskManager.updateSubtask(sub2);

        assertTrue(taskManager.getEpic(epicId).getStatus() == Status.DONE,
                "Статус DONE всех подзадач должен приводить к статусу эпика DONE");

        sub3.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubtask(sub3);

        assertTrue(taskManager.getEpic(epicId).getStatus() == Status.IN_PROGRESS,
                "Понижение статуса одной подзадачи до IN_PROGRESS понижает статус эпика до IN_PROGRESS");

        // переводим все подзадачи в статус NEW
        sub1.setStatus(Status.NEW);
        sub2.setStatus(Status.NEW);
        sub3.setStatus(Status.NEW);
        taskManager.updateSubtask(sub1);
        taskManager.updateSubtask(sub2);
        taskManager.updateSubtask(sub3);
        assertTrue(taskManager.getEpic(epicId).getStatus() == Status.NEW,
                "Понижение статуса всех подзадач до NEW понижает статус эпика до NEW");

    }
}
