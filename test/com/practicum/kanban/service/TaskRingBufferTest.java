package com.practicum.kanban.service;

import com.practicum.kanban.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TaskRingBufferTest {
    TaskRingBuffer ringBuffer = new TaskRingBuffer(10);

    @Test
    void canPutTask() {
        Task[] tasks = new Task[20];
        for (int i = 0; i < 20; i++) {
            tasks[i] = new Task("Задача " + i, "Описание " + i);
        }

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

        for (int i = 9; i < 19; i++) {
            ringBuffer.put(tasks[i]);
        }

        assertTrue(ringBuffer.ifFull(), "буфер полон");
        assertNotNull(ringBuffer.get(9), "не получен 9 элемент");
        assertNotNull(ringBuffer.get(10), "не получен 10 элемент");
        assertNull(ringBuffer.get(11), "получен 11 элемент");
    }
}