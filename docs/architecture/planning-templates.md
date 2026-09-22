# Genetic Planner — Planning Template Architecture

## 1. Purpose

This document defines the planning template architecture of **Genetic Planner**.

Planning templates provide a domain-oriented configuration experience
while preserving a completely generic internal planning model.

For example, users configuring an academic timetable should work with
concepts such as:

```text
Teacher
Student Group
Class
Classroom
Teaching Period
```

while users configuring work shifts may work with:

```text
Employee
Work Assignment
Workplace
Shift Period
```

Both scenarios are translated into the same generic:

```text
PlanningProblem
```

which is processed by the same Genetic Engine.

The central architectural principle is:

> **Templates provide domain-specific configuration and terminology,
> while the Genetic Engine operates only on generic planning concepts.**

---

## 2. Current Status

The planning template architecture was designed during the foundation
phase of Genetic Planner.

At the completion of M2, the generic domain model and Genetic Engine
required by templates are implemented.

The complete application/template layer is part of:

```text
M3 — Functional MVP
```

Therefore, this document distinguishes between:

* **Implemented core contracts** already available in the domain and
  Genetic Engine.
* **Planned template/application components** that define how M3 will
  expose those contracts to users.

The mandatory MVP scenario is:

```text
Academic Scheduling
```

The secondary genericity validation scenario is:

```text
Work Shift Scheduling
```

---

## 3. Template Boundary

Planning templates act as adapters between the user's mental model and
the generic planning model.

For Academic Scheduling:

```text
Teacher
    ↓
Resource

Student Group
    ↓
Resource

Class / Lesson
    ↓
Activity

Classroom
    ↓
Location

Teaching Period
    ↓
TimeSlot

Scheduling Rule
    ↓
Constraint
```

For Work Shift Scheduling:

```text
Employee
    ↓
Resource

Work Assignment
    ↓
Activity

Workplace
    ↓
Location

Shift Period
    ↓
TimeSlot

Workforce Rule
    ↓
Constraint
```

Once this transformation has occurred, the domain-specific terminology
is irrelevant to optimization.

---

## 4. Architecture

The intended application flow is:

```text
                 Genetic Planner
                       │
          ┌────────────┴────────────┐
          │                         │
          ▼                         ▼
 Academic Template          Work Shift Template
          │                         │
          └────────────┬────────────┘
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
              OptimizationResult
                       │
                       ▼
             Schedule Visualization
```

The important architectural boundary is:

```text
Template / Application Layer
              │
              ▼
       PlanningProblem
────────────────────────────────
       Genetic Engine
```

The Genetic Engine receives exactly the same domain model regardless of
which template created it.

---

## 5. PlanningTemplate Concept

A planning template conceptually defines how a particular planning
scenario is configured and transformed into a generic `PlanningProblem`.

A possible application-layer abstraction is:

```kotlin
interface PlanningTemplate<TConfig> {

    val id: String

    val name: String

    val description: String

    fun defaultConfig(): TConfig

    fun defaultConstraints(): List<Constraint>

    fun buildPlanningProblem(
        config: TConfig
    ): PlanningProblem
}
```

> **Implementation status:** this interface describes the intended M3
> template/application architecture. It is not part of the M2 Genetic
> Engine implementation.

A template is responsible for:

* Providing domain-specific terminology.
* Providing sensible default configuration.
* Providing appropriate default constraints.
* Collecting template-specific user input.
* Transforming that input into generic domain objects.
* Producing a `PlanningProblem` suitable for validation.

A template is not responsible for:

* Candidate generation.
* Genotype encoding.
* Fitness evaluation.
* Running Jenetics.
* Selection.
* Crossover.
* Mutation.
* Elitism.
* Genetic termination.

Those responsibilities belong to the generic optimization architecture.

---

## 6. Template Responsibilities

### 6.1 Terminology

Templates provide terminology appropriate to the selected planning
scenario.

Academic:

| Generic concept | UI concept |
| --- | --- |
| Resource | Teacher / Student Group |
| Activity | Class |
| Location | Classroom |
| TimeSlot | Teaching Period |

Work Shift:

| Generic concept | UI concept |
| --- | --- |
| Resource | Employee |
| Activity | Work Assignment |
| Location | Workplace |
| TimeSlot | Shift Period |

Terminology affects the application and UI only.

It does not change the generic domain model.

---

### 6.2 Default Configuration

Templates may provide sensible starting values.

Academic example:

```text
Resource types:
- Teacher
- Student Group

Activity label:
- Class

Location label:
- Classroom

TimeSlot label:
- Teaching Period
```

Work Shift example:

```text
Resource type:
- Employee

Activity label:
- Work Assignment

Location label:
- Workplace

TimeSlot label:
- Shift Period
```

Defaults are starting configurations rather than fixed planning rules.

---

### 6.3 Default Constraints

Templates select appropriate constraints from the generic implemented
constraint catalogue.

They do not implement their own constraint engines.

The currently implemented generic catalogue is:

```text
HARD
├── NoOverlapConstraint
├── AvailabilityConstraint
├── RequiredResourceConstraint
└── LocationCapacityConstraint

SOFT
├── PreferredTimeSlotConstraint
├── MaxConsecutiveConstraint
└── BalancedWorkloadConstraint
```

Templates may select and configure an appropriate subset of these
constraints.

---

### 6.4 Input Transformation

Template-specific configuration is transformed into:

```text
PlanningProblem
├── ResourceType
├── Resource
├── Activity
│     └── ResourceRequirement
├── TimeSlot
├── Location
└── Constraint
```

The resulting object contains no template-specific domain classes.

---

### 6.5 UI Guidance

Templates may expose metadata that allows the UI to display:

```text
Teacher
```

instead of:

```text
Resource
```

or:

```text
Classroom
```

instead of:

```text
Location
```

This improves usability without introducing template-specific behaviour
into the Genetic Engine.

---

## 7. Template Metadata

The application layer may expose template metadata such as:

```kotlin
data class PlanningTemplateMetadata(
    val id: String,
    val name: String,
    val description: String,
    val resourceLabel: String,
    val activityLabel: String,
    val locationLabel: String,
    val timeSlotLabel: String
)
```

> **Implementation status:** the exact metadata model belongs to M3 and
> may be refined during frontend/application implementation.

For example:

```text
Academic

resourceLabel = "Teacher / Group"
activityLabel = "Class"
locationLabel = "Classroom"
timeSlotLabel = "Teaching Period"
```

and:

```text
Work Shift

resourceLabel = "Employee"
activityLabel = "Work Assignment"
locationLabel = "Workplace"
timeSlotLabel = "Shift"
```

These labels belong to the application/UI layer.

---

# 8. Academic Scheduling Template

## 8.1 Role in the MVP

Academic Scheduling is the **primary and mandatory end-to-end template**
for the MVP.

Its purpose is to allow users to configure an academic timetable using
familiar concepts while internally producing the generic
`PlanningProblem` already supported by the Genetic Engine.

The expected flow is:

```text
Academic Configuration
        ↓
Academic Template
        ↓
PlanningProblem
        ↓
ProblemValidator
        ↓
Genetic Engine
        ↓
OptimizationResult
        ↓
Academic Timetable
```

---

## 8.2 Academic Mapping

| Academic concept | Generic model |
| --- | --- |
| Teacher | Resource |
| Student Group | Resource |
| Class / Lesson | Activity |
| Classroom | Location |
| Teaching Period | TimeSlot |
| Teacher requirement | ResourceRequirement |
| Student group requirement | ResourceRequirement |
| Scheduling rule | Constraint |

For example:

```text
Mathematics — Group 1A — Session 1
```

becomes one:

```text
Activity
```

with ResourceRequirements such as:

```text
Teacher
quantity = 1
```

and:

```text
Student Group
quantity = 1
```

---

## 8.3 Academic Configuration

A possible M3 configuration model is:

```kotlin
data class AcademicTemplateConfig(
    val name: String,
    val planningHorizon: PlanningHorizon,
    val teachers: List<AcademicResourceConfig>,
    val studentGroups: List<AcademicResourceConfig>,
    val classes: List<AcademicActivityConfig>,
    val teachingPeriods: List<TimeSlot>,
    val classrooms: List<Location>,
    val constraints: List<Constraint>
)
```

This is an application/template representation, not part of the generic
domain.

The exact DTO/configuration structure may be refined during M3.

---

## 8.4 Academic Resource Types

The Academic Template can define:

```text
teacher
student-group
```

as generic `ResourceType` values.

For example:

```text
ResourceType
id = teacher
name = Teacher
```

and:

```text
Resource
id = teacher-ana
name = Ana
typeId = teacher
```

Similarly:

```text
ResourceType
id = student-group
name = Student Group
```

and:

```text
Resource
id = group-1a
name = Group 1A
typeId = student-group
```

The Genetic Engine only sees generic Resources.

---

## 8.5 Academic Activities

Each schedulable lesson becomes one atomic Activity.

For example:

```text
Mathematics 1A — Session 1
```

may contain:

```text
ResourceRequirement

id = teacher-requirement
resourceTypeId = teacher
quantity = 1
candidateResourceIds = [teacher-ana, teacher-carlos]
```

and:

```text
ResourceRequirement

id = group-requirement
resourceTypeId = student-group
quantity = 1
candidateResourceIds = [group-1a]
```

Repeated lessons are expanded into separate Activities:

```text
math-1a-session-1
math-1a-session-2
math-1a-session-3
```

This preserves:

> **One Activity = one genetic scheduling decision.**

---

## 8.6 Academic TimeSlots

Teaching periods become generic TimeSlots.

For example:

```text
Monday 09:00–10:00
Monday 10:00–11:00
Monday 11:00–12:00
```

Each period contains concrete:

```text
start
end
```

values represented through `LocalDateTime`.

The Genetic Engine does not know that these TimeSlots represent teaching
periods.

---

## 8.7 Academic Locations

Classrooms become generic Locations.

For example:

```text
Room 101
Computer Lab
Physics Lab
```

Intrinsic Location information may include:

```text
type
capacity
attributes
```

For example:

```text
Room 101
capacity = 30
```

An Activity may independently define:

```text
requiredLocationCapacity = 25
```

and `LocationCapacityConstraint` evaluates whether the selected
classroom provides sufficient capacity.

---

## 8.8 Academic Default Constraints

A reasonable Academic Template configuration uses:

```text
HARD
├── NoOverlapConstraint
├── AvailabilityConstraint
├── RequiredResourceConstraint
└── LocationCapacityConstraint   (when capacity is relevant)

SOFT
├── PreferredTimeSlotConstraint
├── MaxConsecutiveConstraint
└── BalancedWorkloadConstraint   (when workload balancing is relevant)
```

`NoOverlapConstraint` prevents teachers and student groups from
participating in overlapping classes.

`AvailabilityConstraint` prevents Resources from being assigned outside
their configured availability.

`RequiredResourceConstraint` verifies that each class receives the
Resources described by its ResourceRequirements.

`LocationCapacityConstraint` verifies that a selected classroom provides
the capacity required by the Activity.

`PreferredTimeSlotConstraint` allows Activities to prefer particular
teaching periods.

`MaxConsecutiveConstraint` discourages excessive consecutive assignments
for Resources.

`BalancedWorkloadConstraint` can encourage activities to be distributed
more evenly between configured Resources.

The exact defaults and weights will be finalized when the Academic
Template is implemented in M3.

---

## 8.9 Academic Optimization Configuration

The default optimization preset is intended to be:

```text
BALANCED
```

The underlying Genetic Engine already supports:

```text
FAST
BALANCED
EXHAUSTIVE
```

as well as explicit `GeneticAlgorithmConfig`.

Template selection does not change Genetic Engine behaviour.

---

## 8.10 Academic Transformation Example

User-facing configuration:

```text
Teachers:
- Ana

Student Groups:
- 1A

Class:
- Mathematics

Teaching periods:
- Monday 09:00–10:00
- Monday 10:00–11:00

Classrooms:
- Room 101
- Room 102
```

is transformed conceptually into:

```text
PlanningProblem

ResourceTypes
├── Teacher
└── Student Group

Resources
├── Ana
└── Group 1A

Activities
└── Mathematics 1A — Session 1
      ├── Teacher × 1
      └── Student Group × 1

TimeSlots
├── Monday 09:00–10:00
└── Monday 10:00–11:00

Locations
├── Room 101
└── Room 102

Constraints
├── NoOverlap
├── Availability
├── RequiredResource
├── LocationCapacity
├── PreferredTimeSlot
└── MaxConsecutive
```

The Genetic Engine receives only the resulting generic
`PlanningProblem`.

---

# 9. Work Shift Scheduling Template

## 9.1 Role in the MVP

Work Shift Scheduling is the **secondary validation scenario**.

Its purpose is to demonstrate that a substantially different planning
domain can use exactly the same generic model and Genetic Engine.

Its implementation is a Should Have capability.

If the complete UI workflow cannot be completed within the available
development time, its mapping may still be validated through an
automated dataset and documented scenario.

---

## 9.2 Work Shift Mapping

| Work Shift concept | Generic model |
| --- | --- |
| Employee | Resource |
| Work Assignment | Activity |
| Workplace | Location |
| Shift Period | TimeSlot |
| Employee requirement | ResourceRequirement |
| Workforce rule | Constraint |

For example:

```text
Reception — Monday Morning
```

becomes:

```text
Activity
    │
    ├── ResourceRequirement
    │       └── Employee × 1
    │
    ├── TimeSlot
    │       └── Monday 08:00–16:00
    │
    └── Location
            └── Main Office
```

---

## 9.3 Work Shift Configuration

A possible M3 configuration model is:

```kotlin
data class WorkShiftTemplateConfig(
    val name: String,
    val planningHorizon: PlanningHorizon,
    val employees: List<WorkResourceConfig>,
    val workAssignments: List<WorkActivityConfig>,
    val shifts: List<TimeSlot>,
    val workplaces: List<Location>,
    val constraints: List<Constraint>
)
```

As with the Academic Template, this is an application/template model and
not part of the generic domain.

The exact implementation may be refined during M3.

---

## 9.4 Work Shift Resource Types

The Work Shift Template can define:

```text
employee
```

as a generic ResourceType.

For example:

```text
ResourceType
id = employee
name = Employee
```

and:

```text
Resource
id = employee-ana
name = Ana
typeId = employee
```

Additional ResourceTypes may be introduced without modifying the Genetic
Engine.

---

## 9.5 Work Shift Activities

Each required work assignment becomes an atomic Activity.

For example:

```text
Reception — Monday Morning
```

may contain:

```text
ResourceRequirement

id = employee-requirement
resourceTypeId = employee
quantity = 1
```

An Activity requiring two employees may instead use:

```text
quantity = 2
```

Candidate employees may be restricted through:

```text
candidateResourceIds
```

For example:

```text
candidateResourceIds =
[
    employee-ana,
    employee-laura,
    employee-pedro
]
```

---

## 9.6 Work Shift TimeSlots

Work periods become generic TimeSlots.

For example:

```text
Monday 08:00–16:00
Monday 16:00–00:00
Tuesday 08:00–16:00
```

The Genetic Engine handles them in exactly the same way as academic
teaching periods.

---

## 9.7 Work Shift Locations

Workplaces become generic Locations.

Examples include:

```text
Main Office
Warehouse
Reception
Branch A
```

Locations remain optional.

A planning problem that does not require a physical or logical location
may omit them.

---

## 9.8 Work Shift Default Constraints

A reasonable Work Shift Template configuration uses constraints from the
same generic catalogue:

```text
HARD
├── NoOverlapConstraint
├── AvailabilityConstraint
└── RequiredResourceConstraint

SOFT
├── PreferredTimeSlotConstraint
├── MaxConsecutiveConstraint
└── BalancedWorkloadConstraint
```

`LocationCapacityConstraint` may also be used when the scenario includes
capacity requirements.

`BalancedWorkloadConstraint` provides the current generic mechanism for
encouraging a more even distribution of assignments between employees.

The previously considered:

```text
MaximumAssignmentsConstraint
MinimumAssignmentsConstraint
```

are not part of the implemented M2 constraint catalogue.

If future Work Shift requirements require explicit upper or lower
assignment limits, additional generic constraints can be introduced
without modifying the Genetic Engine.

---

## 9.9 Work Shift Optimization Configuration

The intended default optimization preset is:

```text
BALANCED
```

The Work Shift Template uses the same:

```text
GeneticAlgorithmConfig
```

and the same:

```text
GeneticEngine
```

as Academic Scheduling.

---

## 9.10 Work Shift Transformation Example

Input:

```text
Employees:
- Ana
- Carlos

Work Assignment:
- Reception Morning

Shift:
- Monday 08:00–16:00

Workplace:
- Main Office
```

becomes:

```text
PlanningProblem

ResourceTypes
└── Employee

Resources
├── Ana
└── Carlos

Activities
└── Reception Morning
      └── Employee × 1

TimeSlots
└── Monday 08:00–16:00

Locations
└── Main Office

Constraints
├── NoOverlap
├── Availability
├── RequiredResource
├── PreferredTimeSlot
├── MaxConsecutive
└── BalancedWorkload
```

Again, the Genetic Engine does not know that the PlanningProblem
originated from a Work Shift Template.

---

# 10. Common Template Structure

Both templates follow the same architectural pattern:

```text
Template-specific configuration
            │
            ▼
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
```

Academic:

```text
AcademicTemplateConfig
        ↓
AcademicTemplate
        ↓
PlanningProblem
```

Work Shift:

```text
WorkShiftTemplateConfig
        ↓
WorkShiftTemplate
        ↓
PlanningProblem
```

The output contract is always the same.

---

# 11. Template Selection

The intended user flow is:

```text
New Planning
     ↓
Choose Template
     ↓
┌────────────────┐
│ Academic       │
│ Work Shift     │
└────────────────┘
     ↓
Template-specific configuration
     ↓
PlanningProblem
     ↓
ProblemValidator
     ↓
Genetic Engine
     ↓
OptimizationResult
```

Academic Scheduling is the mandatory MVP path.

Work Shift Scheduling is the secondary genericity validation path.

---

# 12. Template Registry

The application layer may provide a registry or equivalent mechanism for
resolving templates.

For example:

```kotlin
enum class PlanningTemplateType {
    ACADEMIC,
    WORK_SHIFT
}
```

and conceptually:

```kotlin
interface PlanningTemplateRegistry {

    fun get(
        type: PlanningTemplateType
    ): PlanningTemplate<*>
}
```

> **Implementation status:** the exact registry design is intentionally
> left to M3 and may change if a simpler application-layer approach is
> more appropriate.

The important architectural requirement is not the registry itself.

It is that template selection occurs **before** creation of the generic
`PlanningProblem`.

---

# 13. Default Configuration Strategy

Templates should simplify configuration without creating fixed planning
models.

A template may provide:

```text
Default ResourceTypes
Default terminology
Default constraints
Default constraint weights
Default optimization preset
```

For example:

```text
Academic Template
        ↓
NoOverlapConstraint enabled by default
        ↓
User/application may configure its weight
```

The resulting PlanningProblem remains generic and configurable.

Therefore templates are:

```text
starting configurations
```

rather than:

```text
separate scheduling engines
```

---

# 14. Constraint Defaults

Only constraints from the implemented generic catalogue should be
selected by current templates.

A reasonable initial template mapping is:

| Constraint | Academic | Work Shift | Type |
| --- | --- | --- | --- |
| `NoOverlapConstraint` | Default | Default | HARD |
| `AvailabilityConstraint` | Default | Default | HARD |
| `RequiredResourceConstraint` | Default | Default | HARD |
| `LocationCapacityConstraint` | When applicable | When applicable | HARD |
| `PreferredTimeSlotConstraint` | Default | Default | SOFT |
| `MaxConsecutiveConstraint` | Default | Default | SOFT |
| `BalancedWorkloadConstraint` | When applicable | Default | SOFT |

The exact template defaults may be refined during M3 according to the
final UI workflow and validation datasets.

Templates never implement separate academic or workforce versions of
these constraints.

---

# 15. Constraint Weights

Constraint weights remain configurable.

The evaluation model applies:

```text
weightedPenalty =
rawPenalty × weight
```

to both HARD and SOFT constraints.

There is no hidden global HARD multiplier.

Templates may provide default weights, but those values are
configuration decisions rather than part of the generic constraint
semantics.

Therefore this architecture does not prescribe values such as:

```text
all HARD constraints = 1000
```

Different template defaults may be evaluated experimentally without
modifying the Genetic Engine.

---

# 16. Genetic Algorithm Configuration

Templates may select a default Genetic Algorithm preset.

The implemented presets are:

```text
FAST
BALANCED
EXHAUSTIVE
```

The intended default for the initial templates is:

```text
BALANCED
```

The template does not implement or modify the Genetic Algorithm.

It merely selects configuration passed to the same Genetic Engine.

Conceptually:

```text
Template
    │
    └── default preset = BALANCED
                │
                ▼
       GeneticAlgorithmConfig
                │
                ▼
          Genetic Engine
```

---

# 17. Genetic Engine Independence

The Genetic Engine may depend on generic concepts such as:

```text
PlanningProblem
Activity
Resource
ResourceRequirement
TimeSlot
Location
Constraint
Schedule
Assignment
```

It must not depend on:

```text
AcademicTemplate
AcademicTemplateConfig
Teacher
Subject
Classroom
StudentGroup

WorkShiftTemplate
WorkShiftTemplateConfig
Employee
WorkShift
Workplace
```

For optimization:

```text
templateType = ignored
```

The engine must never contain logic such as:

```kotlin
if (problem.templateType == "ACADEMIC") {
    // ...
}
```

or:

```kotlin
when (problem.templateType) {
    "ACADEMIC" -> ...
    "WORK_SHIFT" -> ...
}
```

All scheduling semantics must already be represented through generic:

```text
Resources
Activities
ResourceRequirements
TimeSlots
Locations
Constraints
```

---

# 18. templateType

`PlanningProblem` contains:

```kotlin
val templateType: String?
```

This value is metadata.

It may be useful for:

* UI display.
* Persistence.
* Reopening a configuration.
* Selecting template-specific terminology.

It must not influence:

* Candidate generation.
* Genotype encoding.
* Fitness evaluation.
* Selection.
* Crossover.
* Mutation.
* Genetic evolution.

---

# 19. Adding Future Templates

A new template should require no Genetic Engine modification.

For example:

```text
Exam Scheduling Template
```

could map:

```text
Examiner
    ↓
Resource

Exam
    ↓
Activity

Exam Room
    ↓
Location

Exam Period
    ↓
TimeSlot
```

and produce:

```text
PlanningProblem
```

The existing Genetic Engine can then optimize the problem without
knowing that it represents examinations.

Conceptually:

```text
New Template
     ↓
Domain-specific configuration
     ↓
PlanningProblem
     ↓
Existing Genetic Engine
```

---

# 20. Custom Template

A fully configurable generic template is a future capability.

Conceptually:

```text
Custom Template
      ↓
User defines:
- ResourceTypes
- Resources
- Activities
- TimeSlots
- Locations
- Constraints
      ↓
PlanningProblem
```

This is not required for the MVP.

The current architecture should nevertheless avoid preventing such an
extension.

---

# 21. Layer Responsibilities

## Template / Application Layer

Responsible for:

```text
Terminology
Template configuration
Default values
Default constraints
Default weights
UI metadata
Transformation into PlanningProblem
```

## Domain Layer

Responsible for:

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
```

## Validation

Responsible for:

```text
Structural PlanningProblem validation
Reference validation
Configuration consistency
```

## Genetic Engine

Responsible for:

```text
Assignment candidate generation
Genotype encoding / decoding
Population evolution
Selection
Crossover
Mutation
Elitism
Fitness integration
Termination
OptimizationResult
```

This separation keeps domain-specific configuration independent from
optimization.

---

# 22. Implementation Decisions

### PT1 — Templates are adapters

Templates translate domain-oriented configuration into the generic
planning model.

### PT2 — PlanningProblem is the common contract

Every template ultimately produces the same:

```text
PlanningProblem
```

### PT3 — Academic Scheduling is the primary MVP template

Academic Scheduling must be supported end-to-end.

### PT4 — Work Shift Scheduling validates genericity

Work Shift Scheduling provides a second substantially different planning
scenario using the same model and Genetic Engine.

### PT5 — Templates provide defaults

Templates may provide:

```text
ResourceTypes
Terminology
Constraints
Weights
Optimization preset
```

### PT6 — Defaults are configurable

Defaults are starting values rather than fixed Genetic Engine behaviour.

### PT7 — Template-specific configuration remains outside the domain

Classes such as:

```text
AcademicTemplateConfig
WorkShiftTemplateConfig
```

belong to the application/template layer.

### PT8 — Templates use the generic constraint catalogue

No separate Academic or Work Shift constraint engine exists.

### PT9 — The Genetic Engine is template-independent

No domain-specific branching is permitted in the Genetic Engine.

### PT10 — templateType is metadata only

`PlanningProblem.templateType` must never influence optimization.

### PT11 — Atomic Activities are created by templates

Templates are responsible for expanding recurring user concepts into
individual atomic Activities when required.

This preserves:

```text
one Activity = one genetic decision
```

### PT12 — Templates do not determine feasibility

Templates configure constraints.

Constraint evaluation determines Schedule feasibility.

### PT13 — Template architecture remains an application concern

The generic domain and Genetic Engine do not depend on the existence of
a particular `PlanningTemplate` interface or registry implementation.

---

# 23. Changes from Initial Design

The template architecture was refined during implementation of the
generic domain and Genetic Engine.

## 23.1 Constraint Catalogue

The initial template design referenced:

```text
MaximumAssignmentsConstraint
MinimumAssignmentsConstraint
CapacityConstraint
DifferentDayConstraint
```

The implemented M2 catalogue is:

```text
HARD
├── NoOverlapConstraint
├── AvailabilityConstraint
├── RequiredResourceConstraint
└── LocationCapacityConstraint

SOFT
├── PreferredTimeSlotConstraint
├── MaxConsecutiveConstraint
└── BalancedWorkloadConstraint
```

Template defaults have therefore been updated to use the implemented
catalogue.

---

## 23.2 Capacity

Capacity was initially described mainly as optional Location metadata.

The implemented model explicitly represents:

```text
Location.capacity
```

and:

```text
Activity.requiredLocationCapacity
```

with:

```text
LocationCapacityConstraint
```

performing the corresponding evaluation.

---

## 23.3 Constraint Weights

The initial design suggested fixed example values such as:

```text
HARD = 1000
```

The implemented architecture has no global HARD multiplier.

Every Constraint owns its configurable weight and uses:

```text
weightedPenalty =
rawPenalty × weight
```

The actual default values belong to template/application configuration.

---

## 23.4 Workload Balancing

The initial Work Shift design used:

```text
MaximumAssignmentsConstraint
MinimumAssignmentsConstraint
```

The implemented M2 catalogue instead provides:

```text
BalancedWorkloadConstraint
```

as the generic SOFT workload distribution objective.

Explicit minimum or maximum assignment constraints remain possible
future extensions.

---

## 23.5 Optimization Output

The initial architecture represented the Genetic Engine output mainly as:

```text
Schedule
```

The implemented engine returns:

```text
OptimizationResult
```

which contains:

```text
Schedule
ScheduleEvaluation
Generations executed
Execution time
Random seed
```

The Schedule itself remains independent from optimization metadata.

---

## 23.6 Template Implementation Status

The original document presented conceptual interfaces and configuration
classes without always distinguishing them from implemented components.

At the end of M2:

```text
Generic domain model       → implemented
Problem validation         → implemented
Constraint evaluation      → implemented
Genetic Engine             → implemented
OptimizationResult         → implemented

Academic Template          → M3
Work Shift Template        → M3 / secondary scenario
Template UI                → M3
Template registry          → M3 if required
```

This distinction avoids treating planned application-layer structures as
existing production code.

---

# 24. Current Architecture Summary

At the end of M2:

```text
                    TEMPLATE LAYER
                     (M3 work)
                         │
          ┌──────────────┴──────────────┐
          │                             │
          ▼                             ▼
 Academic Template              Work Shift Template
          │                             │
          └──────────────┬──────────────┘
                         │
                         ▼
                  PlanningProblem
                         │
                         ▼
                  ProblemValidator
                         │
                         ▼
               Assignment Candidates
                         │
                         ▼
                 Genotype Codec
                         │
                         ▼
                   Jenetics Engine
                         │
                         ▼
                     Schedule
                         │
                         ▼
               ConstraintEvaluator
                         │
                         ▼
                OptimizationResult
```

The important architectural invariant is:

> **The template determines how a planning problem is expressed; the
> Genetic Engine determines how that generic problem is optimized.**

Academic Scheduling and Work Shift Scheduling therefore differ before
the `PlanningProblem` boundary, not inside the Genetic Engine.