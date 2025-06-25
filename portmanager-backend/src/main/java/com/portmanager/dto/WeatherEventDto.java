package com.portmanager.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

/**
 * Event: adverse weather that closes the entire port.
 *
 * @param start start
 * @param end end
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WeatherEventDto(
        LocalDateTime start,
        LocalDateTime end,
        String description
) implements EventDto {

    @Override
    public EventType getEventType() {
        return EventType.WEATHER;
    }
}
