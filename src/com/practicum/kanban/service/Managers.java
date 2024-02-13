package com.practicum.kanban.service;

public class Managers {
    public TaskManager getDefault() {
        TaskManager memoryTaskManager = new InMemoryTaskManager();
        return memoryTaskManager;
    }
    public HistoryManager getDefaultHistory() {
        HistoryManager memoryHistoryManager = new InMemoryHistoryManager(10);
        return memoryHistoryManager;
    }
}
