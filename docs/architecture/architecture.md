# Genetic Planner — Application Architecture

## 1. Purpose

This document defines the logical architecture of Genetic Planner.

The architecture must:

* Separate frontend and backend responsibilities.
* Keep the planning domain independent from infrastructure technologies.
* Keep the Genetic Engine independent from React, Spring Boot and PostgreSQL.
* Define clear dependencies between application layers.
* Support the Academic and Work Shift planning templates.
* Allow persistence without coupling the domain to database technology.
* Provide a maintainable architecture suitable for the MVP.

The high-level architecture is:

```text
React
  ↓ REST
Spring Boot
  ↓
Application Services
  ↓
Planning Domain
  ↓
Genetic Engine
  ↓
Persistence
  ↓
PostgreSQL
```

This representation is conceptual.

The actual dependency rules are more restrictive and are described below.

---

# 2. Architectural Style

Genetic Planner follows a layered architecture with dependency inversion around the domain and Genetic Engine.

The main logical areas are:

```text
Frontend
Backend
    ├── API
    ├── Application
    ├── Domain
    ├── Genetic Engine
    └── Infrastructure
            └── Persistence
```

The architecture prioritizes:

* Separation of concerns.
* Explicit dependencies.
* Domain independence.
* Testability.
* Replaceable infrastructure.
* Simplicity appropriate for the MVP.

It is not intended to become a complex enterprise architecture.

---

# 3. High-Level Architecture

The overall system is:

```text
┌─────────────────────────────┐
│         React UI            │
│      TypeScript client      │
└──────────────┬──────────────┘
               │
               │ REST / JSON
               ▼
┌─────────────────────────────┐
│       Spring Boot API       │
│        Controllers          │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│    Application Services     │
└───────┬───────────┬─────────┘
        │           │
        ▼           ▼
 Planning Domain   Planning Templates
        │
        ▼
 Genetic Engine
        │
        ▼
 Repository Interfaces
        │
        ▼
 Persistence Adapters
        │
        ▼
 PostgreSQL
```

The frontend never communicates directly with:

* PostgreSQL.
* The Genetic Engine.
* Domain repositories.

All interaction passes through the backend REST API.

---

# 4. Physical Separation

The project uses separate frontend and backend applications.

Recommended repository structure:

```text
genetic-planner/
├── backend/
├── frontend/
├── docs/
├── datasets/
├── docker-compose.yml
└── README.md
```

This separation allows:

```text
frontend/
→ React + TypeScript

backend/
→ Kotlin + Spring Boot + Jenetics

PostgreSQL
→ external service/container
```

Frontend and backend communicate exclusively through REST.

---

# 5. Frontend Layer

The frontend is responsible for user interaction and presentation.

Technology:

```text
React
TypeScript
```

Its responsibilities include:

* Navigation.
* Planning creation workflow.
* Template selection.
* Problem configuration.
* Constraint configuration.
* Genetic Algorithm preset selection.
* Starting optimization.
* Displaying execution status.
* Displaying generated schedules.
* Displaying fitness and constraint breakdowns.

The frontend does not contain optimization logic.

---

# 6. Frontend Responsibilities

The frontend may understand concepts such as:

```text
Academic Planning
Work Shift Planning
Teacher
Employee
Class
Shift
```

because templates provide domain-oriented UX.

However, the frontend communicates with the backend through DTOs and API contracts.

Example:

```text
React Form
    ↓
Academic Planning Request
    ↓ REST
Backend
```

The UI does not instantiate or manipulate Jenetics objects.

---

# 7. REST API Layer

The REST API is the entry point to the backend.

It is implemented using Spring Boot controllers.

Responsibilities include:

* Receiving HTTP requests.
* Validating request structure.
* Converting DTOs into application commands.
* Invoking application services.
* Returning HTTP responses.
* Mapping application errors to appropriate HTTP status codes.

Conceptually:

```text
HTTP Request
    ↓
Controller
    ↓
Application Service
```

Controllers must remain thin.

---

# 8. API DTOs

API DTOs belong to the API layer.

Examples:

```text
CreatePlanningRequest
GenerateScheduleRequest
PlanningResponse
ScheduleResponse
ConstraintResultResponse
```

DTOs must not be used as core domain entities.

The mapping is:

```text
REST DTO
    ↓
Application layer
    ↓
Domain model
```

and:

```text
Domain result
    ↓
Application/API mapping
    ↓
REST DTO
```

This avoids coupling the planning domain to the HTTP API.

---

# 9. Application Layer

Application Services coordinate use cases.

Typical use cases include:

```text
Create planning problem
Update planning configuration
Build problem from template
Validate planning problem
Generate schedule
Retrieve schedule
Retrieve planning problem
```

The Application Layer orchestrates domain and infrastructure components.

Example:

```text
GenerateScheduleService
       │
       ├── load PlanningProblem
       ├── validate problem
       ├── invoke Genetic Engine
       ├── save result
       └── return PlanningResult
```

---

# 10. Application Services

Possible application services include:

```text
PlanningService
TemplateService
ScheduleGenerationService
ScheduleQueryService
```

For example:

```kotlin
class ScheduleGenerationService(
    private val planningRepository: PlanningRepository,
    private val geneticEngine: GeneticEngine,
    private val scheduleRepository: ScheduleRepository
)
```

The service coordinates the workflow but does not implement:

* Constraint algorithms.
* Genetic operators.
* Database queries.
* HTTP handling.

---

# 11. Planning Domain

The Planning Domain contains the generic planning model.

Core concepts include:

```text
PlanningProblem
PlanningHorizon

ResourceType
Resource

Activity
ResourceRequirement

TimeSlot
Location

Constraint

Schedule
Assignment

PlanningResult
ScheduleEvaluation
```

The domain contains planning semantics but no infrastructure dependencies.

---

# 12. Domain Independence

Domain classes must not depend on:

```text
React
Spring MVC
Spring Data JPA
Hibernate
PostgreSQL
Jenetics
Jackson
HTTP
```

For example, this is correct:

```kotlin
data class Resource(
    val id: String,
    val name: String,
    val typeId: String
)
```

The domain should not require annotations such as:

```text
@Entity
@Table
@RestController
@JsonProperty
```

---

# 13. Planning Templates

Planning Templates belong outside the core planning domain.

Initial templates:

```text
AcademicTemplate
WorkShiftTemplate
```

Their responsibility is:

```text
Template-specific configuration
        ↓
PlanningTemplate
        ↓
PlanningProblem
```

Templates may contain:

* Domain terminology.
* Default resource types.
* Default constraints.
* Default weights.
* Default optimization preset.
* Transformation logic.

---

# 14. Template Relationship

The architecture is:

```text
AcademicTemplate ─────────┐
                          │
                          ▼
                   PlanningProblem
                          │
                          ▼
                   Genetic Engine
                          ▲
                          │
WorkShiftTemplate ────────┘
```

Templates never modify the Genetic Engine.

Once the `PlanningProblem` exists, the source template is irrelevant to optimization.

---

# 15. Genetic Engine

The Genetic Engine performs optimization.

Its responsibilities include:

* Building the genetic representation.
* Generating the initial population.
* Decoding genotypes.
* Evaluating candidates.
* Applying genetic operators.
* Running evolution.
* Returning the best planning result.

Conceptually:

```text
PlanningProblem
      ↓
Candidate Domain Builder
      ↓
Genotype Factory
      ↓
Jenetics Engine
      ↓
Evolution
      ↓
Schedule
      ↓
PlanningResult
```

---

# 16. Genetic Engine Interface

The application layer should depend on an abstraction such as:

```kotlin
interface GeneticEngine {

    fun optimize(
        problem: PlanningProblem,
        config: GeneticAlgorithmConfig
    ): PlanningResult
}
```

This interface describes what the application needs.

The application does not need to know how Jenetics performs the evolution.

---

# 17. Jenetics Adapter

Jenetics belongs inside the Genetic Engine infrastructure implementation.

For example:

```text
GeneticEngine
      ▲
      │ implements
      │
JeneticsGeneticEngine
```

Conceptually:

```kotlin
class JeneticsGeneticEngine(
    private val constraintEvaluator: ConstraintEvaluator
) : GeneticEngine
```

Jenetics-specific types stay inside this implementation.

Examples:

```text
Genotype
IntegerGene
IntegerChromosome
Engine
EvolutionResult
Mutator
Crossover
```

These types must not leak into:

* REST API.
* Planning Domain.
* React frontend.
* Persistence domain interfaces.

---

# 18. Genetic Engine Independence

The Genetic Engine may depend on:

```text
PlanningProblem
Resource
Activity
ResourceRequirement
TimeSlot
Location
Constraint
Schedule
ScheduleEvaluation
GeneticAlgorithmConfig
```

It must not depend on:

```text
React
REST controllers
Spring MVC
Spring Data
Hibernate
PostgreSQL

AcademicTemplate
WorkShiftTemplate

Teacher
StudentGroup
Subject

Employee
WorkShift
```

---

# 19. Forbidden Genetic Engine Logic

The engine must not contain logic such as:

```kotlin
if (problem.templateType == "ACADEMIC") {
    ...
}
```

or:

```kotlin
when (problem.templateType) {
    "ACADEMIC" -> ...
    "WORK_SHIFT" -> ...
}
```

The engine optimizes only generic `PlanningProblem` structures.

---

# 20. Constraint Evaluation

Constraint evaluation is part of the planning optimization logic but remains independent from Jenetics.

Conceptually:

```text
Schedule
    ↓
ConstraintEvaluator
    ↓
ScheduleEvaluation
    ↓
Fitness
```

The Jenetics adapter invokes this generic evaluation mechanism.

Therefore:

```text
Jenetics
    ↓
decode Genotype
    ↓
Schedule
    ↓
ConstraintEvaluator
    ↓
Fitness
```

Constraint implementations do not depend on Jenetics.

---

# 21. Fitness Integration

The fitness flow defined previously remains:

```text
Genotype
    ↓
Decoder
    ↓
Schedule
    ↓
ConstraintEvaluator
    ↓
ScheduleEvaluation
    ↓
totalPenalty
    ↓
Fitness
```

The Genetic Algorithm minimizes:

```text
ScheduleEvaluation.totalPenalty
```

Ideal value:

```text
0
```

---

# 22. Persistence Boundary

Persistence is accessed through repository abstractions.

For example:

```kotlin
interface PlanningRepository {

    fun save(
        planningProblem: PlanningProblem
    ): PlanningProblem

    fun findById(
        id: String
    ): PlanningProblem?
}
```

and:

```kotlin
interface ScheduleRepository {

    fun save(
        schedule: Schedule
    ): Schedule

    fun findByPlanningProblemId(
        planningProblemId: String
    ): List<Schedule>
}
```

These interfaces describe application needs rather than database technology.

---

# 23. Persistence Adapters

The infrastructure layer implements repository interfaces.

For example:

```text
PlanningRepository
       ▲
       │
PostgresPlanningRepository
```

and:

```text
ScheduleRepository
       ▲
       │
PostgresScheduleRepository
```

The implementation may use:

```text
Spring Data JPA
Hibernate
PostgreSQL
```

without exposing these technologies to the domain.

---

# 24. Persistence Models

Database persistence models may differ from domain models.

For example:

```text
Domain

PlanningProblem
```

may be persisted through:

```text
PlanningProblemEntity
ResourceEntity
ActivityEntity
ConstraintEntity
```

Mapping occurs inside the infrastructure layer:

```text
Domain Model
    ↓
Persistence Mapper
    ↓
JPA Entity
    ↓
PostgreSQL
```

and vice versa.

---

# 25. Why Domain Models Are Not JPA Entities

The domain model should not contain:

```kotlin
@Entity
```

or other persistence annotations.

This keeps:

```text
Planning Domain
```

independent from:

```text
Hibernate / JPA
```

and makes domain objects easier to:

* Unit test.
* Reuse.
* Serialize differently.
* Optimize without persistence concerns.

---

# 26. PostgreSQL

PostgreSQL is the persistence technology selected for Genetic Planner.

It stores persistent application data such as:

```text
Planning configurations
Planning problems
Generated schedules
Generation metadata
```

The Genetic Engine never accesses PostgreSQL directly.

Incorrect:

```text
Genetic Engine
    ↓
PostgreSQL
```

Correct:

```text
Application Service
     │
     ├── Genetic Engine
     │
     └── Repository
              ↓
         PostgreSQL
```

---

# 27. Correct Generation Flow

A schedule generation request follows:

```text
React
    ↓ REST
ScheduleController
    ↓
ScheduleGenerationService
    ↓
PlanningRepository
    ↓
PlanningProblem
    ↓
ProblemValidator
    ↓
GeneticEngine
    ↓
PlanningResult
    ↓
ScheduleRepository
    ↓
PostgreSQL
    ↓
REST Response
    ↓
React
```

The Application Service coordinates the process.

The Genetic Engine does not manage persistence.

---

# 28. Dependency Rules

Dependencies must follow these rules.

## Frontend

May depend on:

```text
REST API contracts
```

Must not depend on:

```text
Backend implementation
Database
Jenetics
```

---

## API

May depend on:

```text
Application Layer
API DTOs
```

Must not implement:

```text
Genetic algorithms
Database access
Planning logic
```

---

## Application

May depend on:

```text
Planning Domain
GeneticEngine abstraction
Repository abstractions
Planning Templates
```

It coordinates use cases.

---

## Planning Domain

May depend only on:

```text
Kotlin / Java standard libraries
```

It should not depend on infrastructure frameworks.

---

## Genetic Engine

May depend on:

```text
Planning Domain
Constraint evaluation
Genetic configuration
```

The Jenetics implementation additionally depends on:

```text
Jenetics
```

---

## Infrastructure

May depend on:

```text
Domain abstractions
Repository interfaces
Spring Data
PostgreSQL driver
JPA/Hibernate
```

Infrastructure may depend inward.

The core must not depend outward.

---

# 29. Dependency Direction

The logical dependency direction is:

```text
API
 │
 ▼
Application
 │
 ├───────────────┐
 ▼               ▼
Domain       Genetic Engine
 ▲               │
 │               │
 └───── uses ────┘
```

Infrastructure implements abstractions required by the application:

```text
Application
     │
     ▼
Repository Interface
     ▲
     │ implements
Infrastructure
```

Therefore dependencies point toward stable abstractions.

---

# 30. Important Architectural Distinction

The simplified objective diagram says:

```text
Planning Domain
      ↓
Genetic Engine
      ↓
Persistence
```

However, this must not be interpreted as:

```text
GeneticEngine depends on Persistence
```

That dependency would be undesirable.

The real orchestration is:

```text
                Application Service
                 /              \
                /                \
               ▼                  ▼
       Genetic Engine        Repositories
               │                  │
               ▼                  ▼
            Domain            PostgreSQL
```

This distinction keeps optimization independent from data storage.

---

# 31. Logical Architecture

The complete logical architecture is:

```text
┌──────────────────────────────────┐
│             Frontend             │
│                                  │
│       React + TypeScript         │
└────────────────┬─────────────────┘
                 │
              REST/JSON
                 │
                 ▼
┌──────────────────────────────────┐
│            API Layer             │
│                                  │
│     Spring Boot Controllers      │
└────────────────┬─────────────────┘
                 │
                 ▼
┌──────────────────────────────────┐
│        Application Layer         │
│                                  │
│       Application Services       │
│        Use Case Orchestration    │
└──────────┬───────────┬───────────┘
           │           │
           ▼           ▼
┌────────────────┐ ┌─────────────────────┐
│ Planning Domain│ │ Planning Templates  │
│                │ │                     │
│ Resources      │ │ Academic            │
│ Activities     │ │ Work Shift          │
│ Constraints    │ └──────────┬──────────┘
│ Schedule       │            │
└───────┬────────┘            │
        │                     │
        └──────────┬──────────┘
                   ▼
            PlanningProblem
                   │
                   ▼
┌──────────────────────────────────┐
│          Genetic Engine          │
│                                  │
│ Encoding                         │
│ Constraint Evaluation            │
│ Fitness                          │
│ Jenetics Adapter                 │
│ Evolution                        │
└──────────────────────────────────┘

        Application Layer
                 │
                 ▼
┌──────────────────────────────────┐
│       Repository Interfaces      │
└────────────────┬─────────────────┘
                 ▲
                 │ implements
┌────────────────┴─────────────────┐
│       Persistence Adapters       │
│                                  │
│ Spring Data / JPA                │
└────────────────┬─────────────────┘
                 │
                 ▼
┌──────────────────────────────────┐
│           PostgreSQL             │
└──────────────────────────────────┘
```

---

# 32. Backend Logical Packages

A possible backend structure is:

```text
backend/
└── src/main/kotlin/com/geneticplanner/
    ├── api/
    │   ├── controller/
    │   ├── request/
    │   └── response/
    │
    ├── application/
    │   └── service/
    │
    ├── domain/
    │   ├── model/
    │   ├── constraint/
    │   ├── evaluation/
    │   └── repository/
    │
    ├── template/
    │   ├── academic/
    │   └── workshift/
    │
    ├── genetic/
    │   ├── GeneticEngine.kt
    │   ├── config/
    │   ├── encoding/
    │   ├── fitness/
    │   └── jenetics/
    │
    └── infrastructure/
        └── persistence/
            ├── entity/
            ├── repository/
            └── mapper/
```

This is a logical proposal.

Exact package names may be adjusted during #20.

---

# 33. Frontend Logical Structure

The frontend may follow:

```text
frontend/
└── src/
    ├── api/
    ├── components/
    ├── features/
    │   ├── planning/
    │   ├── templates/
    │   ├── constraints/
    │   └── results/
    ├── pages/
    ├── types/
    └── utils/
```

Detailed component strategy belongs to task #23.

---

# 34. Cross-Layer Example

For Academic Scheduling:

```text
React Academic Form
        ↓
REST Academic Request
        ↓
TemplateService
        ↓
AcademicTemplate
        ↓
PlanningProblem
        ↓
ScheduleGenerationService
        ↓
GeneticEngine
        ↓
Schedule
        ↓
Persistence
        ↓
ScheduleResponse
        ↓
React Results
```

For Work Shift Scheduling:

```text
React Work Shift Form
        ↓
REST Work Shift Request
        ↓
TemplateService
        ↓
WorkShiftTemplate
        ↓
PlanningProblem
        ↓
same GeneticEngine
```

The optimization path is identical.

---

# 35. Technology Boundaries

The architecture defines clear technology boundaries.

```text
React / TypeScript
→ Presentation

Spring Boot
→ Backend application and API

Kotlin
→ Backend implementation

Jenetics
→ Genetic optimization implementation

Spring Data JPA
→ Persistence adapter

PostgreSQL
→ Database
```

No technology should unnecessarily propagate into another layer.

---

# 36. Testing Strategy by Layer

The separation supports different test levels.

## Domain

```text
Unit tests
```

for:

* Constraint evaluation.
* Problem validation.
* Schedule evaluation.

## Genetic Engine

Tests for:

* Encoding.
* Decoding.
* Fitness.
* Evolution.
* Candidate domains.

## Application

Tests for:

* Use case orchestration.
* Template transformation.

## Persistence

Integration tests for:

* Repository adapters.
* Database mapping.

## API

Tests for:

* HTTP contracts.
* Request validation.

## Frontend

Tests for:

* Components.
* User workflows.

---

# 37. Deployment View

For the MVP, deployment remains simple:

```text
Browser
   │
   ▼
React Frontend
   │
   ▼
Spring Boot Backend
   │
   ▼
PostgreSQL
```

Docker Compose can provide:

```text
frontend
backend
postgres
```

No microservices architecture is required.

---

# 38. Explicitly Rejected Complexity

The MVP does not require:

```text
Microservices
Kubernetes
Message brokers
Event sourcing
CQRS
Distributed optimization
Separate Genetic Engine service
Separate template service
```

The Genetic Engine is a logical component inside the backend application.

This keeps implementation achievable within the project scope.

---

# 39. Architectural Rules

The following rules are mandatory.

### AR1

Frontend and backend are separate applications.

### AR2

Frontend communicates with backend only through REST.

### AR3

REST controllers invoke Application Services.

### AR4

Controllers contain no optimization or persistence logic.

### AR5

Application Services coordinate use cases.

### AR6

Planning Domain contains generic planning concepts only.

### AR7

Domain models contain no Spring, JPA or Jenetics annotations.

### AR8

Planning Templates produce generic `PlanningProblem` objects.

### AR9

Genetic Engine receives only generic planning objects.

### AR10

Genetic Engine contains no template-specific branching.

### AR11

Jenetics-specific types remain inside the Genetic Engine implementation.

### AR12

Genetic Engine does not access PostgreSQL.

### AR13

Persistence is accessed through repository abstractions.

### AR14

PostgreSQL-specific code remains in infrastructure.

### AR15

Application Services orchestrate Genetic Engine and Persistence independently.

---

# 40. Architecture Diagram

The architecture diagram is stored in:

```text
docs/diagrams/application-architecture.puml
```

It represents:

```text
React
  ↓ REST
Spring Boot API
  ↓
Application Services
  ├── Planning Templates
  ├── Planning Domain
  ├── Genetic Engine
  └── Repository Interfaces
                   ↑
           Persistence Adapters
                   ↓
              PostgreSQL
```

The essential rule is:

> **The Application Layer orchestrates the Genetic Engine and Persistence; the Genetic Engine does not depend on Persistence.**

---

# 41. Design Decisions

## AD1 — Separate frontend and backend

```text
frontend/
backend/
```

are independent applications.

## AD2 — REST is the application boundary

React communicates with Spring Boot through JSON REST endpoints.

## AD3 — Application Services orchestrate use cases

Controllers remain thin.

## AD4 — Planning Domain is framework-independent

No Spring, JPA, database or Jenetics dependencies are allowed in core domain models.

## AD5 — Planning Templates remain outside the core domain

They transform template-specific configuration into `PlanningProblem`.

## AD6 — Genetic Engine uses only the generic planning model

Academic and Work Shift concepts never enter optimization logic.

## AD7 — Jenetics is an implementation detail

Jenetics-specific classes remain behind the `GeneticEngine` abstraction.

## AD8 — Persistence uses dependency inversion

Repository interfaces define persistence requirements.

## AD9 — PostgreSQL remains infrastructure

The domain and Genetic Engine do not depend on PostgreSQL.

## AD10 — Genetic Engine does not persist results

Application Services decide when and what to persist.

## AD11 — Architecture remains monolithic for the backend

Logical modules are separated inside one Spring Boot application.

## AD12 — Avoid unnecessary enterprise complexity

The architecture should support the MVP rather than introduce infrastructure without clear project value.

---

# 42. Summary

The Genetic Planner architecture is:

```text
React
  ↓ REST
Spring Boot API
  ↓
Application Services
  ├───────────────┬────────────────┐
  ↓               ↓                ↓
Templates    Planning Domain   Genetic Engine
  │               │                │
  └──────→ PlanningProblem ←────────┘

Application Services
        ↓
Repository Interfaces
        ↑
Persistence Adapters
        ↓
PostgreSQL
```

The central architectural boundaries are:

```text
Templates
    ↓
PlanningProblem
    ↓
Genetic Engine
```

and:

```text
Application Service
    ├── Genetic Engine
    └── Persistence
```

not:

```text
Genetic Engine
    ↓
Persistence
```

This preserves a reusable, testable and domain-independent Genetic Engine while keeping the overall system simple enough for the Genetic Planner MVP.
