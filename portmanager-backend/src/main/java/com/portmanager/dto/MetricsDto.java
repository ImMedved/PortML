package com.portmanager.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.util.Map;

/**
 * MetricsDto
 *
 * Aggregated KPI values for a generated plan.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class MetricsDto {

    @PositiveOrZero
    private double totalWaitingTimeHours;

    @PositiveOrZero
    private double avgWaitingTimeHours;

    @PositiveOrZero
    private double maxWaitingTimeHours;

    //Terminal ID → utilisation ratio in [0,1].

    private Map<Integer, Double> utilizationByTerminal;

    @PositiveOrZero
    private int totalScheduledShips;
}
