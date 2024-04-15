package com.practicum.kanban.model;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class TasksHandler implements HttpHandler {


    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String metod = httpExchange.getRequestMethod();
        switch (metod) {
            case "GET":
                AnswerSet result = parseGet(httpExchange);
                httpExchange.sendResponseHeaders(result.code, 0);
                sendBody(httpExchange, result.body);

                break;
            case "POST":
        }


    }

    private void sendBody(HttpExchange httpExchange, String body) throws IOException {
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(body.getBytes());
        }
    }

    private AnswerSet parseGet(HttpExchange httpExchange) {
        AnswerSet result = null;
        String[] path = httpExchange.getRequestURI().getPath().split("/");
        // В случае /tasks вызвать метод getTasks()
        if (path.length == 2) {

        }
        // В случае /tasks/{id} вызываем метод getTaskById(id)
        if (path.length == 3) {

        }
        // 400 Bad Request
        return new AnswerSet(400, "");
    }
}
