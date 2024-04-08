package com.practicum.kanban.service;

import com.practicum.kanban.model.Epic;
import com.practicum.kanban.model.Subtask;
import com.practicum.kanban.model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {
    //static TaskManager taskManager;
    static TaskManager taskManager;

    TaskManagerTest(T taskManager) {
        this.taskManager = taskManager;
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
    void managerShouldBeClearBeforeUse() {
        Map allTasks = taskManager.getTaskList();
        Map allEpics = taskManager.getEpicList();
        assertTrue(allTasks.isEmpty());
        assertTrue(allEpics.isEmpty());
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
    void canAddNonRepeatHistory() {
        Task task = new Task("Задача", "Описание");
        Epic epic = new Epic("Эпик", "Описание");
        Subtask sub = new Subtask("Подзадача", "Описание");

        int taskId = taskManager.addTask(task);
        int epicId = taskManager.addEpic(epic);
        sub.setParentId(epicId);
        int subtaskId = taskManager.addSubtask(sub);

        // Пустая история
        assertTrue(taskManager.getHistory().isEmpty(), "в начале работы история должна быть пуста");

        taskManager.getTask(taskId);
        List list = taskManager.getHistory();

        assertEquals(list.size(), 1, "в истории должен быть один таск");

        taskManager.getEpic(epicId);
        list = taskManager.getHistory();

        assertEquals(list.size(), 2, "в истории должен быть таск и эпик");

        taskManager.getSubtask(subtaskId);
        list = taskManager.getHistory();

        assertEquals(list.size(), 3, "в истории должен быть таск, эпик и сабтаск");
        // Проверить состав полученного листа истории
        assertEquals(((Task) list.get(0)).getTaskId(), task.getTaskId(), "неверный 1 элемент истории");
        assertEquals(((Task) list.get(1)).getTaskId(), epic.getTaskId(), "неверный 2 элемент истории");
        assertEquals(((Task) list.get(2)).getTaskId(), sub.getTaskId(), "неверный 3 элемент истории");

        // Повторно запросим элементы, история не должна вырасти
        Task getTask = taskManager.getTask(taskId);
        Epic getEpic = taskManager.getEpic(epicId);
        Subtask getSubtask = taskManager.getSubtask(subtaskId);

        list = taskManager.getHistory();
        assertEquals(3, list.size(), "история не должна увеличиться, запросы повторны");

        // Изменение свойств задач
        getTask.setName("Другое имя задачи");
        getEpic.setName("Другое имя эпика");
        getSubtask.setName("Другое имя подзадачи");

        taskManager.updateTask(getTask);
        // а эпик обновлять не будем taskManager.updateEpic(getEpic);
        taskManager.updateSubtask(getSubtask);

        // сравним имена объектов в taskManager и History
        list = taskManager.getHistory();

        // история не должна увеличиться при обновлении задач
        assertEquals(3, list.size());

        String historyName1 = ((Task) list.get(0)).getName();
        String historyName2 = ((Task) list.get(1)).getName();
        String historyName3 = ((Task) list.get(2)).getName();

        String managerName1 = taskManager.getSubtask(subtaskId).getName();
        String managerName2 = taskManager.getEpic(epicId).getName();
        String managerName3 = taskManager.getTask(taskId).getName();

        assertFalse(historyName1.equals(managerName1));
        // эпик мы не обновляли, не должен измениться
        assertTrue(historyName2.equals(managerName2));
        assertFalse(historyName3.equals(managerName3));

        // Удаление из истории. Сейчас там 3 элемента
        // первый элемент списка истории
        taskManager.deleteTask(taskId);
        list = taskManager.getHistory();
        assertEquals(list.size(), 2, "в истории должно быть 2 элемента");
        assertEquals(((Task) list.get(0)).getTaskId(), sub.getTaskId(), "неверный первый элемент истории");
        assertEquals(((Task) list.get(1)).getTaskId(), epic.getTaskId(), "неверный второй элемент истории");

        // последний элемент списка истории
        taskManager.deleteSubtask(subtaskId);
        list = taskManager.getHistory();
        assertEquals(list.size(), 1, "в истории должен быть 1 элемент");
        assertEquals(((Task) list.get(0)).getTaskId(), epic.getTaskId(), "неверный элемент истории");

        // последний оставшийся в истории элемент
        taskManager.deleteEpic(epicId);
        assertTrue(taskManager.getHistory().isEmpty(), "все элементы из истории удалены, история должна быть пуста");
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
