package com.practicum.kanban.service;

import com.practicum.kanban.model.Task;

import java.util.List;

public interface HistoryManager {
    void add(Task task);
    List<Task> getHistory();
}
