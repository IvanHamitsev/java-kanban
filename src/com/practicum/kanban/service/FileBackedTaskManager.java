package com.practicum.kanban.service;

import com.practicum.kanban.model.Epic;
import com.practicum.kanban.model.Subtask;
import com.practicum.kanban.model.Task;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private Path kanbanFilePath;

    public static final int COUNT_LOADABLE_FIELDS_IN_TASK = 7;
    public static final int COUNT_LOADABLE_FIELDS_IN_EPIC = 7;
    public static final int COUNT_LOADABLE_FIELDS_IN_SUBTASK = 8;

    // конструктор без параметров создаёт временный файл
    public FileBackedTaskManager() {
        super();
        try {
            kanbanFilePath = Files.createTempFile("kanbanTemp", ".csv");
            load();
        } catch (IOException e) {
            throw new ManagerLoadException("При создании временного файла произошла ошибка " + e.getMessage());
        }
    }

    public FileBackedTaskManager(String kanbanFileName) {
        super();
        kanbanFilePath = Paths.get(kanbanFileName);
        load();
    }

    @Override
    public void clearInstance() {
        super.clearInstance();
        deleteKanbanFile();
    }

    public void deleteKanbanFile() {
        // при использовании временного файла в тестах, удалять за собой
        try {
            Files.deleteIfExists(kanbanFilePath);
        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось удалить файл " + kanbanFilePath);
        }
    }

    public static FileBackedTaskManager loadFromFile(String kanbanFileName) {
        return new FileBackedTaskManager(kanbanFileName);
    }

    private void load() {
        try (BufferedReader reader = Files.newBufferedReader(kanbanFilePath)) {
            // из-за того, что файл разделён на блок элементов коллекции и блок элементов истории,
            // это противоречит идее стрима - элемент нельзя обрабатывать без контекста положения в файле
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
            throw new ManagerLoadException("Ошибка при загрузке данных из файла " + kanbanFilePath);
        }
    }

    private void parseElement(String description) {
        String[] fields = description.split(",");

        if ((fields.length >= COUNT_LOADABLE_FIELDS_IN_TASK) && (fields[1].equals("TASK"))) {
            // нужно взять родительскую реализацию метода, без записи в файл. Нельзя писать на этапе загрузки
            super.addTask(Task.fromStrings(fields));
        } else if ((fields.length >= COUNT_LOADABLE_FIELDS_IN_EPIC) && (fields[1].equals("EPIC"))) {
            super.addEpic(Epic.fromStrings(fields));
        } else if ((fields.length >= COUNT_LOADABLE_FIELDS_IN_SUBTASK) && (fields[1].equals("SUBTASK"))) {
            super.addSubtask(Subtask.fromString(fields));
        } else {
            // не подходит под формат
            throw new ManagerLoadException("Неверный формат строки с элементом коллекции");
        }
    }

    private void parseHistory(String description) {
        String[] fields = description.split(",");
        if ((fields.length >= COUNT_LOADABLE_FIELDS_IN_TASK) && (fields[1].equals("TASK"))) {
            historyManager.add(Task.fromStrings(fields));
        } else if ((fields.length >= COUNT_LOADABLE_FIELDS_IN_EPIC) && (fields[1].equals("EPIC"))) {
            historyManager.add(Epic.fromStrings(fields));
        } else if ((fields.length >= COUNT_LOADABLE_FIELDS_IN_SUBTASK) && (fields[1].equals("SUBTASK"))) {
            historyManager.add(Subtask.fromStrings(fields));
        } else {
            // не подходит под формат
            throw new ManagerLoadException("Неверный формат строки с элементом истории");
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
        // сохраняем только если действие произошло
        if (res != null) {
            save();
        }
        return res;
    }

    @Override
    public Epic getEpic(int id) {
        Epic res = super.getEpic(id);
        if (res != null) {
            save();
        }
        return res;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask res = super.getSubtask(id);
        if (res != null) {
            save();
        }
        return res;
    }

    @Override
    public int addTask(Task task) {
        int res = super.addTask(task);
        if (res > 0) {
            save();
        }
        return res;
    }

    @Override
    public int addEpic(Epic epic) {
        int res = super.addEpic(epic);
        if (res > 0) {
            save();
        }
        return res;
    }

    @Override
    public int addSubtask(Subtask subtask) {
        int res = super.addSubtask(subtask);
        if (res > 0) {
            save();
        }
        return res;
    }

    @Override
    public int updateTask(Task task) {
        int res = -1;
        // помним, что прямой addTask не сработает - задача будет конфликтовать во времени
        Task oldTask = taskList.get(task.getTaskId());
        if (null != oldTask) {
            LocalDateTime start = oldTask.getStartTime().orElse(null);
            Duration duration = oldTask.getDuration().orElse(null);
            // на время апдейта надо убрать из временной шкалы задачу
            freeTime(start, duration);
            res = super.updateTask(task);
            if (0 > res) {
                // после неуспешеного апдейта - вернуть старые параметры на временную шкалу
                reservTime(start, duration);
                return -1;
            }
        } else {
            res = super.updateTask(task);
        }
        save();
        return res;
    }

    @Override
    public int updateEpic(Epic epic) {
        // особенность Epic - он не занимает время. Время занимают его подзадачи.
        int res = super.updateEpic(epic);
        if (0 < res) {
            save();
        }
        return res;
    }

    @Override
    public int updateSubtask(Subtask subtask) {
        int res = -1;
        Optional<Epic> epic = epicList.values().stream()
                .filter(e -> e.getSubtasks().get(subtask.getTaskId()) == null ? false : true)
                .findFirst();
        if (epic.isPresent()) {
            Subtask oldSubtask = epic.get().getSubtasks().get(subtask.getTaskId());
            LocalDateTime start = oldSubtask.getStartTime().orElse(null);
            Duration duration = oldSubtask.getDuration().orElse(null);
            // на время апдейта надо убрать из временной шкалы задачу
            freeTime(start, duration);
            res = super.updateSubtask(subtask);
            if (0 > res) {
                // после неуспешеного апдейта - вернуть старые параметры на временную шкалу
                reservTime(start, duration);
                return -1;
            }
        } else {
            res = super.updateSubtask(subtask);
        }
        save();
        return res;
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
            writer.write("id,type,name,status,start time,duration,description,epic\n");
            // сохранить все текущие задачи/эпики/подзадачи
            for (var task : getTaskList().values()) {
                writer.write(task.toFileString() + "\n");
            }
            for (var task : getEpicList().values()) {
                writer.write(task.toFileString() + "\n");
                for (var subTask : getSubtaskList(task.getTaskId()).values()) {
                    writer.write(subTask.toFileString() + "\n");
                }
            }
            // записать заголовок истории
            writer.write("history\n");
            for (var task : historyManager.getHistory()) {
                writer.write(task.toFileString() + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении данных в файл " + kanbanFilePath);
        }

    }
}
