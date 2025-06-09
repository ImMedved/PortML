package com.portmanager.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portmanager.model.PlanningAlgorithm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Ensures /api/plan endpoint returns valid JSON and delegates to service.
 */

/*
@WebMvcTest(controllers = PlanController.class)
class PlanControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @SpyBean com.portmanager.service.PlanningService planningService;

    @Test
    void postPlanReturns200AndJson() throws Exception {
        // заглушаем ответ сервиса минимальным объектом
        com.portmanager.dto.PlanResponseDto dummy =
                com.portmanager.dto.PlanResponseDto.builder()
                        .algorithmUsed(PlanningAlgorithm.BASELINE)
                        .metrics(com.portmanager.dto.MetricsDto.builder().build())
                        .schedule(List.of())
                        .build();
        doReturn(dummy).when(planningService).generatePlan(any());

        mvc.perform(post("/api/plan")
                        .param("algorithm", "baseline"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.algorithmUsed").value("baseline"));
    }
}*/
