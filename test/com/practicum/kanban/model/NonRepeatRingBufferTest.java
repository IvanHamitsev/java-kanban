package com.practicum.kanban.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NonRepeatRingBufferTest {
    private NonRepeatRingBuffer ringBuffer = new NonRepeatRingBuffer(10);
    private static Task[] tasks;

    void fillBuffer() {
        for (int i = 0; i < 10; i++) {
            ringBuffer.put(tasks[i]);
        }
    }

    @BeforeAll
    static void prepareTests() {
        tasks = new Task[20];
        for (int i = 0; i < 20; i++) {
            tasks[i] = new Task("Задача " + i, "Описание " + i);
        }
    }

    @BeforeEach
    void cleanBuffer() {
        ringBuffer.clear();
    }

    @Test
    void canPutTask() {
        ringBuffer.put(tasks[0]);
        assertFalse(ringBuffer.ifFull(), "полон, но в истории один элемент");
        assertNotNull(ringBuffer.get(1), "не получен первый элемент");
        assertNull(ringBuffer.get(2), "получен второй элемент, его не должно быть");

        ringBuffer.put(tasks[1]);
        assertFalse(ringBuffer.ifFull(), "преждевременно полон");
        assertNotNull(ringBuffer.get(1), "не получен 1 элемент");
        assertNotNull(ringBuffer.get(1), "не получен 2 элемент");

        for (int i = 2; i < 9; i++) {
            ringBuffer.put(tasks[i]);
        }
        assertFalse(ringBuffer.ifFull(), "буфер ещё не полон");
        assertNotNull(ringBuffer.get(9), "не получен 9 элемент");
        assertNull(ringBuffer.get(10), "получен 10 элемент");
    }

    @Test
    void canPutInFullBuffer() {
        // заполним хранилище
        fillBuffer();
        assertTrue(ringBuffer.ifFull(), "буфер должен быть полон");

        for (int i = 10; i < 19; i++) {
            ringBuffer.put(tasks[i]);
        }
        assertTrue(ringBuffer.ifFull(), "потеряна полнота после вставки в полный буфер");
        assertNotNull(ringBuffer.get(9), "не получен 9 элемент");
        assertNotNull(ringBuffer.get(10), "не получен 10 элемент");
        assertNull(ringBuffer.get(11), "получен 11 элемент");
    }

    @Test
    void canPutRepeats() {
        // заполним хранилище
        fillBuffer();
        ringBuffer.put(tasks[5]);
        assertTrue(ringBuffer.ifFull(), "потеряна полнота буфера после вставки повтора");

        Task getTask = ringBuffer.get(1);
        assertNotNull(getTask, "не получен свежий элемент");
        assertTrue(getTask.equals(tasks[5]), "самый свежий элемент не тот, что добавлен");

        for (int i = 0; i < 8; i++) {
            ringBuffer.put(tasks[i]);
        }
        ringBuffer.put(tasks[5]);
        getTask = ringBuffer.get(1);
        assertNotNull(getTask, "не получен свежий элемент");
        assertTrue(getTask.equals(tasks[5]), "самый свежий элемент не тот, что добавлен");

        for (int i = 10; i < 15; i++) {
            ringBuffer.put(tasks[i]);
        }
        ringBuffer.put(tasks[7]);
        getTask = ringBuffer.get(1);
        assertNotNull(getTask, "не получен свежий элемент");
        assertTrue(getTask.equals(tasks[7]), "самый свежий элемент не тот, что добавлен");
    }
}