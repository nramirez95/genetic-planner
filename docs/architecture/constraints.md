# Genetic Planner — Constraint Model

## 1. Purpose

This document describes the constraint model implemented by **Genetic Planner**.

Constraints represent the rules used to evaluate whether a generated
`Schedule` is feasible and how desirable the solution is.

The constraint model remains independent from specific planning domains
so that the same Genetic Engine can be used for Academic Scheduling,
Work Shift Scheduling, and future planning scenarios.

The central design principle is:

> **Constraints must be extensible without requiring modifications to the Genetic Engine.**

---

## 2. Constraint Concept

A `Constraint` represents a planning rule capable of evaluating a
complete `Schedule`.

The implemented contract is:

```kotlin
interface Constraint {
    val id: String
    val name: String
    val type: ConstraintType
    val weight: Double

    fun evaluate(schedule: Schedule): ConstraintResult
}
```

Each constraint therefore defines:

* Its identity.
* Its descriptive name.
* Whether it is `HARD` or `SOFT`.
* Its configurable weight.
* Its own evaluation logic.

The Genetic Engine does not need to understand the concrete
implementation of a constraint.

It operates through the common abstraction:

```kotlin
constraint.evaluate(schedule)
```

and consumes the resulting `ConstraintResult`.

---

## 3. Constraint Types

Constraints are divided into two categories:

```kotlin
enum class ConstraintType {
    HARD,
    SOFT
}
```

The type determines the planning semantics of the constraint.

```text
Constraint
    │
    ├── HARD
    │     └── affects feasibility
    │
    └── SOFT
          └── affects schedule quality
```

Constraint type and constraint weight represent different concepts and
must not be confused.

---

## 4. HARD Constraints

A `HARD` constraint represents a mandatory planning rule.

A Schedule containing one or more HARD constraint violations is
considered infeasible.

The currently implemented HARD constraints are:

```text
NoOverlapConstraint
AvailabilityConstraint
RequiredResourceConstraint
LocationCapacityConstraint
```

Examples of HARD planning rules include:

* A Resource cannot participate in overlapping Activities.
* A Resource must respect its configured availability.
* An Activity must receive the Resources required by its
  `ResourceRequirement` definitions.
* A selected Location must provide sufficient capacity.

A Schedule containing HARD violations may still exist as a candidate
solution during genetic optimization.

This is intentional:

```text
Candidate Schedule
        │
        ├── feasible
        │
        └── infeasible
                │
                ▼
        Genetic evolution
```

The Genetic Algorithm may explore infeasible intermediate solutions while
penalties guide evolution toward better candidates.

Feasibility is determined from violations:

```text
HARD violations = 0
        ↓
Feasible Schedule

HARD violations > 0
        ↓
Infeasible Schedule
```

---

## 5. SOFT Constraints

A `SOFT` constraint represents a preference or quality objective.

Violating a SOFT constraint does not make a Schedule infeasible.
Instead, the violation contributes to the optimization penalty.

The currently implemented SOFT constraints are:

```text
PreferredTimeSlotConstraint
MaxConsecutiveConstraint
BalancedWorkloadConstraint
```

Examples include:

* Prefer particular TimeSlots for an Activity.
* Avoid excessive consecutive assignments.
* Balance workload between Resources.

Conceptually:

```text
SOFT constraint satisfied
        ↓
Lower penalty

SOFT constraint violated
        ↓
Higher penalty
        ↓
Schedule remains feasible
```

SOFT constraints therefore influence which feasible solutions are
preferred by the optimization process.

---

## 6. Constraint Violations

A `ConstraintViolation` represents one concrete rule violation detected
during evaluation.

The implemented model is:

```kotlin
data class ConstraintViolation(
    val message: String,
    val penalty: Double,
    val relatedEntityIds: Set<String> = emptySet()
)
```

Each violation provides:

* A human-readable explanation.
* A raw penalty representing its cost or severity.
* The identifiers of relevant planning entities when applicable.

For example:

```text
Resource worker-1 has overlapping assignments.
```

or:

```text
Location room-1 has insufficient capacity.
```

The violation model supports:

* Fitness calculation.
* Debugging.
* Automated testing.
* Experimental evaluation.
* Future user-facing explanations in the Results interface.

---

## 7. Penalties

Every violation has a non-negative penalty.

Conceptually:

```text
penalty = 0
    → no cost

penalty > 0
    → violation cost
```

Different constraints may use different penalty semantics.

A constraint may produce:

* No violations.
* One violation.
* Multiple violations.
* Violations with severity-dependent penalties.

For example, a simple violation may use:

```text
raw penalty = 1
```

while `LocationCapacityConstraint` can use the capacity shortage as the
violation penalty:

```text
required capacity = 30
actual capacity   = 20

shortage = 10

raw penalty = 10
```

The calculation of each individual violation penalty belongs to the
concrete constraint implementation.

---

## 8. Configurable Weight

Every Constraint exposes:

```kotlin
val weight: Double
```

The weight determines how strongly the constraint penalty influences
optimization.

The relationship is:

```text
weightedPenalty =
rawPenalty × weight
```

For example:

```text
rawPenalty = 2
weight     = 5

weightedPenalty = 10
```

The same formula applies to both HARD and SOFT constraints.

There is no hidden global HARD multiplier.

---

## 9. Type vs Weight

`ConstraintType` and `weight` have different responsibilities.

### Type

The type determines planning semantics:

```text
HARD violation
→ Schedule is infeasible

SOFT violation
→ Schedule remains feasible
```

### Weight

The weight determines optimization impact:

```text
rawPenalty × weight
→ weightedPenalty
```

Therefore:

```text
type = HARD
weight = 1
```

and:

```text
type = HARD
weight = 100
```

are both mandatory constraints.

The second simply creates greater optimization pressure when violated.

The fundamental rule is:

> **Constraint type determines planning semantics; weight determines optimization impact.**

---

## 10. Zero Weight

The constraint evaluation model supports:

```text
weight >= 0
```

Negative weights are invalid because a violation must not improve a
solution.

A zero weight produces:

```text
weightedPenalty = 0
```

but does not change the constraint type.

This distinction is particularly important for HARD constraints.

For example:

```text
type = HARD
violations = 1
weight = 0
```

produces no weighted fitness penalty, but the Schedule remains
infeasible because feasibility depends on HARD violations rather than
the numeric penalty.

Therefore:

> **Fitness measures optimization penalty, while HARD constraint violations determine feasibility.**

---

## 11. ConstraintResult

Constraint evaluation returns detailed information rather than a simple
Boolean.

The implemented model is:

```kotlin
data class ConstraintResult(
    val constraintId: String,
    val type: ConstraintType,
    val violationDetails: List<ConstraintViolation>,
    val weight: Double
) {
    val violations: Int
        get() = violationDetails.size

    val rawPenalty: Double
        get() = violationDetails.sumOf { it.penalty }

    val weightedPenalty: Double
        get() = rawPenalty * weight
}
```

Only the fundamental evaluation information is stored:

```text
constraintId
type
violationDetails
weight
```

The remaining values are derived:

```text
violations
    = number of violationDetails

rawPenalty
    = sum of violation penalties

weightedPenalty
    = rawPenalty × weight
```

This avoids storing redundant values that could become inconsistent.

---

## 12. ConstraintResult Semantics

A constraint with no violations produces conceptually:

```text
violationDetails = []
violations        = 0
rawPenalty        = 0
weightedPenalty   = 0
```

A violated constraint may produce:

```text
violationDetails =
    violation A → penalty 1
    violation B → penalty 2

violations = 2

rawPenalty =
1 + 2
= 3

weight = 5

weightedPenalty =
3 × 5
= 15
```

The complete relationship is:

```text
Constraint
    │
    ▼
evaluate(Schedule)
    │
    ▼
ConstraintResult
    │
    ├── violationDetails
    │       │
    │       └── ConstraintViolation
    │               ├── message
    │               ├── penalty
    │               └── relatedEntityIds
    │
    ├── violations
    ├── rawPenalty
    └── weightedPenalty
```

---

## 13. Evaluation Scope

Constraints evaluate a complete `Schedule`.

The common public contract remains:

```kotlin
fun evaluate(schedule: Schedule): ConstraintResult
```

Individual constraint implementations may also contain the planning
information required to perform their evaluation.

For example:

```text
AvailabilityConstraint
        │
        ├── configured availability
        │
        └── Schedule
                │
                ▼
             evaluate
```

or:

```text
NoOverlapConstraint
        │
        ├── relevant TimeSlots
        │
        └── Schedule
                │
                ▼
      compare Resource assignments
```

The Genetic Engine does not need to understand how each concrete
constraint performs its evaluation.

---

## 14. Constraint Parameters

Different constraints require different configuration parameters.

These parameters belong to the concrete constraint implementation rather
than to the generic `Constraint` interface.

Conceptually:

```text
Constraint
│
├── common:
│     id
│     name
│     type
│     weight
│
└── concrete implementation:
      constraint-specific configuration
```

Examples include:

```text
AvailabilityConstraint
    → Resource availability by TimeSlot

PreferredTimeSlotConstraint
    → preferred TimeSlots by Activity

MaxConsecutiveConstraint
    → configured maximum consecutive assignments

BalancedWorkloadConstraint
    → Resources included in workload evaluation
```

This keeps the generic interface small and extensible.

---

## 15. Constraint Evaluation Aggregation

Individual constraint results are aggregated by
`ConstraintEvaluator`.

Conceptually:

```text
Schedule
    │
    ▼
Constraint 1 ──→ ConstraintResult
Constraint 2 ──→ ConstraintResult
Constraint 3 ──→ ConstraintResult
    │
    ▼
ConstraintEvaluator
    │
    ▼
ScheduleEvaluation
```

`ConstraintEvaluator` separates weighted penalties according to
constraint type:

```text
HARD weighted penalties
        ↓
hardPenalty

SOFT weighted penalties
        ↓
softPenalty
```

The complete fitness is:

```text
fitness =
hardPenalty + softPenalty
```

Feasibility is derived separately from HARD violations.

Detailed aggregation semantics are documented in:

```text
docs/architecture/constraint-evaluation.md
```

---

## 16. Relationship with Fitness

Constraint evaluation and fitness calculation are deliberately separated
from Jenetics.

The flow is:

```text
Schedule
    │
    ▼
Constraint.evaluate()
    │
    ▼
ConstraintResult
    │
    ▼
ConstraintEvaluator
    │
    ▼
ScheduleEvaluation
    │
    ├── hardPenalty
    ├── softPenalty
    ├── totalPenalty
    ├── fitness
    └── feasible
```

Jenetics receives only the resulting numeric fitness and minimizes it.

The planning domain therefore remains independent from the Genetic
Algorithm library.

---

## 17. Extensibility

A fundamental architectural requirement is that new constraints can be
added without modifying the Genetic Engine.

The Genetic Engine operates only against the `Constraint` abstraction.

Conceptually:

```kotlin
for (constraint in planningProblem.constraints) {
    constraint.evaluate(schedule)
}
```

The engine does not contain constraint-specific branching such as:

```kotlin
if (constraint is NoOverlapConstraint) {
    // constraint-specific evaluation
}
```

or:

```kotlin
when (constraint) {
    // inspect concrete constraint types
}
```

Constraint-specific behaviour remains inside each implementation.

---

## 18. Adding a New Constraint

A future constraint such as:

```text
MinimumRestTimeConstraint
```

would require:

1. Creating the new Constraint implementation.
2. Defining its configuration parameters.
3. Implementing `evaluate(schedule)`.
4. Producing the corresponding `ConstraintViolation` objects.
5. Exposing the constraint through the application or template layer
   when required.

No changes to the Genetic Engine are required.

Conceptually:

```text
                     Constraint
                         ▲
                         │
        ┌────────────────┼────────────────┐
        │                │                │
NoOverlapConstraint  ...       MinimumRestTimeConstraint
```

This follows the Open/Closed Principle:

> **The constraint system is open for extension while the Genetic Engine remains closed to constraint-specific modification.**

---

## 19. Genetic Engine Boundary

The Genetic Engine depends on generic planning abstractions:

```text
Schedule
    │
    ▼
List<Constraint>
    │
    ▼
ConstraintEvaluator
    │
    ▼
ScheduleEvaluation
    │
    ▼
Fitness
```

It does not need to understand:

* Why a Resource is unavailable.
* Why a particular TimeSlot is preferred.
* Why a Location requires a particular capacity.
* What a Resource represents in a specific planning domain.

This separation allows new planning rules and templates to be introduced
without coupling them to the Genetic Algorithm implementation.

---

## 20. Constraint Model Overview

The implemented model can be summarized as:

```text
                         Constraint
                             │
                ┌────────────┴────────────┐
                │                         │
              HARD                       SOFT
                │                         │
                └────────────┬────────────┘
                             │
                             ▼
                     evaluate(Schedule)
                             │
                             ▼
                    ConstraintResult
                             │
          ┌──────────────────┼──────────────────┐
          │                  │                  │
 violationDetails       rawPenalty       weightedPenalty
          │
          ▼
 ConstraintViolation
 ├── message
 ├── penalty
 └── relatedEntityIds
```

At aggregate level:

```text
ConstraintResults
        │
        ▼
ConstraintEvaluator
        │
        ▼
ScheduleEvaluation
        │
        ├── hardPenalty
        ├── softPenalty
        ├── totalPenalty
        ├── fitness
        └── feasible
```

---

## 21. Implemented Kotlin Model

The core implemented constraint model is:

```kotlin
enum class ConstraintType {
    HARD,
    SOFT
}

interface Constraint {
    val id: String
    val name: String
    val type: ConstraintType
    val weight: Double

    fun evaluate(schedule: Schedule): ConstraintResult
}

data class ConstraintViolation(
    val message: String,
    val penalty: Double,
    val relatedEntityIds: Set<String> = emptySet()
)

data class ConstraintResult(
    val constraintId: String,
    val type: ConstraintType,
    val violationDetails: List<ConstraintViolation>,
    val weight: Double
) {
    val violations: Int
        get() = violationDetails.size

    val rawPenalty: Double
        get() = violationDetails.sumOf { it.penalty }

    val weightedPenalty: Double
        get() = rawPenalty * weight
}
```

The constraint model contains no dependency on:

```text
Spring
JPA
Jenetics
Academic Scheduling
Work Shift Scheduling
```

---

## 22. Design Decisions

### C1 — Constraints evaluate Schedules

All constraints expose:

```kotlin
evaluate(schedule)
```

This provides a uniform evaluation boundary.

### C2 — HARD and SOFT share one abstraction

Both types implement the same `Constraint` interface.

Their semantic difference is represented by `ConstraintType`.

### C3 — Violations are explicit

Constraint evaluation produces `ConstraintViolation` objects rather than
only a Boolean or numeric value.

This enables explainability and detailed evaluation.

### C4 — Penalties belong to violations

Each violation defines its own raw penalty.

This allows different constraints to represent violation severity
appropriately.

### C5 — Weight is configurable

Every Constraint exposes a weight controlling its optimization impact.

### C6 — Type and weight are independent

`ConstraintType` determines feasibility semantics.

`weight` determines optimization pressure.

### C7 — Derived values are not stored

`violations`, `rawPenalty`, and `weightedPenalty` are derived from the
underlying violation details and weight.

This prevents redundant state.

### C8 — Feasibility is not part of ConstraintResult

Individual Constraint results do not decide overall Schedule
feasibility.

Feasibility is derived at `ScheduleEvaluation` level from all HARD
constraint results.

### C9 — Constraint-specific parameters belong to implementations

The common abstraction contains only properties shared by all
constraints.

### C10 — Constraints remain independent from Jenetics

The domain model evaluates Schedules without exposing genetic library
types.

### C11 — Genetic Engine depends on abstractions

Adding a new constraint does not require changes to the Genetic Engine.

---

## 23. Deviations from Initial Design

The constraint model was refined during implementation.

### ConstraintResult

The initial design proposed storing:

```text
violations
rawPenalty
weightedPenalty
```

directly in `ConstraintResult`.

The implemented model instead stores:

```text
violationDetails
weight
```

and derives:

```text
violations
rawPenalty
weightedPenalty
```

This reduces duplicated state and guarantees consistency between
violation details and calculated penalties.

### Feasibility

The implemented model explicitly separates:

```text
fitness
```

from:

```text
feasibility
```

Fitness depends on weighted penalties.

Feasibility depends on the existence of HARD violations.

This distinction means that a zero-weight HARD constraint can still make
a Schedule infeasible.

### Constraint Catalogue

The examples in the initial design included
`MaximumAssignmentsConstraint`.

The implemented core catalogue instead contains:

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

The complete implemented catalogue is documented in:

```text
docs/architecture/constraint-catalogue.md
```

---

## 24. Scope Boundary

This document defines:

* The `Constraint` abstraction.
* HARD and SOFT semantics.
* `ConstraintViolation`.
* `ConstraintResult`.
* Raw and weighted penalties.
* Configurable weights.
* Extensibility principles.
* The boundary between constraints and the Genetic Engine.

Detailed topics are documented separately:

```text
Concrete constraint catalogue
→ docs/architecture/constraint-catalogue.md

Constraint aggregation and ScheduleEvaluation
→ docs/architecture/constraint-evaluation.md

Fitness calculation
→ docs/algorithm/fitness.md

Genetic Algorithm implementation
→ docs/algorithm/genetic-algorithm.md
```

This separation keeps the constraint model focused on the common
abstractions while allowing the concrete catalogue, evaluation process,
and optimization strategy to evolve independently.