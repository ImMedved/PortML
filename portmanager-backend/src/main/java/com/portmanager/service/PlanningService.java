package com.portmanager.service;

import com.portmanager.client.MlServiceClient;
import com.portmanager.dto.*;
import com.portmanager.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.modelmapper.ModelMapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PlanningService
 *
 * Builds PlanRequestDto from DB entities, calls ML service, stores / returns last plan.
 */
@Service
@RequiredArgsConstructor
public class PlanningService {

    private final DataService dataService;
    private final MlServiceClient mlClient;
    private final ModelMapper mapper = new ModelMapper();  // quick mapper; configure later

    private PlanResponseDto lastPlan;

    public PlanResponseDto generatePlan(com.portmanager.model.PlanningAlgorithm algorithm) {

        PlanRequestDto req = PlanRequestDto.builder()
                .port(buildPortDto())
                .ships(buildShipDtos())
                .conditions(buildConditionsDto())
                .algorithm(algorithm)
                .build();

        PlanResponseDto resp = mlClient.requestPlan(req);
        lastPlan = resp;
        return resp;
    }

    public PlanResponseDto getLastPlan() {
        return lastPlan;
    }

    // --- helpers ------------------------------------------------------------
    private PortDto buildPortDto() {
        List<TerminalDto> terminals = dataService.getAllTerminals().stream()
                .map(t -> mapper.map(t, TerminalDto.class))
                .toList();

        OffsetDateTime now = OffsetDateTime.now();
        return PortDto.builder()
                .terminals(terminals)
                .startTime(now)
                .endTime(now.plusDays(7))
                .build();
    }

    private List<ShipDto> buildShipDtos() {
        return dataService.getAllShips().stream()
                .map(s -> mapper.map(s, ShipDto.class))
                .collect(Collectors.toList());
    }

    private ConditionsDto buildConditionsDto() {
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
