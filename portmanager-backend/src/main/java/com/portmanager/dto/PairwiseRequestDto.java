package com.portmanager.dto;

import lombok.*;

/**
 * PairwiseRequestDto
 *
 * Two alternative plans shown to the user for preference feedback.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PairwiseRequestDto {

    private String comparisonId;

    private PlanResponseDto planA;

    private PlanResponseDto planB;

    private String question;
}
