package com.practicum.kanban.service;

import com.practicum.kanban.model.Epic;
import com.practicum.kanban.model.Subtask;
import com.practicum.kanban.model.Task;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    static TaskManager taskManager;

    @BeforeAll
    static void prepareManager() {
        // для тестов воспользуемся временным файлом ОС
        taskManager = new FileBackedTaskManager();
    }

    @AfterAll
    static void deleteTempFile() {
        // удалить временный файл за собой
        ((FileBackedTaskManager)taskManager).deleteKanbanFile();
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
    void canCorrectWorkWithFile() {
        FileBackedTaskManager newTaskManager;
        try {
            newTaskManager = new FileBackedTaskManager("");
            assertFalse(true, "Недопустима работа с пустым именем файла");
        } catch (ManagerLoadException e) {
            assertTrue(true, "Успех, работа с пустым именем файла прекращена");
        }

        try {
            newTaskManager = new FileBackedTaskManager("wrong_file1.csv");
            assertFalse(true, "Недопустима работа с неверным форматом коллекции в файле");
        } catch (ManagerLoadException e) {
            assertTrue(true, "Успех, работа с неверным форматом коллекции в файле прекращена");
        }

        try {
            newTaskManager = new FileBackedTaskManager("wrong_file2.csv");
            assertFalse(true, "Недопустима работа с неверным форматом элемента истории в файле");
        } catch (ManagerLoadException e) {
            assertTrue(true, "Успех, работа с неверным форматом элемента истории в файле прекращена");
        }

        try {
            newTaskManager = new FileBackedTaskManager("wrong_file3.csv");
            Task task2 = new Task("Задача2", "Описание2");
            newTaskManager.addTask(task2);
            assertFalse(true, "Недопустима работа с файлом, защищённом от записи");
        } catch (ManagerSaveException e) {
            assertTrue(true, "Успех, работа с файлом, защищённом от записи прекращена");
        }
    }

    @Test
    void canSaveCollectionToFile() {
        FileBackedTaskManager newTaskManager = new FileBackedTaskManager("test.csv");
        Task task1 = new Task("Задача1", "Описание1");
        Task task2 = new Task("Задача2", "Описание2");
        Epic epic1 = new Epic("Эпик1", "Описание1");
        Epic epic2 = new Epic("Эпик2", "Описание2");
        Subtask subtask1 = new Subtask("Подзадача1", "Описание1");
        Subtask subtask2 = new Subtask("Подзадача2", "Описание2");
        subtask1.setParentId(epic1.getTaskId());
        subtask2.setParentId(epic1.getTaskId());

        newTaskManager.addTask(task1);
        newTaskManager.addTask(task2);
        newTaskManager.addEpic(epic1);
        newTaskManager.addEpic(epic2);
        newTaskManager.addSubtask(subtask1);
        newTaskManager.addSubtask(subtask2);

        assertTrue(newTaskManager.getTaskList().size() + newTaskManager.getEpicList().size() +
                        newTaskManager.getSubtaskList(epic1.getTaskId()).size() ==  6,
                "В первоначальной коллекции сохранены не все элементы");

        // создадим события истории
        newTaskManager.getTask(task1.getTaskId());
        newTaskManager.getEpic(epic1.getTaskId());
        newTaskManager.getSubtask(subtask1.getTaskId());
        newTaskManager.getTask(task1.getTaskId()); // повтор не влияет на число событий в истории

        try (BufferedReader reader = Files.newBufferedReader(Paths.get("test.csv"))) {
            int lineCount = 0;
            while (reader.readLine() != null) {
                lineCount++;
            }
            // в файле должно быть 6 объектов коллекции + 3 объекта истории + 2 заголовка
            assertTrue(lineCount == 6 + 3 + 2, "число строк в файле не соответствует числу объектов");
        } catch (IOException e) {
            assertTrue(false, "Ошибка доступа к файлу " + e.getMessage());
        }

        newTaskManager.deleteKanbanFile();
    }

    @Test
    void canLoadCollectionFromFile() {
        FileBackedTaskManager newTaskManager = new FileBackedTaskManager("test.csv");
        Task task1 = new Task("Задача1", "Описание1");
        Task task2 = new Task("Задача2", "Описание2");
        Epic epic1 = new Epic("Эпик1", "Описание1");
        Epic epic2 = new Epic("Эпик2", "Описание2");
        Subtask subtask1 = new Subtask("Подзадача1", "Описание1");
        Subtask subtask2 = new Subtask("Подзадача2", "Описание2");
        subtask1.setParentId(epic1.getTaskId());
        subtask2.setParentId(epic1.getTaskId());

        newTaskManager.addTask(task1);
        newTaskManager.addTask(task2);
        newTaskManager.addEpic(epic1);
        newTaskManager.addEpic(epic2);
        newTaskManager.addSubtask(subtask1);
        newTaskManager.addSubtask(subtask2);

        assertTrue(newTaskManager.getTaskList().size() + newTaskManager.getEpicList().size() +
                        newTaskManager.getSubtaskList(epic1.getTaskId()).size() ==  6,
                "В первоначальной коллекции сохранены не все элементы");

        TaskManager copyOfTaskManager = FileBackedTaskManager.loadFromFile("test.csv");

        assertTrue(copyOfTaskManager.getTaskList().size() + copyOfTaskManager.getEpicList().size() +
                        copyOfTaskManager.getSubtaskList(epic1.getTaskId()).size() ==  6,
                "В копии FileBackedTaskManager, полученной с помощью loadFromFile не все элементы");

        copyOfTaskManager = new FileBackedTaskManager("test.csv");

        assertTrue(copyOfTaskManager.getTaskList().size() + copyOfTaskManager.getEpicList().size() +
                        copyOfTaskManager.getSubtaskList(epic1.getTaskId()).size() ==  6,
                "В копии FileBackedTaskManager, полученной с помощью конструктора не все элементы");

        newTaskManager.deleteKanbanFile();
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