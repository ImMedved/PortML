package com.portmanager.web;

import com.portmanager.dto.*;
import com.portmanager.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PlanController {

    private final PlanningService planningService;
    private final DataService dataService;
    private final ScenarioGeneratorService generatorService;

    /* ---------- GET /conditions ---------- */
    @GetMapping("/conditions")
    public ConditionsDto getConditions() {
        return dataService.getCurrentConditions();
    }

    /* ---------- POST /data/save ---------- */
    @PostMapping("/data/save")
    public void saveData(@RequestBody ConditionsDto dto) {
        dataService.overwriteWithUserData(dto);
    }

    /* ---------- POST /data/generate?ships=XX ---------- */
    @PostMapping("/data/generate")
    public ConditionsDto generate(@RequestParam(defaultValue = "500") int ships) {
        generatorService.generate(ships);
        return dataService.getCurrentConditions();   // <-- we will return the finished script
    }

    /* ---------- POST /plan ---------- */
    @PostMapping("/plan")
    public PlanResponseDto getPlan(@RequestBody PlanningRequestDto req) {
        return planningService.generatePlan(req);
    }

    /* ---------- DELETE /ships/{id} ---------- */
    @DeleteMapping("/ships/{id}")
    public ResponseEntity<Void> deleteShip(@PathVariable Long id) {
        dataService.deleteShip(id);
        return ResponseEntity.noContent().build();             // 204
    }

    /* ---------- DELETE /terminals/{id} ---------- */
    @DeleteMapping("/terminals/{id}")
    public ResponseEntity<Void> deleteTerminal(@PathVariable Long id) {
        dataService.deleteTerminal(id);
        return ResponseEntity.noContent().build();             // 204
    }
}
