package com.portmanager.dto;

/**
 * Request to build a plan.
 *
 * @param scenario full scenario (terminals + vessels + events)
 * @param algorithm algorithm name (baseline, rl, etc.)
 */
public record PlanningRequestDto(
        ConditionsDto scenario,
        String algorithm
) {}
