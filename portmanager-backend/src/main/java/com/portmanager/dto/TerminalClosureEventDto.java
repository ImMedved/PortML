package com.portmanager.dto;

import java.time.OffsetDateTime;

/**
 * Event: repair/closing of a specific terminal.
 *
 * @param terminalId terminal id
 * @param start start of closing
 * @param end end of closing
 */
public record TerminalClosureEventDto(
        long terminalId,
        OffsetDateTime start,
        OffsetDateTime end
) implements EventDto {

    @Override
    public EventType getEventType() {
        return EventType.TERMINAL_CLOSURE;
    }
}
