package com.practicum.kanban.service;

import com.practicum.kanban.model.NonRepeatLinearHistoryStorage;
import com.practicum.kanban.model.HistoryStorage;
import com.practicum.kanban.model.NonRepeatRingBuffer;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static TaskManager getFileTaskManager(String filePath) {
        return new FileBackedTaskManager(filePath);
    }

    public static TaskManager getFileTaskManager() {
        return new FileBackedTaskManager();
    }

    public static HistoryManager getDefaultHistoryManager() {
        return new InMemoryHistoryManager(Managers.getDefaultHistoryStorage());
    }

    public static HistoryStorage getDefaultHistoryStorage() {
        return new NonRepeatLinearHistoryStorage();
    }

    public static HistoryStorage getRingHistoryStorage(int size) {
        return new NonRepeatRingBuffer(size);
    }
}
