package com.portmanager.client;

import com.portmanager.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class MlServiceClient {

    private final RestTemplate rest;
    private final String       baseUrl;

    public MlServiceClient(RestTemplate rt) {
        this.rest    = rt;
        this.baseUrl = System.getenv().getOrDefault(
                "ML_SERVICE_URL",
                "http://ml-service:5000/v1"
        );
    }

    public PlanResponseDto requestPlan(ConditionsDto scenario, String algorithm) {

        /* ---------- events → ML-format -------------------------------- */
        List<Map<String, Object>> termClosures = scenario.events().stream()
                .filter(e -> e.getEventType() == EventType.TERMINAL_CLOSURE)
                .map(e -> {
                    TerminalClosureEventDto t = (TerminalClosureEventDto) e;
                    Map<String, Object> m = new HashMap<>();
                    m.put("terminalId", t.terminalId());
                    m.put("start",      t.start());
                    m.put("end",        t.end());
                    m.put("reason",     t.description());   // <-- required even if null
                    return m;
                })
                .toList();

        List<Map<String, Object>> weatherEvents = scenario.events().stream()
                .filter(e -> e.getEventType() == EventType.WEATHER)
                .map(e -> {
                    WeatherEventDto w = (WeatherEventDto) e;
                    Map<String, Object> m = new HashMap<>();
                    m.put("start",       w.start());
                    m.put("end",         w.end());
                    m.put("description", w.description());
                    return m;
                })
                .toList();

        /* ---------- main body of the request ----------------------------- */
        Map<String, Object> body = new HashMap<>();
        body.put("algorithm",
                (algorithm == null || algorithm.isBlank()) ? "baseline" : algorithm);

        body.put("ships", scenario.ships());

        body.put("port", Map.of(
                "terminals", scenario.terminals(),
                "startTime", OffsetDateTime.now(),
                "endTime",   OffsetDateTime.now().plusDays(7)
        ));

        body.put("conditions", Map.of(
                "terminalClosures", termClosures,
                "weatherEvents",    weatherEvents
        ));

        /* ---------- HTTP --------------------------------------------- */
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        URI uri = URI.create(baseUrl + "/plan");
        log.info("[ML] POST {}", uri);

        return rest.postForObject(uri, new HttpEntity<>(body, headers),
                PlanResponseDto.class);
    }
}
