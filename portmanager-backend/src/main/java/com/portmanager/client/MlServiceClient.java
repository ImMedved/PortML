package com.portmanager.client;

import com.portmanager.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Thin proxy to ML-service (/v1/plan).
 * Base URL is taken from env-var ML_SERVICE_URL, defaults to http://ml-service:5000/v1
 */
@Slf4j
@Component
public class MlServiceClient {

    private final RestTemplate rest;
    private final String baseUrl;

    public MlServiceClient(RestTemplate rt) {
        this.rest    = rt;
        this.baseUrl = System.getenv().getOrDefault(
                "ML_SERVICE_URL",
                "http://ml-service:5000/v1"
        );
    }

    public PlanResponseDto requestPlan(ConditionsDto scenario, String algorithm) {

        /* Build lightweight JSON compatible with ML PlanRequestModel */
        Map<String, Object> body = new HashMap<>();
        body.put("algorithm", algorithm);
        body.put("ships",    scenario.ships());
        body.put("port", Map.of(
                "terminals", scenario.terminals(),
                "startTime", java.time.OffsetDateTime.now(),
                "endTime",   java.time.OffsetDateTime.now().plusDays(7)
        ));
        body.put("conditions", Map.of(
                "terminalClosures", scenario.events()
                        .stream()
                        .filter(e -> e.getEventType() == com.portmanager.dto.EventType.TERMINAL_CLOSURE)
                        .toList(),
                "weatherEvents",    scenario.events()
                        .stream()
                        .filter(e -> e.getEventType() == com.portmanager.dto.EventType.WEATHER)
                        .toList()
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<?> entity = new HttpEntity<>(body, headers);
        URI uri = URI.create(baseUrl + "/plan");

        log.info("[ML] POST {}", uri);
        return rest.postForObject(uri, entity, PlanResponseDto.class);
    }
}
