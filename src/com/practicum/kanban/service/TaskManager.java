package com.practicum.kanban.service;

import com.practicum.kanban.model.*;

import java.util.HashMap;

public class TaskManager {
    // Класс TaskManager хранит только Task и Epic
    // Подзадачи каждый Epic хранит самостоятельно
    private HashMap<Integer, Task> taskList = new HashMap<>();
    private HashMap<Integer, Epic> epicList = new HashMap<>();

    // a. Получение списка всех задач.
    public HashMap<Integer, Task> getTaskList() {
        // не хочу отдавать сам список, отдадим его копию, чтобы не вмешались в оригинал
        HashMap<Integer, Task> res = (HashMap<Integer, Task>) taskList.clone();
        return res;
    }

    public HashMap<Integer, Epic> getEpicList() {
        // отдать копию
        HashMap<Integer, Epic> res = (HashMap<Integer, Epic>) epicList.clone();
        return res;
    }

    public HashMap<Integer, Subtask> grtSubtaskList(Integer epicId) {
        HashMap<Integer, Subtask> result = null;
        Epic epic = epicList.get(epicId);
        if (null != epic) {
            // не нужно проверять на null Subtask, он проинициализирован при созадании
            result = (HashMap<Integer, Subtask>) epic.getSubtasks().clone();
        }
        return result;
    }

    // b. Удаление всех задач.
    public void deleteAllTasks() {
        taskList.clear();
    }

    public void deleteAllEpics() {
        epicList.clear();
    }

    public void deleteSubtasks(Integer epicId) {
        Epic epic = epicList.get(epicId);
        if (null != epic) {
            epic.getSubtasks().clear();
            // обновить статус эпика
            epic.setStatus(Status.NEW);
        }
    }

    // c. Получение по идентификатору.
    public Task getTask(Integer taskId) throws CloneNotSupportedException {
        if (taskList.get(taskId) != null) {
            // отдаём копию
            return (Task) taskList.get(taskId).clone();
        }
        return null;
    }

    public Epic getEpic(Integer taskId) throws CloneNotSupportedException {
        if (epicList.get(taskId) != null) {
            // отдаём копию
            return (Epic) epicList.get(taskId).clone();
        }
        return null;
    }

    public Subtask getSubtask(Integer epicId, Integer taskId) {
        Epic epic = epicList.get(epicId);
        if (null != epic) {
            return epic.getSubtasks().get(taskId);
        }
        return null;
    }

    // тот же метод, но тяжелее, зато не требует знать epicId
    public Subtask getSubtask(Integer taskId) {
        for (Epic epic : epicList.values()) {
            Subtask result = epic.getSubtasks().get(taskId);
            if (null != result) {
                return result;
            }
        }
        return null;
    }

    // d. Создание. Сам объект должен передаваться в качестве параметра.
    public void addTask(Task task) throws CloneNotSupportedException {
        // добавим копию полученной задачи, чтобы у пользователя не оставалось ссылки для
        // несанкционированного доступа
        if (task != null) {
            Task newTask = (Task) task.clone();
            taskList.put(newTask.getTaskId(), newTask);
        }
    }

    public void addEpic(Epic epic) throws CloneNotSupportedException {
        if (epic != null) {
            Epic newEpic = (Epic) epic.clone();
            epicList.put(newEpic.getTaskId(), newEpic);
        }
    }

    // добавление Subtask если указан эпик
    public void addSubtask(Integer epicId, Subtask subtask) throws CloneNotSupportedException {
        Epic epic = epicList.get(epicId);
        if (null != epic) {
            Subtask newSubtask = (Subtask) subtask.clone();
            // клонирование не проставляет ссылки, проставим их
            newSubtask.setParentId(epic.getTaskId());
            epic.getSubtasks().put(newSubtask.getTaskId(), newSubtask);
            // обновить статус эпика
            calcStatusAdd(epic, newSubtask.getStatus());
        }
    }

    // добавление Subtask если не указан эпик
    public void addSubtask(Subtask subtask) throws CloneNotSupportedException {
        Epic epic = (Epic) epicList.get(subtask.getParentId());
        if (epic != null) {
            addSubtask(epic.getTaskId(), subtask);
        }
    }

    //  e. Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
    public void updateTask(Task task) throws CloneNotSupportedException {
        if (task != null) {
            deleteTask(task.getTaskId());
            addTask(task);
        }
    }

    public void updateEpic(Epic epic) throws CloneNotSupportedException {
        if (epic != null) {
            deleteEpic(epic.getTaskId());
            addTask(epic);
        }
    }

    public void updateSubtask(Subtask subtask) throws CloneNotSupportedException {
        Epic epic = (Epic) epicList.get(subtask.getParentId());
        if (epic != null) {
            deleteSubtask(epic.getTaskId(), subtask.getTaskId());
            addSubtask(subtask);
            // пересчёты статусов есть внутри функций
        }
    }

    // f. Удаление по идентификатору.
    public void deleteTask(Integer taskId) {
        taskList.remove(taskId);
    }

    public void deleteEpic(Integer taskId) {
        epicList.remove(taskId);
    }

    // с указанием эпика
    public void deleteSubtask(Integer epicId, Integer subtaskId) {
        Epic epic = (Epic) epicList.get(epicId);
        if (null != epic) {
            Status status = epic.getSubtasks().get(subtaskId).getStatus();
            epic.getSubtasks().remove(subtaskId);
            calcStatusRemove(epic, status);
        }
    }

    // без указания эпика зная объект Subtask
    public void deleteSubtask(Subtask subtask) {
        Epic epic = (Epic) epicList.get(subtask.getParentId());
        if (null != epic) {
            Status status = epic.getSubtasks().get(subtask.getTaskId()).getStatus();
            epic.getSubtasks().remove(subtask.getTaskId());
            calcStatusRemove(epic, status);
        }
    }

    // без указания эпика зная subtaskId
    public void deleteSubtask(Integer subtaskId) {
        for (Epic epic : epicList.values()) {
            Task deletedSubtask = epic.getSubtasks().remove(subtaskId);
            if (null != deletedSubtask) {
                // выход - удаляем лишь первое вхождение
                return;
            }
        }
    }

    // Дополнительные методы:
    // a. Получение списка всех подзадач определённого эпика.
    public HashMap<Integer, Subtask> getSubtaskList(Integer epicId) {
        HashMap<Integer, Subtask> result = null;
        Epic epic = (Epic) epicList.get(epicId);
        if (null != epic) {
            result = (HashMap<Integer, Subtask>) epic.getSubtasks().clone();
        }
        return result;
    }

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
