package com.practicum.kanban.model;

import java.time.*;

public class InaccurateTime {
    // сам класс не хранит время, только параметры для его конвертации
    private long step;
    private ZoneId zoneId;

    public InaccurateTime(Duration step, ZoneId zoneId) {
        // шаг всегда в минутах по тому, что в задании точность Duration в минутах
        this.step = step.toMinutes();
        // если пользователь указал меньше минуты, всё равно минута
        if (this.step < 1) {
            this.step = 1;
        }
        this.zoneId = zoneId;
    }

    public LocalDateTime getLocalDateTime(long minutes) {
        Instant instant = Instant.ofEpochSecond(60 * step * Math.round(minutes / step));
        return LocalDateTime.ofInstant(instant, zoneId);
    }

    public long getMinutes(LocalDateTime localDateTime) {
        Instant instant = localDateTime.atZone(zoneId).toInstant();
        return Math.round((instant.getEpochSecond() / 60 ) / step) * step;
    }
}
