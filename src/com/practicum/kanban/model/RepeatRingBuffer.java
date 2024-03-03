package com.practicum.kanban.model;

import java.util.Comparator;

public class RepeatRingBuffer<T extends Task> implements RingBuffer<T> {
    private T[] source;
    // счётчик занятых элементов от 0 до size
    private int counter;
    // указатель на записываемый элемент
    private int writePointer;

    public RepeatRingBuffer(int size) {
        if (size > 1) {
            source = (T[]) new Task[size];
        }
        counter = 0;
        writePointer = 0;
    }
    public void clear() {
        counter = 0;
        writePointer = 0;
    }
    // нужно, чтобы знать, как вычитывать буфер
    public boolean ifFull() {
        Comparator<String> comp = String.CASE_INSENSITIVE_ORDER;
        if (counter < source.length) {
            return false;
        } else {
            return true;
        }
    }

    public void put(T newElement) {
        if (writePointer == source.length) {
            writePointer = 0;
        }
        source[writePointer] = newElement;
        writePointer++;
        if (counter < source.length) {
            counter++;
        }
    }
    // получить элемент "возрастом" age. age = 1 самый новый, age = size самый старый, больше нельзя
    // метод будет удобен, чтобы в цикле получать элементы
    public T get(int age) {
        if ((age > source.length)||(age < 1)) {
            return null;
        }
        int point = writePointer - age;
        if  (counter < source.length) {
            //если буфер ещё не полон, проходить по кругу нельзя, элементы лежат от 0 до writePointer
            if (point >= 0) {
                // вернуть копию сущности
                return (T)source[point].copy();
                //return (T)source[point];
            } else {
                return null;
            }
        } else {
            if (point < 0) {
                // всё нормально, мы прошли по кругу
                point += source.length;
            }
            return (T)source[point].copy();
            //return (T)source[point];
        }
    }
}
