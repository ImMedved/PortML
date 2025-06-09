package com.portmanager.dto;

import lombok.*;
import jakarta.validation.constraints.*;

/**
 * PairwiseFeedbackDto
 *
 * User choice between two alternative plans.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PairwiseFeedbackDto {

    private String comparisonId;

    // Value must be "A" or "B".
    @Pattern(regexp = "A|B")
    private String chosenPlan;
}
