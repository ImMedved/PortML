package com.portmanager.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Базовый интерфейс для всех событий.
 * Полиморфная сериализация → в JSON всегда есть поле "eventType".
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "eventType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TerminalClosureEventDto.class, name = "TERMINAL_CLOSURE"),
        @JsonSubTypes.Type(value = WeatherEventDto.class,        name = "WEATHER")
})
public interface EventDto {
    EventType getEventType();
}
