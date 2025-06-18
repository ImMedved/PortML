package com.portmanager.service;

import com.portmanager.client.MlServiceClient;
import com.portmanager.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Builds a simple “baseline” plan locally; for other algorithms
 * proxies the request to an external ML service.
 */
@Service
@RequiredArgsConstructor
public class PlanningService {

    private final MlServiceClient ml;

    public PlanResponseDto generatePlan(PlanningRequestDto req) {
        return ml.requestPlan(req.scenario(), req.algorithm());
    }
}
