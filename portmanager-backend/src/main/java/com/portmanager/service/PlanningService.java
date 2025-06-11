package com.portmanager.service;

import com.portmanager.client.MlServiceClient;
import com.portmanager.dto.*;
import com.portmanager.entity.*;
import com.portmanager.repository.ShipRepository;
import lombok.Getter;
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

    private final ShipRepository shipRepo;
    private final ScenarioGeneratorService generatorService;

    private final DataService dataService;
    private final MlServiceClient mlClient;
    private final ModelMapper mapper = new ModelMapper();

    @Getter
    private PlanResponseDto lastPlan;

    public PlanResponseDto generatePlan(com.portmanager.model.PlanningAlgorithm algorithm,
                                        boolean disableT1,
                                        boolean disableT2) {

        if (shipRepo.count() == 0) {
            generatorService.generate(20);
        }

        List<ShipDto> ships = buildShipDtos();
        ConditionsDto conditions = buildConditionsDto();
        PortDto port = buildPortDto(disableT1, disableT2);

        PlanRequestDto req = PlanRequestDto.builder()
                .port(port)
                .ships(ships)
                .conditions(conditions)
                .algorithm(algorithm)
                .build();

        PlanResponseDto mlResponse = mlClient.requestPlan(req);

        PlanResponseDto response = PlanResponseDto.builder()
                .schedule(mlResponse.getSchedule())
                .metrics(mlResponse.getMetrics())
                .algorithmUsed(mlResponse.getAlgorithmUsed())
                .scenarioId(mlResponse.getScenarioId())
                .ships(ships)
                .terminalClosures(conditions.getTerminalClosures())
                .weatherEvents(conditions.getWeatherEvents())
                .build();

        lastPlan = response;
        return response;
    }

    // fallback for existing code
    public PlanResponseDto generatePlan(com.portmanager.model.PlanningAlgorithm algorithm) {
        return generatePlan(algorithm, false, false);
    }

    private PortDto buildPortDto(boolean disableT1, boolean disableT2) {
        List<TerminalDto> terminals = dataService.getAllTerminals().stream()
                .map(t -> mapper.map(t, TerminalDto.class))
                .filter(t -> !(disableT1 && "T1".equalsIgnoreCase(t.getName())))
                .filter(t -> !(disableT2 && "T2".equalsIgnoreCase(t.getName())))
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
                .toList();
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

    public ConditionsDto getCurrentConditions() {
        return buildConditionsDto();
    }
}
