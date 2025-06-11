package com.portmanager.ui.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ShipDto {
    private String id;
    private LocalDateTime arrivalTime;
    private double length;
    private double draft;
    private String cargoType;
    private double estDurationHours;
    private String priority;

    public static ShipDto fromJson(Map<String, Object> map) {
        return new ShipDto(
                (String) map.get("id"),
                LocalDateTime.parse((String) map.get("arrivalTime"), DateTimeFormatter.ISO_DATE_TIME),
                ((Number) map.get("length")).doubleValue(),
                ((Number) map.get("draft")).doubleValue(),
                (String) map.get("cargoType"),
                ((Number) map.get("estDurationHours")).doubleValue(),
                (String) map.get("priority")
        );
    }

    public ShipDto() {}

    public ShipDto(String id, LocalDateTime arrivalTime, double length, double draft, String cargoType, double estDurationHours, String priority) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.length = length;
        this.draft = draft;
        this.cargoType = cargoType;
        this.estDurationHours = estDurationHours;
        this.priority = priority;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalDateTime arrivalTime) { this.arrivalTime = arrivalTime; }

    public double getLength() { return length; }
    public void setLength(double length) { this.length = length; }

    public double getDraft() { return draft; }
    public void setDraft(double draft) { this.draft = draft; }

    public String getCargoType() { return cargoType; }
    public void setCargoType(String cargoType) { this.cargoType = cargoType; }

    public double getEstDurationHours() { return estDurationHours; }
    public void setEstDurationHours(double estDurationHours) { this.estDurationHours = estDurationHours; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}
