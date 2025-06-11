package com.portmanager.service;

import com.portmanager.repository.*;
import com.portmanager.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * DataService
 * <p>
 * Generates or fetches current scenario (terminals + ships + conditions).
 * For now: stub that loads everything already stored in DB; generator will be added later.
 */
@Service
@RequiredArgsConstructor
public class DataService {

    private final TerminalRepository terminalRepo;
    private final ShipRepository shipRepo;
    private final TerminalClosureRepository closureRepo;
    private final WeatherEventRepository weatherRepo;

    /** Return lists for building PlanRequest. */
    @Transactional(readOnly = true)
    public List<TerminalEntity> getAllTerminals() {
        return terminalRepo.findAll();
    }

    @Transactional(readOnly = true)
    public List<ShipEntity> getAllShips() {
        return shipRepo.findAll();
    }

    @Transactional(readOnly = true)
    public List<TerminalClosureEntity> getClosures() {
        return closureRepo.findAll();
    }

    @Transactional(readOnly = true)
    public List<WeatherEventEntity> getWeatherEvents() {
        return weatherRepo.findAll();
    }

    /** TODO: generateScenario(#ships) – will wipe and insert fresh test data. */
}
