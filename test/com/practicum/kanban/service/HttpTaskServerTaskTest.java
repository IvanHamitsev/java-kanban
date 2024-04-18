package com.practicum.kanban.service;

import com.practicum.kanban.model.JsonConverter;
import com.practicum.kanban.model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpTaskServerTaskTest {
    static HttpClient httpClient;
    static String serverUrlPart = "http://localhost"; //http://localhost:8080/hello
    static int port = 8080;

    @BeforeAll
    static void initHttpTaskTest() {
        // создать
        assertDoesNotThrow(() -> {
                    HttpTaskServer.initHttpTaskServer(port, Managers.getFileTaskManager());
                },
                "создание HTTP сервера вызывает исключение");
        httpClient = HttpClient.newHttpClient();
    }

    @BeforeEach
    void prepareHttpServerForTest() {
        HttpTaskServer.serverStart();
        // для чистого эксперимента надо пересоздать TaskManager
        HttpTaskServer.setTaskManager(Managers.getFileTaskManager());
    }

    @AfterEach
    void closeHttpServerAfterTest() {
        HttpTaskServer.serverStop();
    }

    @Test
    void shouldCreateTask() {
        URI uri = URI.create(serverUrlPart + ":" + port + "/tasks");

        // подготовить данные
        Task task = new Task("Задача", "Описание");

        // подготовить запрос
        String requestBody = JsonConverter.convert(task);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        // отправить запрос
        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
        try {
            HttpResponse<String> response = httpClient.send(request, handler);
            // проверить статус код ответа
            assertTrue(response.statusCode() == 200, "сервер не может создать задачу");
        } catch (InterruptedException | IOException e) {
            System.out.println("отправка HTTP запроса на создание задачи вызывает исключение");
        }
    }

}