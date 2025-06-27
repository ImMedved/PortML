package com.portmanager.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Ship DTO used in REST.
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShipDto {

    private String id;
    private double length;
    private double draft;
    private String cargoType;

    @JsonAlias({"arrival", "arrivalTime"})
    private LocalDateTime arrivalTime;

    @JsonAlias({"estDurationHours", "duration"})
    private double estDurationHours;

    private String priority;

    private double  deadweight;
    private String  flagCountry;
    private String  imoNumber;
    private String  shipType;
    private boolean requiresCustomsClearance;
    private String  hazardClass;
    private boolean temperatureControlled;
    private String  fuelType;
    private String  emissionRating;
    private String  arrivalPort;
    private String  nextPort;
    private boolean requiresPilot;
    private LocalDateTime arrivalWindowStart;
    private LocalDateTime arrivalWindowEnd;
    private double  expectedDelayHours;
}
