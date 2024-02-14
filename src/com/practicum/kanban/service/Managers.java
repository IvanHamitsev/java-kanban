package com.practicum.kanban.service;

public class Managers {
    public static TaskManager getDefault() {
        TaskManager memoryTaskManager = new InMemoryTaskManager();
        return memoryTaskManager;
    }
    public static HistoryManager getDefaultHistory() {
        HistoryManager memoryHistoryManager = new InMemoryHistoryManager(10);
        return memoryHistoryManager;
    }
}
