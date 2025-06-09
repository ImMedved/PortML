package com.portmanager.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;

/**
 * AssignmentDto
 *
 * Single berth occupation interval for one vessel.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class AssignmentDto {

    @NotBlank
    private String shipId;

    private int terminalId;

    @NotNull
    private OffsetDateTime startTime;

    @NotNull
    private OffsetDateTime endTime;
}
