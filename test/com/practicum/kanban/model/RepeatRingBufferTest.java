package com.practicum.kanban.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RepeatRingBufferTest {
    private RingBuffer<Task> ringBuffer = new RepeatRingBuffer(10);
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
            tasks[i] = new Task("Задача " + (i+1), "Описание " + (i+1));
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
    void canMakeRepeats() {
        fillBuffer();
        ringBuffer.put(tasks[5]);
        ringBuffer.put(tasks[5]);

        Task task1 = ringBuffer.get(1);
        Task task2 = ringBuffer.get(2);

        assertNotNull(task1, "не удалось достать свежий элемент");
        assertNotNull(task2, "не удалось достать 2-ой элемент");

        assertTrue(task1.equals(task2), "два крайних элемента не равны");

        assertTrue(task1.equals(tasks[5]), "самый свежий элемент не тот, что положили");

    }

}