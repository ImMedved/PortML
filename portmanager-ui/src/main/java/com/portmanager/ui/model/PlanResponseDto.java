package com.portmanager.ui.model;

import java.util.List;

public class PlanResponseDto {
    private String scenarioId;
    private String algorithmUsed;
    private List<ScheduleItemDto> schedule;

    public String getScenarioId() {
        return scenarioId;
    }

    public void setScenarioId(String scenarioId) {
        this.scenarioId = scenarioId;
    }

    public String getAlgorithmUsed() {
        return algorithmUsed;
    }

    public void setAlgorithmUsed(String algorithmUsed) {
        this.algorithmUsed = algorithmUsed;
    }

    public List<ScheduleItemDto> getSchedule() {
        return schedule;
    }

    public void setSchedule(List<ScheduleItemDto> schedule) {
        this.schedule = schedule;
    }
}
