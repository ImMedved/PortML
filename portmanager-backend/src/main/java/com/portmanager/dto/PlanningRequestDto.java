package com.portmanager.dto;

/**
 * Запрос на построение плана.
 *
 * @param scenario  полный сценарий (терминалы + суда + события)
 * @param algorithm название алгоритма (baseline, rl и т.п.)
 */
public record PlanningRequestDto(
        ConditionsDto scenario,
        String algorithm
) {}
