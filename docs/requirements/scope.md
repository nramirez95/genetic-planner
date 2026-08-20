# Genetic Planner — MVP Scope

## 1. Product Vision

**Genetic Planner** is a flexible web-based software tool for the automatic generation and optimization of schedules and planning problems using genetic algorithms.

The application is designed to support different scheduling scenarios, such as academic timetables, work shifts, school calendars, or other resource allocation problems.

Rather than implementing a scheduling engine tied to a specific domain, Genetic Planner models planning problems through a set of generic concepts such as **resources, activities, time slots, locations, and constraints**.

The same genetic optimization engine should therefore be able to process different planning scenarios without requiring changes to its core implementation.

The application will provide an intuitive and visually attractive graphical interface so that users can configure and generate schedules without requiring knowledge of genetic algorithms or optimization techniques.

---

## 2. Project Objectives

The main objective of the project is to design and develop a generic planning tool capable of generating optimized schedules using genetic algorithms.

The specific objectives are:

* Design a domain-independent model for representing scheduling and planning problems.
* Design and implement a configurable genetic algorithm for schedule generation and optimization.
* Support both mandatory (**hard**) and desirable (**soft**) planning constraints.
* Allow different types of planning problems to be represented using the same optimization engine.
* Provide predefined templates for common planning scenarios.
* Provide an intuitive graphical interface for configuring planning problems.
* Display generated schedules and relevant optimization information in a clear and understandable way.
* Evaluate the quality and performance of the genetic algorithm under different configurations and problem sizes.
* Maintain a modular architecture that allows new constraints and planning scenarios to be added in the future.

---

## 3. Target User

The main user of Genetic Planner is a **Planner**.

A Planner represents any person responsible for creating or managing a schedule or resource allocation plan.

Examples include:

* A coordinator responsible for creating academic timetables.
* A manager responsible for assigning employee shifts.
* A school administrator responsible for organizing school schedules.
* A person responsible for allocating resources, activities, or locations over a set of time periods.

Genetic Planner is a **planning and optimization tool**, rather than a complete school management, human resources, or workforce management system.

For the MVP, only one generic user profile will therefore be considered. Authentication, authorization, and domain-specific user roles are outside the scope of the first version.

---

## 4. Generic Planning Model

Genetic Planner must represent planning problems independently of their specific application domain.

A planning problem will be conceptually represented as:

```text
Planning Problem
      │
      ├── Resources
      ├── Activities
      ├── Time Slots
      ├── Locations
      └── Constraints
              │
              ▼
       Genetic Algorithm
              │
              ▼
           Schedule
```

The exact structure and relationships between these concepts will be defined as part of the domain model design.

The genetic optimization engine must not contain domain-specific concepts such as `Teacher`, `Student`, `Employee`, `Subject`, or `WorkShift`.

Domain-specific concepts will instead be translated into the generic planning model through planning templates.

---

## 5. MVP Functional Scope

Features are classified according to three priority levels:

* **Must Have:** required for the MVP to be considered complete. Failure to implement one of these capabilities means that the MVP acceptance criteria are not fully satisfied.
* **Should Have:** provides significant value to the application but is not required to demonstrate the main objectives of the project.
* **Could Have:** optional functionality that will only be considered after all Must Have functionality is complete and stable.

The priority assigned in this section is also reflected in the MVP acceptance criteria.

### 5.1 Must Have

#### Planning Management

The application must allow the Planner to:

* Create a new planning problem.
* Assign a name to the planning problem.
* Select an available planning template.
* Configure the planning problem before generating a schedule.

Persistence of planning problems between application sessions is **not required for the MVP**.

#### Planning Configuration

The Planner must be able to configure:

* Resources.
* Activities.
* Time slots.
* Locations, when applicable.
* Constraints.

The configured information must internally produce a generic `PlanningProblem` that can be processed by the optimization engine.

#### Academic Scheduling Template

The MVP must provide an **Academic Scheduling** template as its primary validation scenario.

The template must map domain-specific academic concepts to the generic planning model without introducing academic concepts into the Genetic Engine.

#### Generic Planning Architecture

Although Academic Scheduling is the mandatory implemented scenario, the architecture must demonstrate that additional domains can be supported without modifying the Genetic Engine.

The **Work Shift Scheduling** scenario will be designed as a second mapping during the MVP and is classified as a Should Have implementation.

#### Constraints

The MVP must distinguish between:

* **Hard constraints:** conditions that determine whether a schedule is valid.
* **Soft constraints:** desirable conditions whose violation reduces solution quality.

The mandatory constraint catalogue consists of:

* `NoOverlap`
* `Availability`
* `RequiredResource`
* `MaximumAssignments`

The constraint architecture must allow additional constraint types to be introduced without modifying the Genetic Engine.

#### Genetic Optimization

The system must generate schedules using a genetic algorithm.

The optimization process must support:

* Population generation.
* Fitness evaluation.
* Selection.
* Crossover.
* Mutation.
* Elitism.
* Termination criteria.

The Genetic Engine must operate on the generic planning model and remain independent of specific planning templates.

#### Genetic Algorithm Configuration

The MVP must provide predefined optimization configurations:

* **Fast**
* **Balanced**
* **Exhaustive**

`Balanced` will be the default configuration.

The underlying genetic configuration must support parameters such as population size, generation limit, mutation probability, crossover probability, and elitism.

Direct modification of these parameters through the graphical interface is classified as a Should Have feature.

#### Schedule Generation

The Planner must be able to start the optimization process after configuring the planning problem.

The resulting `PlanningResult` must contain:

* Generated schedule.
* Fitness value.
* Hard constraint violations.
* Soft constraint violations.
* Number of generations performed.
* Execution time.

#### Graphical User Interface

The MVP must provide a web-based graphical interface following a guided workflow:

```text
Dashboard
    ↓
New Planning
    ↓
Select Template
    ↓
Configure Resources
    ↓
Configure Activities
    ↓
Configure Time Slots
    ↓
Configure Constraints
    ↓
Review
    ↓
Generate Schedule
    ↓
View Results
```

The interface must:

* Provide clear navigation.
* Guide the Planner through configuration.
* Validate required information.
* Provide understandable error messages.
* Provide feedback while optimization is running.
* Present results in a clear graphical format.
* Avoid requiring genetic algorithm knowledge for normal use.

#### Results Visualization

The generated schedule must be displayed graphically.

The result view must also display:

* Fitness.
* Hard constraint violations.
* Soft constraint violations.
* Number of generations.
* Execution time.

The interface must clearly indicate whether the solution contains hard constraint violations.

#### Quality and Evaluation

The MVP must include:

* Automated tests for the core planning and genetic functionality.
* At least one dataset suitable for validating the Academic Scheduling scenario.
* Experimental evaluation of the genetic algorithm using different configurations.
* Measurement of solution quality and execution time.

---

### 5.2 Should Have

These capabilities will be implemented only after the mandatory end-to-end Academic Scheduling workflow is functional.

#### Work Shift Scheduling Template

A second **Work Shift Scheduling** template should be implemented to provide practical evidence that the same Genetic Engine can solve a different planning domain.

It should:

* Map employees to generic resources.
* Map shifts or work assignments to activities.
* Use generic time slots and locations where applicable.
* Configure applicable generic constraints.
* Use exactly the same Genetic Engine as Academic Scheduling.

If development time prevents completion of this template, its mapping and architecture must still be documented to demonstrate how the generic model supports the scenario.

#### Additional Constraints

The following additional constraints should be considered:

* `MinimumAssignments`
* `MaxConsecutive`
* `PreferredTimeSlot`

`PreferredTimeSlot` is particularly useful for demonstrating weighted soft constraints.

#### Advanced Genetic Configuration

An advanced configuration section should allow modification of:

* Population size.
* Generation limit.
* Mutation probability.
* Crossover probability.
* Elitism.

The underlying parameters already form part of the Must Have Genetic Engine; this priority applies only to exposing them through the UI.

#### Persistence

The application should support:

* Saving planning problems.
* Loading existing planning problems.
* Saving generated schedules.
* Viewing previous planning results.

The core MVP may operate on the currently configured planning problem without persistent storage.

#### User Experience Improvements

The following improvements should be implemented when possible:

* Basic responsive design.
* Improved empty states.
* Improved loading states.
* Detailed validation feedback.
* Enhanced visualization of constraint violations.

---

### 5.3 Could Have

The following features are optional and must not compromise Must or Should functionality:

* Fully configurable custom planning templates.
* CSV import/export.
* Excel import/export.
* PDF export.
* iCalendar export.
* Advanced optimization charts.
* Visual comparison between algorithm executions.
* Planning duplication.
* Dark mode.
* Additional animations.
* Advanced execution history.
* Additional predefined planning templates.

---

## 6. Out of Scope

The following features are explicitly outside the scope of the MVP:

* User authentication.
* User registration.
* Role-based access control.
* Multi-organization support.
* Single Sign-On.
* Native mobile applications.
* Email notifications.
* Push notifications.
* Google Calendar integration.
* Microsoft Calendar integration.
* Real-time collaborative editing.
* Mandatory cloud deployment.
* Microservices architecture.
* Kubernetes deployment.
* Generative AI functionality.
* Machine Learning techniques beyond the genetic optimization approach.
* Complete domain-specific regulatory or legal rule sets.

In particular, the Work Shift Scheduling scenario is intended to demonstrate the flexibility of the generic planning engine. It is **not intended to provide a legally certified workforce management system or to implement complete employment regulations**.

---

## 7. Validation Scenarios

### 7.1 Academic Scheduling — Must Have

Academic Scheduling is the **mandatory end-to-end validation scenario** for the MVP.

It must demonstrate:

* Configuration through the graphical interface.
* Translation into the generic `PlanningProblem`.
* Constraint evaluation.
* Genetic optimization.
* Schedule generation.
* Graphical result visualization.
* Optimization metrics.

The expected result is an optimized academic timetable.

### 7.2 Work Shift Scheduling — Should Have

Work Shift Scheduling is the **secondary validation scenario**.

Its purpose is to demonstrate that the architecture can represent a substantially different planning problem without modifying the Genetic Engine.

The implementation should demonstrate:

* Employees represented as resources.
* Work assignments or shifts represented as activities.
* Work periods represented as time slots.
* Generic constraints applied to the work planning problem.
* Schedule generation using the same Genetic Engine.

If the complete graphical workflow cannot be implemented within the available development time, the scenario may be validated through an automated dataset and documented mapping.


## 8. Flexibility Principle

A fundamental requirement of Genetic Planner is the separation between the generic optimization engine and domain-specific planning scenarios.

The intended architecture follows this principle:

```text
Academic Template ───────┐
                         │
                         ▼
                  PlanningProblem
                         │
                         ▼
                  Genetic Engine
                         │
                         ▼
                      Schedule
                         ▲
                         │
Work Shift Template ─────┘
```

Adding a new planning scenario should primarily involve defining how its domain concepts are mapped to the generic planning model and which constraints are applicable.

The core Genetic Engine should not require modification when introducing a new planning template.

---

---

## 9. MVP Acceptance Criteria

The Genetic Planner MVP will be considered successfully completed when all **Must Have** criteria below are satisfied:

* [ ] Planning problems can be represented independently of a specific application domain.
* [ ] Resources, activities, time slots, locations, and constraints can be configured.
* [ ] Hard and soft constraints are supported.
* [ ] The mandatory generic constraints are implemented.
* [ ] A genetic algorithm generates and optimizes schedules.
* [ ] The Genetic Engine is independent of planning templates.
* [ ] The Academic Scheduling scenario can be configured and executed end-to-end.
* [ ] Academic Scheduling uses the generic `PlanningProblem` and Genetic Engine.
* [ ] Planning problems can be configured through a graphical web interface.
* [ ] Normal use of the application does not require knowledge of genetic algorithms.
* [ ] Generated schedules are displayed graphically.
* [ ] Fitness, constraint violations, generations, and execution time are displayed.
* [ ] The application clearly identifies hard constraint violations.
* [ ] The application can be executed locally following the project documentation.
* [ ] Automated tests cover the core planning, constraint, and optimization functionality.
* [ ] The genetic algorithm is experimentally evaluated using different configurations.
* [ ] The main architecture and design decisions are documented.

The following are **Should Have success criteria** and therefore enhance, but do not determine, MVP completion:

* [ ] The Work Shift Scheduling scenario is implemented and executed using the same Genetic Engine.
* [ ] Additional generic constraints are available.
* [ ] Genetic algorithm parameters can be configured through the UI.
* [ ] Planning problems and generated schedules can be persisted.
* [ ] Additional UX improvements are implemented.

### MVP Completion Rule

The project reaches **MVP Complete** when all Must Have acceptance criteria are satisfied.

Should Have functionality may only be prioritized once the mandatory Academic Scheduling workflow works end-to-end:

```text
Configure Problem
      ↓
PlanningProblem
      ↓
Genetic Engine
      ↓
PlanningResult
      ↓
Graphical Schedule
```

Could Have functionality must only be considered after the MVP is complete and the remaining Should Have work has been evaluated against the available development time.


## 10. Future Extensions

Although not part of the MVP, the architecture should facilitate future improvements such as:

* Additional planning templates.
* Additional generic constraints.
* More advanced multi-objective optimization strategies.
* Alternative evolutionary operators.
* Import and export formats.
* Calendar integrations.
* Parallel fitness evaluation.
* Comparison between different optimization algorithms.
* Cloud-based execution for larger planning problems.

These extensions must not influence the scope or delivery of the initial MVP unless all mandatory functionality has already been completed.
