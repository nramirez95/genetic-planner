# Genetic Planner — Generic Domain Model

## 1. Purpose

This document describes the generic domain model implemented by
**Genetic Planner**.

The model represents different planning and scheduling problems using the
same domain concepts, independently of whether the problem represents:

* Academic timetables.
* Work shifts.
* School calendars.
* Resource allocation schedules.
* Other time-based planning scenarios.

The model acts as the contract between:

* Planning templates.
* The application layer.
* Problem validation.
* Assignment candidate generation.
* Constraint evaluation.
* The Genetic Engine.
* Persistence adapters.

The Genetic Engine operates exclusively on this generic model and must
not contain domain-specific concepts such as `Teacher`, `Employee`,
`Subject`, `StudentGroup`, or `WorkShift`.

The central architectural principle is:

> **Genericity belongs to the domain model and Genetic Engine;
> specificity belongs to templates and user experience.**

---

## 2. Design Principles

### 2.1 Domain Independence

The core model does not depend on any particular planning scenario.

For example:

```text
Academic domain        Generic model

Teacher          ────→ Resource
Student group    ────→ Resource
Lesson           ────→ Activity
Classroom        ────→ Location
Teaching period  ────→ TimeSlot
```

and:

```text
Work domain            Generic model

Employee         ────→ Resource
Work assignment  ────→ Activity
Work area        ────→ Location
Shift period     ────→ TimeSlot
```

Both scenarios ultimately produce the same `PlanningProblem`.

---

### 2.2 Explicit Scheduling Concepts

The model explicitly represents the concepts required by optimization:

* What must be scheduled.
* When it can be scheduled.
* Which Resources participate.
* Where an Activity takes place.
* Which Resource requirements must be satisfied.
* Which rules apply to the Schedule.

Scheduling concepts required by the Genetic Engine must be represented
explicitly rather than hidden inside arbitrary metadata.

---

### 2.3 Structural Validation vs Planning Quality

Domain objects represent planning data but do not enforce all structural
rules through constructor-level validation.

Structural validation is performed by `ProblemValidator` before a
planning problem reaches optimization.

Examples of structural problems include:

* Duplicate identifiers.
* References to unknown entities.
* Invalid planning horizons.
* Invalid TimeSlots.
* Invalid Resource requirements.
* Activities with no possible TimeSlot.

Planning quality and Schedule feasibility are evaluated separately
through constraints.

For example:

```text
Two Activities assigned to the same Resource
during overlapping TimeSlots
```

is not a malformed `PlanningProblem`.

It is a:

```text
NoOverlapConstraint
→ HARD violation
```

The separation is:

```text
PlanningProblem
      │
      ▼
ProblemValidator
      │
      ├── structurally invalid
      │
      └── structurally valid
              │
              ▼
       Genetic Engine
              │
              ▼
           Schedule
              │
              ▼
      ConstraintEvaluator
```

---

### 2.4 Framework Independence

The domain model remains independent from:

```text
Spring
JPA
PostgreSQL
REST
React
Jenetics
```

Framework-specific representations belong to their corresponding
application, API, persistence, or genetic adaptation layers.

---

### 2.5 MVP Scheduling Unit

For the MVP:

> **One Activity represents one atomic schedulable unit and is assigned
> to exactly one TimeSlot.**

The duration of an Activity is therefore represented by the duration of
its selected `TimeSlot`.

For example:

```text
Academic:

Activity:
    Mathematics 1A — Session 1

TimeSlot:
    Monday 09:00–10:00
```

or:

```text
Work Shift:

Activity:
    Reception Morning Shift

TimeSlot:
    Monday 08:00–16:00
```

Activities spanning multiple TimeSlots are outside the current MVP.

---

## 3. Domain Overview

The core domain structure is:

```text
PlanningProblem
│
├── PlanningHorizon
├── ResourceTypes
├── Resources
├── Activities
│     └── ResourceRequirements
├── TimeSlots
├── Locations
└── Constraints
        │
        ▼
  Genetic Engine
        │
        ▼
     Schedule
        │
        └── Assignments
```

A `PlanningProblem` describes the problem to solve.

A `Schedule` represents one candidate or final solution.

Optimization metadata is represented separately through
`OptimizationResult`.

---

# 4. PlanningProblem

## 4.1 Responsibility

`PlanningProblem` is the root object of the planning domain.

The implemented model is:

```kotlin
data class PlanningProblem(
    val id: String,
    val name: String,
    val templateType: String? = null,
    val planningHorizon: PlanningHorizon,
    val resourceTypes: List<ResourceType> = emptyList(),
    val resources: List<Resource> = emptyList(),
    val activities: List<Activity> = emptyList(),
    val timeSlots: List<TimeSlot> = emptyList(),
    val locations: List<Location> = emptyList(),
    val constraints: List<Constraint> = emptyList()
)
```

Persistence annotations, API DTOs, and framework-specific code do not
form part of this model.

---

## 4.2 Identifiers

All domain identifiers use:

```kotlin
String
```

IDs are treated as opaque identifiers.

The domain must not interpret prefixes, formatting, or semantic content
inside an ID.

For example:

```text
teacher-ana
resource-1
550e8400-e29b-41d4-a716-446655440000
```

may all be valid identifiers from the perspective of the core model.

Readable identifiers are particularly useful in tests and experimental
datasets, while production layers may use UUID-like values.

Using `String` keeps the domain independent from persistence-specific ID
strategies.

---

## 4.3 Collections

`PlanningProblem` uses `List` collections.

This preserves stable ordering, which is useful for:

* Genetic encoding.
* Candidate generation.
* Serialization.
* Testing.
* Reproducibility.

Identifier uniqueness is checked by `ProblemValidator` rather than being
implicitly enforced through `Set` collections.

---

## 4.4 templateType

`templateType` optionally identifies the template used to create the
problem.

Examples:

```text
ACADEMIC
WORK_SHIFT
```

It exists for application, persistence, and UI purposes.

The Genetic Engine must never branch on:

```kotlin
problem.templateType
```

For example, logic such as:

```kotlin
if (problem.templateType == "ACADEMIC") {
    // academic-specific genetic behaviour
}
```

is prohibited.

---

# 5. PlanningHorizon

`PlanningHorizon` represents the temporal boundaries of a planning
problem.

```kotlin
data class PlanningHorizon(
    val start: LocalDateTime,
    val end: LocalDateTime
)
```

The valid relationship is:

```text
start < end
```

This is checked by `ProblemValidator`.

Using `LocalDateTime` allows the model to represent:

* Weekly schedules.
* Concrete calendar periods.
* Multi-day planning.
* Multi-week planning.

Time zones are outside the current MVP.

---

# 6. ResourceType

A `ResourceType` identifies a generic category of Resources.

```kotlin
data class ResourceType(
    val id: String,
    val name: String
)
```

Examples include:

```text
Academic:
    TEACHER
    STUDENT_GROUP

Work Shift:
    EMPLOYEE

Future:
    VEHICLE
    MACHINE
    EQUIPMENT
```

`ResourceType` is deliberately represented as data rather than an enum.

This allows templates to introduce Resource categories without modifying
the domain model or Genetic Engine.

---

# 7. Resource

A `Resource` represents an entity that can participate in an Activity.

```kotlin
data class Resource(
    val id: String,
    val name: String,
    val typeId: String,
    val attributes: Map<String, String> = emptyMap()
)
```

A Resource is not assumed to represent a person.

Examples include:

```text
Teacher
Employee
Student group
Machine
Vehicle
Equipment
```

`typeId` references a `ResourceType`.

---

## 7.1 Attributes

`attributes` provides optional template-specific metadata.

For example:

```text
department = Mathematics
```

Metadata must not become a substitute for explicit scheduling concepts.

> If information is required by the Genetic Engine, candidate
> generation, or constraint evaluation, it should normally be modeled
> explicitly rather than hidden in `attributes`.

---

## 7.2 Availability

Availability is deliberately not stored directly inside `Resource`.

For example:

```text
Ana is unavailable Monday 09:00
```

is a planning rule represented by:

```text
AvailabilityConstraint
```

This keeps Resource data separate from planning policy.

---

# 8. Activity

An `Activity` represents one atomic schedulable unit.

The implemented model is:

```kotlin
data class Activity(
    val id: String,
    val name: String,
    val type: String? = null,
    val resourceRequirements: List<ResourceRequirement>,
    val allowedTimeSlotIds: Set<String>? = null,
    val allowedLocationIds: Set<String>? = null,
    val requiredLocationCapacity: Int? = null,
    val attributes: Map<String, String> = emptyMap()
)
```

Each Activity produces exactly one Assignment in a complete Schedule.

---

## 8.1 type

`type` provides optional classification metadata.

It is currently represented as:

```kotlin
String?
```

rather than introducing a separate `ActivityType`.

No current Genetic Engine behaviour depends on an Activity type, so a
dedicated domain abstraction would add unnecessary complexity.

---

## 8.2 Allowed TimeSlots

`allowedTimeSlotIds` restricts the TimeSlots that may be considered for
an Activity.

Its semantics are:

```text
null
→ unrestricted
→ all valid TimeSlots may be considered

emptySet()
→ explicitly none
→ Activity has no possible TimeSlot

non-empty set
→ only referenced TimeSlots may be considered
```

Unknown references are reported by `ProblemValidator`.

An Activity with no possible valid TimeSlot is structurally invalid.

---

## 8.3 Allowed Locations

`allowedLocationIds` restricts the Locations that may be selected.

Its semantics follow the same nullable-set convention:

```text
null
→ unrestricted

emptySet()
→ explicitly no allowed Locations

non-empty set
→ only referenced Locations may be selected
```

Locations themselves remain optional in the generic model.

---

## 8.4 Required Location Capacity

An Activity may define:

```kotlin
requiredLocationCapacity: Int?
```

This expresses the capacity required from a selected Location.

For example:

```text
Activity:
    Physics Lecture

requiredLocationCapacity:
    30
```

may be assigned to:

```text
Location:
    Auditorium

capacity:
    50
```

Capacity sufficiency is evaluated by:

```text
LocationCapacityConstraint
```

The Activity stores the requirement and the Location stores its
intrinsic capacity.

---

## 8.5 Repeated Activities

Repeated activities are represented as separate Activity instances.

For example, if Mathematics 1A occurs three times:

```text
math-1a-session-1
math-1a-session-2
math-1a-session-3
```

rather than:

```text
Mathematics 1A
occurrences = 3
```

This preserves:

> **One Activity = one genetic scheduling decision.**

It simplifies:

* Candidate generation.
* Genotype encoding.
* Mutation.
* Crossover.
* Constraint evaluation.

Recurrence remains a template-level concept.

Templates may expand one user-facing recurring definition into multiple
Activities.

---

# 9. ResourceRequirement

`ResourceRequirement` describes the Resources required by an Activity.

```kotlin
data class ResourceRequirement(
    val id: String,
    val resourceTypeId: String,
    val quantity: Int = 1,
    val candidateResourceIds: Set<String>? = null
)
```

The abstraction allows an Activity to request Resources without
introducing fields such as:

```text
teacherId
employeeId
studentGroupId
```

---

## 9.1 Requirement Identity

Each requirement has its own `id`.

This identifier is important because Assignment uses:

```kotlin
Map<String, List<String>>
```

where the key is:

```text
ResourceRequirement.id
```

Therefore:

```text
ResourceRequirement
        │
        │ id
        ▼
Assignment.resourceAssignments
```

Requirements must have unique IDs within their Activity.

---

## 9.2 Quantity

`quantity` represents the exact number of distinct Resources required.

For example:

```text
quantity = 2
```

is satisfied by:

```text
[worker-1, worker-2]
```

but not by:

```text
[worker-1]
```

or:

```text
[worker-1, worker-1]
```

or:

```text
[worker-1, worker-2, worker-3]
```

`ProblemValidator` requires:

```text
quantity >= 1
```

---

## 9.3 Candidate Resources

`candidateResourceIds` optionally restricts which Resources may satisfy
the requirement.

Its semantics are:

```text
null
→ unrestricted
→ any compatible Resource of resourceTypeId may be considered

emptySet()
→ explicitly none
→ the requirement cannot be satisfied

non-empty set
→ only those compatible Resources may be considered
```

This distinction is important.

`null` and `emptySet()` do **not** mean the same thing.

Candidate Resources must:

* Exist.
* Have the required ResourceType.
* Provide at least `quantity` distinct candidates.

These rules are checked by `ProblemValidator`.

---

## 9.4 Example

An academic Activity may contain:

```text
Requirement:
    id = teacher-requirement
    resourceTypeId = TEACHER
    quantity = 1
    candidateResourceIds = [teacher-ana]
```

and:

```text
Requirement:
    id = group-requirement
    resourceTypeId = STUDENT_GROUP
    quantity = 1
    candidateResourceIds = [group-1a]
```

A Work Shift Activity may instead contain:

```text
Requirement:
    id = worker-requirement
    resourceTypeId = EMPLOYEE
    quantity = 2
    candidateResourceIds =
        [employee-ana, employee-laura, employee-pedro]
```

The Genetic Engine handles both through the same abstraction.

---

# 10. TimeSlot

A `TimeSlot` represents a temporal period in which an Activity may be
assigned.

```kotlin
data class TimeSlot(
    val id: String,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val label: String? = null
)
```

A TimeSlot must satisfy:

```text
start < end
```

and must fall within the `PlanningHorizon`.

These conditions are validated by `ProblemValidator`.

---

## 10.1 Different Durations

Genetic Planner does not require all TimeSlots to have equal duration.

For example:

```text
Academic:
09:00–10:00

Work Shift:
08:00–16:00
```

are both valid representations.

---

## 10.2 Temporal Overlap

Two TimeSlots overlap when:

```text
slotA.start < slotB.end
AND
slotB.start < slotA.end
```

Therefore:

```text
09:00–10:00
10:00–11:00
```

do not overlap.

This rule is used by `NoOverlapConstraint`.

---

# 11. Location

A `Location` represents a physical or logical place where an Activity
may occur.

```kotlin
data class Location(
    val id: String,
    val name: String,
    val type: String? = null,
    val capacity: Int? = null,
    val attributes: Map<String, String> = emptyMap()
)
```

Examples include:

```text
Classroom
Laboratory
Reception
Work area
Virtual room
```

Locations are optional.

A PlanningProblem may contain no Locations and an Assignment may have:

```kotlin
locationId = null
```

---

## 11.1 Capacity

`capacity` is intrinsic Location data.

For example:

```text
Location.capacity = 30
```

The Activity may separately define:

```text
Activity.requiredLocationCapacity = 40
```

Whether the selected Location satisfies that requirement is evaluated by:

```text
LocationCapacityConstraint
```

This preserves the separation between:

```text
domain data
→ Location.capacity
→ Activity.requiredLocationCapacity

planning evaluation
→ LocationCapacityConstraint
```

---

# 12. Constraint

A `PlanningProblem` contains the constraints applicable to that problem.

The core abstraction is:

```kotlin
interface Constraint {
    val id: String
    val name: String
    val type: ConstraintType
    val weight: Double

    fun evaluate(schedule: Schedule): ConstraintResult
}
```

The currently implemented catalogue is:

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

Constraint details are documented separately in:

```text
docs/architecture/constraints.md
docs/architecture/constraint-catalogue.md
docs/architecture/constraint-evaluation.md
```

---

# 13. Assignment

An `Assignment` represents the scheduling decision produced for one
Activity.

```kotlin
data class Assignment(
    val activityId: String,
    val timeSlotId: String,
    val resourceAssignments: Map<String, List<String>>,
    val locationId: String? = null
)
```

It connects:

```text
Activity
    +
TimeSlot
    +
Resources
    +
Location (optional)
```

---

## 13.1 Resource Assignments

`resourceAssignments` maps:

```text
ResourceRequirement.id
        ↓
List<Resource.id>
```

For example:

```text
teacher-requirement
→ [teacher-ana]

group-requirement
→ [group-1a]
```

Using requirement IDs allows an Activity to contain several independent
Resource requirements, including multiple requirements for the same
ResourceType.

---

## 13.2 No Assignment ID

`Assignment` deliberately has no independent identifier in the core
domain.

For the current model, the relevant identity is the Activity being
assigned.

Persistence layers may introduce technical identifiers if required
without exposing them to the domain model.

---

# 14. Schedule

A `Schedule` represents one candidate or final solution for a
PlanningProblem.

```kotlin
data class Schedule(
    val planningProblemId: String,
    val assignments: List<Assignment>
)
```

`Schedule` contains scheduling decisions only.

It does not contain:

```text
fitness
feasibility
hardPenalty
softPenalty
constraintResults
executionTime
generation statistics
```

Those values belong to optimization/evaluation models.

---

## 14.1 Complete Schedule

For the current Genetic Engine:

> **Every Activity must have exactly one Assignment.**

Therefore, for a complete Schedule:

```text
number of Assignments
=
number of Activities
```

A complete Schedule may still be infeasible.

For example:

```text
Activity A
→ worker-1
→ Monday 09:00

Activity B
→ worker-1
→ Monday 09:00
```

may be structurally complete but violate:

```text
NoOverlapConstraint
```

---

## 14.2 Schedule vs OptimizationResult

`Schedule` represents the planning solution.

`OptimizationResult` represents the outcome of genetic optimization.

Conceptually:

```text
OptimizationResult
│
├── Schedule
├── ScheduleEvaluation
│     ├── fitness
│     ├── feasible
│     ├── hardPenalty
│     ├── softPenalty
│     └── constraintResults
│
├── generationsExecuted
├── executionTime
└── randomSeed
```

Keeping these models separate prevents optimization metadata from
contaminating the scheduling domain.

---

# 15. Entity Relationships

The principal relationships are:

```text
PlanningProblem
│
├── 1 PlanningHorizon
├── * ResourceType
├── * Resource
├── * Activity
│      └── * ResourceRequirement
├── * TimeSlot
├── * Location
└── * Constraint


PlanningProblem
      │
      │ Genetic Engine
      ▼
   Schedule
      │
      └── * Assignment
             ├── 1 Activity
             ├── 1 TimeSlot
             ├── * Resource
             └── 0..1 Location
```

The implementation uses IDs between these objects rather than direct
object references.

This keeps the domain representation simple and facilitates candidate
generation, serialization, testing, and persistence adaptation.

---

# 16. Domain Validation

Structural validity is evaluated by:

```text
ProblemValidator
```

rather than through `require()` calls inside the domain data classes.

This allows multiple validation problems to be collected and reported
together.

The current validation includes:

* Identifier uniqueness.
* Planning horizon validity.
* TimeSlot validity.
* ResourceType references.
* ResourceRequirement quantity.
* Candidate Resource existence.
* Candidate Resource type compatibility.
* Candidate Resource sufficiency.
* Allowed TimeSlot references.
* Allowed Location references.
* Existence of at least one possible TimeSlot for every Activity.

---

## 16.1 Validation Errors

The implemented validation codes are:

```kotlin
enum class ValidationErrorCode {
    DUPLICATE_ID,
    INVALID_PLANNING_HORIZON,
    INVALID_TIME_SLOT,
    UNKNOWN_RESOURCE_TYPE,
    UNKNOWN_CANDIDATE_RESOURCE,
    CANDIDATE_RESOURCE_TYPE_MISMATCH,
    INVALID_REQUIREMENT_QUANTITY,
    INSUFFICIENT_CANDIDATE_RESOURCES,
    UNKNOWN_TIME_SLOT,
    UNKNOWN_LOCATION,
    NO_POSSIBLE_TIME_SLOT
}
```

Validation details are documented separately in the problem validation
documentation.

---

## 16.2 Nullable Set Semantics

The model consistently distinguishes:

```text
null
```

from:

```text
emptySet()
```

for allowed/candidate ID sets.

The convention is:

```text
null
→ unrestricted

emptySet()
→ explicitly none

non-empty set
→ explicitly restricted
```

This applies to:

```text
ResourceRequirement.candidateResourceIds
Activity.allowedTimeSlotIds
Activity.allowedLocationIds
```

The distinction must be preserved by API, persistence, template, and
candidate-generation layers.

---

# 17. Candidate Generation Boundary

The domain model describes what may be scheduled.

`AssignmentCandidateGenerator` transforms that information into concrete
options that can be encoded genetically.

Conceptually:

```text
PlanningProblem
      │
      ▼
AssignmentCandidateGenerator
      │
      ▼
AssignmentCandidateGenerationResult
      │
      ▼
ScheduleGenotypeCodec
```

Candidate generation uses:

* Activities.
* ResourceRequirements.
* Resources.
* TimeSlots.
* Locations.
* Allowed IDs.

It generates structurally possible Assignment options.

It deliberately does not eliminate complete-Schedule constraint
violations.

For example, two individually valid Assignment options may use the same
Resource at the same TimeSlot.

That conflict is handled later by:

```text
NoOverlapConstraint
```

The architectural rule is:

> **Structural candidate generation and planning constraint evaluation
> are separate responsibilities.**

---

# 18. Academic Scheduling Example

Consider an academic PlanningProblem.

### ResourceTypes

```text
TEACHER
STUDENT_GROUP
```

### Resources

```text
teacher-ana
teacher-pedro
group-1a
```

### Locations

```text
classroom-101
    capacity = 30

classroom-102
    capacity = 20
```

### TimeSlots

```text
monday-09
monday-10
tuesday-09
tuesday-10
```

### Activities

```text
math-1a-session-1
math-1a-session-2
english-1a-session-1
english-1a-session-2
```

A Mathematics Activity may define:

```text
teacher requirement:
    type = TEACHER
    quantity = 1
    candidates = [teacher-ana]

student group requirement:
    type = STUDENT_GROUP
    quantity = 1
    candidates = [group-1a]

requiredLocationCapacity = 25
```

Possible constraints include:

```text
NoOverlapConstraint
AvailabilityConstraint
RequiredResourceConstraint
LocationCapacityConstraint
PreferredTimeSlotConstraint
```

No academic-specific class is required by the Genetic Engine.

---

# 19. Work Shift Scheduling Example

The same model can represent a Work Shift problem.

### ResourceType

```text
EMPLOYEE
```

### Resources

```text
employee-ana
employee-laura
employee-pedro
```

### TimeSlots

```text
monday-morning
monday-afternoon
tuesday-morning
tuesday-afternoon
```

### Activities

```text
monday-morning-reception
monday-afternoon-reception
tuesday-morning-reception
tuesday-afternoon-reception
```

A Reception Activity may define:

```text
ResourceRequirement:
    resourceTypeId = EMPLOYEE
    quantity = 1
    candidateResourceIds =
        [employee-ana, employee-laura, employee-pedro]
```

Possible constraints include:

```text
NoOverlapConstraint
AvailabilityConstraint
RequiredResourceConstraint
PreferredTimeSlotConstraint
MaxConsecutiveConstraint
BalancedWorkloadConstraint
```

Again, the Genetic Engine receives exactly the same domain concepts.

---

# 20. Planning Templates

Planning templates translate user-friendly domain concepts into the
generic model.

For Academic Scheduling:

```text
Academic UI
    │
    ├── Teacher
    ├── Subject
    ├── Student Group
    ├── Classroom
    └── Teaching Period
            │
            ▼
     Academic Template
            │
            ▼
      PlanningProblem
```

For Work Shift Scheduling:

```text
Work Shift UI
    │
    ├── Employee
    ├── Work Assignment
    ├── Work Area
    └── Work Period
            │
            ▼
      Work Shift Template
            │
            ▼
       PlanningProblem
```

Templates may provide:

* Friendly terminology.
* Default ResourceTypes.
* Default constraints.
* Default TimeSlot structures.
* Domain-specific forms.
* Conversion from recurring user concepts into atomic Activities.

Templates must not modify Genetic Engine behaviour.

---

# 21. Domain and Genetic Engine Boundary

The dependency direction is:

```text
User Interface
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
      │
      ▼
OptimizationResult
```

Internally:

```text
PlanningProblem
      │
      ▼
AssignmentCandidateGenerator
      │
      ▼
ScheduleGenotypeCodec
      │
      ▼
Jenetics
      │
      ▼
Schedule
      │
      ▼
ConstraintEvaluator
```

The Genetic Engine may depend on generic domain concepts.

It must not contain domain-specific concepts such as:

```text
Teacher
Employee
Student
Subject
WorkShift
AcademicTemplate
WorkShiftTemplate
```

The domain itself must not depend on Jenetics.

The adaptation belongs to:

```text
genetic.codec
```

---

# 22. Persistence Boundary

Domain classes remain independent from persistence technology.

The domain does not require:

```text
@Entity
@Table
@Column
@OneToMany
```

Persistence concerns belong to separate persistence representations or
adapters.

The intended dependency direction is:

```text
Persistence Adapter
        │
        ▼
   Domain Model
```

rather than:

```text
Domain Model
        │
        ▼
JPA / PostgreSQL
```

This also allows the Genetic Engine and domain model to be tested without
a database.

---

# 23. Key Design Decisions

### D1 — Generic Resources

Teachers, employees, student groups, machines, and similar entities use
the same `Resource` abstraction.

**Reason:** preserve domain independence.

### D2 — Resource Types Are Data

Resource categories use `ResourceType` rather than a domain-specific
enum.

**Reason:** templates can introduce new categories without modifying the
Genetic Engine.

### D3 — String IDs Are Opaque

All domain references use `String` identifiers.

**Reason:** keep domain identity independent from persistence and support
readable tests and datasets.

### D4 — Lists Preserve Stable Order

PlanningProblem collections use `List`.

**Reason:** stable ordering is useful for genetic encoding,
serialization, testing, and reproducibility.

### D5 — Activities Are Atomic

Each Activity represents one scheduling decision and produces exactly
one Assignment.

Repeated activities are expanded into separate Activities.

**Reason:** simplify candidate generation and genetic representation.

### D6 — One Activity Uses One TimeSlot

Activities do not span multiple TimeSlots in the MVP.

**Reason:** keep the genetic representation manageable while supporting
different Activity durations through different TimeSlot durations.

### D7 — Time Uses LocalDateTime

Planning horizons and TimeSlots use concrete date/time values.

**Reason:** support different planning periods without introducing
time-zone complexity in the MVP.

### D8 — Resource Requirements Are Generic

`ResourceRequirement` describes the type, quantity, and optional
candidate Resources required by an Activity.

**Reason:** avoid domain-specific assignment fields.

### D9 — Null and Empty Sets Are Different

For candidate and allowed ID sets:

```text
null = unrestricted
empty = explicitly none
```

**Reason:** preserve an important distinction between absence of a
restriction and an impossible configuration.

### D10 — Locations Are Optional

A planning scenario does not need to use Locations.

**Reason:** avoid forcing irrelevant concepts into every domain.

### D11 — Capacity Is Explicit

Location capacity and Activity capacity requirements are explicit domain
properties.

**Reason:** capacity is used by core planning behaviour and should not be
hidden inside arbitrary attributes.

### D12 — Availability Is a Constraint

Availability does not belong to Resource.

**Reason:** separate intrinsic Resource data from planning rules.

### D13 — Attributes Are Metadata Only

`attributes` supports optional template metadata.

**Reason:** provide limited extensibility without turning the domain into
an untyped property model.

Core optimization behaviour must not depend on hidden metadata.

### D14 — Domain Objects Do Not Perform Aggregate Validation

Structural validation belongs to `ProblemValidator`.

**Reason:** validation errors can be collected and reported together
instead of failing during object construction.

### D15 — Schedule and OptimizationResult Are Separate

`Schedule` contains planning decisions.

`OptimizationResult` contains evaluation and execution information.

**Reason:** keep the scheduling solution independent from the process
that generated it.

### D16 — Domain Is Independent from Jenetics

No Jenetics types appear in the domain model.

**Reason:** genetic representation is an adapter concern rather than a
domain concern.

---

# 24. Implemented Kotlin Model

The current core model is:

```kotlin
data class PlanningHorizon(
    val start: LocalDateTime,
    val end: LocalDateTime
)

data class ResourceType(
    val id: String,
    val name: String
)

data class Resource(
    val id: String,
    val name: String,
    val typeId: String,
    val attributes: Map<String, String> = emptyMap()
)

data class ResourceRequirement(
    val id: String,
    val resourceTypeId: String,
    val quantity: Int = 1,
    val candidateResourceIds: Set<String>? = null
)

data class TimeSlot(
    val id: String,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val label: String? = null
)

data class Location(
    val id: String,
    val name: String,
    val type: String? = null,
    val capacity: Int? = null,
    val attributes: Map<String, String> = emptyMap()
)

data class Activity(
    val id: String,
    val name: String,
    val type: String? = null,
    val resourceRequirements: List<ResourceRequirement>,
    val allowedTimeSlotIds: Set<String>? = null,
    val allowedLocationIds: Set<String>? = null,
    val requiredLocationCapacity: Int? = null,
    val attributes: Map<String, String> = emptyMap()
)

data class PlanningProblem(
    val id: String,
    val name: String,
    val templateType: String? = null,
    val planningHorizon: PlanningHorizon,
    val resourceTypes: List<ResourceType> = emptyList(),
    val resources: List<Resource> = emptyList(),
    val activities: List<Activity> = emptyList(),
    val timeSlots: List<TimeSlot> = emptyList(),
    val locations: List<Location> = emptyList(),
    val constraints: List<Constraint> = emptyList()
)

data class Assignment(
    val activityId: String,
    val timeSlotId: String,
    val resourceAssignments: Map<String, List<String>>,
    val locationId: String? = null
)

data class Schedule(
    val planningProblemId: String,
    val assignments: List<Assignment>
)
```

Constraint models are documented separately and are omitted here for
clarity.

---

# 25. Deviations from Initial Design

Implementation refined several aspects of the original domain design.

### Activity Capacity Requirement

The initial model did not include:

```kotlin
requiredLocationCapacity
```

in `Activity`.

It was introduced when `LocationCapacityConstraint` became part of the
core constraint catalogue.

This makes both sides of capacity evaluation explicit:

```text
Activity.requiredLocationCapacity
Location.capacity
```

### Candidate Set Semantics

The initial documentation treated an absent or empty candidate Resource
set similarly.

The implemented semantics distinguish them:

```text
null
→ unrestricted

emptySet()
→ explicitly no candidates
```

This same convention is used by allowed ID sets.

### Validation

The initial design described the domain model as responsible for
guaranteeing structural validity.

The implemented architecture moved aggregate structural validation to
`ProblemValidator`.

Domain data classes therefore remain simple representations while
validation can collect multiple errors.

### Optimization Result

The initial design referred to:

```text
PlanningResult
```

The implemented optimization output is:

```text
OptimizationResult
```

which contains the generated Schedule, its evaluation, execution
statistics, and random seed.

### Constraint Catalogue

The initial examples included:

```text
MaximumAssignments
```

The implemented core catalogue instead contains:

```text
HARD
├── NoOverlap
├── Availability
├── RequiredResource
└── LocationCapacity

SOFT
├── PreferredTimeSlot
├── MaxConsecutive
└── BalancedWorkload
```

---

# 26. Future Extensions

The following concepts remain intentionally outside the current MVP:

* Activities spanning multiple TimeSlots.
* Recurring Activity definitions in the core domain.
* Multiple simultaneous Locations.
* Hierarchical Resources.
* Composite Activities.
* Activity dependencies.
* Precedence constraints.
* Resource cost models.
* Multiple optimization objectives.
* Partial or unassigned scheduling.
* Dynamic schedule repair.
* Time-zone-aware planning.

These extensions should only be introduced when they provide sufficient
value to justify their additional complexity.

---

# 27. Domain UML

The domain UML diagram is stored in:

```text
docs/diagrams/domain-model.puml
```

It represents the same generic concepts documented here:

```text
PlanningProblem
├── PlanningHorizon
├── ResourceType
├── Resource
├── Activity
│     └── ResourceRequirement
├── TimeSlot
├── Location
└── Constraint

Schedule
└── Assignment
```

The UML must remain consistent with the implemented Kotlin model,
including:

```text
Activity.requiredLocationCapacity
ResourceRequirement.id
ResourceRequirement.quantity
ResourceRequirement.candidateResourceIds
Assignment.resourceAssignments
```

The diagram intentionally contains no domain-specific classes such as:

```text
Teacher
Employee
Subject
WorkShift
```

Those concepts belong to planning templates rather than the generic
domain model.