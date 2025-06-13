package com.portmanager.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple liveness probe.
 */
@RestController
public class HealthController {

    @GetMapping("/api/health")
    public String ok() {
        return "OK";
    }
}
