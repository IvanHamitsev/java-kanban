package com.practicum.kanban.model;

import com.sun.net.httpserver.HttpHandler;

public class TasksHandler extends KanbanHandler<Task> implements HttpHandler {

    public TasksHandler() {
        super();
    }

    @Override
    public String getAllTasksFunction() {
        return JsonConverter.convert(taskManager.getTaskList());
    }

    @Override
    public Task getTaskFunction(Integer id) {
        return taskManager.getTask(id);
    }

    @Override
    public Integer addTaskFunction(Task task) {
        return taskManager.addTask(task);
    }

    @Override
    public Integer updateTaskFunction(Task task) {
        return taskManager.updateTask(task);
    }

    @Override
    public void deleteTaskFunction(Integer id) {
        taskManager.deleteTask(id);
    }

    @Override
    public Task convertToTFunction(String json) {
        return JsonConverter.convertToTask(json);
    }
}
