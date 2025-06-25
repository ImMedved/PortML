package com.portmanager.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Ship DTO used in REST.
 * Accepts both "arrival" and "arrivalTime".
 */
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShipDto {

    /* ---------- getters / setters ---------- */
    private String id;
        private double length;
        private double draft;
        private String cargoType;

        @JsonAlias({"arrival", "arrivalTime"})
        private LocalDateTime arrivalTime;

        @JsonAlias({"estDurationHours", "duration"})
        private double estDurationHours;

        private String priority;

        public ShipDto() {}

}
