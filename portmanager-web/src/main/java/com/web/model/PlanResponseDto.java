package com.web.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlanResponseDto {
    private String scenarioId;
    private String algorithmUsed;
    private List<ScheduleItemDto> schedule;

}
