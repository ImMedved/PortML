package com.web.model;

import com.web.model.ConditionsDto;

/**
 * Same as on server
 * but with UI-DTO (ConditionsDto from ui.model).
 */
public record PlanningRequestDto(
        ConditionsDto scenario,
        String algorithm
) {}
