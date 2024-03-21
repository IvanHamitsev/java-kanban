package com.practicum.kanban.service;

public class ManagerSaveException extends RuntimeException {
    ManagerSaveException(String message) {
        super(message);
    }
}
