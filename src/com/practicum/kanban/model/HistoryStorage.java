package com.practicum.kanban.model;

import java.util.List;

public interface HistoryStorage<T> {
    void clear();

    void put(T newElement);

    void remove(int id);

    List<T> getHistory();
}
