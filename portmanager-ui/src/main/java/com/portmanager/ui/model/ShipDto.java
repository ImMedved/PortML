package com.portmanager.ui.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * DTO describing a vessel (client side).
 * New “advanced-context” fields are marked with // NEW
 */
@Getter @Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShipDto {

    /* ---- legacy fields (kept as-is) ------------------------------------ */
    private String id;
    private LocalDateTime arrivalTime;
    private double length;
    private double draft;
    private String cargoType;
    private double estDurationHours;
    private String priority;

    /* ---- extended context (20+ new fields) ----------------------------- */
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

    /* ---- constructors -------------------------------------------------- */

    /** Empty ctor for Jackson / FX-editing */
    public ShipDto() {}

    /** Constructor with the “old” minimal set of fields */
    public ShipDto(String id,
                   LocalDateTime arrivalTime,
                   double length,
                   double draft,
                   String cargoType,
                   double estDurationHours,
                   String priority) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.length = length;
        this.draft = draft;
        this.cargoType = cargoType;
        this.estDurationHours = estDurationHours;
        this.priority = priority;
    }

    /* ---- convenience static factory (used by RandomData) -------------- */
    @SuppressWarnings("unchecked")
    public static ShipDto fromJson(Map<String, Object> m) {
        ShipDto s = new ShipDto(
                (String)  m.get("id"),
                LocalDateTime.parse((String) m.get("arrivalTime"), DateTimeFormatter.ISO_DATE_TIME),
                ((Number) m.get("length")).doubleValue(),
                ((Number) m.get("draft")).doubleValue(),
                (String)  m.get("cargoType"),
                ((Number) m.get("estDurationHours")).doubleValue(),
                (String)  m.get("priority")
        );

        /* optional extended fields (if server already provides them) */
        s.deadweight               = num(m, "deadweight");
        s.flagCountry              = (String)  m.getOrDefault("flagCountry", null);
        s.imoNumber                = (String)  m.getOrDefault("imoNumber", null);
        s.shipType                 = (String)  m.getOrDefault("shipType", null);
        s.requiresCustomsClearance = bool(m, "requiresCustomsClearance");
        s.hazardClass              = (String)  m.getOrDefault("hazardClass", null);
        s.temperatureControlled    = bool(m, "temperatureControlled");
        s.fuelType                 = (String)  m.getOrDefault("fuelType", null);
        s.emissionRating           = (String)  m.getOrDefault("emissionRating", null);
        s.arrivalPort              = (String)  m.getOrDefault("arrivalPort", null);
        s.nextPort                 = (String)  m.getOrDefault("nextPort", null);
        s.requiresPilot            = bool(m, "requiresPilot");
        s.expectedDelayHours       = num(m, "expectedDelayHours");

        String aws = (String) m.get("arrivalWindowStart");
        if (aws != null) s.arrivalWindowStart = LocalDateTime.parse(aws, DateTimeFormatter.ISO_DATE_TIME);
        String awe = (String) m.get("arrivalWindowEnd");
        if (awe != null) s.arrivalWindowEnd   = LocalDateTime.parse(awe, DateTimeFormatter.ISO_DATE_TIME);

        return s;
    }

    /* ---- helpers for factory ------------------------------------------ */
    private static boolean bool(Map<String, Object> m, String k) {
        Object v = m.get(k); return v instanceof Boolean b && b;
    }
    private static double num(Map<String, Object> m, String k) {
        Object v = m.get(k); return v instanceof Number n ? n.doubleValue() : 0.0;
    }

    /* ---- legacy shortcut (used in BoardController) -------------------- */
    public LocalDateTime getArrival() { return arrivalTime; }
}
