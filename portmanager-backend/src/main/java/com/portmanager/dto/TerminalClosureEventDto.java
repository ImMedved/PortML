package com.portmanager.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * Event: repair/closing of a specific terminal.
 *
 * @param terminalId terminal id
 * @param start start of closing
 * @param end end of closing
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TerminalClosureEventDto(
        long terminalId,
        LocalDateTime start,
        LocalDateTime end,
        String description

) implements EventDto {

    @Override
    public EventType getEventType() {
        return EventType.TERMINAL_CLOSURE;
    }
}
