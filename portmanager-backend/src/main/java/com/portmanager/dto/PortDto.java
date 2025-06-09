package com.portmanager.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.util.List;
import java.time.OffsetDateTime;

/**
 * PortDto
 *
 * Describes the port infrastructure and time horizon for planning.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PortDto {

    @NotNull
    @Size(min = 1)
    private List<TerminalDto> terminals;

    @NotNull
    private OffsetDateTime startTime;

    @NotNull
    private OffsetDateTime endTime;
}
