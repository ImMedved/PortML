package com.portmanager.dto;

import java.time.OffsetDateTime;

/**
 * Событие: неблагоприятная погода, закрывающая весь порт.
 *
 * @param start начало
 * @param end   конец
 */
public record WeatherEventDto(
        OffsetDateTime start,
        OffsetDateTime end
) implements EventDto {

    @Override
    public EventType getEventType() {
        return EventType.WEATHER;
    }
}
