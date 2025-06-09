package com.portmanager.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;

/**
 * TerminalClosureDto
 *
 * Time window when a specific terminal is not operational.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class TerminalClosureDto {

    private int terminalId;

    @NotNull
    private OffsetDateTime start;

    @NotNull
    private OffsetDateTime end;

    private String reason;
}
