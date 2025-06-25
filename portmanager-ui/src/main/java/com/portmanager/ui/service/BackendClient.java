package com.portmanager.ui.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.portmanager.ui.model.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.Optional;

/**
 * REST-client to server. Singleton.
 */
public final class BackendClient {

    /* Singleton */

    private static volatile BackendClient INSTANCE;

    public static BackendClient get() {
        if (INSTANCE == null) {
            synchronized (BackendClient.class) {
                if (INSTANCE == null) INSTANCE = new BackendClient();
            }
        }
        return INSTANCE;
    }

    private final String baseUrl;                 // http://host:8080/api
    private final HttpClient http;
    private static final ObjectMapper JSON = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private BackendClient() {
        this.baseUrl = resolveBaseUrl();
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        System.out.println("[BackendClient] baseUrl = " + baseUrl);
    }

    //Public method to call AppController
    // POST /plan — return Optional with plan
    public Optional<PlanResponseDto> generatePlan(ConditionsDto scenario) {

        PlanningRequestDto req = new PlanningRequestDto(scenario, "baseline");
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/plan"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(req)))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }

        try {
            HttpResponse<String> resp =
                    http.send(request, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 200 && !resp.body().isBlank()) {
                return Optional.of(JSON.readValue(resp.body(), PlanResponseDto.class));
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /* POST /data/generate */
    public Optional<ConditionsDto> requestRandomData(int ships) {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/data/generate?ships=" + ships))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200 && !resp.body().isBlank()) {
                return Optional.of(JSON.readValue(resp.body(), ConditionsDto.class));
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /** POST /data/save */
    public boolean saveDataToDatabase(ConditionsDto scenario) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/data/save"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(scenario)))
                    .build();
            http.send(req, HttpResponse.BodyHandlers.discarding());
            return true;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Outdated dummy, idk implement it or not. */
    public Optional<PlanResponseDto> getLastAcceptedPlan() {
        return Optional.empty();
    }

    /* helpers */

    private static String resolveBaseUrl() {
        String url = System.getProperty("backendUrl");
        if (url != null && !url.isBlank()) return url.trim();
        url = System.getenv("UI_BACKEND_URL");
        if (url != null && !url.isBlank()) return url.trim();
        try (var is = BackendClient.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (is != null) {
                var p = new java.util.Properties();
                p.load(is);
                url = p.getProperty("backend.url");
                if (url != null && !url.isBlank()) return url.trim();
            }
        } catch (Exception ignored) {}
        return "http://localhost:8080/api";
    }
}
