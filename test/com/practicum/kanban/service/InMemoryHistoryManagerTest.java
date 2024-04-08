package com.practicum.kanban.service;

import org.junit.jupiter.api.BeforeAll;

class InMemoryHistoryManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    public InMemoryHistoryManagerTest() {
        super(new InMemoryTaskManager());
    }

    @BeforeAll
    static void prepareManager() {
        // для тестов каждый раз пересоздаём новый менеджер
        taskManager = new InMemoryTaskManager();
    }

    // В результате никаких специфичных InMemoryTaskManager тестов: функционал полностью покрыт FileBackedTaskManager
    // и все тесты оказались в общем абстрактном классе TaskManagerTest
}