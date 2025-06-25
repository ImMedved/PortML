/**
 * PlanningAlgorithm
 * <p>
 * Official list of scheduling algorithms that ML-service understands.
 * Serialized as plain string ("baseline", "boosting", "RL", "pairwise").
 */
package com.portmanager.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PlanningAlgorithm {

    BASELINE("baseline"),
    BOOSTING("boosting"),
    RL("RL"),
    PAIRWISE("pairwise");

    private final String code;

    PlanningAlgorithm(String code) {
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static PlanningAlgorithm fromString(String value) {
        for (PlanningAlgorithm alg : values()) {
            if (alg.code.equalsIgnoreCase(value)) {
                return alg;
            }
        }
        throw new IllegalArgumentException("Unknown algorithm: " + value);
    }
}
