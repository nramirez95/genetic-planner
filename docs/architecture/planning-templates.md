# Genetic Planner — Planning Template Architecture

## 1. Purpose

This document defines the planning template architecture used by Genetic Planner.

The purpose of a planning template is to simplify the configuration of a planning problem for a specific use case while preserving a completely generic internal planning model.

The central architecture is:

```text
Academic Template ───────┐
                         ↓
                  PlanningProblem
                         ↓
                  Genetic Engine
                         ↑
Work Shift Template ─────┘
```

The Genetic Engine must only understand the generic `PlanningProblem`.

It must not contain logic related to:

* Teachers.
* Subjects.
* Students.
* Employees.
* Work shifts.
* Academic scheduling.
* Workforce scheduling.

The main design principle is:

> **Templates provide domain-specific configuration and terminology, while the Genetic Engine operates only on generic planning concepts.**

---

# 2. PlanningTemplate Concept

A `PlanningTemplate` defines how a specific planning scenario is configured and transformed into a generic `PlanningProblem`.

Conceptually:

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

A template is responsible for:

* Providing domain-specific terminology.
* Providing default configuration.
* Defining default constraints.
* Collecting template-specific user input.
* Transforming that input into generic domain objects.
* Producing a valid `PlanningProblem`.

A template is not responsible for:

* Evaluating fitness.
* Running Jenetics.
* Applying mutation.
* Applying crossover.
* Selecting individuals.
* Calculating genetic parameters.

---

# 3. Template Boundary

The template layer acts as an adapter between:

```text
User mental model
```

and:

```text
Generic planning model
```

For example:

```text
Teacher
    ↓
Resource

Subject session
    ↓
Activity

Classroom
    ↓
Location

Teaching period
    ↓
TimeSlot
```

Similarly:

```text
Employee
    ↓
Resource

Work assignment
    ↓
Activity

Workplace
    ↓
Location

Shift period
    ↓
TimeSlot
```

Therefore templates translate domain language into the generic model without changing the Genetic Engine.

---

# 4. Generic Architecture

The overall architecture is:

```text
                Genetic Planner
                       │
           ┌───────────┴───────────┐
           │                       │
           ▼                       ▼
    Academic Template       Work Shift Template
           │                       │
           └───────────┬───────────┘
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

The Genetic Engine receives exactly the same structure regardless of the selected template.

---

# 5. Responsibilities of PlanningTemplate

A planning template is responsible for five main concerns.

## 5.1 Terminology

The template provides user-friendly domain terminology.

For example:

```text
Generic term      Academic UI term

Resource          Teacher / Student Group
Activity          Class
Location          Classroom
TimeSlot          Teaching Period
```

while:

```text
Generic term      Work Shift UI term

Resource          Employee
Activity          Work Assignment
Location          Workplace
TimeSlot          Shift Period
```

Terminology affects the UI only.

It does not change the generic domain model.

---

## 5.2 Default configuration

A template provides a reasonable initial configuration.

Examples:

```text
Academic Template

Resource types:
- Teacher
- Student Group

Default activity type:
- Class

Default location type:
- Classroom
```

or:

```text
Work Shift Template

Resource types:
- Employee

Default activity type:
- Work Assignment

Default location type:
- Workplace
```

---

## 5.3 Default constraints

Each template can suggest constraints appropriate to its planning scenario.

These constraints still implement the generic `Constraint` abstraction.

For example:

```text
Academic Template

HARD
- No overlap
- Availability
- Required resource

SOFT
- Preferred time slot
- Max consecutive
```

and:

```text
Work Shift Template

HARD
- No overlap
- Availability
- Required resource
- Maximum assignments

SOFT
- Minimum assignments
- Max consecutive
- Preferred time slot
```

---

## 5.4 Input transformation

The template transforms domain-oriented configuration into:

```text
PlanningProblem
```

For example:

```text
AcademicTemplateConfig
        ↓
AcademicTemplate
        ↓
PlanningProblem
```

The transformation produces:

* `ResourceType`
* `Resource`
* `Activity`
* `ResourceRequirement`
* `TimeSlot`
* `Location`
* `Constraint`

---

## 5.5 UI guidance

Templates may provide metadata used by the UI.

For example:

```text
Teacher
instead of
Resource
```

or:

```text
Classroom
instead of
Location
```

This improves usability without creating template-specific Genetic Engine behavior.

---

# 6. Template Metadata

A template may expose descriptive metadata such as:

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

Example:

```kotlin
Academic Template

resourceLabel = "Teacher / Group"
activityLabel = "Class"
locationLabel = "Classroom"
timeSlotLabel = "Teaching Period"
```

Example:

```kotlin
Work Shift Template

resourceLabel = "Employee"
activityLabel = "Work Assignment"
locationLabel = "Workplace"
timeSlotLabel = "Shift"
```

These labels belong to the application/UI layer.

The domain model continues using generic names.

---

# 7. Academic Template

The Academic Template represents academic timetable generation.

Its goal is to allow users to configure an academic problem using familiar terminology while internally creating a standard `PlanningProblem`.

---

# 8. Academic Mapping

The conceptual mapping is:

| Academic concept                   | Generic model       |
| ---------------------------------- | ------------------- |
| Teacher                            | Resource            |
| Student Group                      | Resource            |
| Class / Lesson                     | Activity            |
| Classroom                          | Location            |
| Teaching Period                    | TimeSlot            |
| Teacher required for a class       | ResourceRequirement |
| Student group required for a class | ResourceRequirement |
| Scheduling rule                    | Constraint          |

Example:

```text
Mathematics Class

Teacher:
Ana

Student Group:
1A

Classroom:
Room 101

Period:
Monday 09:00–10:00
```

becomes:

```text
Activity
    ↓
ResourceRequirement
    ├── Teacher
    └── Student Group

TimeSlot
    ↓
Monday 09:00–10:00

Location
    ↓
Room 101
```

---

# 9. Academic Template Configuration

Conceptually:

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

These configuration classes belong to the template/application layer.

They do not belong to the generic domain.

---

# 10. Academic Resource Types

The default Academic Template defines:

```text
Teacher
Student Group
```

as configurable `ResourceType` objects.

Conceptually:

```text
ResourceType

teacher
student-group
```

A teacher becomes:

```text
Resource

id = teacher-ana
name = Ana
typeId = teacher
```

A student group becomes:

```text
Resource

id = group-1a
name = Group 1A
typeId = student-group
```

---

# 11. Academic Activities

Each class or lesson becomes one atomic `Activity`.

Example:

```text
Mathematics — Group 1A — Session 1
```

becomes:

```text
Activity
```

with requirements:

```text
Teacher × 1
Student Group × 1
```

For example:

```text
ResourceRequirement

Teacher
quantity = 1
candidateResourceIds =
[Ana, Carlos]
```

and:

```text
ResourceRequirement

Student Group
quantity = 1
candidateResourceIds =
[Group 1A]
```

Repeated lessons are represented as separate atomic activities.

Example:

```text
Mathematics 1A — Session 1
Mathematics 1A — Session 2
Mathematics 1A — Session 3
```

---

# 12. Academic Time Slots

Teaching periods become generic `TimeSlot` objects.

Example:

```text
Monday 09:00–10:00
Monday 10:00–11:00
Monday 11:00–12:00
```

Each period has a concrete:

```text
start
end
```

date and time.

The Genetic Engine does not know that these represent school periods.

---

# 13. Academic Locations

Classrooms become generic `Location` objects.

Example:

```text
Room 101
Computer Lab
Physics Lab
```

Optional attributes may describe:

```text
capacity
type
equipment
```

when necessary.

---

# 14. Academic Default Constraints

The Academic Template proposes the following initial constraints.

## HARD

```text
NoOverlapConstraint
AvailabilityConstraint
RequiredResourceConstraint
```

These represent fundamental timetable requirements.

### NoOverlapConstraint

Prevents the same teacher or student group from being assigned to overlapping activities.

### AvailabilityConstraint

Prevents teachers or other resources from being assigned outside their availability.

### RequiredResourceConstraint

Ensures that every class receives the required resources.

---

## SOFT

```text
PreferredTimeSlotConstraint
MaxConsecutiveConstraint
```

These improve timetable quality.

### PreferredTimeSlotConstraint

Allows teachers or activities to express preferred teaching periods.

### MaxConsecutiveConstraint

Reduces excessive consecutive assignments.

---

# 15. Academic Default Configuration

The initial Academic Template may provide:

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

Default constraints:

```text
HARD
- No overlap
- Availability
- Required resource

SOFT
- Preferred time slot
- Maximum consecutive
```

Optimization preset:

```text
BALANCED
```

The user can modify applicable parameters before generation.

---

# 16. Academic Transformation Example

User configuration:

```text
Teacher:
Ana

Student Group:
1A

Class:
Mathematics

Teaching periods:
Monday 09:00
Monday 10:00

Classrooms:
Room 101
Room 102
```

Template output:

```text
PlanningProblem

ResourceTypes
├── Teacher
└── Student Group

Resources
├── Ana
└── Group 1A

Activities
└── Mathematics 1A
      ├── Teacher × 1
      └── Student Group × 1

TimeSlots
├── Monday 09:00
└── Monday 10:00

Locations
├── Room 101
└── Room 102

Constraints
├── NoOverlap
├── Availability
└── RequiredResource
```

The Genetic Engine receives only this generic object.

---

# 17. Work Shift Template

The Work Shift Template represents workforce scheduling.

It allows users to configure employees, working periods and assignments using workforce terminology while producing the same generic `PlanningProblem`.

---

# 18. Work Shift Mapping

The conceptual mapping is:

| Work Shift concept   | Generic model       |
| -------------------- | ------------------- |
| Employee             | Resource            |
| Work Assignment      | Activity            |
| Workplace            | Location            |
| Shift Period         | TimeSlot            |
| Employee requirement | ResourceRequirement |
| Workforce rule       | Constraint          |

Example:

```text
Reception Shift

Employee:
Ana

Workplace:
Main Office

Shift:
Monday 08:00–16:00
```

becomes:

```text
Activity
    ↓
ResourceRequirement
    ↓
Employee × 1

TimeSlot
    ↓
Monday 08:00–16:00

Location
    ↓
Main Office
```

---

# 19. Work Shift Template Configuration

Conceptually:

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

As with the Academic Template, these classes remain outside the generic domain.

---

# 20. Work Shift Resource Types

The default template defines:

```text
Employee
```

as its main `ResourceType`.

Example:

```text
ResourceType
employee
```

and:

```text
Resource

id = employee-ana
name = Ana
typeId = employee
```

Future versions could define other configurable resource types without modifying the Genetic Engine.

---

# 21. Work Shift Activities

A required work assignment becomes an `Activity`.

Example:

```text
Reception — Monday Morning
```

with:

```text
ResourceRequirement

Employee
quantity = 1
```

An assignment requiring multiple employees may use:

```text
quantity = 2
```

For example:

```text
Night support

Employee requirement
quantity = 2
```

Candidate employees can be restricted through:

```text
candidateResourceIds
```

---

# 22. Work Shift Time Slots

Work shifts become generic `TimeSlot` objects.

Examples:

```text
Monday 08:00–16:00
Monday 16:00–00:00
Tuesday 08:00–16:00
```

The Genetic Engine handles them exactly like Academic teaching periods.

---

# 23. Work Shift Locations

Workplaces become generic `Location` objects.

Examples:

```text
Main Office
Warehouse
Reception Desk
Branch A
```

Locations remain optional.

A planning problem with no relevant physical location may omit them.

---

# 24. Work Shift Default Constraints

The Work Shift Template proposes:

## HARD

```text
NoOverlapConstraint
AvailabilityConstraint
RequiredResourceConstraint
MaximumAssignmentsConstraint
```

### NoOverlapConstraint

Prevents an employee from receiving overlapping work assignments.

### AvailabilityConstraint

Ensures that employees are only assigned when available.

### RequiredResourceConstraint

Ensures every work assignment receives the required employees.

### MaximumAssignmentsConstraint

Limits the number of assignments given to a resource when configured.

---

## SOFT

```text
MinimumAssignmentsConstraint
MaxConsecutiveConstraint
PreferredTimeSlotConstraint
```

### MinimumAssignmentsConstraint

Supports workload distribution.

### MaxConsecutiveConstraint

Reduces excessive consecutive assignments.

### PreferredTimeSlotConstraint

Supports employee scheduling preferences.

---

# 25. Work Shift Default Configuration

The initial Work Shift Template provides:

```text
Resource type:
- Employee

Activity label:
- Work Assignment

Location label:
- Workplace

TimeSlot label:
- Shift
```

Default constraints:

```text
HARD
- No overlap
- Availability
- Required resource
- Maximum assignments

SOFT
- Minimum assignments
- Maximum consecutive
- Preferred time slot
```

Optimization preset:

```text
BALANCED
```

---

# 26. Work Shift Transformation Example

Input:

```text
Employees:
Ana
Carlos

Assignment:
Reception Morning

Shift:
Monday 08:00–16:00

Workplace:
Main Office
```

Template produces:

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
└── MaximumAssignments
```

Again:

```text
Genetic Engine
```

does not know this originated from a Work Shift Template.

---

# 27. Common Template Structure

Both templates follow the same architecture:

```text
Template-specific configuration
            ↓
PlanningTemplate
            ↓
Generic PlanningProblem
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

The output type is identical.

---

# 28. Template Factory

The application layer may expose a registry or factory.

Conceptually:

```kotlin
enum class PlanningTemplateType {
    ACADEMIC,
    WORK_SHIFT
}
```

and:

```kotlin
interface PlanningTemplateRegistry {

    fun get(
        type: PlanningTemplateType
    ): PlanningTemplate<*>
}
```

The UI selects a template.

The application resolves the corresponding implementation.

---

# 29. Template Selection Flow

The user flow becomes:

```text
New Planning
     ↓
Choose Template
     ↓
┌───────────────┐
│ Academic      │
│ Work Shift    │
└───────────────┘
     ↓
Template-specific configuration
     ↓
PlanningProblem
     ↓
Validation
     ↓
Genetic Engine
```

Templates therefore improve usability without affecting the optimization architecture.

---

# 30. Default Configuration Strategy

Defaults should simplify initial configuration without preventing customization.

Each template may provide:

```text
Default resource types
Default terminology
Default constraint catalogue
Default constraint weights
Default optimization preset
```

but the resulting `PlanningProblem` remains configurable.

Example:

```text
Academic Template
        ↓
Default NoOverlapConstraint
        ↓
User may keep/configure it
```

Templates are therefore:

```text
starting configurations
```

not:

```text
fixed planning models
```

---

# 31. Constraint Defaults

The initial constraint configuration is:

| Constraint                   | Academic | Work Shift | Type |
| ---------------------------- | -------- | ---------- | ---- |
| NoOverlapConstraint          | Default  | Default    | HARD |
| AvailabilityConstraint       | Default  | Default    | HARD |
| RequiredResourceConstraint   | Default  | Default    | HARD |
| MaximumAssignmentsConstraint | Optional | Default    | HARD |
| MinimumAssignmentsConstraint | Optional | Default    | SOFT |
| MaxConsecutiveConstraint     | Default  | Default    | SOFT |
| PreferredTimeSlotConstraint  | Default  | Default    | SOFT |
| CapacityConstraint           | Optional | Optional   | HARD |
| DifferentDayConstraint       | Optional | Optional   | SOFT |

Only constraints from the generic catalogue are used.

The templates do not implement separate academic or workforce constraint engines.

---

# 32. Default Weights

Initial default weights follow the fitness design.

For example:

```text
HARD

NoOverlap                1000
Availability             1000
RequiredResource         1000
MaximumAssignments       1000
```

and:

```text
SOFT

PreferredTimeSlot          10
MaxConsecutive               5
MinimumAssignments          10
```

These values remain configurable and experimentally adjustable.

---

# 33. Genetic Engine Independence

The Genetic Engine must depend only on generic abstractions.

Allowed dependencies include:

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

The Genetic Engine must not depend on:

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

Conceptually:

```text
Template
    ↓
PlanningProblem
    ↓
──────────── architecture boundary ────────────
    ↓
Genetic Engine
```

Once the `PlanningProblem` has been generated, the template is irrelevant to optimization.

---

# 34. Forbidden Genetic Engine Logic

The Genetic Engine must never contain code such as:

```kotlin
if (templateType == "ACADEMIC") {
    ...
}
```

or:

```kotlin
when (templateType) {
    "ACADEMIC" -> ...
    "WORK_SHIFT" -> ...
}
```

or:

```text
if Resource is Teacher
```

or:

```text
if Activity is WorkShift
```

All planning semantics must already be represented through generic:

```text
Resources
Activities
Candidate domains
Constraints
```

---

# 35. Template Type in PlanningProblem

`PlanningProblem` may retain:

```kotlin
val templateType: String?
```

as metadata.

Its purpose may include:

* UI display.
* Persistence.
* Reopening an existing planning configuration.
* Selecting appropriate terminology.

However:

> The Genetic Engine must never use `templateType` to change optimization behavior.

For the Genetic Engine:

```text
templateType = ignored
```

---

# 36. Adding Future Templates

The architecture must support adding another template without changing the Genetic Engine.

For example:

```text
Exam Scheduling Template
```

could map:

```text
Examiner
→ Resource

Exam
→ Activity

Exam Room
→ Location

Exam Period
→ TimeSlot
```

and still produce:

```text
PlanningProblem
```

Therefore:

```text
New Template
     ↓
Implement PlanningTemplate
     ↓
Produce PlanningProblem
     ↓
Existing Genetic Engine
```

No Genetic Engine modification should be required.

---

# 37. Custom Template

A fully custom template is considered a future capability.

Conceptually:

```text
Custom Template
      ↓
User defines:
- Resource types
- Activities
- Time slots
- Locations
- Constraints
      ↓
PlanningProblem
```

This is not required for the MVP.

However, the template architecture should not prevent it.

---

# 38. Layer Responsibility

The architecture separates responsibilities as follows.

## Template / Application Layer

Responsible for:

```text
Terminology
Template configuration
Default values
Default constraints
UI metadata
Transformation
```

## Domain Layer

Responsible for:

```text
PlanningProblem
Resource
Activity
TimeSlot
Location
Constraint
Schedule
```

## Genetic Engine

Responsible for:

```text
Encoding
Population
Selection
Crossover
Mutation
Evolution
Fitness integration
```

This separation ensures that planning domains and optimization remain decoupled.

---

# 39. Architecture Diagram

The intended architecture is:

```text
                ┌───────────────────┐
                │   Genetic Planner │
                └─────────┬─────────┘
                          │
             ┌────────────┴────────────┐
             │                         │
             ▼                         ▼
    Academic Template          Work Shift Template
             │                         │
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
                ┌────────────────┐
                │ Genetic Engine │
                └───────┬────────┘
                        │
                        ▼
                     Schedule
```

The important architectural boundary is:

```text
Templates
    ↓
PlanningProblem
────────────
Genetic Engine
```

---

# 40. Design Decisions

## PT1 — Templates are adapters

Planning templates translate domain-oriented configuration into the generic planning model.

## PT2 — PlanningProblem is the common contract

Every template produces:

```text
PlanningProblem
```

## PT3 — Academic Template is the primary MVP template

Academic Scheduling must be supported end-to-end.

## PT4 — Work Shift Template validates genericity

The Work Shift Template demonstrates that the same domain and Genetic Engine can support a second planning scenario.

## PT5 — Templates provide defaults

Templates may define:

```text
Resource types
Terminology
Constraints
Weights
Optimization preset
```

## PT6 — Defaults are configurable

Template defaults are starting values, not fixed behavior.

## PT7 — Templates may use template-specific configuration classes

These classes remain outside the generic domain.

## PT8 — Templates use the generic constraint catalogue

No separate template-specific constraint engine is created.

## PT9 — Genetic Engine is completely template-independent

No domain-specific type or conditional logic may exist inside the Genetic Engine.

## PT10 — templateType is metadata only

It may be stored in `PlanningProblem`, but it must not influence genetic optimization.

## PT11 — Future templates require no Genetic Engine modification

Adding another domain should require only a new template and corresponding UI/configuration.

---

# 41. Summary

The Planning Template architecture follows:

```text
Academic Template ───────┐
                         │
                         ▼
                  PlanningProblem
                         │
                         ▼
                  Genetic Engine
                         ▲
                         │
Work Shift Template ─────┘
```

The Academic Template maps:

```text
Teacher          → Resource
Student Group    → Resource
Class            → Activity
Classroom        → Location
Teaching Period  → TimeSlot
```

The Work Shift Template maps:

```text
Employee         → Resource
Work Assignment  → Activity
Workplace        → Location
Shift            → TimeSlot
```

Both produce exactly the same:

```text
PlanningProblem
```

The Genetic Engine only sees:

```text
Resources
Activities
ResourceRequirements
TimeSlots
Locations
Constraints
```

and remains completely independent from:

```text
Academic Scheduling
Work Shift Scheduling
```

This allows Genetic Planner to support specific and user-friendly planning scenarios while preserving a reusable and generic optimization engine.
