package com.portmanager.client;

import com.portmanager.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class MlServiceClient {

    private static final String BASE = "http://ml-service:5000/api";
    private final RestTemplate rest = new RestTemplate();

    public PlanResponseDto requestPlan(ConditionsDto scenario, String algorithm) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ConditionsDto> entity = new HttpEntity<>(scenario, headers);
        String url = BASE + "/plan?algorithm=" + algorithm;

        return rest.postForObject(url, entity, PlanResponseDto.class);
    }
}
