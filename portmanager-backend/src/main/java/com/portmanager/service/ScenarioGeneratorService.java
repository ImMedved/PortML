package com.portmanager.service;

import com.portmanager.entity.*;
import com.portmanager.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Random;

/**
 * Генерирует демонстрационный набор данных для UI.
 */
@Service
@RequiredArgsConstructor
public class ScenarioGeneratorService {

    private final TerminalRepository terminalRepo;
    private final ShipRepository shipRepo;
    private final TerminalClosureRepository closureRepo;
    private final WeatherEventRepository weatherRepo;

    private final Random rnd = new Random();

    @Transactional
    public void generate(int shipCount) {

        /* очистка */
        closureRepo.deleteAllInBatch();
        weatherRepo.deleteAllInBatch();
        shipRepo.deleteAllInBatch();
        terminalRepo.deleteAllInBatch();

        /* ---------- терминалы ---------- */
        TerminalEntity t1 = new TerminalEntity();
        t1.setName("T1");
        t1.setMaxLength(300);
        t1.setMaxDraft(12);
        t1.setCargoTypes(List.of("container", "general"));

        TerminalEntity t2 = new TerminalEntity();
        t2.setName("T2");
        t2.setMaxLength(250);
        t2.setMaxDraft(10);
        t2.setCargoTypes(List.of("oil", "bulk"));

        TerminalEntity t3 = new TerminalEntity();
        t3.setName("T3");
        t3.setMaxLength(400);
        t3.setMaxDraft(15);
        t3.setCargoTypes(List.of("container"));

        terminalRepo.saveAll(List.of(t1, t2, t3));

        /* ---------- суда ---------- */
        OffsetDateTime horizon = OffsetDateTime.now(ZoneOffset.UTC).withHour(0);

        for (int i = 1; i <= shipCount; i++) {
            ShipEntity v = new ShipEntity();
            v.setName("V" + i);
            v.setLength(150 + rnd.nextDouble(200));
            v.setDraft(7 + rnd.nextDouble(5));
            v.setCargoType(rnd.nextBoolean() ? "container" : "bulk");
            v.setEta(horizon.plusHours(rnd.nextInt(7 * 24)));
            shipRepo.save(v);
        }

        /* ---------- события ---------- */
        TerminalClosureEntity closure = new TerminalClosureEntity();
        closure.setTerminalId(t2.getId());     // закроем T2
        closure.setStartTime(horizon.plusDays(2));
        closure.setEndTime(horizon.plusDays(2).plusHours(12));
        closureRepo.save(closure);

        WeatherEventEntity storm = new WeatherEventEntity();
        storm.setStartTime(horizon.plusDays(3).withHour(18));
        storm.setEndTime(horizon.plusDays(3).withHour(23));
        weatherRepo.save(storm);
    }
}
