package com.portmanager;

/*
  Main entry-point for the Spring Boot backend of the demo berth-allocation project.
  Starts the web server and scans components in the com.portmanager package.
 */
import com.portmanager.service.ScenarioGeneratorService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PortManagerApplication {
    // spring-boot creates a bean via a constructor
    public PortManagerApplication(ScenarioGeneratorService generatorService) {
    }

    public static void main(String[] args) {
        SpringApplication.run(PortManagerApplication.class, args);
    }
}
