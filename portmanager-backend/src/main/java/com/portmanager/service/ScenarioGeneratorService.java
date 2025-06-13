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
 * Produces a *valid* random scenario: every generated vessel can be processed
 * at least by one terminal (by length, draft and cargo type).
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

        /* wipe previous data */
        closureRepo.deleteAllInBatch();
        weatherRepo.deleteAllInBatch();
        shipRepo.deleteAllInBatch();
        terminalRepo.deleteAllInBatch();

        /* -------- terminals -------- */
        TerminalEntity t1 = term("T1", 300, 12, List.of("container", "general"));
        TerminalEntity t2 = term("T2", 260, 11, List.of("oil", "bulk"));
        TerminalEntity t3 = term("T3", 400, 15, List.of("container"));

        var terminals = terminalRepo.saveAll(List.of(t1, t2, t3));

        /* -------- ships -------- */
        OffsetDateTime horizon = OffsetDateTime.now(ZoneOffset.UTC).withHour(0);

        for (int i = 1; i <= shipCount; i++) {
            TerminalEntity target = terminals.get(rnd.nextInt(terminals.size()));

            ShipEntity v = new ShipEntity();
            v.setName("V" + i);

            // length & draft guaranteed to fit chosen terminal
            v.setLength(target.getMaxLength() * (0.5 + 0.4 * rnd.nextDouble()));
            v.setDraft( Math.min(target.getMaxDraft() - 0.5,
                    5 + rnd.nextDouble() * 4) );
            v.setCargoType(target.getCargoTypes()
                    .get(rnd.nextInt(target.getCargoTypes().size())));

            v.setEta(horizon.plusHours(rnd.nextInt(7 * 24)));

            v.setEstDurationHours(4 + rnd.nextInt(8));           // 4-11 h
            v.setPriority(rnd.nextBoolean() ? "normal" : "high");

            shipRepo.save(v);
        }

        /* -------- events -------- */
        TerminalClosureEntity closure = new TerminalClosureEntity();
        closure.setTerminalId(t2.getId());
        closure.setStartTime(horizon.plusDays(2));
        closure.setEndTime(horizon.plusDays(2).plusHours(12));
        closureRepo.save(closure);

        WeatherEventEntity storm = new WeatherEventEntity();
        storm.setStartTime(horizon.plusDays(3).withHour(18));
        storm.setEndTime(horizon.plusDays(3).withHour(23));
        storm.setDescription("Storm");
        weatherRepo.save(storm);
    }

    /* —— helpers —— */
    private TerminalEntity term(String name, double len, double draft, List<String> types) {
        TerminalEntity e = new TerminalEntity();
        e.setName(name);
        e.setMaxLength(len);
        e.setMaxDraft(draft);
        e.setCargoTypes(types);
        return e;
    }
}
