package com.portmanager.dto;

import java.util.List;

/**
 * Complete scenario for scheduling: terminals, ships, events.
 * <p>
 * Matches fields with record used in JavaFX-UI.
 *
 * @param terminals list of terminals
 * @param ships list of ships
 * @param events list of events (polymorphic)
 */
public record ConditionsDto(
        List<TerminalDto> terminals,
        List<ShipDto>     ships,
        List<EventDto>    events
) {}
