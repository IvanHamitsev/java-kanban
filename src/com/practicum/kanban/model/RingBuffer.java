package com.practicum.kanban.model;

public interface RingBuffer <ElementType> {
    // буфер уже полон и затирает старые значения или ещё не заполнился до своего максимального размера
    boolean ifFull();
    void clear();
    void put(ElementType newElement);
    // получить элемент возрастом age, где 1 - самый молодой.
    ElementType get(int age);

}
