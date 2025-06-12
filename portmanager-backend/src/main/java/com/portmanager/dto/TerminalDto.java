package com.portmanager.dto;

import java.util.List;

/**
 * Терминал порта.
 *
 * @param id         первичный ключ (0 или null при создании нового)
 * @param name       удобочитаемое имя (T1, South-1 и т.п.)
 * @param maxLength  максимальная длина судна, м
 * @param maxDraft   максимальная осадка, м
 * @param cargoTypes список поддерживаемых типов груза (CONTAINER, BULK …)
 */
public record TerminalDto(
        Long id,
        String name,
        double maxLength,
        double maxDraft,
        List<String> cargoTypes
) {}
