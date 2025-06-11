package com.portmanager.ui.model;

import java.util.List;
import java.util.Map;

public class PlanResponse {
    private Integer scenarioId;
    private String algorithmUsed;
    private List<ScheduleEntry> schedule;
    private Metrics metrics;
    private List<Map<String, Object>> ships;
    private List<EventDto> events; // заменяет terminalClosures, weatherEvents, conditions

    public Integer getScenarioId() { return scenarioId; }
    public String getAlgorithmUsed() { return algorithmUsed; }
    public List<ScheduleEntry> getSchedule() { return schedule; }
    public Metrics getMetrics() { return metrics; }
    public List<Map<String, Object>> getShips() { return ships; }
    public List<EventDto> getEvents() { return events; }
}
