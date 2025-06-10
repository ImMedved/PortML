package com.portmanager.web;

import com.portmanager.client.MlServiceClient;
import com.portmanager.dto.ConditionsDto;
import com.portmanager.dto.PairwiseFeedbackDto;
import com.portmanager.dto.PairwiseRequestDto;
import com.portmanager.dto.PlanResponseDto;
import com.portmanager.model.PlanningAlgorithm;
import com.portmanager.service.FeedbackService;
import com.portmanager.service.PairwiseService;
import com.portmanager.service.PlanningService;
import com.portmanager.service.ScenarioGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * PlanController
 *
 * REST interface used by Java-FX UI.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PlanController {

    private final PlanningService planningService;
    private final ScenarioGeneratorService generatorService;
    private final PairwiseService pairwiseService;
    private final FeedbackService feedbackService;
    private final MlServiceClient mlClient;

    /* -------- current plan -------- */
    @GetMapping("/plan/current")
    public ResponseEntity<PlanResponseDto> currentPlan() {
        var plan = planningService.getLastPlan();
        return plan == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(plan);
    }

    /* -------- build new plan -------- */
    @PostMapping("/plan")
    public PlanResponseDto generatePlan(@RequestParam(defaultValue = "baseline") String algorithm) {
        PlanningAlgorithm algoEnum = PlanningAlgorithm.fromString(algorithm);
        return planningService.generatePlan(algoEnum);
    }

    /* -------- generate new data -------- */
    @PostMapping("/data/generate")
    public ResponseEntity<Void> generateData(@RequestParam(defaultValue = "20") int ships) {
        generatorService.generate(ships);
        return ResponseEntity.ok().build();
    }

    /* -------- compare 2 plans -------- */
    @GetMapping("/compare")
    public PairwiseRequestDto compare(@RequestParam(defaultValue = "baseline") String algoA,
                                      @RequestParam(defaultValue = "RL") String algoB) {
        return pairwiseService.buildComparison(
                PlanningAlgorithm.fromString(algoA),
                PlanningAlgorithm.fromString(algoB));
    }

    /* -------- feedback -------- */
    @PostMapping("/feedback")
    public ResponseEntity<Void> feedback(@RequestBody PairwiseFeedbackDto dto) {
        feedbackService.accept(dto);
        return ResponseEntity.ok().build();
    }

    /* -------- retrain -------- */
    @PostMapping("/train")
    public ResponseEntity<Void> retrainModel() {
        mlClient.train();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/conditions")
    public ConditionsDto getConditions() {
        return planningService.getCurrentConditions(); // метод нужно реализовать
    }

}