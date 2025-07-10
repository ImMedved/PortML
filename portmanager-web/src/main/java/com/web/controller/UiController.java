package com.web.controller;

import com.web.service.BackendClient;
import com.web.session.ScenarioSession;
import com.web.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.function.Function;

@RestController
@RequestMapping("/ui")
@RequiredArgsConstructor
public class UiController {

    private final BackendClient backend;
    private final ScenarioSession sess;

    /* ---------- RANDOM ---------- */
    @PostMapping("/random")
    public ConditionsDto random(@RequestParam("ships") int ships) {
        ConditionsDto dto = backend.requestRandomData(ships)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY, "Backend empty"));
        sess.setScenario(dto);
        return dto;
    }

    /* ---------- ADVANCED ---------- */
    @PostMapping("/custom")
    public ConditionsDto custom(@RequestBody GenerationConfigDto cfg) {
        ConditionsDto dto = backend.requestCustomData(cfg)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY, "Backend empty"));
        sess.setScenario(dto);
        return dto;
    }

    /* ---------- GENERATE PLAN ---------- */
    @PostMapping("/plan")
    public PlanResponseDto plan(@RequestParam("alg") String alg) {
        ConditionsDto scn = ensureScenario();

        PlanResponseDto rp = backend.generatePlan(scn)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY, "Backend empty"));
        sess.setPlan(rp);
        return rp;
    }

    @PutMapping("/plan/manual")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void saveManual(@RequestBody List<ScheduleItemDto> sched) {
        PlanResponseDto p = sess.getPlan();
        if (p == null)
            throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,"No plan");
        p.setSchedule(sched);
    }

    /* ---------- SHIP INFO ---------- */
    @GetMapping("/ship/{id}")
    public ShipDto ship(@PathVariable String id) {
        return ensureScenario().ships()
                .stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    /* ---------- TERMINALS CRUD (пример) ---------- */
    @PutMapping("/terminals")
    public ConditionsDto saveTerminal(@RequestBody TerminalDto t) {
        upsert(ensureScenario().terminals(), t, TerminalDto::getId);
        return sess.getScenario();
    }
    @DeleteMapping("/terminals/{id}")
    public void deleteTerminal(@PathVariable long id) {
        backend.deleteTerminal(id);
        ensureScenario().terminals().removeIf(x -> x.getId() == id);
    }

    /* ---------- HELP ---------- */
    private ConditionsDto ensureScenario() {
        if (sess.getScenario() == null)
            throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "No scenario");
        return sess.getScenario();
    }

    private <T,K> void upsert(List<T> list, T obj, Function<T,K> keyFun) {
        K key = keyFun.apply(obj);
        list.removeIf(o -> keyFun.apply(o).equals(key));
        list.add(obj);
    }
}
