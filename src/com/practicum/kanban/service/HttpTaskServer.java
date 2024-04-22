package com.practicum.kanban.service;

import com.practicum.kanban.model.*;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    public static final int PORT = 8080;
    private static HttpServer httpServer;
    private static TaskManager taskManager;

    public static void main(String[] args) {
        try {
            initHttpTaskServer(Managers.getFileTaskManager("kanban.csv"));
            serverStart();

        } catch (IOException e) {
            System.out.println("Не удалось сконфигурировать http server");
        }
    }

    public static void initHttpTaskServer(TaskManager manager) throws IOException {
        httpServer = HttpServer.create();
        httpServer.bind(new InetSocketAddress(PORT), 0);
        taskManager = manager;
        httpServer.createContext("/tasks", new TasksHandler());
        httpServer.createContext("/subtasks", new SubtasksHandler());
        httpServer.createContext("/epics", new EpicHandler());
        httpServer.createContext("/history", new HistoryHandler());
        httpServer.createContext("/prioritized", new PrioritizeHandler());
    }

    public static void serverStart() {
        httpServer.start();
    }

    public static void serverStop() {
        httpServer.stop(1);
    }

    public static TaskManager getTaskManager() {
        return taskManager;
    }

    // пересоздавать TaskManager требуется например при тестировании
    public static void setTaskManager(TaskManager tm) {
        taskManager = tm;
    }
}
