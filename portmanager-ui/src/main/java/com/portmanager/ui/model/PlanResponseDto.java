package com.portmanager.ui.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlanResponseDto {
    private String scenarioId;
    private String algorithmUsed;
    private List<ScheduleItemDto> schedule;

}
