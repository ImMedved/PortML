package com.portmanager.service;

import com.portmanager.entity.*;
import com.portmanager.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Random;

/**
 * ScenarioGeneratorService
 *
 * Creates a brand-new random dataset (terminals, ships, closures, weather events)
 * for demonstration purposes.  Existing records are wiped each time.
 */
@Service
@RequiredArgsConstructor
public class ScenarioGeneratorService {

    private final TerminalRepository terminalRepo;
    private final ShipRepository shipRepo;
    private final TerminalClosureRepository closureRepo;
    private final WeatherEventRepository weatherRepo;

    private final Random rnd = new Random();

    /** Вызывается при старте приложения (см. @PostConstruct). */
    public void generateDefault() {
        generate(50);     // 12 судов, 3 терминала – можно отрегулировать
    }

    @Transactional
    public void generate(int shipCount) {

        // 1. Wipe all tables (simple demo – no scenarios table yet)
        closureRepo.deleteAllInBatch();
        weatherRepo.deleteAllInBatch();
        shipRepo.deleteAllInBatch();
        terminalRepo.deleteAllInBatch();

        // 2. Create three terminals
        terminalRepo.saveAll(List.of(
                TerminalEntity.builder()
                        .id(1).name("Terminal A")
                        .maxLength(300).maxDraft(12)
                        .allowedCargoTypes(List.of("container", "general"))
                        .build(),
                TerminalEntity.builder()
                        .id(2).name("Terminal B")
                        .maxLength(250).maxDraft(10)
                        .allowedCargoTypes(List.of("oil", "bulk"))
                        .build(),
                TerminalEntity.builder()
                        .id(3).name("Terminal C")
                        .maxLength(400).maxDraft(15)
                        .allowedCargoTypes(List.of("container"))
                        .build()
        ));

        // 3. Generate vessels
        OffsetDateTime planStart = OffsetDateTime.now(ZoneOffset.UTC).withHour(0).withMinute(0);
        for (int i = 1; i <= shipCount; i++) {
            String cargo = switch (rnd.nextInt(3)) {
                case 0 -> "container";
                case 1 -> "oil";
                default -> "bulk";
            };
            double length = cargo.equals("container") ? 180 + rnd.nextDouble(140)
                    : cargo.equals("oil") ? 220 + rnd.nextDouble(60)
                    : 150 + rnd.nextDouble(120);
            double draft = cargo.equals("oil") ? 10 + rnd.nextDouble(3)
                    : 7 + rnd.nextDouble(4);
            OffsetDateTime eta = planStart.plusHours(rnd.nextInt(7 * 24)); // horizon неделя
            double duration = 10 + rnd.nextDouble(30);

            shipRepo.save(ShipEntity.builder()
                    .id("Vessel_" + i)
                    .arrivalTime(eta)
                    .length(length)
                    .draft(draft)
                    .cargoType(cargo)
                    .estDurationHours(duration)
                    .build());
        }

        // 4. Terminal closure (maintenance)
        closureRepo.save(TerminalClosureEntity.builder()
                .terminal(terminalRepo.getReferenceById(2))
                .startTime(planStart.plusDays(2))
                .endTime(planStart.plusDays(2).plusHours(12))
                .reason("Maintenance")
                .build());

        // 5. Storm for whole port
        weatherRepo.save(WeatherEventEntity.builder()
                .startTime(planStart.plusDays(3).withHour(18))
                .endTime(planStart.plusDays(3).withHour(23))
                .description("Storm - port closed")
                .build());
    }
}
