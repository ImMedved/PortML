package com.portmanager.service;

import com.portmanager.client.MlServiceClient;
import com.portmanager.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlanningService {

    private final MlServiceClient ml;

    public PlanResponseDto generatePlan(PlanningRequestDto req) {
        return ml.requestPlan(req.scenario(), req.algorithm());
    }
}
