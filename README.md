# PortML — Intelligent Port Mooring Scheduler

PortML is an interactive system for planning the mooring of ships in a port, taking into account terminal constraints, ship parameters, and real-world events such as weather and closures.

## Features

- Interactive JavaFX-based UI to manage terminals, ships, and events.
- AI-generated and baseline scheduling algorithms.
- Support for port-wide and per-terminal closures.
- Editable scenario via GUI or randomly generated data.
- Plan comparison and real-time Gantt chart visualization.

## Quick Start

### Prerequisites

- **Java 17+** (Amazon Corretto recommended): https://docs.aws.amazon.com/corretto/latest/corretto-21-ug/downloads-list.html
- **JavaFX SDK 21+** (for UI): https://gluonhq.com/products/javafx/
- **Docker + Docker Compose**

### 1. Build UI

```bash
cd portmanager-ui
mvn clean package
```

### 2. Launch Backend Services
```bash
cd ..
docker compose up --build
```
### 3. Launch UI (in separate terminal)
```bash
cd portmanager-ui
java --module-path "C:\Java\javafx-sdk-21.0.7\lib" \
     --add-modules javafx.controls,javafx.fxml \
     -jar target/portmanager-ui-jar-with-dependencies.jar
```

## Notes
- Random data can be generated via the **"Random Data"** button in the UI.
- The generated scenario includes:
    - A set of ships with randomized length, draft, priority, and cargo type;
    - A predefined set of terminals;
    - Weather events and terminal closures.

> However, the generator data may not be realistic, and sometimes it is impossible to create a plan from it.  
> You must check them before generation to achieve maximum efficiency via **Edit Vessels/Terminals/Events** for the scheduling algorithms to work.

- Once arrival times are filled in, click **"Generate Plan"** to produce a mooring schedule.

## Troubleshooting
Error: failed to execute goal on project portmanager-backend: Could not resolve dependencies...

Fix: remove all Docker containers, volumes, and images, then rebuild:
```bash
docker system prune -a --volumes
docker compose up --build
```

## Structure

- portmanager-ui/          ← JavaFX UI application
- portmanager-backend/     ← Spring Boot backend
- ml-service/              ← ML-based scheduling microservice
- docker-compose.yml       ← Launches backend + ML + database
