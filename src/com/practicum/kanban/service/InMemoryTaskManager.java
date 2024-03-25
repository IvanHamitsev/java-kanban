package com.practicum.kanban.service;

import com.practicum.kanban.model.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class InMemoryTaskManager implements TaskManager {
    // Класс хранит только Task и Epic
    // Подзадачи каждый Epic хранит самостоятельно
    private HashMap<Integer, Task> taskList = new HashMap<>();
    private HashMap<Integer, Epic> epicList = new HashMap<>();

    protected HistoryManager historyManager;

    public InMemoryTaskManager() {
        historyManager = Managers.getDefaultHistoryManager();
    }

    public InMemoryTaskManager(HistoryManager manager) {
        historyManager = manager;
    }

    // a. Получение списка всех задач.
    public Map<Integer, Task> getTaskList() {
        // не хочу отдавать сам список, отдадим его копию, чтобы не вмешались в оригинал
        Map<Integer, Task> res = (Map) taskList.clone();
        return res;
    }

    public Map<Integer, Epic> getEpicList() {
        // отдать копию
        Map<Integer, Epic> res = (Map) epicList.clone();
        return res;
    }

    // Получение списка всех подзадач определённого эпика.
    @Override
    public Map<Integer, Subtask> getSubtaskList(int id) {
        Map<Integer, Subtask> result = null;
        Epic epic = (Epic) epicList.get(id);
        if (null != epic) {
            result = (Map) epic.getSubtasks().clone();
        }
        return result;
    }

    // b. Удаление всех задач.
    @Override
    public void deleteAllTasks() {
        taskList.clear();
    }

    @Override
    public void deleteAllEpics() {
        epicList.clear();
    }

    @Override
    public void deleteSubtasks(int id) {
        Epic epic = epicList.get(id);
        if (null != epic) {
            epic.getSubtasks().clear();
            historyManager.remove(id);
            // обновить статус эпика
            epic.setStatus(Status.NEW);
        }
    }

    // c. Получение по идентификатору.
    @Override
    public Task getTask(int id) {
        Task task = taskList.get(id);
        if (task != null) {
            // добавляем в историю копию объекта
            historyManager.add(task.copy());
            // и возвращаем копию объекта
            return new Task(task);
        }
        return null;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epicList.get(id);
        if (epic != null) {
            // отдаём и добавляем в историю копию объекта
            historyManager.add(epic.copy());
            return new Epic(epic);
        }
        return null;
    }

    @Override
    public Subtask getSubtask(int id) {
        for (Epic epic : epicList.values()) {
            Subtask subtask = epic.getSubtasks().get(id);
            if (null != subtask) {
                historyManager.add(subtask.copy());
                return subtask.copy();
            }
        }
        return null;
    }

    // d. Создание. Сам объект должен передаваться в качестве параметра.
    @Override
    public int addTask(Task task) {
        // добавим копию полученной задачи, чтобы у пользователя не оставалось ссылки для
        // несанкционированного доступа
        if ((task != null) && (task.getClass() == Task.class)) {
            Task newTask = task.copy();
            // в случае, если задача существует - обновим её
            if (taskList.containsKey(newTask.getTaskId())) {
                taskList.replace(newTask.getTaskId(), newTask);
            } else {
                taskList.put(newTask.getTaskId(), newTask);
            }
            return newTask.getTaskId();
        }
        return -1;
    }

    @Override
    public int addEpic(Epic epic) {
        if (epic != null) {
            Epic newEpic = epic.copy();
            if (epicList.containsKey(newEpic.getTaskId())) {
                epicList.replace(newEpic.getTaskId(), newEpic);
            } else {
                epicList.put(newEpic.getTaskId(), newEpic);
            }
            return newEpic.getTaskId();
        }
        return -1;
    }

    @Override
    public int addSubtask(Subtask subtask) {
        if (subtask != null) {
            Epic epic = (Epic) epicList.get(subtask.getParentId());
            if (epic != null) {
                Subtask newSubtask = subtask.copy();
                // клонирование не проставляет ссылки, проставим их
                newSubtask.setParentId(epic.getTaskId());
                if (epic.getSubtasks().containsKey(newSubtask.getTaskId())) {
                    epic.getSubtasks().replace(newSubtask.getTaskId(), newSubtask);
                } else {
                    epic.getSubtasks().put(newSubtask.getTaskId(), newSubtask);
                }
                // обновить статус эпика
                calcStatusAdd(epic, newSubtask.getStatus());
                return newSubtask.getTaskId();
            }
        }
        return -1;
    }

    //  e. Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
    @Override
    public void updateTask(Task task) {
        if (task != null) {
            // в текущей реализации это замена существующего элемента
            addTask(task);
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic != null) {
            addEpic(epic);
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        Epic epic = (Epic) epicList.get(subtask.getParentId());
        if (epic != null) {
            addSubtask(subtask);
            // пересчёты статусов есть внутри функций
        }
    }

    // f. Удаление по идентификатору.
    @Override
    public void deleteTask(int id) {
        taskList.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epicList.get(id);
        if (null != epic) {
            // из истории надо удалить не только эпик, но и его подзадачи
            for (Subtask subtask : epic.getSubtasks().values()) {
                historyManager.remove(subtask.getTaskId());
            }
            historyManager.remove(id);
            epicList.remove(id);
        }
    }

    @Override
    public void deleteSubtask(int id) {
        for (Epic epic : epicList.values()) {
            if (null != epic.getSubtasks().get(id)) {
                Status status = epic.getSubtasks().get(id).getStatus();
                epic.getSubtasks().remove(id);
                historyManager.remove(id);
                calcStatusRemove(epic, status);
                // выход - удаляем лишь первое вхождение
                return;
            }
        }
    }

    // Истории последних операций получения задач/эпиков/подзадач
    @Override
    public List<Task> getHistory() {
        // метод get делает копии элеметов, поэтому достаточно сделать Collections.unmodifiableList
        return Collections.unmodifiableList(historyManager.getHistory());
    }

    // Дополнительные методы:
    // пересчёт статуса с учётом изменения
    private void calcStatusAdd(Epic epic, Status status) {
        if (epic.getSubtasks().size() == 1) {
            // единственная подзадача эпика определит его статус
            epic.setStatus(status);
        } else {
            switch (epic.getStatus()) {
                case NEW:
                    if (status != Status.NEW) {
                        epic.setStatus(Status.IN_PROGRESS);
                    }
                    break;
                case DONE:
                    if (status != Status.DONE) {
                        epic.setStatus(Status.IN_PROGRESS);
                    }
            }
        }
    }

    private void calcStatusRemove(Epic epic, Status status) {
        // если подзадач не осталось статус NEW
        if (epic.getSubtasks().isEmpty()) {
            epic.setStatus(Status.NEW);
        }
        if (epic.getSubtasks().size() == 1) {
            // единственная оставшаяся подзадача эпика определит его статус
            for (Subtask subtask : epic.getSubtasks().values()) {
                epic.setStatus(subtask.getStatus());
                return;
            }
        } else {
            // при удалении подзадачи статус может измениться, только если он был IN_PROGRESS
            if (epic.getStatus() == Status.IN_PROGRESS) {
                // и нужен полный пересчёт
                calcStatus(epic);
            }
        }
    }

    // полный пересчёт статуса
    private void calcStatus(Epic epic) {
        if (epic.getSubtasks().size() > 0) {
            // временные переменные для подсчёта итогового статуса
            Status hiStatus = Status.DONE;
            Status lowStatus = Status.NEW;
            // за один проход по всем подзадачам эпика пересчитать статус
            for (Subtask subtask : epic.getSubtasks().values()) {
                if (subtask.getStatus() == Status.NEW) {
                    hiStatus = Status.IN_PROGRESS;
                }
                if (subtask.getStatus() == Status.IN_PROGRESS) {
                    // есть подзадача IN_PROGRESS - статус эпика может быть только IN_PROGRESS
                    epic.setStatus(Status.IN_PROGRESS);
                    return;
                }
                if (subtask.getStatus() == Status.DONE) {
                    lowStatus = Status.IN_PROGRESS;
                }
                if (lowStatus == hiStatus) {
                    // если обе переменные сравнялись, то только на IN_PROGRESS
                    epic.setStatus(Status.IN_PROGRESS);
                    return;
                }
            }

            if (hiStatus == Status.DONE) {
                epic.setStatus(Status.DONE);
            }
            if (lowStatus == Status.NEW) {
                epic.setStatus(Status.NEW);
            }
        } else {
            // нет подзадач - статус NEW
            epic.setStatus(Status.NEW);
        }
    }

    @Override
    public String toString() {
        String res = "TaskManager{\n";
        for (Task task : taskList.values()) {
            res = res.concat(task.toString());
        }
        for (Task task : epicList.values()) {
            res = res.concat(task.toString());
        }
        res = res.concat("}\n");
        return res;
    }
}
