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
        // здесь нужно не математическое округление, а отбрасывание
        Instant instant = Instant.ofEpochSecond(60 * step * (long) ((double) minutes / step));
        return LocalDateTime.ofInstant(instant, zoneId);
    }

    public long getMinutes(LocalDateTime localDateTime) {
        Instant instant = localDateTime.atZone(zoneId).toInstant();
        long realMinutes = (long) ((double) instant.getEpochSecond() / 60);
        long roundMinutes = (long) ((double) realMinutes / step) * step;
        return roundMinutes;
    }
}
