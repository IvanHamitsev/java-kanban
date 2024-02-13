package com.practicum.kanban.service;

import com.practicum.kanban.model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    // хранилище
    private TaskRingBuffer historyStorage;
    // получить последние 10 просмотренных задач

    public InMemoryHistoryManager(int size) {
        historyStorage = new TaskRingBuffer(size);
    }

    // Есть два подхода. Можно сразу класть на хранение копии объектов или делать копии при выдаче истории.
    // В первом случае мы будем хранить объекты в том виде, какими они были на момент обращения.
    // Во втором - хранить ссылки на настоящие, актуальные объекты. Но тогда будет проблема удалённых объектов.
    // Выбрал пока первый путь,
    @Override
    public void add(Task task) {
        historyStorage.put(task);
    }
    @Override
    public List<Task> getHistory() {
        List<Task> resultList = new ArrayList<>();
        int i = 1;
        Task task = historyStorage.get(i);
        while (task != null) {
            resultList.add(task);
            i++;
            task = historyStorage.get(i);
        }
        return resultList;
    }
}
