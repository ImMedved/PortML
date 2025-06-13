package com.portmanager.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Base interface for all events.
 * Polymorphic serialization → JSON always has an "eventType" field.
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
