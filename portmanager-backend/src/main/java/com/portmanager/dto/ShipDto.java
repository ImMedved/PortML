package com.portmanager.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;

/**
 * ShipDto
 *
 * Vessel arrival data and estimated handling duration.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipDto {

    @NotBlank
    private String id;

    @NotNull
    private OffsetDateTime arrivalTime;

    @Positive
    private double length;   // metres

    @Positive
    private double draft;    // metres

    @NotBlank
    private String cargoType;

    @Positive
    private double estDurationHours;
}
