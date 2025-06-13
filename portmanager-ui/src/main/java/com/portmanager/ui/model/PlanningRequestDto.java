package com.portmanager.ui.model;

/**
 * Same as on server
 * but with UI-DTO (ConditionsDto from ui.model).
 */
public record PlanningRequestDto(
        ConditionsDto scenario,
        String algorithm
) {}
