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
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpTaskServerTaskTest {
    static HttpClient httpClient;
    static String serverUrlPart = "http://localhost";

    @BeforeAll
    static void initHttpTaskTest() {
        // создать только клиент, сервер будет пересоздаваться для каждого теста
        httpClient = HttpClient.newHttpClient();
    }

    @BeforeEach
    void prepareHttpServerForTest() {
        try {
            HttpTaskServer.initHttpTaskServer(Managers.getFileTaskManager());
            HttpTaskServer.serverStart();
        } catch (IOException e) {
            System.out.println("создание HTTP сервера вызывает исключение");
        }
    }

    @AfterEach
    void closeHttpServerAfterTest() {
        // очистить taskManager
        HttpTaskServer.getTaskManager().clearInstance();
        // остановить сервер
        HttpTaskServer.serverStop();
    }

    @Test
    void shouldCreateTask() {
        URI uri = URI.create(serverUrlPart + ":" + HttpTaskServer.PORT + "/tasks");

        // подготовить данные
        Task task = new Task("Задача", "Описание");
        int taskId = task.getTaskId();

        // подготовить запрос
        String requestBody = JsonConverter.convert(task);
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        // отправить запрос
        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
        try {
            HttpResponse<String> response = httpClient.send(request1, handler);
            // проверить статус код ответа
            assertEquals(201, response.statusCode(), "сервер не может создать задачу");
        } catch (InterruptedException | IOException e) {
            System.out.println("отправка HTTP запроса на создание задачи вызывает исключение");
        }

        // получить задачу обратно
        uri = URI.create(serverUrlPart + ":" + HttpTaskServer.PORT + "/tasks/" + taskId);
        request1 = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        // получить другую задачу
        uri = URI.create(serverUrlPart + ":" + HttpTaskServer.PORT + "/tasks/" + taskId + 1);
        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        // отправить запрос
        try {
            HttpResponse<String> response = httpClient.send(request1, handler);
            // проверить статус код ответа
            assertEquals(200, response.statusCode(), "сервер не вернул задачу");
            Task inpTask = JsonConverter.convertToTask(response.body());
            assertTrue(task.equals(inpTask), "получена задача не эквивалентная исходной");

            response = httpClient.send(request2, handler);
            // проверить статус код ответа
            assertEquals(404, response.statusCode(), "сервер вернул несуществующую задачу");
        } catch (InterruptedException | IOException e) {
            System.out.println("отправка HTTP запроса на получение задачи вызывает исключение");
        }
    }

    @Test
    void shouldFormTaskList() {
        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();

        // подготовить данные
        Task task1 = new Task("Задача1", "Описание1");
        int task1Id = task1.getTaskId();
        Task task2 = new Task("Задача2", "Описание2");
        int task2Id = task2.getTaskId();
        Task task3 = new Task("Задача3", "Описание3");
        int task3Id = task3.getTaskId();

        // подготовить запросы

        URI uri = URI.create(serverUrlPart + ":" + HttpTaskServer.PORT + "/tasks");
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
            assertEquals(201, response.statusCode(), "сервер не может создать задачу");

            response = httpClient.send(request2, handler);
            assertEquals(201, response.statusCode(), "сервер не может создать задачу");

            response = httpClient.send(request3, handler);
            assertEquals(201, response.statusCode(), "сервер не может создать задачу");
        } catch (InterruptedException | IOException e) {
            System.out.println("отправка HTTP запроса на создание задачи вызывает исключение");
        }

        // получить список задач
        uri = URI.create(serverUrlPart + ":" + HttpTaskServer.PORT + "/tasks");
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
            assertEquals(200, response.statusCode(), "сервер не вернул список задач");
            var taskMap = JsonConverter.convertToMap(response.body());
            assertEquals(3, taskMap.size(), "полученный список отличается от созданных задач");
        } catch (InterruptedException | IOException e) {
            System.out.println("отправка HTTP запроса на получение задачи вызывает исключение");
        }
    }

    @Test
    void shouldDeleteTask() {
        URI uri = URI.create(serverUrlPart + ":" + HttpTaskServer.PORT + "/tasks");

        // подготовить данные
        Task task = new Task("Задача", "Описание");
        int taskId = task.getTaskId();

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
            assertEquals(201, response.statusCode(), "сервер не может создать задачу");
        } catch (InterruptedException | IOException e) {
            System.out.println("отправка HTTP запроса на создание задачи вызывает исключение");
        }

        // удалить задачу
        uri = URI.create(serverUrlPart + ":" + HttpTaskServer.PORT + "/tasks/" + taskId);
        request = HttpRequest.newBuilder()
                .uri(uri)
                .DELETE()
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();
        // отправить запрос
        try {
            HttpResponse<String> response = httpClient.send(request, handler);
            // проверить статус код ответа
            assertEquals(200, response.statusCode(), "сервер не удалил задачу");
        } catch (InterruptedException | IOException e) {
            System.out.println("отправка HTTP запроса на получение задачи вызывает исключение");
        }

        // получить список задач
        uri = URI.create(serverUrlPart + ":" + HttpTaskServer.PORT + "/tasks");
        request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();
        // отправить запрос
        try {
            HttpResponse<String> response = httpClient.send(request, handler);
            // проверить статус код ответа
            assertEquals(200, response.statusCode(), "сервер не вернул пустой список задач");
            var taskMap = JsonConverter.convertToMap(response.body());
            assertTrue(taskMap.isEmpty(), "получен не пустой список задач");
        } catch (InterruptedException | IOException e) {
            System.out.println("отправка HTTP запроса на получение списка задач вызывает исключение");
        }
    }

    @Test
    void shouldNotAddIntersectionTask() {
        URI uri = URI.create(serverUrlPart + ":" + HttpTaskServer.PORT + "/tasks");

        // подготовить данные
        LocalDateTime dateTime = LocalDateTime.of(2024, 4, 6, 12, 0);

        Task task1 = new Task("Задача1", "Описание1", dateTime, Duration.ofMinutes(16));
        Task task2 = new Task("Задача2", "Описание2", dateTime.plus(Duration.ofMinutes(15)), Duration.ofMinutes(15));
        Task task3 = new Task("Задача3", "Описание3", dateTime.plus(Duration.ofHours(2)), Duration.ofMinutes(1));

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
            assertEquals(201, response.statusCode(), "сервер не может создать задачу 1");

            response = httpClient.send(request2, handler);
            assertEquals(406, response.statusCode(), "сервер создал пересекающуюся задачу");

            response = httpClient.send(request3, handler);
            assertEquals(201, response.statusCode(), "сервер не может создать задачу 3");
        } catch (InterruptedException | IOException e) {
            System.out.println("отправка HTTP запроса на создание задачи вызывает исключение");
        }

        // получить список задач
        uri = URI.create(serverUrlPart + ":" + HttpTaskServer.PORT + "/tasks");
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
            assertEquals(200, response.statusCode(), "сервер не вернул список задач");
            var taskMap = JsonConverter.convertToMap(response.body());
            assertEquals(2, taskMap.size(), "полученный список отличается от созданных задач");
        } catch (InterruptedException | IOException e) {
            System.out.println("отправка HTTP запроса на получение задачи вызывает исключение");
        }
    }
}