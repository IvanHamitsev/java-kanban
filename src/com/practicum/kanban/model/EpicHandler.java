package com.practicum.kanban.model;

import com.sun.net.httpserver.HttpHandler;

public class EpicHandler extends KanbanHandler<Epic> implements HttpHandler {
    public EpicHandler() {
        super();
    }

    @Override
    public String getAllTasksFunction() {
        return JsonConverter.convert(taskManager.getEpicList());
    }

    @Override
    public Epic getTaskFunction(Integer id) {
        return taskManager.getEpic(id);
    }

    @Override
    public Integer addTaskFunction(Epic task) {
        return taskManager.addEpic(task);
    }

    @Override
    public Integer updateTaskFunction(Epic task) {
        return taskManager.updateEpic(task);
    }

    @Override
    public void deleteTaskFunction(Integer id) {
        taskManager.deleteEpic(id);
    }

    @Override
    public Epic convertToTFunction(String json) {
        return JsonConverter.convertToEpic(json);
    }

    // Для эпика обработка метода GET отличается
    @Override
    protected AnswerSet parseGet(String[] path) {
        // В случае /tasks вызвать метод получения всех задач
        if (path.length == 2) {
            String str = getAllTasksFunction();
            // если эпика нет, будет пустой лист
            return new AnswerSet(200, str);
        }
        // В случае /epics/{id}
        if (path.length == 3) {
            try {
                Epic task = getTaskFunction(Integer.parseInt(path[2]));
                if (null != task) {
                    return new AnswerSet(200, JsonConverter.convert(task));
                } else {
                    return new AnswerSet(404, "");
                }
            } catch (NumberFormatException e) {
                // 400 Bad Request
                return new AnswerSet(400, "");
            }
        }
        // В случае /epics/{id}/subtasks
        if ((path.length == 4) && (path[3].equals("subtasks"))) {
            try {
                var list = taskManager.getSubtaskList(Integer.parseInt(path[2]));
                if (null != list) {
                    return new AnswerSet(200, JsonConverter.convert(list));
                } else {
                    return new AnswerSet(404, "");
                }
            } catch (NumberFormatException e) {
                // 400 Bad Request
                return new AnswerSet(400, "");
            }
        }
        // 400 Bad Request
        return new AnswerSet(400, "");
    }
}
