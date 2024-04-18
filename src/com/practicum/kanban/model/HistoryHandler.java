package com.practicum.kanban.model;

import com.practicum.kanban.service.HttpTaskServer;
import com.practicum.kanban.service.TaskManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class HistoryHandler implements HttpHandler {

    TaskManager taskManager;

    public HistoryHandler() {
        this.taskManager = HttpTaskServer.getTaskManager();
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String metod = httpExchange.getRequestMethod();
        String[] path = httpExchange.getRequestURI().getPath().split("/");
        switch (metod) {
            case "GET":
                if (path.length == 2) {
                    String str = JsonConverter.convert(taskManager.getHistory());
                    sendAnswer(httpExchange, new AnswerSet(200, str));
                } else {
                    sendAnswer(httpExchange, new AnswerSet(400, ""));
                }
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
}
