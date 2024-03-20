package com.practicum.kanban.service;

import com.practicum.kanban.model.Epic;
import com.practicum.kanban.model.Status;
import com.practicum.kanban.model.Subtask;
import com.practicum.kanban.model.Task;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private Path kanbanFilePath;

    // конструктор без параметров создаёт временный файл
    public FileBackedTaskManager() {
        super();
        try {
            kanbanFilePath = Files.createTempFile("kanbanTemp", ".csv");
            load();
        } catch (IOException e) {
            throw new ManagerLoadException();
        }
    }

    public FileBackedTaskManager(String kanbanFileName) {
        super();
        kanbanFilePath = Paths.get(kanbanFileName);
        load();
    }

    public void deleteKanbanFile() {
        // при использовании временного файла в тестах, удалять за собой
        try {
            Files.deleteIfExists(kanbanFilePath);
        } catch (IOException e) {
            throw new ManagerSaveException();
        }
    }

    public static FileBackedTaskManager loadFromFile(String kanbanFileName) {
        return new FileBackedTaskManager(kanbanFileName);
    }

    private void load() {
        try (BufferedReader reader = Files.newBufferedReader(kanbanFilePath)) {
            String line = reader.readLine();
            if ((line != null) && (line.startsWith("id"))) {
                // пошёл блок описания элементов коллекции
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("history")) {
                        // блок окончен
                        break;
                    }
                    parseElement(line);
                }
            }
            if ((line != null) && (line.startsWith("history"))) {
                // идёт блок перечисления истории
                while ((line = reader.readLine()) != null) {
                    parseHistory(line);
                }
            }
        } catch (NoSuchFileException e) {
            // нет файла - нет проблем, едем дальше
        } catch (IOException e) {
            throw new ManagerLoadException();
        }
    }

    private void parseElement(String description) {
        String[] fields = description.split(",");
        if ((fields.length >= 5) && (fields[1].equals("TASK"))) {
            addTask(Task.fromStrings(fields));
        } else if ((fields.length >= 5) && (fields[1].equals("EPIC"))) {
            addEpic(Epic.fromStrings(fields));
        } else if ((fields.length >= 6) && (fields[1].equals("SUBTASK"))) {
            addSubtask(Subtask.fromString(fields));
        }
    }

    private void parseHistory(String description) {
        String[] fields = description.split(",");
        if ((fields.length >= 5) && (fields[1].equals("TASK"))) {
            historyManager.add(Task.fromStrings(fields));
        } else if ((fields.length >= 5) && (fields[1].equals("EPIC"))) {
            historyManager.add(Epic.fromStrings(fields));
        } else if ((fields.length >= 6) && (fields[1].equals("SUBTASK"))) {
            historyManager.add(Subtask.fromStrings(fields));
        }
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteSubtasks(int id) {
        super.deleteSubtasks(id);
        save();
    }

    @Override
    public Task getTask(int id) {
        Task res = super.getTask(id);
        save();
        return res;
    }

    @Override
    public Epic getEpic(int id) {
        Epic res = super.getEpic(id);
        save();
        return res;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask res = super.getSubtask(id);
        save();
        return res;
    }

    @Override
    public int addTask(Task task) {
        int res = super.addTask(task);
        save();
        return res;
    }

    @Override
    public int addEpic(Epic epic) {
        int res = super.addEpic(epic);
        save();
        return res;
    }

    @Override
    public int addSubtask(Subtask subtask) {
        int res = super.addSubtask(subtask);
        save();
        return res;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    private void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(kanbanFilePath)) {
            // записать заголовок
            writer.write("id,type,name,status,description,epic\n");
            // сохранить все текущие задачи/эпики/подзадачи
            for (var task : getTaskList().values()) {
                writer.write(task.toString() + "\n");
            }
            for (var task : getEpicList().values()) {
                writer.write(task.toString() + "\n");
                for (var subTask : getSubtaskList(task.getTaskId()).values()) {
                    writer.write(subTask.toString() + "\n");
                }
            }
            // записать заголовок истории
            writer.write("history\n");
            for (var task : historyManager.getHistory()) {
                writer.write(task.toString() + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException();
        }

    }

    public static void main(String[] args) {
        TaskManager taskManager = new FileBackedTaskManager("test.csv");

        Task task1 = new Task("Задача1", "Описание1");
        Task task2 = new Task("Задача2", "Описание2", Status.DONE);

        Epic epic1 = new Epic("Эпик1", "ЭпикОписание1");
        Epic epic2 = new Epic("Эпик2", "ЭпикОписание2");
        Epic epic3 = new Epic("Эпик3", "ЭпикОписание3");

        Subtask sub1 = new Subtask("Подзад1", "ПодзадОписание1");
        Subtask sub2 = new Subtask("Подзад2", "ПодзадОписание2", Status.DONE);
        Subtask sub3 = new Subtask("Подзад3", "ПодзадОписание3", Status.IN_PROGRESS);
        Subtask sub4 = new Subtask("Подзад4", "ПодзадОписание4", Status.DONE);
        Subtask sub5 = new Subtask("Подзад5", "ПодзадОписание5", Status.DONE);
        Subtask sub6 = new Subtask("Подзад6", "ПодзадОписание6", Status.DONE);

        int task1Id = taskManager.addTask(task1);
        int epic1Id = taskManager.addEpic(epic1);
        int epic2Id = taskManager.addEpic(epic2);
        int task2Id = taskManager.addTask(task2);
        int epic3Id = taskManager.addEpic(epic3);

        sub1.setParentId(epic1Id);
        sub2.setParentId(epic1Id);
        sub3.setParentId(epic2Id);
        sub4.setParentId(epic2Id);
        sub5.setParentId(epic2Id);
        sub6.setParentId(epic2Id);

        int sub1Id = taskManager.addSubtask(sub1);
        int sub2Id = taskManager.addSubtask(sub2);
        int sub3Id = taskManager.addSubtask(sub3);
        int sub4Id = taskManager.addSubtask(sub4);
        int sub5Id = taskManager.addSubtask(sub5);
        int sub6Id = taskManager.addSubtask(sub6);

        TaskManager copyTaskManager = FileBackedTaskManager.loadFromFile("test.csv");

        System.out.println(taskManager);
        System.out.println(copyTaskManager);
    }
}
