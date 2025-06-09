package com.portmanager.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;

/**
 * WeatherEventDto
 *
 * Port-wide or terminal-specific weather impact.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class WeatherEventDto {

    @NotNull
    private OffsetDateTime start;

    @NotNull
    private OffsetDateTime end;

    private String description;
}
