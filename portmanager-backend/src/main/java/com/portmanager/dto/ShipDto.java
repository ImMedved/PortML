package com.portmanager.dto;

import java.time.OffsetDateTime;

/**
 * Судно, ожидающее швартовки.
 *
 * @param id        первичный ключ (0/ null для нового)
 * @param name      идентификатор (V123, MSC Aurora …)
 * @param length    длина, м
 * @param draft     осадка, м
 * @param cargoType тип груза
 * @param eta       расчетное время прибытия (UTC)
 */
public record ShipDto(
        Long id,
        String name,
        double length,
        double draft,
        String cargoType,
        OffsetDateTime eta
) {}
