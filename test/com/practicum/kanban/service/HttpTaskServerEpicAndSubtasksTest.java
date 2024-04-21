package com.practicum.kanban.service;

import com.practicum.kanban.model.Epic;
import com.practicum.kanban.model.JsonConverter;
import com.practicum.kanban.model.Status;
import com.practicum.kanban.model.Subtask;
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

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerEpicAndSubtasksTest {
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
    void shouldCreate() {
        // подготовить данные
        Epic epic = new Epic("Эпик", "Описание");
        int epicId = epic.getTaskId();

        Subtask sub1 = new Subtask("Подзадача1", "Описание");
        int sub1Id = sub1.getTaskId();

        LocalDateTime dateTime = LocalDateTime.of(2024, 4, 6, 12, 0);
        sub1.setStatus(Status.IN_PROGRESS);
        sub1.setTime(dateTime, Duration.ofMinutes(15));

        Subtask sub2 = new Subtask("Подзадача2", "Описание");
        int sub2Id = sub2.getTaskId();

        sub1.setParentId(epicId);
        sub2.setParentId(epicId);

        // добавить пока пустой эпик
        URI uri = URI.create(serverUrlPart + ":" + HttpTaskServer.PORT + "/epics");
        String requestBody = JsonConverter.convert(epic);
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        // запрос создания пустого эпика
        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
        try {
            HttpResponse<String> response = httpClient.send(request1, handler);
            // проверить статус код ответа
            assertEquals(201, response.statusCode(), "сервер не может создать пустой эпик'");
        } catch (InterruptedException | IOException e) {
            System.out.println("отправка HTTP запроса на создание задачи вызывает исключение");
        }

        // получить эпик обратно
        uri = URI.create(serverUrlPart + ":" + HttpTaskServer.PORT + "/epics/" + epicId);
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
            assertEquals(200, response.statusCode(), "сервер не вернул пустой эпик");
            var inpEpic = JsonConverter.convertToEpic(response.body());
            assertTrue(epic.equals(inpEpic), "получен эпик, не эквивалентный исходному");
        } catch (InterruptedException | IOException e) {
            System.out.println("отправка HTTP запроса на получение задачи вызывает исключение");
        }

        // добавить подзадачи эпику
        uri = URI.create(serverUrlPart + ":" + HttpTaskServer.PORT + "/subtasks");
        requestBody = JsonConverter.convert(sub1);
        request1 = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        requestBody = JsonConverter.convert(sub2);
        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        // запрос добавления эпиков
        try {
            HttpResponse<String> response = httpClient.send(request1, handler);
            // проверить статус код ответа
            assertEquals(201, response.statusCode(), "сервер не может создать подзадачу 1'");

            response = httpClient.send(request2, handler);
            // проверить статус код ответа
            assertEquals(201, response.statusCode(), "сервер не может создать подзадачу 2'");
        } catch (InterruptedException | IOException e) {
            System.out.println("отправка HTTP запроса на создание задачи вызывает исключение");
        }

        // получить обновлённый непустой эпик
        uri = URI.create(serverUrlPart + ":" + HttpTaskServer.PORT + "/epics/" + epicId);
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
            assertEquals(200, response.statusCode(), "сервер не вернул эпик");
            var inpEpic = JsonConverter.convertToEpic(response.body());
            assertEquals(2, inpEpic.getSubtasks().size(), "получен эпик, не содержащий 2 подзадачи");
            assertSame(Status.IN_PROGRESS, inpEpic.getStatus(), "полученный эпик не содержит статуса подзадачи");
        } catch (InterruptedException | IOException e) {
            System.out.println("отправка HTTP запроса на получение задачи вызывает исключение");
        }
    }

    @Test
    void shouldUpdate() {
        // подготовить данные
        Epic epic = new Epic("Эпик", "Описание");
        int epicId = epic.getTaskId();

        Subtask sub1 = new Subtask("Подзадача1", "Описание");
        int sub1Id = sub1.getTaskId();
        Subtask sub2 = new Subtask("Подзадача2", "Описание");
        int sub2Id = sub2.getTaskId();

        LocalDateTime dateTime = LocalDateTime.of(2024, 4, 6, 12, 0);
        sub1.setStatus(Status.IN_PROGRESS);
        sub1.setTime(dateTime, Duration.ofMinutes(15));
        sub2.setStatus(Status.IN_PROGRESS);
        sub2.setTime(dateTime.plus(Duration.ofMinutes(15)), Duration.ofMinutes(15));

        sub1.setParentId(epicId);
        sub2.setParentId(epicId);

        // добавить эпик
        URI uri = URI.create(serverUrlPart + ":" + HttpTaskServer.PORT + "/epics");
        String requestBody = JsonConverter.convert(epic);
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        // добавить подзадачи эпику
        uri = URI.create(serverUrlPart + ":" + HttpTaskServer.PORT + "/subtasks");
        requestBody = JsonConverter.convert(sub1);
        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        requestBody = JsonConverter.convert(sub2);
        HttpRequest request3 = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        // запрос создания задач
        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
        try {
            HttpResponse<String> response = httpClient.send(request1, handler);
            assertEquals(201, response.statusCode(), "сервер не может создать эпик'");

            response = httpClient.send(request2, handler);
            assertEquals(201, response.statusCode(), "сервер не может создать подзадачу 1'");

            response = httpClient.send(request3, handler);
            assertEquals(201, response.statusCode(), "сервер не может создать подзадачу 2'");
        } catch (InterruptedException | IOException e) {
            System.out.println("отправка HTTP запроса на создание задач вызывает исключение");
        }

        // обновить подзадачи
        sub1.setDescription("Обновлённое описание 1");
        sub1.setStatus(Status.DONE);
        sub1.setTime(dateTime, Duration.ofMinutes(30));

        sub2.setDescription("Обновлённое описание 2");
        sub2.setStatus(Status.DONE);
        sub2.setTime(dateTime.plus(Duration.ofMinutes(30)), Duration.ofMinutes(60));

        // передать на сервер
        uri = URI.create(serverUrlPart + ":" + HttpTaskServer.PORT + "/subtasks/" + sub1Id);
        requestBody = JsonConverter.convert(sub1);
        request1 = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();
        uri = URI.create(serverUrlPart + ":" + HttpTaskServer.PORT + "/subtasks/" + sub2Id);
        requestBody = JsonConverter.convert(sub2);
        request2 = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        // запрос обновления подзадач
        try {
            // попытка обновить подзадачу, не влезающую в расписание
            HttpResponse<String> response = httpClient.send(request1, handler);
            assertEquals(406, response.statusCode(), "нельзя обновлять, время подзадачи 1 пересекается");
            // обновив подзадачу 2, освободим время для подзадачи 1
            response = httpClient.send(request2, handler);
            assertEquals(201, response.statusCode(), "сервер не может обновить подзадачу 2");
            // теперь подзадача 1 должна влезть в расписание
            response = httpClient.send(request1, handler);
            assertEquals(201, response.statusCode(), "сервер не может обновить подзадачу 1, а должен");
        } catch (InterruptedException | IOException e) {
            System.out.println("отправка HTTP запроса на обновление задач вызывает исключение");
        }

        // получить обновлённый эпик и подзадачи
        uri = URI.create(serverUrlPart + ":" + HttpTaskServer.PORT + "/epics/" + epicId);
        request1 = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();
        uri = URI.create(serverUrlPart + ":" + HttpTaskServer.PORT + "/subtasks/" + sub1Id);
        request2 = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();
        uri = URI.create(serverUrlPart + ":" + HttpTaskServer.PORT + "/subtasks/" + sub2Id);
        request3 = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        // отправить запрос
        try {
            HttpResponse<String> response = httpClient.send(request1, handler);
            // проверить статус код ответа
            assertEquals(200, response.statusCode(), "сервер не вернул эпик");
            var inpEpic = JsonConverter.convertToEpic(response.body());
            assertEquals(2, inpEpic.getSubtasks().size(), "получен эпик, не содержащий 2 подзадачи");
            assertSame(Status.DONE, inpEpic.getStatus(), "полученный эпик не содержит верного статуса");
            // теперь подзадачи
            response = httpClient.send(request2, handler);
            assertEquals(200, response.statusCode(), "сервер не вернул подзадачу 1");
            var inpSub1 = JsonConverter.convertToSubtask(response.body());
            assertTrue(inpSub1.equals(sub1), "оригинальная и полученная подзадача 1 не эквивалентны");
            assertSame(inpSub1.getStatus(), sub1.getStatus(), "статусы оригинальной и полученной подзадачи 1 не эквивалентны");
            response = httpClient.send(request3, handler);
            assertEquals(200, response.statusCode(), "сервер не вернул подзадачу 2");
            var inpSub2 = JsonConverter.convertToSubtask(response.body());
            assertTrue(inpSub2.equals(sub2), "оригинальная и полученная подзадача 1 не эквивалентны");
            assertSame(inpSub2.getStatus(), sub2.getStatus(), "статусы оригинальной и полученной подзадачи 1 не эквивалентны");
        } catch (InterruptedException | IOException e) {
            System.out.println("отправка HTTP запроса на получение задачи вызывает исключение");
        }
    }

    @Test
    void shouldFormList() {
        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();

        // подготовить данные
        Epic epic1 = new Epic("Эпик1", "Описание1");
        int epic1Id = epic1.getTaskId();
        Epic epic2 = new Epic("Эпик2", "Описание2");
        int epic2Id = epic2.getTaskId();

        Subtask sub1 = new Subtask("Подзадача1", "Описание");
        int sub1Id = sub1.getTaskId();
        Subtask sub2 = new Subtask("Подзадача2", "Описание");
        int sub2Id = sub2.getTaskId();

        LocalDateTime dateTime = LocalDateTime.of(2024, 4, 6, 12, 0);
        sub1.setStatus(Status.IN_PROGRESS);
        sub1.setTime(dateTime, Duration.ofMinutes(15));
        sub2.setStatus(Status.IN_PROGRESS);
        sub2.setTime(dateTime.plus(Duration.ofMinutes(15)), Duration.ofMinutes(15));

        sub1.setParentId(epic1Id);
        sub2.setParentId(epic2Id);

        // добавить эпики
        URI uri = URI.create(serverUrlPart + ":" + HttpTaskServer.PORT + "/epics");
        String requestBody = JsonConverter.convert(epic1);
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        requestBody = JsonConverter.convert(epic2);
        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        // добавить подзадачи эпикам
        uri = URI.create(serverUrlPart + ":" + HttpTaskServer.PORT + "/subtasks");
        requestBody = JsonConverter.convert(sub1);
        HttpRequest request3 = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        requestBody = JsonConverter.convert(sub2);
        HttpRequest request4 = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        // запрос создания
        try {
            HttpResponse<String> response = httpClient.send(request1, handler);
            assertEquals(201, response.statusCode(), "сервер не может создать эпик 1");

            response = httpClient.send(request2, handler);
            assertEquals(201, response.statusCode(), "сервер не может создать эпик 2");

            response = httpClient.send(request3, handler);
            assertEquals(201, response.statusCode(), "сервер не может создать подзадачу 1");

            response = httpClient.send(request4, handler);
            assertEquals(201, response.statusCode(), "сервер не может создать подзадачу 2");
        } catch (InterruptedException | IOException e) {
            System.out.println("отправка HTTP запроса на создание объектов вызывает исключение");
        }

        // получить список эпиков
        uri = URI.create(serverUrlPart + ":" + HttpTaskServer.PORT + "/epics");
        request1 = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();
        // получить список подзадач
        uri = URI.create(serverUrlPart + ":" + HttpTaskServer.PORT + "/subtasks");
        request2 = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();
        // отправить запрос
        try {
            HttpResponse<String> response = httpClient.send(request1, handler);
            // проверить статус код ответа
            assertEquals(200, response.statusCode(), "сервер не вернул список эпиков");
            var epics = JsonConverter.convertToEpicMap(response.body());
            assertEquals(2, epics.size(), "полученный список отличается от созданных эпиков");

            response = httpClient.send(request2, handler);
            // проверить статус код ответа
            assertEquals(200, response.statusCode(), "сервер не вернул список подзадач");
            var subtasks = JsonConverter.convertToSubtaskMap(response.body());
            assertEquals(2, subtasks.size(), "полученный список отличается от созданных подзадач");
        } catch (InterruptedException | IOException e) {
            System.out.println("отправка HTTP запроса на получение списка объектов вызывает исключение");
        }
    }

    @Test
    void shouldDelete() {
        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();

        // подготовить данные
        Epic epic1 = new Epic("Эпик1", "Описание1");
        int epic1Id = epic1.getTaskId();
        Epic epic2 = new Epic("Эпик2", "Описание2");
        int epic2Id = epic2.getTaskId();

        Subtask sub1 = new Subtask("Подзадача1", "Описание");
        int sub1Id = sub1.getTaskId();
        Subtask sub2 = new Subtask("Подзадача2", "Описание");
        int sub2Id = sub2.getTaskId();

        LocalDateTime dateTime = LocalDateTime.of(2024, 4, 6, 12, 0);
        sub1.setStatus(Status.DONE);
        sub1.setTime(dateTime, Duration.ofMinutes(15));
        sub2.setStatus(Status.NEW);
        sub2.setTime(dateTime.plus(Duration.ofMinutes(15)), Duration.ofMinutes(15));

        sub1.setParentId(epic1Id);
        sub2.setParentId(epic1Id);

        // добавить эпики
        URI uri = URI.create(serverUrlPart + ":" + HttpTaskServer.PORT + "/epics");
        String requestBody = JsonConverter.convert(epic1);
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        requestBody = JsonConverter.convert(epic2);
        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        // добавить подзадачи эпикам
        uri = URI.create(serverUrlPart + ":" + HttpTaskServer.PORT + "/subtasks");
        requestBody = JsonConverter.convert(sub1);
        HttpRequest request3 = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        requestBody = JsonConverter.convert(sub2);
        HttpRequest request4 = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        // запрос создания
        try {
            HttpResponse<String> response = httpClient.send(request1, handler);
            assertEquals(201, response.statusCode(), "сервер не может создать эпик 1");

            response = httpClient.send(request2, handler);
            assertEquals(201, response.statusCode(), "сервер не может создать эпик 2");

            response = httpClient.send(request3, handler);
            assertEquals(201, response.statusCode(), "сервер не может создать подзадачу 1");

            response = httpClient.send(request4, handler);
            assertEquals(201, response.statusCode(), "сервер не может создать подзадачу 2");
        } catch (InterruptedException | IOException e) {
            System.out.println("отправка HTTP запроса на создание объектов вызывает исключение");
        }

        // удалить подзадачу
        uri = URI.create(serverUrlPart + ":" + HttpTaskServer.PORT + "/subtasks/" + sub1Id);
        request1 = HttpRequest.newBuilder()
                .uri(uri)
                .DELETE()
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();
        // удалить эпик
        uri = URI.create(serverUrlPart + ":" + HttpTaskServer.PORT + "/epics/" + epic2Id);
        request2 = HttpRequest.newBuilder()
                .uri(uri)
                .DELETE()
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request1, handler);
            assertEquals(200, response.statusCode(), "сервер не может удалить подзадачу 1");

            response = httpClient.send(request2, handler);
            assertEquals(200, response.statusCode(), "сервер не может удалить эпик 2");

        } catch (InterruptedException | IOException e) {
            System.out.println("отправка HTTP запроса на удаление объектов вызывает исключение");
        }

        // получение получившегося списка эпиков
        uri = URI.create(serverUrlPart + ":" + HttpTaskServer.PORT + "/epics");
        request1 = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();
        // получить список подзадач
        uri = URI.create(serverUrlPart + ":" + HttpTaskServer.PORT + "/subtasks");
        request2 = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();
        // отправить запрос
        try {
            HttpResponse<String> response = httpClient.send(request1, handler);
            // проверить статус код ответа
            assertEquals(200, response.statusCode(), "сервер не вернул список эпиков");
            var epics = JsonConverter.convertToEpicMap(response.body());
            assertEquals(1, epics.size(), "полученный список отличается от созданных эпиков");

            response = httpClient.send(request2, handler);
            // проверить статус код ответа
            assertEquals(200, response.statusCode(), "сервер не вернул список подзадач");
            var subtasks = JsonConverter.convertToSubtaskMap(response.body());
            assertEquals(1, subtasks.size(), "полученный список отличается от созданных подзадач");
        } catch (InterruptedException | IOException e) {
            System.out.println("отправка HTTP запроса на получение списка объектов вызывает исключение");
        }
    }
}
