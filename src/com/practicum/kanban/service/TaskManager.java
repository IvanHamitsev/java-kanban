package com.practicum.kanban.service;

import com.practicum.kanban.model.*;

import java.util.List;
import java.util.Map;

public interface TaskManager {
    // a. Получение списка всех задач.
    Map<Integer, Task> getTaskList();

    Map<Integer, Epic> getEpicList();

    Map<Integer, Subtask> getSubtaskList(int id);

    // b. Удаление всех задач.
    void deleteAllTasks();

    void deleteAllEpics();

    void deleteSubtasks(int id);

    // c. Получение по идентификатору.
    Task getTask(int id);

    Epic getEpic(int id);

    Subtask getSubtask(int id);

    // d. Создание. Сам объект должен передаваться в качестве параметра.
    int addTask(Task task);

    int addEpic(Epic epic);

    int addSubtask(Subtask subtask);

    //  e. Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubtask(Subtask subtask);

    // f. Удаление по идентификатору.
    void deleteTask(int id);

    void deleteEpic(int id);

    void deleteSubtask(int id);

    // История операций получения всех типов задач
    List<Task> getHistory();
}
