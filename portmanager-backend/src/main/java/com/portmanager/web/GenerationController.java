package com.portmanager.web;

import com.portmanager.dto.ConditionsDto;
import com.portmanager.dto.GenerationConfigDto;
import com.portmanager.service.DataService;
import com.portmanager.service.ScenarioGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class GenerationController {

    private final ScenarioGeneratorService generator;
    private final DataService              dataService;

    /** Generate scenario with user-provided percentages. */
    @PostMapping("/generate-custom")
    public ResponseEntity<ConditionsDto> generateCustom(
            @RequestBody GenerationConfigDto cfg) {

        generator.generateCustom(cfg);                 // wipe + fill DB
        ConditionsDto dto = dataService.getCurrentConditions();
        return ResponseEntity.ok(dto);
    }
}
