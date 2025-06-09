package com.portmanager.service;

import com.portmanager.entity.*;
import com.portmanager.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that ScenarioGeneratorService produces data
 * satisfying hard business constraints from the spec.
 */
/*
@ExtendWith(SpringExtension.class)
@DataJpaTest
@Import(ScenarioGeneratorService.class)
class ScenarioGeneratorServiceTest {

    @Autowired ScenarioGeneratorService generator;
    @Autowired TerminalRepository terminalRepo;
    @Autowired ShipRepository shipRepo;
    @Autowired TerminalClosureRepository closureRepo;

    @Test
    void generatedScenarioIsSelfConsistent() {
        generator.generate(30);

        List<TerminalEntity> terms = terminalRepo.findAll();
        assertThat(terms).hasSize(3);

        OffsetDateTime horizonStart = OffsetDateTime.now().minusHours(1);

        shipRepo.findAll().forEach(ship -> {
            // ETA внутри планируемой недели
            assertThat(ship.getArrivalTime()).isAfter(horizonStart);
            // подходящий терминал по габаритам существует
            boolean fits = terms.stream().anyMatch(t ->
                    ship.getLength() <= t.getMaxLength() &&
                            ship.getDraft()  <= t.getMaxDraft() &&
                            t.getAllowedCargoTypes().contains(ship.getCargoType()));
            assertThat(fits).isTrue();
        });

        // Правильный интервал закрытия
        TerminalClosureEntity cl = closureRepo.findAll().get(0);
        assertThat(cl.getEndTime()).isAfter(cl.getStartTime());
    }
}
 */
