package com.portmanager.dto;

import java.time.OffsetDateTime;

/**
 * Event: adverse weather that closes the entire port.
 *
 * @param start start
 * @param end end
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
