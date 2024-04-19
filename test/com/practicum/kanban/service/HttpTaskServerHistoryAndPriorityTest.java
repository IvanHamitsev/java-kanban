package com.practicum.kanban.service;

import com.practicum.kanban.model.JsonConverter;
import com.practicum.kanban.model.Task;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpTaskServerHistoryAndPriorityTest {
    static HttpClient httpClient;
    static String serverUrlPart = "http://localhost";
    static int port = 8080;

    @BeforeAll
    static void initHttpTaskTest() {
        // создать
        assertDoesNotThrow(() -> {
                    HttpTaskServer.initHttpTaskServer(port, Managers.getFileTaskManager());
                },
                "создание HTTP сервера вызывает исключение");
        httpClient = HttpClient.newHttpClient();
        HttpTaskServer.serverStart();
    }

    @BeforeEach
    void prepareHttpServerForTest() {
        // для чистого эксперимента надо пересоздать TaskManager
        HttpTaskServer.setTaskManager(Managers.getFileTaskManager());
    }

    @AfterEach
    void closeHttpServerAfterTest() {
        // очистить taskManager
        HttpTaskServer.getTaskManager().deleteAllTasks();
        HttpTaskServer.getTaskManager().deleteAllEpics();
    }

    @AfterAll
    static void stopHttpServer() {
        HttpTaskServer.serverStop();
    }

    @Test
    void shouldFormHistory() {
        URI uri = URI.create(serverUrlPart + ":" + port + "/tasks");

        // подготовить данные
        Task task1 = new Task("Задача1", "Описание1");
        int task1Id = task1.getTaskId();
        Task task2 = new Task("Задача2", "Описание2");
        int task2Id = task2.getTaskId();
        Task task3 = new Task("Задача3", "Описание3");
        int task3Id = task3.getTaskId();

        // подготовить запросы
        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();

        String requestBody1 = JsonConverter.convert(task1);
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody1))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        String requestBody2 = JsonConverter.convert(task2);
        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody2))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        String requestBody3 = JsonConverter.convert(task3);
        HttpRequest request3 = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody3))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        // отправить запросы на создание задач
        try {
            HttpResponse<String> response = httpClient.send(request1, handler);
            assertTrue(response.statusCode() == 201, "сервер не может создать задачу 1");

            response = httpClient.send(request2, handler);
            assertTrue(response.statusCode() == 201, "сервер не может создать задачу 2");

            response = httpClient.send(request3, handler);
            assertTrue(response.statusCode() == 201, "сервер не может создать задачу 3");
        } catch (InterruptedException | IOException e) {
            System.out.println("отправка HTTP запроса на создание задачи вызывает исключение");
        }

        // получить задачи для формирования истории
        uri = URI.create(serverUrlPart + ":" + port + "/tasks/" + task1Id);
        request1 = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        uri = URI.create(serverUrlPart + ":" + port + "/tasks/" + task2Id);
        request2 = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        uri = URI.create(serverUrlPart + ":" + port + "/tasks/" + task3Id);
        request3 = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request1, handler);
            assertTrue(response.statusCode() == 200, "сервер не может отдать задачу 1");

            response = httpClient.send(request2, handler);
            assertTrue(response.statusCode() == 200, "сервер не может отдать задачу 2");

            response = httpClient.send(request3, handler);
            assertTrue(response.statusCode() == 200, "сервер не может отдать задачу 3");
        } catch (InterruptedException | IOException e) {
            System.out.println("отправка HTTP запроса на создание задачи вызывает исключение");
        }

        // запросить историю
        uri = URI.create(serverUrlPart + ":" + port + "/history");
        request1 = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request1, handler);
            // проверить статус код ответа
            assertTrue(response.statusCode() == 200, "сервер не вернул историю");
            var history = JsonConverter.convertToList(response.body());
            assertTrue(history.size() == 3, "история сформирована неверно");
        } catch (InterruptedException | IOException e) {
            System.out.println("отправка HTTP запроса на получение истории вызывает исключение");
        }
    }

    @Test
    void shouldFormPriorityList() {
        URI uri = URI.create(serverUrlPart + ":" + port + "/tasks");

        // подготовить данные
        LocalDateTime dateTime = LocalDateTime.of(2024, 4, 6, 12, 0);

        Task task1 = new Task("Задача1", "Описание1", dateTime.plus(Duration.ofHours(2)), Duration.ofMinutes(15));
        Task task2 = new Task("Задача2", "Описание2", dateTime.plus(Duration.ofMinutes(15)), Duration.ofMinutes(15));
        Task task3 = new Task("Задача3", "Описание3", dateTime.plus(Duration.ofHours(1)), Duration.ofMinutes(1));

        // подготовить запросы
        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();

        String requestBody1 = JsonConverter.convert(task1);
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody1))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        String requestBody2 = JsonConverter.convert(task2);
        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody2))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        String requestBody3 = JsonConverter.convert(task3);
        HttpRequest request3 = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody3))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        // отправить запросы
        try {
            HttpResponse<String> response = httpClient.send(request1, handler);
            assertTrue(response.statusCode() == 201, "сервер не может создать задачу 1");

            response = httpClient.send(request2, handler);
            assertTrue(response.statusCode() == 201, "сервер не может создать задачу 2");

            response = httpClient.send(request3, handler);
            assertTrue(response.statusCode() == 201, "сервер не может создать задачу 3");
        } catch (InterruptedException | IOException e) {
            System.out.println("отправка HTTP запроса на создание задачи вызывает исключение");
        }

        // получить приоритетный список задач
        uri = URI.create(serverUrlPart + ":" + port + "/prioritized");
        request1 = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();
        // отправить запрос
        try {
            HttpResponse<String> response = httpClient.send(request1, handler);
            // проверить статус код ответа
            assertTrue(response.statusCode() == 200, "сервер не вернул приоритетный список задач");
            var taskList = JsonConverter.convertToList(response.body());
            assertTrue(taskList.size() == 3, "полученный список отличается от созданных задач");
        } catch (InterruptedException | IOException e) {
            System.out.println("отправка HTTP запроса на получение задачи вызывает исключение");
        }
    }
}
