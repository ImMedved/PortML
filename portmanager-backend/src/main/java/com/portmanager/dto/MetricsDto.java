package com.portmanager.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import jakarta.validation.constraints.*;
import java.util.Map;

/**
 * Aggregated KPI values for a generated plan.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
public class MetricsDto {

    @PositiveOrZero
    private double totalWaitingTimeHours;

    @PositiveOrZero
    private double avgWaitingTimeHours;

    @PositiveOrZero
    private double maxWaitingTimeHours;

    //Terminal ID → utilisation ratio in [0,1].

    private Map<String, Double> utilizationByTerminal;

    @PositiveOrZero
    private int totalScheduledShips;
}
