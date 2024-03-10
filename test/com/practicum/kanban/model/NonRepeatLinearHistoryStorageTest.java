package com.practicum.kanban.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NonRepeatLinearHistoryStorageTest {
    private NonRepeatLinearHistoryStorage<Task> storage = new NonRepeatLinearHistoryStorage<>();

    private static Task[] tasks;

    @BeforeAll
    static void prepareTests() {
        tasks = new Task[20];
        for (int i = 0; i < 20; i++) {
            tasks[i] = new Task("Задача " + i, "Описание " + i);
        }
    }

    @BeforeEach
    void cleanBuffer() {
        storage.clear();
    }

    @Test
    void canPutTask() {
        List<Task> list = storage.getHistory();
        assertEquals(0, list.size(),"Размер истории должен быть 0");
        storage.put(tasks[0]);
        list = storage.getHistory();
        assertEquals(1, list.size(),"Размер истории должен быть 1");
        for (int i = 1; i < 15; i++) {
            storage.put(tasks[i]);
        }
        list = storage.getHistory();
        assertEquals(15, list.size(),"Размер истории должен быть 15");
        storage.put(tasks[10]); // добавили повтор
        list = storage.getHistory();
        assertEquals(15, list.size(),"Размер истории должен всё ещё быть 15");
    }

    @Test
    void canDeleteTask() {
        storage.put(tasks[0]);
        storage.remove(tasks[0].getTaskId());
        List<Task> list = storage.getHistory();
        assertEquals(0, list.size(),"Размер истории должен быть 0");

        for (int i = 0; i < 15; i++) {
            storage.put(tasks[i]);
        }
        storage.remove(tasks[0].getTaskId());
        list = storage.getHistory();
        assertEquals(14, list.size(),"Размер истории должен быть 14");
    }
}