package com.portmanager.ui.model;

import java.util.List;

public class PlanResponse {
    private Integer scenarioId;          // == backend
    private String  algorithmUsed;
    private List<ScheduleEntry> schedule;
    private Metrics metrics;

    public Integer getScenarioId() { return scenarioId; }

    public String  getAlgorithmUsed() { return algorithmUsed; }

    public List<ScheduleEntry> getSchedule() {
        return schedule;
    }

    public Metrics getMetrics() {
        return metrics;
    }
}
