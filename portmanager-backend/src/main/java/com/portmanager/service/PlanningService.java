package com.portmanager.service;

import com.portmanager.client.MlServiceClient;
import com.portmanager.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;

/**
 * Builds a quick “baseline” schedule locally; for any other algorithm
 * delegates to external ML-service.
 */
@Service
@RequiredArgsConstructor
public class PlanningService {

    private final MlServiceClient ml;

    public PlanResponseDto generatePlan(PlanningRequestDto req) {

        /* baseline → local dummy scheduler */
        if ("baseline".equalsIgnoreCase(req.algorithm())) {
            return buildBaseline(req.scenario());
        }

        /* any other algorithm → proxy to ML service */
        return ml.requestPlan(req.scenario(), req.algorithm());
    }

    /* ------------------------------------------------------------ */

    private PlanResponseDto buildBaseline(ConditionsDto sc) {

        /* greedy FIFO assignment */
        List<ShipDto> queue = new ArrayList<>(sc.ships());
        queue.sort(Comparator.comparing(ShipDto::eta));

        Map<Long, OffsetDateTime> nextFree = new HashMap<>();
        List<ScheduleItemDto> out = new ArrayList<>();

        for (ShipDto ship : queue) {

            Optional<TerminalDto> maybe = sc.terminals().stream()
                    .filter(t -> fits(ship, t))
                    .findFirst();

            if (maybe.isEmpty()) continue;            // ship cannot be handled → skip

            TerminalDto t = maybe.get();
            OffsetDateTime start = ship.eta();
            OffsetDateTime end   = start.plusHours(
                    (long) Math.ceil(ship.estDurationHours()));

            nextFree.put(t.id(), end);

            out.add(new ScheduleItemDto(
                    String.valueOf(t.id()),
                    ship.name(),           // UI treats as vesselId
                    start.toString(),
                    end.toString()
            ));
        }

        return PlanResponseDto.builder()
                .algorithmUsed("baseline")
                .schedule(out)
                .ships(sc.ships())
                .build();
    }

    private boolean fits(ShipDto s, TerminalDto t) {
        return s.length() <= t.maxLength()
                && s.draft() <= t.maxDraft()
                && t.cargoTypes().contains(s.cargoType());
    }
}
