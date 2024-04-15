package com.practicum.kanban.service;

import com.practicum.kanban.model.*;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static int PORT;
    HttpServer httpServer;

    public HttpTaskServer(int port) throws IOException {
        PORT = port;
        httpServer = HttpServer.create();
        httpServer.bind(new InetSocketAddress(PORT), 0);

        httpServer.createContext("/tasks", new TasksHandler());
        httpServer.createContext("/subtasks", new SubtasksHandler());
        httpServer.createContext("/epics", new EpicHandler());
        httpServer.createContext("/history", new HistoryHandler());
        httpServer.createContext("/prioritized", new PrioritizeHandler());
    }

    public HttpTaskServer() throws IOException {
        this(8080);
    }

    public void serverStart() {
        httpServer.start();
    }

    public void serverStop() {
        httpServer.stop(0);
    }

    public HttpServer getServer() {
        return httpServer;
    }


}
