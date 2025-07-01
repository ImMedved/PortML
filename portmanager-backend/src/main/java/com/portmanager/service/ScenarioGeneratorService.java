package com.portmanager.service;

import com.portmanager.entity.*;
import com.portmanager.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Creates a VALID random scenario:
 * ▸ 5-20 terminals (real berths) + 1 virtual “Raid”
 * ▸ exactly {@code shipCount} vessels (default call = 1 000)
 * ▸ every vessel fits at least one terminal
 * ▸ all fields filled
 */
@Service
@RequiredArgsConstructor
public class ScenarioGeneratorService {

    private final TerminalRepository        terminalRepo;
    private final ShipRepository            shipRepo;
    private final TerminalClosureRepository closureRepo;
    private final WeatherEventRepository    weatherRepo;

    /* ---------- pools ---------- */
    private static final List<String> CARGO_POOL =
            List.of("container", "general", "bulk", "oil", "lng");

    private static final List<String> FUEL_POOL =
            List.of("diesel", "lng", "fuel_oil");

    private static final List<String> FLAG_POOL =
            List.of("PA", "MT", "LR", "HK", "CY", "NL");

    private static final List<String> PORT_POOL =
            List.of("SEA", "HKG", "SIN", "RTM", "DXB",
                    "NYC", "LON", "HAM", "TYO", "MUM");

    private final Random rnd = new Random();

    /* ============================================================= */

    @Transactional
    public void generate(int shipCount) {
        shipCount = 500;                        // fixed for UI

        /* ---------- wipe ---------- */
        closureRepo.deleteAllInBatch();
        weatherRepo.deleteAllInBatch();
        shipRepo.deleteAllInBatch();
        terminalRepo.deleteAllInBatch();

        /* ---------- 1. terminals ---------- */
        int termN = 5 + rnd.nextInt(16);
        List<TerminalEntity> terminals = new ArrayList<>(termN + 1);

        /* 1.0  virtual Raid                */
        TerminalEntity raid = new TerminalEntity();
        raid.setName("Raid");
        raid.setMaxLength(10_000);
        raid.setMaxDraft(100);
        raid.setCargoTypes(List.of());
        raid.setFuelSupported(List.of());
        terminals.add(raid);                   // index 0  (excluded in UI)

        /* 1.1  real berths                 */
        for (int i = 1; i <= termN; i++) {
            TerminalEntity t = new TerminalEntity();
            t.setName("T" + i);
            t.setMaxLength(180 + rnd.nextDouble() * 250);   // 180-430 m
            t.setMaxDraft(8   + rnd.nextDouble() * 7);      // 8-15 m
            t.setCargoTypes(randomSubset(CARGO_POOL, 1 + rnd.nextInt(3)));
            t.setFuelSupported(randomSubset(FUEL_POOL, 1 + rnd.nextInt(2)));
            terminals.add(t);
        }
        terminalRepo.saveAll(terminals);       // ids are generated here

        /* ---------- 2. vessels ---------- */
        OffsetDateTime horizon = OffsetDateTime.now(ZoneOffset.UTC).withHour(0);

        for (int i = 1; i <= shipCount; i++) {
            TerminalEntity target = terminals.get(1 + rnd.nextInt(termN)); // skip index 0 (Raid)

            ShipEntity v = new ShipEntity();
            v.setName("V" + i);

            v.setLength(target.getMaxLength() * (0.40 + 0.50 * rnd.nextDouble()));
            v.setDraft(Math.min(target.getMaxDraft() - 0.3, 5 + rnd.nextDouble() * 5));

            v.setCargoType(randomElement(target.getCargoTypes()));
            v.setShipType(v.getCargoType().toUpperCase());
            v.setFuelType(randomElement(target.getFuelSupported()));

            v.setDeadweight(v.getLength() * v.getDraft() * 2.5);
            v.setFlagCountry(randomElement(FLAG_POOL));
            v.setImoNumber(String.valueOf(9_000_000 + rnd.nextInt(1_000_000)));
            v.setEmissionRating(String.valueOf((char) ('A' + rnd.nextInt(5))));
            v.setHazardClass(String.valueOf(rnd.nextInt(6) + 1));
            v.setTemperatureControlled(rnd.nextDouble() < .12);
            v.setRequiresCustomsClearance(rnd.nextDouble() < .18);
            v.setRequiresPilot(rnd.nextDouble() < .25);

            OffsetDateTime eta = horizon.plusHours(rnd.nextInt(7 * 24));
            v.setEta(eta);
            v.setArrivalWindowStart(eta.minusHours(6 + rnd.nextInt(6)));
            v.setArrivalWindowEnd(eta.plusHours(4 + rnd.nextInt(6)));
            v.setExpectedDelayHours(rnd.nextDouble() * 6);

            v.setArrivalPort(randomElement(PORT_POOL));
            v.setNextPort(randomElement(PORT_POOL));

            v.setEstDurationHours(4 + rnd.nextInt(8));
            v.setPriority(rnd.nextDouble() < .2 ? "high" : "normal");

            shipRepo.save(v);
        }

        /* ---------- 3. events ---------- */
        // choose first NON-Raid berth for maintenance
        TerminalEntity troubled = terminals.stream()
                .filter(t -> !"Raid".equalsIgnoreCase(t.getName()))
                .findFirst().orElse(null);

        if (troubled != null) {
            TerminalClosureEntity cl = new TerminalClosureEntity();
            cl.setTerminalId(troubled.getId());
            cl.setStartTime(horizon.plusDays(2));
            cl.setEndTime(horizon.plusDays(2).plusHours(16));
            cl.setReason("Equipment maintenance");
            closureRepo.save(cl);
        }

        WeatherEventEntity storm = new WeatherEventEntity();
        storm.setStartTime(horizon.plusDays(3).withHour(18));
        storm.setEndTime(horizon.plusDays(3).withHour(23));
        storm.setDescription("Storm");
        weatherRepo.save(storm);
    }

    /* ---------- helpers ---------- */
    private <T> T randomElement(List<T> list) { return list.get(rnd.nextInt(list.size())); }

    private <T> List<T> randomSubset(List<T> src, int size) {
        if (size >= src.size()) return new ArrayList<>(src);
        List<T> copy = new ArrayList<>(src);
        Collections.shuffle(copy, rnd);
        return new ArrayList<>(copy.subList(0, size));
    }
}
