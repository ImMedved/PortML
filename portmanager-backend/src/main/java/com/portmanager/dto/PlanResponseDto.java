package com.portmanager.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.util.List;

/**
 * PlanResponseDto
 *
 * Returned by ML service and forwarded to UI. Contains the full schedule and KPI metrics.,
 * And ship list
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanResponseDto {
    private List<TerminalClosureDto> terminalClosures;
    private List<WeatherEventDto> weatherEvents;

    @NotNull
    private List<AssignmentDto> schedule;

    @NotNull
    private MetricsDto metrics;

    // Mirrors the enum value actually used by ML
    private com.portmanager.model.PlanningAlgorithm algorithmUsed;

    private Integer scenarioId;

    @NotNull
    private List<ShipDto> ships;
}
