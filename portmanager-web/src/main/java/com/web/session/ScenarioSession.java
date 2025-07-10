package com.web.session;

import com.web.model.ConditionsDto;
import com.web.model.PlanResponseDto;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

/**
 * Stores the script and the last generated plan inside the user's HTTP session.
 */

@Component
@Scope(
        value = WebApplicationContext.SCOPE_SESSION,
        proxyMode = ScopedProxyMode.TARGET_CLASS
)
public class ScenarioSession {

    private ConditionsDto scenario;
    private PlanResponseDto plan;

    /* getters / setters */
    public ConditionsDto getScenario()          { return scenario; }
    public void          setScenario(ConditionsDto scenario) { this.scenario = scenario; }

    public PlanResponseDto getPlan()            { return plan; }
    public void           setPlan(PlanResponseDto plan)      { this.plan = plan; }
}