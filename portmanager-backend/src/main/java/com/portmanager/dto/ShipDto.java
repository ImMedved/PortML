package com.portmanager.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

/**
 * ShipDto — unified between UI ↔ backend.
 */
public record ShipDto(

        Long id,
        String name,
        double length,
        double draft,
        String cargoType,

        /** UI field = arrivalTime (LocalDateTime); backend keeps OffsetDateTime. */
        @JsonAlias({"arrivalTime", "arrival"})
        OffsetDateTime eta,

        /** Unloading duration, h. */
        @JsonAlias({"estDurationHours", "duration"})
        double estDurationHours,

        /** Priority tag (optional). */
        String priority
) {}
