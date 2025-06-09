package com.portmanager.ui.model;

public class PlanRequest {
    private final String algorithm;
    private final String cargoPreference;     // "bulk", "liquid", "containers"
    private final String priorityLevel;       // "normal", "high", "critical"
    private final boolean disableTerminal1;   // флаг отключения терминалов
    private final boolean disableTerminal2;

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
}
