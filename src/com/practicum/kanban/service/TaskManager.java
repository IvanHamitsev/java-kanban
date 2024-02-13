package com.practicum.kanban.service;

import com.practicum.kanban.model.*;

import java.util.List;
import java.util.HashMap;

public interface TaskManager {
    // a. Получение списка всех задач.
    public HashMap<Integer, Task> getTaskList();
    public HashMap<Integer, Epic> getEpicList();

    // b. Удаление всех задач.
    public void deleteAllTasks();
    public void deleteAllEpics();
    public void deleteSubtasks(Integer id);

    // c. Получение по идентификатору.
    public Task getTask(Integer id);
    public Epic getEpic(Integer id);
    public Subtask getSubtask(Integer taskId);

    // d. Создание. Сам объект должен передаваться в качестве параметра.
    public int addTask(Task task);
    public int addEpic(Epic epic);
    public int addSubtask(Subtask subtask);

    //  e. Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
    public void updateTask(Task task);
    public void updateEpic(Epic epic);
    public void updateSubtask(Subtask subtask);

    // f. Удаление по идентификатору.
    public void deleteTask(Integer taskId);
    public void deleteEpic(Integer taskId);
    public void deleteSubtask(Integer subtaskId);

    // Дополнительные методы:
    // a. Получение списка всех подзадач определённого эпика.
    public HashMap<Integer, Subtask> getSubtaskList(Integer epicId);

    // почему бы и классу InMemoryTaskManager не уметь возвращать свою историю?
    public List<Task> getHistory();
}
