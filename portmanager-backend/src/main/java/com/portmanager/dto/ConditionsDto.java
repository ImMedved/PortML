package com.portmanager.dto;

import java.util.List;

/**
 * Полный сценарий для планирования: терминалы, суда, события.
 *
 * Совпадает по полям с record, используемым в JavaFX-UI.
 *
 * @param terminals список терминалов
 * @param ships     список судов
 * @param events    список событий (полиморфных)
 */
public record ConditionsDto(
        List<TerminalDto> terminals,
        List<ShipDto>     ships,
        List<EventDto>    events
) {}
