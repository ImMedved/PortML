package com.portmanager.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

/**
 * Ship DTO used in REST.
 * Accepts both "arrival" and "arrivalTime".
 */
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

        public ShipDto() {}

        /* ---------- getters / setters ---------- */
        public String getId()                   { return id; }
        public void   setId(String id)          { this.id = id; }

        public double getLength()               { return length; }
        public void   setLength(double l)       { this.length = l; }

        public double getDraft()                { return draft; }
        public void   setDraft(double d)        { this.draft = d; }

        public String getCargoType()            { return cargoType; }
        public void   setCargoType(String c)    { this.cargoType = c; }

        public LocalDateTime getArrivalTime()   { return arrivalTime; }
        public void          setArrivalTime(LocalDateTime t) { this.arrivalTime = t; }

        public double getEstDurationHours()     { return estDurationHours; }
        public void   setEstDurationHours(double h) { this.estDurationHours = h; }

        public String getPriority()             { return priority; }
        public void   setPriority(String p)     { this.priority = p; }
}
