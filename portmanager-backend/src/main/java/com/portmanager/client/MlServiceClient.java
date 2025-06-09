package com.portmanager.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.portmanager.dto.PairwiseFeedbackDto;
import com.portmanager.dto.PlanRequestDto;
import com.portmanager.dto.PlanResponseDto;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MlServiceClient
 *
 * Thin wrapper around HTTP calls to FastAPI ML service.
 * All endpoints are fixed under the /v1 prefix.
 */
@Component
@RequiredArgsConstructor
public class MlServiceClient {

    private static final Logger log = LoggerFactory.getLogger(MlServiceClient.class);

    private final RestTemplate restTemplate;

    @Value("${ml.service.url:http://localhost:8000}")
    private String baseUrl;

    private String mlServiceUrl;

    @PostConstruct
    private void init() {
        // ensure it ends with /v1
        if (baseUrl.endsWith("/v1")) {
            mlServiceUrl = baseUrl;
        } else {
            mlServiceUrl = baseUrl.endsWith("/") ? baseUrl + "v1" : baseUrl + "/v1";
        }
        log.info("ML Service URL resolved to: {}", mlServiceUrl);
    }

    public PlanResponseDto requestPlan(PlanRequestDto req) {
        return restTemplate.postForObject(mlServiceUrl + "/plan", req, PlanResponseDto.class);
    }

    public void sendFeedback(PairwiseFeedbackDto dto) {
        restTemplate.postForLocation(mlServiceUrl + "/feedback", dto);
    }

    public void train() {
        restTemplate.postForLocation(mlServiceUrl + "/train", null);
    }
}
