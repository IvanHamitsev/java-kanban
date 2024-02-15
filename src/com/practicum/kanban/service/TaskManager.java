package com.practicum.kanban.service;

import com.practicum.kanban.model.*;

import java.util.List;
import java.util.Map;

public interface TaskManager {
    // a. Получение списка всех задач.
    Map<Integer, Task> getTaskList();
    Map<Integer, Epic> getEpicList();
    Map<Integer, Subtask> getSubtaskList(Integer epicId);

    // b. Удаление всех задач.
    void deleteAllTasks();
    void deleteAllEpics();
    void deleteSubtasks(Integer id);

    // c. Получение по идентификатору.
    Task getTask(Integer id);
    Epic getEpic(Integer id);
    Subtask getSubtask(Integer taskId);

    // d. Создание. Сам объект должен передаваться в качестве параметра.
    int addTask(Task task);
    int addEpic(Epic epic);
    int addSubtask(Subtask subtask);

    //  e. Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
    void updateTask(Task task);
    void updateEpic(Epic epic);
    void updateSubtask(Subtask subtask);

    // f. Удаление по идентификатору.
   void deleteTask(Integer taskId);
   void deleteEpic(Integer taskId);
   void deleteSubtask(Integer subtaskId);

    // История операций получения всех типов задач
    List<Task> getHistory();
}
