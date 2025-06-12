package com.portmanager.dto;

import java.time.OffsetDateTime;

/**
 * Событие: ремонт / закрытие конкретного терминала.
 *
 * @param terminalId id терминала
 * @param start      начало закрытия
 * @param end        конец закрытия
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
