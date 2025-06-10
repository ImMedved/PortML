package com.portmanager.ui.model;

import java.util.List;
import java.util.Map;

public class PlanResponse {
    private Integer scenarioId;                  // == backend
    private String algorithmUsed;
    private List<ScheduleEntry> schedule;
    private Metrics metrics;
    private List<TerminalClosure> terminalClosures;
    private List<WeatherEvent> weatherEvents;
    private Conditions conditions;

    private List<Map<String, Object>> ships;     // list of filled JSON-vessel objects

    public Integer getScenarioId() { return scenarioId; }

    public String getAlgorithmUsed() { return algorithmUsed; }

    public List<ScheduleEntry> getSchedule() {
        return schedule;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public List<Map<String, Object>> getShips() {
        return ships;
    }
    public Conditions getConditions() {
        return conditions;
    }
}
