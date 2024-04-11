package com.practicum.kanban.service;

import com.practicum.kanban.model.Epic;
import com.practicum.kanban.model.Subtask;
import com.practicum.kanban.model.Task;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    //static TaskManager taskManager;

    public FileBackedTaskManagerTest() {
        // для тестов воспользуемся временным файлом ОС
        super(new FileBackedTaskManager());
    }

    @BeforeAll
    static void prepareManager() {
        // для тестов каждый раз пересоздаём новый менеджер с временным файлом
        taskManager = new FileBackedTaskManager();
    }

    @AfterAll
    static void deleteTempFile() {
        // удалить временный файл за собой
        ((FileBackedTaskManager) taskManager).deleteKanbanFile();
    }

    @Test
    void canCorrectWorkWithFile() {
        FileBackedTaskManager newTaskManager;

        assertThrows(ManagerLoadException.class, () -> {
            new FileBackedTaskManager("");
        }, "Недопустима работа с пустым именем файла");

        assertThrows(ManagerLoadException.class, () -> {
            new FileBackedTaskManager("wrong_file1.csv");
        }, "Недопустима работа с неверным форматом коллекции в файле");

        assertThrows(ManagerLoadException.class, () -> {
            new FileBackedTaskManager("wrong_file2.csv");
        }, "Недопустима работа с неверным форматом элемента истории в файле");

        // данный тест будет проходить, только если поставить признак защиты от записи для файла,
        // к сожалению, этот признак теряется, при прогоне тестов на github
        // assertThrows(ManagerLoadException.class, () -> {
        //     new FileBackedTaskManager("wrong_file3.csv");
        // }, "Недопустима работа с файлом, защищённом от записи");

        assertDoesNotThrow(() -> {
            new FileBackedTaskManager("wrong_file3.csv");
        }, "Недопустима работа с файлом, защищённом от записи");
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
                        newTaskManager.getSubtaskList(epic1.getTaskId()).size() == 6,
                "В первоначальной коллекции сохранены не все элементы");

        Task i;
        // создадим 3 события истории
        i = newTaskManager.getTask(task1.getTaskId());
        i = newTaskManager.getEpic(epic1.getTaskId());
        i = newTaskManager.getSubtask(subtask1.getTaskId());
        i = newTaskManager.getTask(task1.getTaskId()); // повтор не влияет на число событий в истории

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
                        newTaskManager.getSubtaskList(epic1.getTaskId()).size() == 6,
                "В первоначальной коллекции сохранены не все элементы");

        TaskManager copyOfTaskManager = FileBackedTaskManager.loadFromFile("test.csv");

        assertTrue(copyOfTaskManager.getTaskList().size() + copyOfTaskManager.getEpicList().size() +
                        copyOfTaskManager.getSubtaskList(epic1.getTaskId()).size() == 6,
                "В копии FileBackedTaskManager, полученной с помощью loadFromFile не все элементы");

        copyOfTaskManager = new FileBackedTaskManager("test.csv");

        assertTrue(copyOfTaskManager.getTaskList().size() + copyOfTaskManager.getEpicList().size() +
                        copyOfTaskManager.getSubtaskList(epic1.getTaskId()).size() == 6,
                "В копии FileBackedTaskManager, полученной с помощью конструктора не все элементы");

        newTaskManager.deleteKanbanFile();
    }

    @Test
    void canNotAddOverlappingTask() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 4, 6, 12, 0);

        Task task1 = new Task("Задача1", "Описание1", dateTime, Duration.ofMinutes(15));
        Task task2 = new Task("Задача2", "Описание2", dateTime.plus(Duration.ofMinutes(15)), Duration.ofMinutes(15));
        // время задачи пересекается
        Task task3 = new Task("Задача3", "Описание3", dateTime.plus(Duration.ofMinutes(14)), Duration.ofMinutes(1));

        Epic epic1 = new Epic("Эпик1", "ЭпикОписание1");

        Subtask sub1 = new Subtask("Подзад1", "ПодзадОписание1", dateTime.plus(Duration.ofHours(3)), Duration.ofMinutes(60));
        Subtask sub2 = new Subtask("Подзад2", "ПодзадОписание2", dateTime.plus(Duration.ofHours(4)), Duration.ofMinutes(61));
        // время задачи пересекается
        Subtask sub3 = new Subtask("Подзад3", "ПодзадОписание3", dateTime.plus(Duration.ofHours(5)), Duration.ofMinutes(60));

        int epic1Id = taskManager.addEpic(epic1);
        // надо подготовить subtask
        sub1.setParentId(epic1Id);
        sub2.setParentId(epic1Id);
        sub3.setParentId(epic1Id);

        int task1Id = taskManager.addTask(task1);
        assertTrue(task1Id > 0, "Задача в пустой менеджер не добавлена");
        int task2Id = taskManager.addTask(task2);
        assertTrue(task2Id > 0, "Непересекающаяся задача не добавлена");
        int task3Id = taskManager.addTask(task3);
        assertTrue(task3Id < 0, "Пересекающаяся задача добавлена");

        int sub1Id = taskManager.addSubtask(sub1);
        assertTrue(sub1Id > 0, "Непересекающаяся подзадача в путом эпике не добавлена");
        int sub2Id = taskManager.addSubtask(sub2);
        assertTrue(sub2Id > 0, "Непересекающаяся подзадача в непустом эпике не добавлена");
        int sub3Id = taskManager.addSubtask(sub3);
        assertTrue(sub3Id < 0, "Пересекающаяся подзадача добавлена");
    }
}