package com.practicum.kanban.service;

import com.practicum.kanban.model.Task;

import java.util.List;

public interface HistoryManager {
    public void add(Task task);
    public List<Task> getHistory();
}
