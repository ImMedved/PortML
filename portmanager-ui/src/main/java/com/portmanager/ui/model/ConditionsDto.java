package com.portmanager.ui.model;

import java.util.List;

public record ConditionsDto(List<TerminalDto> terminals,
                            List<ShipDto> ships,
                            List<EventDto> events) { }