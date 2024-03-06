package com.practicum.kanban.service;

import com.practicum.kanban.model.NonRepeatLinearHistoryStorage;
import com.practicum.kanban.model.HistoryStorage;
import com.practicum.kanban.model.NonRepeatRingBuffer;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static HistoryStorage getDefaultHistoryStorage() {
        return new NonRepeatLinearHistoryStorage();
    }

    public static HistoryStorage getRingHistoryStorage(int size) {
        return new NonRepeatRingBuffer(size);
    }
}
