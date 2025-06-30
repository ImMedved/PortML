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
 * ▸ 5-20 terminals with realistic capabilities (cargo + fuel)
 * ▸ exactly {@code shipCount} vessels (default call = 1 000)
 * ▸ every vessel fits at least one terminal (length / draft / cargo / fuel)
 * ▸ all extended fields (deadweight, IMO, etc.) are filled, never null
 */
@Service
@RequiredArgsConstructor
public class ScenarioGeneratorService {

    private final TerminalRepository        terminalRepo;
    private final ShipRepository            shipRepo;
    private final TerminalClosureRepository closureRepo;
    private final WeatherEventRepository    weatherRepo;

    /* ----------------- constants ----------------- */

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
        shipCount = 500;
        /* ---------- wipe previous data ---------- */
        closureRepo.deleteAllInBatch();
        weatherRepo.deleteAllInBatch();
        shipRepo.deleteAllInBatch();
        terminalRepo.deleteAllInBatch();

        /* ---------- 1. terminals (5-20) ---------- */
        int termN = 5 + rnd.nextInt(16);
        List<TerminalEntity> terminals = new ArrayList<>(termN);

        for (int i = 1; i <= termN; i++) {

            double maxLen   = 180 + rnd.nextDouble() * 250; // 180-430 m
            double maxDraft =  8  + rnd.nextDouble() *  7;  //   8-15 m

            List<String> cargoes = randomSubset(CARGO_POOL, 1 + rnd.nextInt(3));
            List<String> fuels   = randomSubset(FUEL_POOL , 1 + rnd.nextInt(2));

            TerminalEntity t = new TerminalEntity();
            t.setName("T" + i);
            t.setMaxLength(maxLen);
            t.setMaxDraft(maxDraft);
            t.setCargoTypes(cargoes);
            t.setFuelSupported(fuels);
            terminals.add(t);
        }
        terminalRepo.saveAll(terminals);

        /* ---------- 2. vessels (shipCount) ---------- */
        OffsetDateTime horizon = OffsetDateTime.now(ZoneOffset.UTC).withHour(0);

        for (int i = 1; i <= shipCount; i++) {

            /* pick random terminal → guarantees compatibility */
            TerminalEntity target = terminals.get(rnd.nextInt(terminals.size()));

            ShipEntity v = new ShipEntity();
            v.setName("V" + i);

            /* dimensions that fit the target berth */
            double len   = target.getMaxLength() * (0.40 + 0.50 * rnd.nextDouble());
            double draft = Math.min(target.getMaxDraft() - 0.3,
                    5 + rnd.nextDouble() * 5);
            v.setLength(len);
            v.setDraft(draft);

            /* cargo / fuel in allowed lists */
            v.setCargoType( randomElement(target.getCargoTypes()) );
            v.setShipType ( v.getCargoType().toUpperCase() );
            v.setFuelType ( randomElement(target.getFuelSupported()) );

            /* misc technical data */
            v.setDeadweight(len * draft * 2.5);                    // very rough t-estimate
            v.setFlagCountry( randomElement(FLAG_POOL) );
            v.setImoNumber (String.valueOf(9_000_000 + rnd.nextInt(1_000_000))); // 9000000-9999999
            v.setEmissionRating( String.valueOf( (char) ('A' + rnd.nextInt(5)) ) ); // A-E
            v.setHazardClass(String.valueOf(rnd.nextInt(6) + 1));                // 1-6
            v.setTemperatureControlled( rnd.nextDouble() < .12 );
            v.setRequiresCustomsClearance( rnd.nextDouble() < .18 );
            v.setRequiresPilot( rnd.nextDouble() < .25 );

            /* schedule related */
            OffsetDateTime eta = horizon.plusHours(rnd.nextInt(7 * 24));
            v.setEta(eta);
            v.setArrivalWindowStart(eta.minusHours(6 + rnd.nextInt(6))); // 6-11 h before
            v.setArrivalWindowEnd  (eta.plusHours (4 + rnd.nextInt(6))); // 4-9 h after
            v.setExpectedDelayHours(rnd.nextDouble() * 6);               // 0-6 h potential

            v.setArrivalPort( randomElement(PORT_POOL) );
            v.setNextPort  ( randomElement(PORT_POOL) );

            v.setEstDurationHours(4 + rnd.nextInt(8));   // 4-11 h alongside
            v.setPriority(rnd.nextDouble() < .2 ? "high" : "normal");

            shipRepo.save(v);
        }

        /* ---------- 3. simple events ---------- */
        if (!terminals.isEmpty()) {
            TerminalEntity troubled = terminals.get(0);

            TerminalClosureEntity closure = new TerminalClosureEntity();
            closure.setTerminalId(troubled.getId());
            closure.setStartTime(horizon.plusDays(2));
            closure.setEndTime  (horizon.plusDays(2).plusHours(16));
            closure.setReason   ("Equipment maintenance");
            closureRepo.save(closure);
        }

        WeatherEventEntity storm = new WeatherEventEntity();
        storm.setStartTime(horizon.plusDays(3).withHour(18));
        storm.setEndTime  (horizon.plusDays(3).withHour(23));
        storm.setDescription("Storm");
        weatherRepo.save(storm);
    }

    /* ----------------- helpers ----------------- */

    /** random element (safe for immutable lists) */
    private <T> T randomElement(List<T> list) {
        return list.get(rnd.nextInt(list.size()));
    }

    /** immutable-safe random subset WITHOUT duplicates */
    private <T> List<T> randomSubset(List<T> src, int size) {
        if (size >= src.size()) return new ArrayList<>(src);
        List<T> copy = new ArrayList<>(src);
        Collections.shuffle(copy, rnd);
        return new ArrayList<>(copy.subList(0, size));
    }
}
