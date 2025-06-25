package com.portmanager.ui.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShipDto {
    @Setter
    private String id;
    @Setter
    private LocalDateTime arrivalTime;
    @Setter
    private double length;
    @Setter
    private double draft;
    @Setter
    private String cargoType;
    @Setter
    private double estDurationHours;
    private String priority;

    /* static factory */
    public static ShipDto fromJson(Map<String, Object> map) {
        return new ShipDto(
                (String)  map.get("id"),
                LocalDateTime.parse((String) map.get("arrivalTime"), DateTimeFormatter.ISO_DATE_TIME),
                ((Number) map.get("length")).doubleValue(),
                ((Number) map.get("draft")).doubleValue(),
                (String)  map.get("cargoType"),
                ((Number) map.get("estDurationHours")).doubleValue(),
                (String)  map.get("priority")
        );
    }

    /* ctors / getters / setters */
    public ShipDto() {}

    public ShipDto(String id, LocalDateTime arrivalTime,
                   double length, double draft, String cargoType,
                   double estDurationHours, String priority) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.length = length;
        this.draft = draft;
        this.cargoType = cargoType;
        this.estDurationHours = estDurationHours;
        this.priority = priority;
    }

    public LocalDateTime getArrival() {
        return arrivalTime;
    }

    public void setArrival(LocalDateTime arrival) {
        this.arrivalTime = arrival;
    }
}
