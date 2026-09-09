# Genetic Planner — Generic Domain Model

## 1. Purpose

This document defines the generic domain model used by **Genetic Planner**.

The objective of the model is to represent different planning and scheduling problems using the same set of domain concepts, independently of whether the problem represents:

* Academic timetables.
* Work shifts.
* School calendars.
* Resource allocation schedules.
* Other time-based planning scenarios.

The model acts as the contract between:

* The graphical user interface.
* Planning templates.
* The application layer.
* The constraint evaluation system.
* The Genetic Engine.
* Persistence.

The Genetic Engine must operate exclusively on this generic model and must not contain domain-specific concepts such as `Teacher`, `Employee`, `Subject`, `StudentGroup`, or `WorkShift`.

---

## 2. Design Principles

The domain model follows the principles below.

### 2.1 Domain Independence

The core model must not depend on any particular planning scenario.

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

Both scenarios must ultimately create the same type of `PlanningProblem`.

---

### 2.2 Explicit Scheduling Concepts

The model must explicitly represent the concepts required by the optimization process:

* What must be scheduled.
* When it can be scheduled.
* Which resources participate.
* Where it takes place.
* Which rules must be satisfied.

---

### 2.3 Structural Validity vs Optimization Quality

The domain model is responsible for guaranteeing that a planning problem is structurally valid.

Examples include:

* References point to existing entities.
* Time slots contain valid start and end times.
* IDs are unique.
* Activities define valid resource requirements.

Planning quality is handled separately through constraints and fitness evaluation.

For example:

> Two activities assigned to the same teacher at the same time

is not a structural error in the domain model.

It is a `NoOverlap` constraint violation.

---

### 2.4 Extensibility

The model should make it possible to introduce:

* New resource categories.
* New activity types.
* New location types.
* New constraints.
* New planning templates.

without modifying the core Genetic Engine.

---

### 2.5 MVP Simplicity

The first version intentionally limits some scheduling concepts to keep the genetic representation manageable.

For the MVP:

> **One Activity represents one schedulable unit and is assigned to exactly one TimeSlot.**

The duration of an activity is therefore represented by the duration of the selected `TimeSlot`.

Examples:

```text
Academic planning

TimeSlot:
Monday 09:00 → 10:00

Activity:
Mathematics 1A
```

```text
Work shift planning

TimeSlot:
Monday 08:00 → 16:00

Activity:
Reception Morning Shift
```

Multi-slot activities may be considered as a future extension.

---

# 3. Domain Overview

The main domain structure is:

```text
PlanningProblem
│
├── PlanningHorizon
│
├── ResourceTypes
│
├── Resources
│
├── Activities
│   └── ResourceRequirements
│
├── TimeSlots
│
├── Locations
│
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

A `PlanningProblem` describes everything necessary to generate a schedule.

A `Schedule` represents one candidate or final solution for that problem.

---

# 4. PlanningProblem

## 4.1 Responsibility

`PlanningProblem` is the root object of the planning domain.

It contains all information required by the Genetic Engine to produce a schedule.

Conceptually:

```kotlin
data class PlanningProblem(
    val id: String,
    val name: String,
    val templateType: String?,
    val planningHorizon: PlanningHorizon,
    val resourceTypes: List<ResourceType>,
    val resources: List<Resource>,
    val activities: List<Activity>,
    val timeSlots: List<TimeSlot>,
    val locations: List<Location>,
    val constraints: List<Constraint>
)
```

This is a conceptual definition. Persistence annotations, API DTOs, and framework-specific code must not form part of the domain model.

---

## 4.2 Attributes

### `id`

Unique identifier of the planning problem.

### `name`

Human-readable name.

Examples:

```text
Secondary School Weekly Timetable
September Reception Shifts
Computer Science Department Schedule
```

### `templateType`

Optional identifier indicating which template was used to create the problem.

Examples:

```text
ACADEMIC
WORK_SHIFT
CUSTOM
```

This value exists for application and UI purposes.

The Genetic Engine must not use `templateType` to change its behaviour.

### `planningHorizon`

Defines the time range represented by the planning problem.

### `resourceTypes`

Defines the categories of resources available in the problem.

### `resources`

All resources that may participate in the schedule.

### `activities`

All schedulable units that must be assigned.

### `timeSlots`

Available periods where activities may be scheduled.

### `locations`

Available locations.

Locations are optional at problem level. A planning problem may contain no locations.

### `constraints`

Rules used to evaluate schedule validity and quality.

---

# 5. PlanningHorizon

## 5.1 Responsibility

Represents the temporal boundaries of the planning problem.

Conceptually:

```kotlin
data class PlanningHorizon(
    val start: LocalDateTime,
    val end: LocalDateTime
)
```

---

## 5.2 Examples

Academic weekly timetable:

```text
Start: 2026-09-14 08:00
End:   2026-09-18 15:00
```

Work schedule:

```text
Start: 2026-09-14 00:00
End:   2026-09-20 23:59
```

Using real date/time values rather than an abstract `MONDAY / TUESDAY` representation gives the model enough flexibility to support:

* Weekly schedules.
* Concrete calendar periods.
* Multi-week planning.
* Future school calendars.

The UI may still present the information as a weekly calendar.

---

# 6. ResourceType

## 6.1 Responsibility

A `ResourceType` identifies a category of resources.

It prevents domain-specific types from being hard-coded into the Genetic Engine.

Conceptually:

```kotlin
data class ResourceType(
    val id: String,
    val name: String
)
```

---

## 6.2 Examples

Academic template:

```text
TEACHER
STUDENT_GROUP
```

Work Shift template:

```text
EMPLOYEE
```

Future templates could introduce:

```text
VEHICLE
MACHINE
MEDICAL_STAFF
EQUIPMENT
```

without modifying the domain model.

---

# 7. Resource

## 7.1 Responsibility

A `Resource` represents an entity that can participate in an activity.

A resource is not assumed to be a person.

Conceptually:

```kotlin
data class Resource(
    val id: String,
    val name: String,
    val typeId: String,
    val attributes: Map<String, String>
)
```

---

## 7.2 Attributes

### `id`

Unique identifier.

### `name`

Human-readable name.

### `typeId`

Reference to its `ResourceType`.

### `attributes`

Optional extensible metadata associated with the resource.

Examples:

Academic:

```text
Resource:
    id: teacher-ana
    name: Ana
    type: TEACHER

attributes:
    department: Mathematics
```

Work:

```text
Resource:
    id: employee-laura
    name: Laura
    type: EMPLOYEE

attributes:
    department: Reception
```

---

## 7.3 Design Decision

Availability must **not** be stored directly inside `Resource`.

For example:

```text
Ana unavailable Monday 08:00
```

is a scheduling rule and therefore belongs to:

```text
AvailabilityConstraint
```

This keeps the resource definition independent from individual planning executions.

---

# 8. Activity

## 8.1 Responsibility

An `Activity` represents the fundamental unit that must be scheduled.

Each activity must eventually produce one `Assignment`.

Conceptually:

```kotlin
data class Activity(
    val id: String,
    val name: String,
    val type: String?,
    val resourceRequirements: List<ResourceRequirement>,
    val allowedTimeSlotIds: Set<String>?,
    val allowedLocationIds: Set<String>?,
    val attributes: Map<String, String>
)
```

---

## 8.2 Examples

Academic:

```text
Mathematics 1A — Monday session
English 2B — Session 1
Physics Laboratory — Session 2
```

Work:

```text
Monday Morning Reception
Monday Afternoon Support
Tuesday Morning Reception
```

---

## 8.3 Activity Occurrences

An important MVP design decision is:

> Repeated activities are represented as separate schedulable Activity instances.

For example, if Mathematics 1A must occur three times per week:

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

Planning templates may automatically create these activity instances for the user.

This significantly simplifies:

* Genetic encoding.
* Assignment generation.
* Mutation.
* Crossover.
* Constraint evaluation.

A higher-level recurring activity model can be introduced later if necessary.

---

# 9. ResourceRequirement

## 9.1 Responsibility

Defines which kinds of resources an activity requires.

This concept allows an activity to request resources without introducing domain-specific fields.

Conceptually:

```kotlin
data class ResourceRequirement(
    val id: String,
    val resourceTypeId: String,
    val quantity: Int,
    val candidateResourceIds: Set<String>?
)
```

---

## 9.2 Examples

### Academic Activity

Mathematics 1A may require:

```text
Requirement 1

Type: TEACHER
Quantity: 1
Candidates:
    teacher-ana
```

and:

```text
Requirement 2

Type: STUDENT_GROUP
Quantity: 1
Candidates:
    group-1a
```

The first requirement therefore fixes Ana as the teacher.

The second fixes Group 1A.

---

### Work Shift Activity

Reception Monday Morning:

```text
Requirement

Type: EMPLOYEE
Quantity: 1

Candidates:
    ana
    laura
    pedro
    carlos
```

The Genetic Engine decides which candidate employee is assigned.

---

## 9.3 Why ResourceRequirement Exists

Without this abstraction, the model would require fields such as:

```text
teacherId
employeeId
studentGroupId
```

which would destroy domain independence.

`ResourceRequirement` gives the Genetic Engine a generic question:

> Which resources satisfying this requirement should be assigned to this activity?

---

# 10. TimeSlot

## 10.1 Responsibility

Represents a valid temporal period in which an activity may be assigned.

Conceptually:

```kotlin
data class TimeSlot(
    val id: String,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val label: String?
)
```

---

## 10.2 Examples

Academic:

```text
id: mon-09
start: 2026-09-14 09:00
end:   2026-09-14 10:00
label: Monday 09:00–10:00
```

Work:

```text
id: mon-morning
start: 2026-09-14 08:00
end:   2026-09-14 16:00
label: Monday Morning
```

---

## 10.3 Why Time Slots May Have Different Durations

Genetic Planner does not assume that every planning problem uses equal-length periods.

Therefore:

```text
Academic TimeSlot = 1 hour
```

and:

```text
Work Shift TimeSlot = 8 hours
```

can coexist as different planning scenarios.

Within one particular planning problem, templates should normally generate a coherent set of time slots.

---

## 10.4 Temporal Overlap

Two time slots overlap when:

```text
slotA.start < slotB.end
AND
slotB.start < slotA.end
```

This definition will later be used by constraints such as `NoOverlap`.

---

# 11. Location

## 11.1 Responsibility

Represents a physical or logical place where an activity may occur.

Conceptually:

```kotlin
data class Location(
    val id: String,
    val name: String,
    val type: String?,
    val capacity: Int?,
    val attributes: Map<String, String>
)
```

---

## 11.2 Examples

Academic:

```text
Classroom 101
Laboratory 2
Gymnasium
```

Work:

```text
Reception
Customer Support Area
Building A
```

Some planning scenarios may not require locations at all.

---

## 11.3 Capacity

`capacity` is included as optional domain data because it is an intrinsic property of many locations.

However:

> Whether the capacity is sufficient for a particular activity is evaluated by a constraint.

Therefore:

```text
Location.capacity = 20
```

is domain data.

While:

```text
Activity requires capacity >= 30
```

belongs to planning rules and constraint evaluation.

---

# 12. Constraint

## 12.1 Responsibility

Constraints define rules used to evaluate candidate schedules.

Only its relationship with the domain model is defined here.

The detailed constraint architecture is specified separately in:

```text
docs/architecture/constraints.md
```

Conceptually:

```text
Constraint
│
├── HARD
└── SOFT
```

Examples:

```text
NoOverlap
Availability
RequiredResource
MaximumAssignments
PreferredTimeSlot
```

A `PlanningProblem` contains the complete set of constraints applicable to that particular problem.

---

# 13. Assignment

## 13.1 Responsibility

An `Assignment` represents the placement of one activity into the generated schedule.

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

Conceptually:

```kotlin
data class Assignment(
    val activityId: String,
    val timeSlotId: String,
    val resourceAssignments: Map<String, List<String>>,
    val locationId: String?
)
```

The key in `resourceAssignments` refers to a `ResourceRequirement.id`.

---

## 13.2 Example — Academic

Activity:

```text
Mathematics 1A — Session 1
```

Assignment:

```text
Activity:
    math-1a-session-1

TimeSlot:
    monday-09

Resource assignments:
    teacher-requirement → [teacher-ana]
    group-requirement   → [group-1a]

Location:
    classroom-101
```

Rendered for the user:

```text
Monday 09:00–10:00

Mathematics 1A
Teacher: Ana
Group: 1A
Room: Classroom 101
```

---

## 13.3 Example — Work Shift

Activity:

```text
Monday Morning Reception
```

Assignment:

```text
Activity:
    reception-monday-morning

TimeSlot:
    monday-morning

Resource assignments:
    employee-requirement → [employee-laura]

Location:
    reception
```

Rendered:

```text
Monday 08:00–16:00

Reception
Employee: Laura
```

---

# 14. Schedule

## 14.1 Responsibility

A `Schedule` represents one complete candidate solution to a `PlanningProblem`.

Conceptually:

```kotlin
data class Schedule(
    val planningProblemId: String,
    val assignments: List<Assignment>
)
```

---

## 14.2 MVP Completeness Rule

For the MVP:

> Every Activity must have exactly one Assignment.

Therefore:

```text
number of Assignments = number of Activities
```

A schedule may still violate hard constraints.

For example:

```text
Ana assigned to two simultaneous activities
```

The schedule is structurally complete, but its fitness will contain a severe penalty.

This allows the Genetic Engine to evolve from poor schedules toward increasingly valid schedules.

---

## 14.3 Schedule vs PlanningResult

`Schedule` only represents the generated planning solution.

Optimization metadata belongs to a separate `PlanningResult`.

Conceptually:

```text
PlanningResult
│
├── Schedule
├── Fitness
├── Hard violations
├── Soft violations
├── Generations
└── Execution time
```

Keeping these concepts separate prevents optimization-specific information from contaminating the core scheduling model.

---

# 15. Entity Relationships

The conceptual relationships are:

```text
PlanningProblem
│
│ 1
│
├──────── 1 PlanningHorizon
│
├──────── * ResourceType
│             │
│             │ 1
│             │
├──────── * Resource
│
├──────── * Activity
│             │
│             └──────── * ResourceRequirement
│
├──────── * TimeSlot
│
├──────── * Location
│
└──────── * Constraint


PlanningProblem
      │
      │ Genetic Engine
      ▼
   Schedule
      │
      └──────── * Assignment
                     │
                     ├── 1 Activity
                     ├── 1 TimeSlot
                     ├── * Resource
                     └── 0..1 Location
```

---

# 16. Domain Invariants

The following invariants must hold before a `PlanningProblem` can be processed by the Genetic Engine.

## 16.1 Identifier Integrity

All entity IDs must be unique within their entity type.

References must point to existing entities.

---

## 16.2 Planning Horizon

```text
planningHorizon.start < planningHorizon.end
```

All time slots must fall within the planning horizon.

---

## 16.3 TimeSlot Validity

Every time slot must satisfy:

```text
start < end
```

---

## 16.4 Resource Integrity

Every `Resource.typeId` must reference an existing `ResourceType`.

---

## 16.5 Resource Requirement Integrity

For each `ResourceRequirement`:

```text
quantity >= 1
```

Candidate resources must:

* Exist.
* Match the required `ResourceType`.

If the candidate set is empty or absent, all resources belonging to the required type may be considered candidates.

---

## 16.6 Activity Integrity

Each activity must:

* Have a unique ID.
* Reference valid resource requirements.
* Reference only existing allowed time slots.
* Reference only existing allowed locations.

At least one valid time slot must be available for every activity.

---

## 16.7 Schedule Integrity

Every assignment must:

* Reference one existing activity.
* Reference one existing time slot.
* Reference only existing resources.
* Reference an existing location when one is specified.

Every activity must appear exactly once in the schedule.

Every resource requirement must receive the expected number of resources.

---

# 17. Structural Validation

Before starting genetic optimization, Genetic Planner should execute a validation phase.

Conceptually:

```text
PlanningProblem
      │
      ▼
ProblemValidator
      │
      ├── Invalid → Validation errors
      │
      └── Valid
             │
             ▼
       Genetic Engine
```

Examples of validation errors:

```text
Activity "Mathematics 1A" has no available time slots.

Resource requirement references unknown resource type "TEACHER".

Time slot "monday-09" ends before it starts.

Activity references unknown classroom "room-99".
```

This prevents the Genetic Engine from attempting to solve structurally malformed problems.

---

# 18. Academic Scheduling Example

Consider the following academic problem.

## Resources

```text
TEACHER
    Ana
    Pedro

STUDENT_GROUP
    1A
```

## Locations

```text
Classroom 101
Classroom 102
```

## Time Slots

```text
Monday 09:00–10:00
Monday 10:00–11:00
Tuesday 09:00–10:00
Tuesday 10:00–11:00
```

## Activities

```text
Mathematics 1A — Session 1
Mathematics 1A — Session 2
English 1A — Session 1
English 1A — Session 2
```

Mathematics requires:

```text
Teacher candidate:
    Ana

Student group:
    1A
```

English requires:

```text
Teacher candidate:
    Pedro

Student group:
    1A
```

## Constraints

Examples:

```text
No resource overlap.

Ana unavailable Tuesday 09:00.

Pedro unavailable Monday 10:00.

Prefer Mathematics before 11:00.
```

## Possible Schedule

```text
Monday 09:00
Mathematics 1A — Session 1
Ana
Group 1A
Classroom 101

Monday 10:00
English 1A — Session 1
Pedro
Group 1A
Classroom 102

Tuesday 09:00
English 1A — Session 2
Pedro
Group 1A
Classroom 101

Tuesday 10:00
Mathematics 1A — Session 2
Ana
Group 1A
Classroom 102
```

No academic-specific object is required by the Genetic Engine.

---

# 19. Work Shift Scheduling Example

Consider the following work planning problem.

## Resources

```text
EMPLOYEE
    Ana
    Laura
    Pedro
```

## Locations

```text
Reception
Support Desk
```

## Time Slots

```text
Monday 08:00–16:00
Monday 16:00–00:00
Tuesday 08:00–16:00
Tuesday 16:00–00:00
```

## Activities

```text
Monday Morning Reception
Monday Afternoon Reception
Tuesday Morning Reception
Tuesday Afternoon Reception
```

Each activity contains:

```text
ResourceRequirement

type:
    EMPLOYEE

quantity:
    1

candidates:
    Ana
    Laura
    Pedro
```

## Constraints

```text
No employee overlap.

Ana unavailable Monday morning.

Laura prefers morning periods.

Maximum assignments per employee: 2.
```

## Possible Schedule

```text
Monday 08:00–16:00
Reception
Laura

Monday 16:00–00:00
Reception
Pedro

Tuesday 08:00–16:00
Reception
Ana

Tuesday 16:00–00:00
Reception
Pedro
```

Again, the Genetic Engine receives exactly the same domain concepts as in the academic scenario.

---

# 20. Mapping Through Planning Templates

Planning templates are responsible for translating user-friendly domain concepts into the generic model.

```text
Academic UI

Teacher
Subject
Group
Classroom
Teaching period

        │
        ▼

AcademicTemplate

        │
        ▼

PlanningProblem
```

Likewise:

```text
Work Shift UI

Employee
Shift
Work area
Availability

        │
        ▼

WorkShiftTemplate

        │
        ▼

PlanningProblem
```

Templates may therefore provide:

* Friendly terminology.
* Default resource types.
* Default constraints.
* Default time slot structures.
* Domain-specific forms.
* Data transformation.

They must not alter the Genetic Engine.

---

# 21. Domain Model and Genetic Engine Boundary

The intended dependency direction is:

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
Genetic Engine
      │
      ▼
Schedule
```

The Genetic Engine is allowed to know about:

```text
PlanningProblem
Activity
ResourceRequirement
Resource
TimeSlot
Location
Constraint
Assignment
Schedule
```

It must not know about:

```text
Teacher
Employee
Student
Subject
WorkShift
AcademicTemplate
WorkShiftTemplate
React
REST
PostgreSQL
Spring Boot
```

---

# 22. Domain Model and Persistence Boundary

Domain classes should initially remain independent from persistence technology.

Therefore, the domain model should not require concepts such as:

```text
@Entity
@Table
@Column
@OneToMany
```

Persistence concerns may be represented through separate persistence entities or adapters where necessary.

The dependency direction should remain:

```text
Persistence
     │
     ▼
Domain Model
```

rather than:

```text
Domain Model
     │
     ▼
PostgreSQL / JPA
```

This also makes Genetic Engine unit testing substantially easier.

---

# 23. Key Design Decisions

## D1 — Generic Resources

Teachers, employees, student groups, machines, and similar entities are represented using the same `Resource` abstraction.

**Reason:** preserve domain independence.

---

## D2 — Resource Types Are Configurable

Resource categories are data rather than hard-coded classes.

**Reason:** new planning scenarios can define new resource categories without modifying the Genetic Engine.

---

## D3 — Activities Are Atomic Scheduling Units

Each `Activity` must be assigned exactly once.

Repeated activities are expanded into multiple activities.

**Reason:** greatly simplifies the genetic representation and operators.

---

## D4 — One Activity Uses One TimeSlot

Activities do not span several time slots in the MVP.

A time slot itself may have any duration.

**Reason:** supports both hourly lessons and long work shifts while keeping chromosomes manageable.

---

## D5 — Time Uses Concrete Date/Time Values

Time slots use start and end timestamps.

**Reason:** supports both weekly planning and future calendar-based planning scenarios.

---

## D6 — Resources Required by Activities Are Generic

`ResourceRequirement` replaces concepts such as teacher assignment or employee assignment.

**Reason:** allows both fixed and selectable resources without domain-specific fields.

---

## D7 — Locations Are Optional

Not every planning scenario requires physical locations.

**Reason:** maintain flexibility without forcing meaningless data.

---

## D8 — Constraints Are Separate From Entities

Availability, overlap, assignment limits, and preferences are represented through constraints.

**Reason:** separate domain data from optimization policy.

---

## D9 — Attributes Support Limited Extensibility

Resources, activities, and locations may contain additional key/value attributes.

**Reason:** templates may need metadata that does not justify changing the generic model.

Core optimization logic must not rely extensively on arbitrary attributes. Important scheduling concepts should be represented explicitly or through constraints.

---

## D10 — Schedule and PlanningResult Are Different Concepts

`Schedule` contains assignments.

`PlanningResult` contains optimization metrics.

**Reason:** keep scheduling data independent from the optimization process that generated it.

---

# 24. Initial Conceptual Kotlin Model

The first implementation is expected to resemble the following structure:

```kotlin
data class PlanningProblem(
    val id: String,
    val name: String,
    val templateType: String?,
    val planningHorizon: PlanningHorizon,
    val resourceTypes: List<ResourceType>,
    val resources: List<Resource>,
    val activities: List<Activity>,
    val timeSlots: List<TimeSlot>,
    val locations: List<Location>,
    val constraints: List<Constraint>
)

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

data class Activity(
    val id: String,
    val name: String,
    val type: String?,
    val resourceRequirements: List<ResourceRequirement>,
    val allowedTimeSlotIds: Set<String>? = null,
    val allowedLocationIds: Set<String>? = null,
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
    val type: String?,
    val capacity: Int? = null,
    val attributes: Map<String, String> = emptyMap()
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

This code remains conceptual until the constraint and genetic representation designs have been completed.

---

# 25. Acceptance Criteria

The generic domain model is considered complete when:

* [x] `PlanningProblem` is defined.
* [x] `PlanningHorizon` is defined.
* [x] `ResourceType` is defined.
* [x] `Resource` is defined.
* [x] `Activity` is defined.
* [x] `ResourceRequirement` is defined.
* [x] `TimeSlot` is defined.
* [x] `Location` is defined.
* [x] `Assignment` is defined.
* [x] `Schedule` is defined.
* [x] Entity relationships are documented.
* [x] Structural invariants are documented.
* [x] Academic Scheduling can be represented using the generic model.
* [x] Work Shift Scheduling can be represented using the same model.
* [x] No domain-specific concept is required by the Genetic Engine.
* [x] The model maintains separation from persistence and UI concerns.

---

# 26. Future Extensions

The following concepts are intentionally excluded from the MVP domain but may be introduced later:

* Activities spanning multiple time slots.
* Recurring activity definitions.
* Dynamic planning horizons.
* Multiple simultaneous locations.
* Hierarchical resources.
* Composite activities.
* Dependencies between activities.
* Precedence constraints.
* Multi-stage planning.
* Resource cost models.
* Multiple optimization objectives.
* Partial/unassigned scheduling.
* Dynamic schedule repair after changes.

These extensions should only be introduced if they provide sufficient value to justify the additional complexity.

---

## 27. Domain UML Overview

The domain UML diagram provides a simplified visual representation of the main concepts used by Genetic Planner and the relationships between them.

The diagram is stored in:

```text
/docs/diagrams/domain-model.puml
```

The central element is `PlanningProblem`, which aggregates the information required to describe a planning scenario:

* `Resource`: entities that may participate in scheduled activities.
* `Activity`: schedulable units that must be placed in the final schedule.
* `TimeSlot`: available temporal periods.
* `Location`: optional physical or logical places where activities may occur.
* `Constraint`: rules used to evaluate whether a schedule is valid or desirable.

Each `Activity` may contain one or more `ResourceRequirement` elements. These requirements define the type and number of resources needed by the activity without introducing domain-specific concepts such as teachers or employees.

The result of the planning process is represented by `Schedule`, which contains a collection of `Assignment` objects.

Each `Assignment` links:

* one `Activity`,
* one `TimeSlot`,
* zero or more assigned `Resource` objects,
* and optionally one `Location`.

The model therefore separates the **definition of the planning problem** from the **resulting schedule**:

```text
PlanningProblem
      │
      ▼
Genetic Engine
      │
      ▼
Schedule
      │
      ▼
Assignments
```

This separation allows the same Genetic Engine to operate on different planning scenarios, such as academic timetables and work shift scheduling, without requiring domain-specific modifications.

### Main Cardinalities

The most relevant relationships represented in the UML are:

```text
PlanningProblem 1 ---- 0..* Resource
PlanningProblem 1 ---- 1..* Activity
PlanningProblem 1 ---- 1..* TimeSlot
PlanningProblem 1 ---- 0..* Location
PlanningProblem 1 ---- 0..* Constraint

Activity 1 ---- 0..* ResourceRequirement

Schedule 1 ---- 1..* Assignment

Assignment * ---- 1 Activity
Assignment * ---- 1 TimeSlot
Assignment * ---- 0..* Resource
Assignment * ---- 0..1 Location
```

These cardinalities reflect the MVP design decisions:

* A planning problem must contain at least one activity and one available time slot.
* Resources and locations are optional because some planning scenarios may not require them.
* An activity may require several resources.
* Each activity produces one assignment in a complete schedule.
* An assignment always has one activity and one time slot.
* An assignment may contain multiple resources and optionally one location.

The UML intentionally remains domain-independent. Concepts such as `Teacher`, `Employee`, `Subject`, or `WorkShift` are not represented as core domain classes. They are mapped to the generic model through planning templates.
