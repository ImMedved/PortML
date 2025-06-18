package com.portmanager.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

/**
 * PlanResponseDto — structure consumed by the UI.
 * Schedule uses plain strings identical to UI DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
public class PlanResponseDto {

    /** Algorithm actually used (“baseline”, “boosting”, “RL” …). */
    @NotBlank
    private String algorithmUsed;

    /** Optional scenario identifier (string for UI compatibility). */
    private String scenarioId;

    /** Full berth-occupation schedule. */
    @NotNull
    private List<ScheduleItemDto> schedule;

    /** Optional KPI block (may be null for baseline). */
    private MetricsDto metrics;

    /** Echo of ships list — handy for UI to refresh its table instantly. */
    private List<ShipDto> ships;
}
