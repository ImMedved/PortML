package com.portmanager.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.portmanager.dto.*;
import com.portmanager.model.PlanningAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies request/response (de)serialization against a stub ML-service.
 */
/*
@SpringBootTest
class MlServiceClientTest {

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @Autowired
    MlServiceClient client;

    @BeforeEach
    void init() {
        client = new MlServiceClient(new RestTemplate());
    }

    @Test
    void requestPlanSendsCorrectBody() {
        wm.stubFor(post("/plan")
                .willReturn(okJson("{\"algorithmUsed\":\"baseline\"," +
                        "\"metrics\":{}," +
                        "\"schedule\":[]}")));

        PlanRequestDto req = PlanRequestDto.builder()
                .port(PortDto.builder()
                        .terminals(List.of())
                        .startTime(java.time.OffsetDateTime.now())
                        .endTime(java.time.OffsetDateTime.now().plusDays(1))
                        .build())
                .ships(List.of())
                .algorithm(PlanningAlgorithm.BASELINE)
                .build();

        PlanResponseDto resp = client.requestPlan(req);

        assertThat(resp.getAlgorithmUsed()).isEqualTo(PlanningAlgorithm.BASELINE);

        wm.verify(postRequestedFor(urlEqualTo("/plan")));
    }
}
*/