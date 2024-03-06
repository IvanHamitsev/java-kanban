package com.practicum.kanban.service;

import com.practicum.kanban.model.HistoryStorage;
import com.practicum.kanban.model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    // хранилище
    private HistoryStorage<Task> historyStorage;
    // получить последние 10 просмотренных задач

    public InMemoryHistoryManager() {
        historyStorage = Managers.getDefaultHistoryStorage();
    }
    @Override
    public void add(Task task) {
        historyStorage.put(task);
    }

    @Override
    public void remove(int id) {
        historyStorage.remove(id);
    }

    @Override
    public List<Task> getHistory() {
        // о копиях элементов позаботилось само хранилище
        return historyStorage.getHistory();
    }
}
