# Genetic Planner

Genetic Planner is a generic planning and scheduling application based on genetic algorithms.

The project aims to automatically generate and optimize schedules while satisfying mandatory constraints and improving preferences such as workload balance, continuity, or preferred time slots.

The system is designed around a generic planning model so that the same optimization engine can be applied to different scheduling domains. Academic scheduling is the primary MVP scenario, while work-shift scheduling is planned as a second validation scenario.

This project is being developed as a Final Degree Project (TFG) in Computer Engineering at UNED.

## Features

### Current

- Generic planning domain model.
- Hard and soft constraint model.
- Weighted penalty-based fitness model.
- Genetic representation based on one scheduling decision per activity.
- Genetic algorithm configuration with predefined optimization profiles.
- Jenetics 9.1.0 proof of concept.
- Template-based architecture for different planning domains.
- Academic scheduling template design.
- Layered modular-monolith application architecture.
- Frontend architecture and UX strategy.
- Low-fidelity application wireframes.
- Spring Boot backend foundation.
- PostgreSQL development database using Docker Compose.
- Application and Actuator health endpoints.
- Automated backend API tests.

### Planned for the MVP

- Planning problem creation and configuration.
- Resource, activity, time-slot and location management.
- Constraint configuration.
- Genetic schedule generation and optimization.
- Academic scheduling workflow.
- Schedule results visualization.
- Calendar and table result views.
- Constraint violation and fitness breakdown.
- Experimental evaluation of the genetic algorithm.

Additional planning templates and advanced features may be added after the core MVP is stable.

## Architecture

Genetic Planner follows a pragmatic layered modular-monolith architecture.

```text
Frontend
   │
   │ REST API
   ▼
Backend
├── API
├── Application
├── Domain
├── Template
├── Genetic Engine
└── Infrastructure
    └── Persistence
```

The main architectural principle is to keep the planning domain and genetic engine independent from specific scheduling scenarios.

Planning templates provide domain-specific terminology, defaults and configuration, but are transformed into the same generic `PlanningProblem` consumed by the genetic engine.

```text
Planning Template
       │
       ▼
PlanningProblem
       │
       ▼
ProblemValidator
       │
       ▼
Genetic Engine
       │
       ▼
Schedule
```

The domain layer does not depend on Spring, JPA or Jenetics. Framework-specific and persistence concerns are kept outside the core domain.

## Technologies

### Backend

- Kotlin 2.4.0
- Java 25 LTS
- Spring Boot 4.1.1
- Spring Web
- Spring Data JPA
- Hibernate
- PostgreSQL 18
- Gradle 9.7.x

### Genetic Algorithm

- Jenetics 9.1.0

### Frontend

Planned frontend stack:

- React
- TypeScript
- Vite
- Material UI
- FullCalendar
- React Hook Form
- Zod
- TanStack Query
- Axios

### Development and Infrastructure

- Docker
- Docker Compose
- Git
- GitHub
- GitHub Projects
- IntelliJ IDEA
- PlantUML

## Getting Started

### Requirements

Before running the project, install:

- JDK 25
- Docker Desktop
- Git

The Gradle Wrapper is included in the repository, so a separate Gradle installation is not required.

### Clone the repository

```bash
git clone <repository-url>
cd genetic-planner
```

### Start PostgreSQL

The development database runs in Docker.

```bash
docker compose up -d
```

Check that the container is running and healthy:

```bash
docker compose ps
```

The default development database configuration is:

```text
Database: genetic_planner
Host: localhost
Port: 5432
User: genetic_planner
Password: genetic_planner
```

JDBC URL:

```text
jdbc:postgresql://localhost:5432/genetic_planner
```

The credentials above are intended for the local development environment only.

## Running Genetic Planner

### Backend

Start the backend from the project root:

#### Windows

```powershell
.\gradlew.bat :backend:bootRun
```

#### Linux / macOS

```bash
./gradlew :backend:bootRun
```

The backend is available at:

```text
http://localhost:8080
```

### Health checks

Genetic Planner provides a simple application health endpoint:

```text
GET http://localhost:8080/health
```

Expected response:

```json
{
  "status": "UP",
  "application": "genetic-planner"
}
```

Spring Boot Actuator also provides:

```text
GET http://localhost:8080/actuator/health
```

### Run backend tests

#### Windows

```powershell
.\gradlew.bat :backend:test
```

#### Linux / macOS

```bash
./gradlew :backend:test
```

### Build the backend

#### Windows

```powershell
.\gradlew.bat :backend:build
```

#### Linux / macOS

```bash
./gradlew :backend:build
```

### Stop PostgreSQL

```bash
docker compose down
```

To also remove the development database volume:

```bash
docker compose down -v
```

## Project Structure

```text
genetic-planner/
├── app/
│   └── Jenetics proof of concept
│
├── backend/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── kotlin/
│       │   │   └── com/geneticplanner/
│       │   │       ├── GeneticPlannerApplication.kt
│       │   │       └── api/
│       │   └── resources/
│       └── test/
│
├── frontend/
│   └── Frontend application (planned)
│
├── datasets/
│   └── Evaluation and example datasets
│
├── docs/
│   ├── requirements/
│   ├── architecture/
│   ├── algorithm/
│   ├── ux/
│   └── diagrams/
│
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── docker-compose.yml
├── gradlew
├── gradlew.bat
└── README.md
```

The `app` module currently contains the Jenetics proof of concept used to validate the genetic algorithm technology and Java/Kotlin baseline. The production backend is implemented in the separate `backend` module.

## Documentation

Detailed design and project documentation is maintained under [`docs/`](docs/).

The documentation currently covers:

- Project scope and requirements.
- Generic planning domain model.
- Constraint model and constraint catalogue.
- Constraint evaluation and fitness strategy.
- Genetic representation and algorithm configuration.
- Planning template architecture.
- Application architecture.
- Frontend strategy.
- User flows and low-fidelity wireframes.

Architecture and UX diagrams are maintained as PlantUML source files under [`docs/diagrams/`](docs/diagrams/).

The repository documentation is intended to serve both as development documentation and as supporting material for the final TFG report.

## Project Status

Genetic Planner is currently under active development.

The initial architecture and technical foundations have been defined and the backend development environment is operational. The next development phase focuses on implementing the generic planning domain, constraint evaluation and genetic optimization engine.

Development is organized using GitHub Issues and GitHub Projects, following a lightweight Kanban workflow.

## License

This project has been developed as an academic Final Degree Project (TFG) at UNED.

License terms have not yet been defined.