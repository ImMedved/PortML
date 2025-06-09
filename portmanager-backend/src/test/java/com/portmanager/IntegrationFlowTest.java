package com.portmanager;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.portmanager.dto.*;
import com.portmanager.model.PlanningAlgorithm;
import com.portmanager.repository.ShipRepository;
import com.portmanager.service.ScenarioGeneratorService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spins up Postgres + stub ML and walks full happy-path: data→plan→feedback.
 */
/*
@Testcontainers
@SpringBootTest
@ContextConfiguration(initializers = IntegrationFlowTest.Initializer.class)
class IntegrationFlowTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine");

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext ctx) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgres.getJdbcUrl(),
                    "spring.datasource.username=" + postgres.getUsername(),
                    "spring.datasource.password=" + postgres.getPassword(),
                    "ml.service.url=http://localhost:" + wm.getPort()
            ).applyTo(ctx.getEnvironment());
        }
    }

    @Autowired ScenarioGeneratorService generator;
    @Autowired com.portmanager.service.PlanningService planningService;
    @Autowired ShipRepository shipRepo;

    @BeforeEach
    void stubML() {
        wm.resetAll();
        wm.stubFor(post("/plan")
                .willReturn(okJson("{\"algorithmUsed\":\"baseline\",\"metrics\":{},\"schedule\":[]}")));
        wm.stubFor(post("/feedback").willReturn(ok()));
    }

    @Test
    void fullFlowWorks() {
        generator.generate(5);
        assertThat(shipRepo.count()).isEqualTo(5);

        PlanResponseDto plan = planningService.generatePlan(PlanningAlgorithm.BASELINE);
        assertThat(plan.getAlgorithmUsed()).isEqualTo(PlanningAlgorithm.BASELINE);

        wm.verify(postRequestedFor(urlEqualTo("/plan")));
        planningService.getLastPlan();   // just touch
    }
}
*/