package com.portmanager.ui.model;

/**
 * Такая же структура, что и на сервере,
 * но использует UI-DTO (ConditionsDto из пакета ui.model).
 */
public record PlanningRequestDto(
        ConditionsDto scenario,
        String algorithm
) {}
