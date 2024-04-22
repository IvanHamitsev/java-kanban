package com.practicum.kanban.model;

import com.sun.net.httpserver.HttpHandler;

public class SubtasksHandler extends KanbanHandler<Subtask> implements HttpHandler {
    public SubtasksHandler() {
        super();
    }

    @Override
    public String getAllTasksFunction() {
        return JsonConverter.convert(taskManager.getSubtaskList());
    }

    @Override
    public Subtask getTaskFunction(Integer id) {
        return taskManager.getSubtask(id);
    }

    @Override
    public Integer addTaskFunction(Subtask task) {
        return taskManager.addSubtask(task);
    }

    @Override
    public Integer updateTaskFunction(Subtask task) {
        return taskManager.updateSubtask(task);
    }

    @Override
    public void deleteTaskFunction(Integer id) {
        taskManager.deleteSubtask(id);
    }

    @Override
    public Subtask convertToTFunction(String json) {
        return JsonConverter.convertToSubtask(json);
    }
}
