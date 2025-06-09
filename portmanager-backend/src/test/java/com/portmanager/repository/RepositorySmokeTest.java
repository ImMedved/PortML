package com.portmanager.repository;

import com.portmanager.entity.TerminalEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Simple CRUD validation for JPA mapping.
 */
/*
@DataJpaTest
class RepositorySmokeTest {

    @Autowired TerminalRepository termRepo;

    @Test
    void saveAndLoadTerminal() {
        termRepo.save(
                TerminalEntity.builder()
                        .id(77)
                        .name("Smoke")
                        .maxLength(100)
                        .maxDraft(8)
                        .allowedCargoTypes(List.of("container"))
                        .build());

        var t = termRepo.findById(77).orElseThrow();
        assertThat(t.getName()).isEqualTo("Smoke");
    }
}
*/