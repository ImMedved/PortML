package com.portmanager.ui.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

/**
 * DTO for plan metrics, synchronized with the format returned by the backend/ML.
 */
public class Metrics {

    @SerializedName("totalScheduledShips")
    private int totalVessels;

    @SerializedName("totalWaitingTimeHours")
    private double totalWaitingTimeHours;

    @SerializedName("avgWaitingTimeHours")
    private double avgWaitingTimeHours;

    @SerializedName("maxWaitingTimeHours")
    private double maxWaitingTimeHours;

    @SerializedName("utilizationByTerminal")
    private Map<Integer, Double> utilizationByTerminal;

    public int getTotalVessels() {
        return totalVessels;
    }

    public double getTotalWaitingTimeHours() {
        return totalWaitingTimeHours;
    }

    public double getAvgWaitingTimeHours() {
        return avgWaitingTimeHours;
    }

    public double getMaxWaitingTimeHours() {
        return maxWaitingTimeHours;
    }

    public Map<Integer, Double> getUtilizationByTerminal() {
        return utilizationByTerminal;
    }

    /**
     * Average load across all terminals (0..1).
     */
    public double getOverallUtilization() {
        if (utilizationByTerminal == null || utilizationByTerminal.isEmpty()) {
            return 0.0;
        }
        return utilizationByTerminal.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }
}