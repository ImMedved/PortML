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
import java.time.ZoneOffset;
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
                    var src = (TerminalClosureEventDto) ev;
                    var ent = new TerminalClosureEntity();
                    ent.setTerminalId(src.terminalId());
                    ent.setStartTime(src.start().atOffset(ZoneOffset.UTC));
                    ent.setEndTime  (src.end().atOffset(ZoneOffset.UTC));
                    ent.setReason   (src.description());          // ←
                    closureRepo.save(ent);
                }

                case WEATHER -> {
                    var src = (WeatherEventDto) ev;
                    var ent = new WeatherEventEntity();
                    ent.setStartTime(src.start().atOffset(ZoneOffset.UTC));   // ←
                    ent.setEndTime  (src.end().atOffset(ZoneOffset.UTC));   // ←
                    ent.setDescription(src.description());
                    weatherRepo.save(ent);
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
                        c.getTerminalId(), c.getStartTime().toLocalDateTime(), c.getEndTime().toLocalDateTime(),
                        c.getReason()))
                .collect(Collectors.toList());

        events.addAll(
                weatherRepo.findAll().stream()
                        .map(w -> (EventDto) new WeatherEventDto(
                                w.getStartTime().toLocalDateTime(),
                                w.getEndTime().toLocalDateTime(),
                                w.getDescription()))
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
