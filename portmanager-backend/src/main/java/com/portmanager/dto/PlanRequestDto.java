package com.portmanager.dto;

import com.portmanager.model.PlanningAlgorithm;
import lombok.*;
import jakarta.validation.constraints.*;
import java.util.List;

/**
 * PlanRequestDto
 *
 * JSON payload sent from Backend to ML service (and optionally from UI to Backend)
 * requesting a berth-allocation plan for a given scenario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PlanRequestDto {

    @NotNull
    private PortDto port;

    @NotNull
    @Size(min = 1)
    private List<ShipDto> ships;

    @Builder.Default
    private ConditionsDto conditions = new ConditionsDto();

    /**
     * Desired planning algorithm.
     * Allowed values: "baseline", "boosting", "RL", "pairwise".
     */
    @NotNull
    private com.portmanager.model.PlanningAlgorithm algorithm;
  }
