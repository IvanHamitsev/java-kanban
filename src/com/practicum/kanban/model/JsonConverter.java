package com.practicum.kanban.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@SuppressWarnings("checkstyle:RegexpSinglelineJava")
public class JsonConverter {

    static Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .create();

    static class UserMapToken extends TypeToken<Map<Integer, Task>> {
    }

    static class UserEpicMapToken extends TypeToken<Map<Integer, Epic>> {
    }

    static class UserSubtaskMapToken extends TypeToken<Map<Integer, Subtask>> {
    }

    static class UserListToken extends TypeToken<List<Task>> {
    }

    static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd-MM-yyyy");

        @Override
        public void write(final JsonWriter jsonWriter, final LocalDateTime localDateTime) throws IOException {
            if (null != localDateTime) {
                jsonWriter.value(localDateTime.format(formatter));
            } else {
                jsonWriter.nullValue();
            }
        }

        @Override
        public LocalDateTime read(final JsonReader jsonReader) throws IOException {
            return LocalDateTime.parse(jsonReader.nextString(), formatter);
        }
    }

    static class DurationAdapter extends TypeAdapter<Duration> {

        @Override
        public void write(final JsonWriter jsonWriter, final Duration duration) throws IOException {
            if (null != duration) {
                String inString = duration.toString();
                jsonWriter.value(inString);
            } else {
                jsonWriter.nullValue();
            }
        }

        @Override
        public Duration read(final JsonReader jsonReader) throws IOException {
            return Duration.parse(jsonReader.nextString());
        }
    }

    // методу преобразования из POJO в строку всё равно на тип
    public static String convert(Object o) {
        return gson.toJson(o);
    }

    // методы преобразования из строки в POJO
    public static Map<Integer, Task> convertToMap(String inp) {
        return gson.fromJson(inp, new UserMapToken().getType());
    }

    public static Map<Integer, Epic> convertToEpicMap(String inp) {
        return gson.fromJson(inp, new UserEpicMapToken().getType());
    }

    public static Map<Integer, Subtask> convertToSubtaskMap(String inp) {
        return gson.fromJson(inp, new UserSubtaskMapToken().getType());
    }

    public static List<Task> convertToList(String inp) {
        return gson.fromJson(inp, new UserListToken().getType());
    }

    public static Task convertToTask(String inp) {
        return gson.fromJson(inp, Task.class);
    }

    public static Epic convertToEpic(String inp) {
        return gson.fromJson(inp, Epic.class);
    }

    public static Subtask convertToSubtask(String inp) {
        return gson.fromJson(inp, Subtask.class);
    }
}
