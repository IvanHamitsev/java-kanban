package com.practicum.kanban.model;

import java.util.List;

public interface HistoryStorage<ElementType> {
    void clear();
    void put(ElementType newElement);
    void remove(int id);
    List<ElementType> getHistory();
}
