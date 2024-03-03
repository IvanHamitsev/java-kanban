package com.practicum.kanban.service;

import com.practicum.kanban.model.NonRepeatRingBuffer;
import com.practicum.kanban.model.RingBuffer;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager(10);
    }

    public static RingBuffer getDefaultHistoryStorage(int size) {
        return new NonRepeatRingBuffer(size);
    }
}
