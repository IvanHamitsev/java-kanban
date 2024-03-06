package com.practicum.kanban.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NonRepeatLinearHistoryStorage<T extends Task> implements HistoryStorage<T> {
    static int nodeCounter = 0;

    private class Node {
        Node before;
        Node after;
        Integer time;

        T value;

        Node(T value) {
            // время неумолимо течёт вперёд, каждый новый узел - позже
            this.time = nodeCounter++;
            this.value = value;
            this.after = null;
            this.before = null;
        }

        T getValue() {
            return value;
        }

        Node getBefore() {
            return before;
        }

        Node getAfter() {
            return after;
        }

        void setNext(Node next) {
            this.after = next;
        }

        void setPrev(Node prev) {
            this.before = prev;
        }
    }

    // вспомогательное хранилище, где ключ Id задачи, а значение - узел хранилища tasksStorage
    private HashMap<Integer, Node> linksStorage = new HashMap<>();

    Node head;
    Node tail;

    public NonRepeatLinearHistoryStorage() {
        head = null;
        tail = null;
    }

    Node linkLast(T newElement) {
        Node node = new Node(newElement);
        if (head == null) {
            head = node;
        } else {
            node.setPrev(tail);
            tail.setNext(node);
        }
        tail = node;
        return tail;
    }

    void removeNode(Node element) {
        Node before = element.getBefore();
        Node after = element.getAfter();
        // что если удаляемый узел на одном из краёв коллекции
        if (element == head) {
            head = after;
        }
        if (element == tail) {
            tail = before;
        }
        // срастить предыдущий и последующие узлы
        if (before != null) {
            before.setNext(after);
        }
        if (after != null) {
            after.setPrev(before);
        }
    }

    @Override
    public void clear() {
        head = null;
        tail = null;
        linksStorage.clear();
        // поймёт ли сборщик мусора, что на объекты Node никто не ссылается, не будет ли утечки памяти?
    }

    @Override
    public void put(T newElement) {
        Integer id = newElement.getTaskId();
        // проверить на повтор
        Node node = linksStorage.get(id);
        if (null != node) {
            // задача уже есть, сначала исключить её упоминания
            linksStorage.remove(id);
            removeNode(node);
        }
        // теперь добавим задачу в конец
        linksStorage.put(id, linkLast(newElement));
    }

    @Override
    public void remove(int id) {
        Node node = linksStorage.get(id);
        if (null != node) {
            linksStorage.remove(id);
            removeNode(node);
        }
    }

    @Override
    public List<T> getHistory() {
        List<T> resultList = new ArrayList<>(nodeCounter);
        Node next = head;
        while (null != next) {
            // не забываем в List добавлять копию, а не оригинальный объект Task
            resultList.add((T) next.getValue().copy());
            next = next.getAfter();
        }
        return resultList;
    }
}
