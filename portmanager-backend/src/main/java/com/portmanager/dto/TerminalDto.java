package com.portmanager.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.util.List;

/**
 * TerminalDto
 *
 * Static capabilities of a berth/terminal.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TerminalDto {

    private int id;

    private String name;

    @Positive
    private double maxLength;  // metres

    @Positive
    private double maxDraft;   // metres

    @NotNull
    private List<String> allowedCargoTypes;
}
