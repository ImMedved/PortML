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

## Reference: entity fields

| **ShipDto field**            | Type      | Notes / constraints                                                            |
|------------------------------|-----------|--------------------------------------------------------------------------------|
| `id`                         | string    | UI identifier (unique)                                                         |
| `arrivalTime`                | datetime  | ETA, local TZ                                                                  |
| `arrivalWindowStart`         | datetime  | — optional hard window                                                         |
| `arrivalWindowEnd`           | datetime  |                                                                                |
| `length`                     | double ⟨m⟩|                                                                                |
| `draft`                      | double ⟨m⟩|                                                                                |
| `deadweight`                 | double ⟨t⟩|                                                                                |
| `cargoType`                  | enum      | *container / general / bulk / oil / lng*                                       |
| `shipType`                   | enum      | must be compatible with `cargoType`                                            |
| `hazardClass`                | string    | IMDG codes; priority scheduling if set                                         |
| `temperatureControlled`      | boolean   | high-priority, waiting ≤ X h (configurable)                                    |
| `fuelType`                   | enum      | *diesel / lng*                                                                 |
| `emissionRating`             | enum      | *A/B/C*                                                                        |
| `flagCountry`                | string    | ISO-2                                                                          |
| `imoNumber`                  | string    |                                                                                |
| `arrivalPort`                | string    | ISO-UN/LOCODE                                                                  |
| `nextPort`                   | string    |                                                                                |
| `requiresCustomsClearance`   | boolean   | must visit *Raid* terminal in daylight before service                          |
| `requiresPilot`              | boolean   | can arrive/depart only 09:00-18:00 → auto-delay after night finish             |
| `estDurationHours`           | double ⟨h⟩| service time                                                                  |
| `expectedDelayHours`         | double ⟨h⟩| used by ML as feature                                                          |
| `priority`                   | enum      | *normal / high*                                                                |

| **TerminalDto field**        | Type      | Notes / constraints                                                            |
|------------------------------|-----------|--------------------------------------------------------------------------------|
| `id`                         | long      | primary key                                                                    |
| `name`                       | string    |                                                                                |
| `maxLength`                  | double ⟨m⟩| total quay length                                                              |
| `maxDraft`                   | double ⟨m⟩|                                                                                |
| `allowedCargoTypes`          | list&lt;string&gt; | as in `cargoType`                                                          |
| `fuelSupported`              | list&lt;string&gt; | must intersect with vessel `fuelType`                                      |

## Structure

- portmanager-ui/          ← JavaFX UI application
- portmanager-backend/     ← Spring Boot backend
- ml-service/              ← ML-based scheduling microservice
- docker-compose.yml       ← Launches backend + ML + database
