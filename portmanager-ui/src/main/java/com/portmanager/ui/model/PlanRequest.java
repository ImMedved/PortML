package com.portmanager.ui.model;
import java.util.List;
import java.util.Map;

public class PlanRequest {
    private Integer scenarioId;
    private String algorithmUsed;
    private List<ScheduleEntry> schedule;
    private Metrics metrics;

    private final String algorithm;
    private final String cargoPreference;     // "bulk", "liquid", "containers"
    private final String priorityLevel;       // "normal", "high", "critical"
    private final boolean disableTerminal1;
    private final boolean disableTerminal2;

    private List<Map<String, Object>> ships;

    public PlanRequest(String algorithm, String cargoPreference, String priorityLevel,
                       boolean disableTerminal1, boolean disableTerminal2) {
        this.algorithm = algorithm;
        this.cargoPreference = cargoPreference;
        this.priorityLevel = priorityLevel;
        this.disableTerminal1 = disableTerminal1;
        this.disableTerminal2 = disableTerminal2;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getCargoPreference() {
        return cargoPreference;
    }

    public String getPriorityLevel() {
        return priorityLevel;
    }

    public boolean isDisableTerminal1() {
        return disableTerminal1;
    }

    public boolean isDisableTerminal2() {
        return disableTerminal2;
    }

    public Integer getScenarioId() { return scenarioId; }
    public String getAlgorithmUsed() { return algorithmUsed; }
    public List<ScheduleEntry> getSchedule() { return schedule; }
    public Metrics getMetrics() { return metrics; }
    public List<Map<String, Object>> getShips() { return ships; }
}
