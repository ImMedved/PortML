package com.portmanager.service;

import com.portmanager.dto.*;
import com.portmanager.entity.*;
import com.portmanager.mapper.ShipMapper;
import com.portmanager.mapper.TerminalMapper;
import com.portmanager.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DataService {

    private final TerminalRepository        terminalRepo;
    private final ShipRepository            shipRepo;
    private final TerminalClosureRepository closureRepo;
    private final WeatherEventRepository    weatherRepo;

    private final TerminalMapper terminalMapper;
    private final ShipMapper      shipMapper;

    /* ---------- save script ---------- */

    @Transactional
    public void overwriteWithUserData(ConditionsDto dto) {
        closureRepo.deleteAllInBatch();
        weatherRepo.deleteAllInBatch();
        shipRepo.deleteAllInBatch();
        terminalRepo.deleteAllInBatch();

        terminalRepo.saveAll(
                dto.terminals().stream()
                        .map(terminalMapper::toEntity)
                        .toList());

        shipRepo.saveAll(
                dto.ships().stream()
                        .map(shipMapper::toEntity)
                        .toList());

        for (EventDto ev : dto.events()) {
            switch (ev.getEventType()) {
                case TERMINAL_CLOSURE -> {
                    var e = (TerminalClosureEventDto) ev;
                    var entity = new TerminalClosureEntity();
                    entity.setTerminalId(e.terminalId());
                    entity.setStartTime(e.start());
                    entity.setEndTime(e.end());
                    closureRepo.save(entity);
                }
                case WEATHER -> {
                    var w = (WeatherEventDto) ev;
                    var entity = new WeatherEventEntity();
                    entity.setStartTime(w.start());
                    entity.setEndTime(w.end());
                    weatherRepo.save(entity);
                }
            }
        }
    }

    /* ---------- new single snapshot ---------- */

    @Transactional(readOnly = true)
    public ConditionsDto getCurrentConditions() {

        List<TerminalDto> terminals = terminalRepo.findAll().stream()
                .map(terminalMapper::toDto)
                .toList();

        List<ShipDto> ships = shipRepo.findAll().stream()
                .map(shipMapper::toDto)
                .toList();

        List<EventDto> events = closureRepo.findAll().stream()
                .<EventDto>map(c -> new TerminalClosureEventDto(
                        c.getTerminalId(), c.getStartTime(), c.getEndTime()))
                .collect(Collectors.toList());

        events.addAll(
                weatherRepo.findAll().stream()
                        .map(w -> (EventDto) new WeatherEventDto(
                                w.getStartTime(), w.getEndTime()))
                        .toList());

        return new ConditionsDto(terminals, ships, events);
    }

    /* ---------- helper methods (can be used by other services) ---------- */

    @Transactional(readOnly = true)
    public List<ShipDto> mapShipsToDto() {
        return shipRepo.findAll().stream()
                .map(shipMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public PortDto buildPortDto() {
        return new PortDto(
                terminalRepo.findAll().stream()
                        .map(terminalMapper::toDto)
                        .toList(),
                OffsetDateTime.now(),
                OffsetDateTime.now().plusDays(7)
        );
    }
}
