package com.web.model;

import java.util.List;

public record ConditionsDto(List<TerminalDto> terminals,
                            List<ShipDto> ships,
                            List<EventDto> events) { }