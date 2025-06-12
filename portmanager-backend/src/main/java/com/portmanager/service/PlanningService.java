package com.portmanager.service;

import com.portmanager.client.MlServiceClient;
import com.portmanager.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Builds a simple “baseline” plan locally; for other algorithms
 * proxies the request to an external ML service.
 */
@Service
@RequiredArgsConstructor
public class PlanningService {

    private final MlServiceClient ml;

    public PlanResponseDto generatePlan(PlanningRequestDto req) {

        if ("baseline".equalsIgnoreCase(req.algorithm())) {
            return buildBaseline(req.scenario());
        }
        return ml.requestPlan(req.scenario(), req.algorithm());
    }

    /* ------------------------------------------------------------ */

    private PlanResponseDto buildBaseline(ConditionsDto sc) {

        List<ShipDto> queue = new ArrayList<>(sc.ships());
        queue.sort(
                Comparator.comparing(
                        ShipDto::getArrivalTime,
                        Comparator.nullsLast(Comparator.naturalOrder())
                )
        );

        Map<Long, OffsetDateTime> nextFree = new HashMap<>();
        List<ScheduleItemDto> out = new ArrayList<>();

        for (ShipDto ship : queue) {

            /* если ETA не задана – пропускаем судно */
            if (ship.getArrivalTime() == null) continue;

            Optional<TerminalDto> maybe = sc.terminals().stream()
                    .filter(t -> fits(ship, t))
                    .findFirst();
            if (maybe.isEmpty()) continue;

            TerminalDto t = maybe.get();
            OffsetDateTime start = ship.getArrivalTime().atOffset(ZoneOffset.UTC);
            OffsetDateTime end   = start.plusHours(
                    (long) Math.ceil(ship.getEstDurationHours()));

            nextFree.put(t.id(), end);

            out.add(new ScheduleItemDto(
                    String.valueOf(t.id()),
                    ship.getId(),
                    start.toString(),
                    end.toString()
            ));
        }

        return PlanResponseDto.builder()
                .algorithmUsed("baseline")
                .ships(sc.ships())
                .schedule(out)
                .build();
    }

    /* ---------- helpers ---------- */

    private boolean fits(ShipDto s, TerminalDto t) {
        return s.getLength() <= t.maxLength()
                && s.getDraft() <= t.maxDraft()
                && t.cargoTypes().contains(s.getCargoType());
    }
}
