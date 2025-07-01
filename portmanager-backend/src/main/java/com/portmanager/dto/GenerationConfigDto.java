package com.portmanager.dto;

import lombok.Data;

import java.util.Map;

/** User–defined settings for random scenario generator. */
@Data
public class GenerationConfigDto {

    /** ───── ships ───── */
    private Integer shipCount;                 // null ⇒ default (500)
    private Double pilotPercent;               // 0-100; null ⇒ random
    private Double customsPercent;
    private Double temperaturePercent;
    private Double priorityPercent;

    /** cargoType → percent, e.g. {"container":40,"bulk":30,…} */
    private Map<String, Double> cargoDistribution;
    /** fuelType  → percent */
    private Map<String, Double> fuelDistribution;

    /** ───── terminals ───── */
    private Integer terminalCount;             // null ⇒ 5-20 random
    /** cargoType → percent of berths that support it */
    private Map<String, Double> terminalCargoDistribution;
    /** fuelType  → percent */
    private Map<String, Double> terminalFuelDistribution;
}
