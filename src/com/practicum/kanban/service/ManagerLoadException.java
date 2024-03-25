package com.practicum.kanban.service;

public class ManagerLoadException extends RuntimeException {
    ManagerLoadException(String message) {
        super(message);
    }
}
