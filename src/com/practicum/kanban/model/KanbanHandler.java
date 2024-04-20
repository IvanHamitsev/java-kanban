package com.practicum.kanban.model;

import com.practicum.kanban.service.HttpTaskServer;
import com.practicum.kanban.service.TaskManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public abstract class KanbanHandler<T extends Task> implements HttpHandler {
    TaskManager taskManager;

    public KanbanHandler() {
        this.taskManager = HttpTaskServer.getTaskManager();
    }

    public abstract String getAllTasksFunction();

    public abstract T getTaskFunction(Integer id);

    public abstract Integer addTaskFunction(T task);

    public abstract Integer updateTaskFunction(T task);

    public abstract void deleteTaskFunction(Integer id);

    public abstract T convertToTFunction(String json);

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String metod = httpExchange.getRequestMethod();
        String[] path = httpExchange.getRequestURI().getPath().split("/");
        switch (metod) {
            case "GET":
                sendAnswer(httpExchange, parseGet(path));
                break;
            case "POST":
                sendAnswer(httpExchange, parsePost(httpExchange));
                break;
            case "DELETE":
                sendAnswer(httpExchange, parseDelete(path));
                break;
            default:
                // 400 Bad Request
                sendAnswer(httpExchange, new AnswerSet(400, ""));
        }
    }

    private void sendAnswer(HttpExchange httpExchange, AnswerSet set) throws IOException {
        try (OutputStream os = httpExchange.getResponseBody()) {
            httpExchange.sendResponseHeaders(set.code, 0);
            os.write(set.body.getBytes());
        }
    }

    protected AnswerSet parseGet(String[] path) {
        // В случае /tasks вызвать метод получения всех задач
        if (path.length == 2) {
            String str = getAllTasksFunction();
            // если задач нет, будет пустой лист, но по заданию для этой функции не предусмотрена ошибка
            return new AnswerSet(200, str);
        }
        // В случае /tasks/{id} вызываем метод получения конкретной задачи
        if (path.length == 3) {
            try {
                T task = getTaskFunction(Integer.parseInt(path[2]));
                if (null != task) {
                    return new AnswerSet(200, JsonConverter.convert(task));
                } else {
                    // если задачи нет
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

    protected AnswerSet parsePost(HttpExchange httpExchange) {
        try {
            String[] path = httpExchange.getRequestURI().getPath().split("/");
            // достать из тела саму задачу
            String body = new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            T task = convertToTFunction(body);
            // без указания id создать новый элемент
            if (path.length == 2) {
                Integer id = addTaskFunction(task);
                if (0 < id) {
                    return new AnswerSet(201, JsonConverter.convert(id));
                } else {
                    // если задача пересекается с существующей, вернуть 406
                    return new AnswerSet(406, "");
                }
            }
            // если указан /tasks/{id} то обновление
            if (path.length == 3) {
                try {
                    // собственно сам id из пути нам не нужен, он лишь признак выполнить обновление
                    if (0 < Integer.parseInt(path[2])) {
                        Integer id = updateTaskFunction(task);
                        if (0 < id) {
                            return new AnswerSet(201, "");
                        } else {
                            return new AnswerSet(406, "");
                        }
                    } else {
                        return new AnswerSet(400, "");
                    }
                } catch (NumberFormatException e) {
                    return new AnswerSet(406, "");
                }
            }
            // 400 Bad Request
            return new AnswerSet(400, "");
        } catch (IOException e) {
            return new AnswerSet(400, "");
        }
    }

    protected AnswerSet parseDelete(String[] path) {
        // Всегда должен быть аргумент id: /tasks/{id}
        if (path.length == 3) {
            try {
                deleteTaskFunction(Integer.parseInt(path[2]));
                return new AnswerSet(200, "");
            } catch (NumberFormatException e) {
                // 400 Bad Request
                return new AnswerSet(400, "");
            }
        }
        // 400 Bad Request
        return new AnswerSet(400, "");
    }

}
