package com.portmanager;

/**
 * PortManagerApplication
 *
 * Main entry-point for the Spring Boot backend of the demo berth-allocation project.
 * Starts the web server and scans components in the com.portmanager package.
 */
import com.portmanager.service.ScenarioGeneratorService;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PortManagerApplication {
    private final ScenarioGeneratorService generatorService;
    // spring-boot создаёт bean через конструктор
    public PortManagerApplication(ScenarioGeneratorService generatorService) {
        this.generatorService = generatorService;
    }

    /** Автоматически создаём дефолтный сценарий,
     *  чтобы ML сразу получил непустой PlanRequest. */
    @PostConstruct
    public void initScenario() {
        generatorService.generateDefault();   // ← новый метод (см. ниже)
    }

    public static void main(String[] args) {
        SpringApplication.run(PortManagerApplication.class, args);
    }
}
