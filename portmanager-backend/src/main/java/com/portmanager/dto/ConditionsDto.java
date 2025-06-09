package com.portmanager.dto;

import lombok.*;
import java.util.List;

/**
 * ConditionsDto
 *
 * External constraints affecting scheduling, such as terminal maintenance or weather events.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ConditionsDto {

    @Builder.Default
    private List<TerminalClosureDto> terminalClosures = List.of();

    @Builder.Default
    private List<WeatherEventDto> weatherEvents = List.of();
}
