// src/main/java/com/portmanager/entity/PairwiseFeedbackEntity.java
package com.portmanager.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * PairwiseFeedbackEntity
 *
 * Stores user's choice between two alternative plans.
 */
@Entity
@Table(name = "pairwise_feedback")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PairwiseFeedbackEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String comparisonId;
    private OffsetDateTime timestamp;

    private String planAAlgorithm;
    private String planBAlgorithm;

    private Double planAWaiting;
    private Double planBWaiting;

    /** "A" or "B" */
    private String chosenPlan;
}
