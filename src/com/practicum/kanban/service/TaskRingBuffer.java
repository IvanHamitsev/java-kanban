package com.practicum.kanban.service;

import com.practicum.kanban.model.Task;

// Класс реализует кольцевой буфер для хранения size элементов истории, затирая их по кругу при добавлении
public class TaskRingBuffer {
    private Task[] source;
    // счётчик занятых элементов от 0 до size
    private int counter;
    // указатель на записываемый элемент
    private int writePointer;

    public TaskRingBuffer(int size) {
        source = new Task[size];
        counter = 0;
        writePointer = 0;
    }

    // нужно, чтобы знать, как вычитывать буфер
    public boolean ifFull() {
        if (counter < source.length) {
            return false;
        } else {
            return true;
        }
    }

    public void put(Task newTask) {
        if (writePointer == source.length) {
            writePointer = 0;
        }
        source[writePointer] = newTask;
        writePointer++;
        if (counter < source.length) {
            counter++;
        }
    }
    // получить элемент "возрастом" age. age = 1 самый новый, age = size самый старый, больше нельзя
    // метод будет удобен, чтобы в цикле получать элементы
    public Task get(int age) {
        if ((age > source.length)||(age < 1)) {
            return null;
        }
        int point = writePointer - age;
        if  (counter < source.length) {
            //если буфер ещё не полон, проходить по кругу нельзя, элементы лежат от 0 до writePointer
            if (point >= 0) {
                return source[point];
            } else {
                return null;
            }
        } else {
            if (point < 0) {
                // всё нормально, мы прошли по кругу
                point += source.length;
            }
            return source[point];
        }
    }
}
