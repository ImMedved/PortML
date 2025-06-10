package com.portmanager.ui.model;

import java.util.Map;

public class ShipDto {
    private String id;
    private String arrivalTime;
    private String cargoType;
    private String priority;
    private double length;
    private double draft;
    private double estDurationHours;

    public static ShipDto fromJson(Map<String, Object> json) {
        ShipDto s = new ShipDto();
        s.id = (String) json.get("id");
        s.arrivalTime = (String) json.get("arrivalTime");
        s.cargoType = (String) json.get("cargoType");
        s.priority = (String) json.get("priority");
        s.length = ((Number) json.get("length")).doubleValue();
        s.draft = ((Number) json.get("draft")).doubleValue();
        s.estDurationHours = ((Number) json.get("estDurationHours")).doubleValue();
        return s;
    }

    public String getId() { return id; }
    public String getArrivalTime() { return arrivalTime; }
    public String getCargoType() { return cargoType; }
    public String getPriority() { return priority; }
    public double getLength() { return length; }
    public double getDraft() { return draft; }
    public double getEstDurationHours() { return estDurationHours; }
}
