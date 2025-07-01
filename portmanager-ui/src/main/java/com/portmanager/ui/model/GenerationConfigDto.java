package com.portmanager.ui.model;

import lombok.Data;

import java.util.Map;

/** Mirrors backend GenerationConfigDto for REST calls. */
@Data
public class GenerationConfigDto {

    /* ships */
    private Integer shipCount;
    private Double  pilotPercent;
    private Double  customsPercent;
    private Double  temperaturePercent;
    private Double  priorityPercent;
    private Map<String, Double> cargoDistribution;
    private Map<String, Double> fuelDistribution;

    /* terminals */
    private Integer terminalCount;
    private Map<String, Double> terminalCargoDistribution;
    private Map<String, Double> terminalFuelDistribution;
}
