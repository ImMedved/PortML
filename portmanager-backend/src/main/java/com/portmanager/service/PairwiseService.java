package com.portmanager.service;

import com.portmanager.client.MlServiceClient;
import com.portmanager.dto.*;
import com.portmanager.model.PlanningAlgorithm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * PairwiseService
 *
 * Requests two alternative plans from ML (e.g., baseline vs RL) and returns a
 * PairwiseRequestDto suitable for UI display.
 */
@Service
@RequiredArgsConstructor
public class PairwiseService {

    private final DataService dataService;
    private final MlServiceClient mlClient;
    private final ModelMapper mapper;
    private final PairwiseSessionCache sessionCache;   // <--- NEW

    public PairwiseRequestDto buildComparison(PlanningAlgorithm algoA, PlanningAlgorithm algoB) {

        PlanRequestDto baseReq = PlanRequestDto.builder()
                .port(buildPortDto())
                .ships(buildShips())
                .conditions(buildConditions())
                .build();

        PlanRequestDto reqA = baseReq.toBuilder().algorithm(algoA).build();
        PlanRequestDto reqB = baseReq.toBuilder().algorithm(algoB).build();

        PlanResponseDto planA = mlClient.requestPlan(reqA);
        PlanResponseDto planB = mlClient.requestPlan(reqB);

        PairwiseRequestDto result = PairwiseRequestDto.builder()
                .comparisonId("cmp-" + UUID.randomUUID())
                .planA(planA)
                .planB(planB)
                .question("Which berth plan do you prefer (A or B)?")
                .build();

        sessionCache.save(result);      // <--- NEW
        return result;
    }

    // helpers (reuse DataService + ModelMapper)
    private PortDto buildPortDto() {
        List<TerminalDto> terminals = dataService.getAllTerminals().stream()
                .map(t -> mapper.map(t, TerminalDto.class))
                .toList();
        return PortDto.builder()
                .terminals(terminals)
                .startTime(OffsetDateTime.now())
                .endTime(OffsetDateTime.now().plusDays(7))
                .build();
    }

    private List<ShipDto> buildShips() {
        return dataService.getAllShips().stream()
                .map(s -> mapper.map(s, ShipDto.class))
                .collect(Collectors.toList());
    }

    private ConditionsDto buildConditions() {
        List<TerminalClosureDto> closures = dataService.getClosures().stream()
                .map(c -> TerminalClosureDto.builder()
                        .terminalId(c.getTerminal().getId())
                        .start(c.getStartTime())
                        .end(c.getEndTime())
                        .reason(c.getReason())
                        .build())
                .toList();

        List<WeatherEventDto> weather = dataService.getWeatherEvents().stream()
                .map(w -> mapper.map(w, WeatherEventDto.class))
                .toList();

        return ConditionsDto.builder()
                .terminalClosures(closures)
                .weatherEvents(weather)
                .build();
    }
}
