package com.practicum.kanban.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Map;

public class JsonConverter {

    static Gson gson = new Gson();

    static class UserMapToken extends TypeToken<Map<Integer, Task>> {
    }

    // методу преобразования из POJO в строку всё равно на тип
    public static String convert(Object o) {
        return gson.toJson(o);
    }

    // методы преобразования из строки в POJO
    public static Map<Integer, Task> convertToMap(String inp) {
        return gson.fromJson(inp, new UserMapToken().getType());
    }

    public static Task convertToTask(String inp) {
        return gson.fromJson(inp, Task.class);
    }

    public static Subtask convertToSubtask(String inp) {
        return gson.fromJson(inp, Subtask.class);
    }

    public static Epic convertToEpic(String inp) {
        return gson.fromJson(inp, Epic.class);
    }
}
