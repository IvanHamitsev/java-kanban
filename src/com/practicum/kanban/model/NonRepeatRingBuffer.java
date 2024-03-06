package com.practicum.kanban.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class NonRepeatRingBuffer<T extends Task> implements HistoryStorage<T> {
    private LinkedList<T> source = new LinkedList<>();
    // счётчик занятых элементов от 0 до size
    private int counter;
    // указатель на записываемый элемент
    private int writePointer;
    // размер буфера
    private int size;

    public NonRepeatRingBuffer(int size) {
        counter = 0;
        writePointer = 0;
        this.size = size;
    }
    @Override
    public void clear() {
        counter = 0;
        writePointer = 0;
        source.clear();
    }
    public boolean ifFull() {
        if (counter < size) {
            return false;
        } else {
            return true;
        }
    }

    public void put(T newElement) {
        // есть ли такой элемент в хранилище
        int position = source.indexOf(newElement);
        if (position >= 0) {
            // если новый элемент уже есть в списке, он перемещается на позицию самого свежего элемента
            source.remove(position);

            if (writePointer > source.size()) {
                // на границе List-а получается особый случай, нет сдвига элементов
                source.add(writePointer-1, newElement);
            } else {
                source.add(writePointer, newElement);
                writePointer++;
                if (counter < size) {
                    counter++;
                }
            }

        } else {
            if (writePointer == size) {
                writePointer = 0;
            }
            if (counter < size) {
                // хранилище ещё не полно, вставка новых элементов
                source.add(newElement);
                counter++;
            } else {
                // хранилище полно, замена
                source.set(writePointer, newElement);
            }
            writePointer++;
        }
    }

    @Override
    public void remove(int id) {
        // На этапе истории ограниченного размера не требовалось
    }

    // получить элемент "возрастом" age. age = 1 самый новый, age = size самый старый, больше нельзя
    // метод будет удобен, чтобы в цикле получать элементы
    public T get(int age) {
        if ((age > size) || (age < 1)) {
            return null;
        }
        int point = writePointer - age;
        if (counter < size) {
            //если буфер ещё не полон, проходить по кругу нельзя, элементы лежат от 0 до writePointer
            if (point >= 0) {
                // вернуть копию сущности
                return (T)source.get(point).copy();
            } else {
                return null;
            }
        } else {
            if (point < 0) {
                // всё нормально, мы прошли по кругу
                point += size;
            }
            return (T)source.get(point).copy();
        }
    }

    @Override
    public List<T> getHistory() {
        List<T> resultList = new ArrayList<>();
        int i = 1;
        T task = get(i);
        while (task != null) {
            resultList.add(task);
            i++;
            task = get(i);
        }
        return resultList;
    }
}
