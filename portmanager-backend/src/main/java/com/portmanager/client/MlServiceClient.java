package com.portmanager.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.portmanager.dto.PairwiseFeedbackDto;
import com.portmanager.dto.PlanRequestDto;
import com.portmanager.dto.PlanResponseDto;

/**
 * MlServiceClient
 *
 * Thin wrapper around HTTP calls to FastAPI ML service.
 * All endpoints are fixed under the <code>/v1</code> prefix (see main.py in ML service).
 */
@Component
@RequiredArgsConstructor
public class MlServiceClient {

    private final RestTemplate restTemplate;

    //URL can be overridden via application.yml (ml.service.url).
    @Value("${ml.service.url:http://localhost:8000/v1}")
    private String mlServiceUrl;

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