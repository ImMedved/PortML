package com.portmanager.ui.service;

import com.google.gson.Gson;
import com.portmanager.ui.model.*;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class BackendClient {
    private static final String BASE_URL = "http://localhost:8080/api";
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    public Optional<PlanResponse> generatePlan(String algorithm)
    {
        try {
            //String json = gson.toJson(request);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/plan?algorithm=" + URLEncoder.encode(algorithm, StandardCharsets.UTF_8)))
                 .POST(HttpRequest.BodyPublishers.noBody())      // тело не нужно
                 .build();

            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            return Optional.of(gson.fromJson(response.body(), PlanResponse.class));
        } catch (Exception e) {
            System.err.println("Error while requesting plan: " + e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<PairwiseRequest> getPairwisePlans() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/compare"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return Optional.of(gson.fromJson(response.body(), PairwiseRequest.class));
        } catch (IOException | InterruptedException e) {
            System.err.println("Error while getting comparison: " + e.getMessage());
            return Optional.empty();
        }
    }

    public void sendFeedback(String comparisonId, String chosenPlan) {
        try {
            PairwiseFeedback feedback = new PairwiseFeedback(comparisonId, chosenPlan);
            String json = gson.toJson(feedback);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/feedback"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            System.err.println("Error sending feedback: " + e.getMessage());
        }
    }
}
