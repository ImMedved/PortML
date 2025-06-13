package com.portmanager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Port terminal.
 *
 * @param id primary key (0 or null when creating new)
 * @param name human-readable name (T1, South-1, etc.)
 * @param maxLength maximum vessel length, m
 * @param maxDraft maximum draft, m
 * @param cargoTypes list of supported cargo types (CONTAINER, BULK …)
 */
public record TerminalDto(
        Long id,
        String name,
        double maxLength,
        double maxDraft,
        @JsonProperty("allowedCargoTypes")
        List<String> cargoTypes
) {}
