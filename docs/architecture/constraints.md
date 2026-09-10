# Genetic Planner — Constraint Model

## 1. Purpose

This document defines the conceptual constraint model used by **Genetic Planner**.

Constraints represent the rules used to evaluate whether a generated `Schedule` is valid and how desirable it is.

The constraint model must remain independent from specific planning domains so that the same Genetic Engine can be used for academic scheduling, work shifts, and future planning scenarios.

The central design goal is:

> **Constraints must be extensible without requiring modifications to the Genetic Engine.**

---

## 2. Constraint Concept

A `Constraint` represents a rule that can evaluate a complete `Schedule` and produce an evaluation result.

Conceptually:

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

The Genetic Engine does not need to know the concrete implementation of the constraint.

It only needs to execute:

```kotlin
constraint.evaluate(schedule)
```

and consume the returned result.

---

## 3. Constraint Types

Constraints are divided into two categories:

```kotlin
enum class ConstraintType {
    HARD,
    SOFT
}
```

---

## 4. Hard Constraints

A `HARD` constraint represents a rule that should not be violated in a valid planning solution.

Examples include:

* A resource cannot participate in overlapping activities.
* A resource must respect its availability.
* An activity must receive the required resources.
* A resource must not exceed a configured assignment limit.

A schedule containing hard constraint violations may still exist as a candidate solution during the genetic optimization process, but it is considered infeasible.

This distinction is important because the Genetic Engine may need to explore infeasible intermediate solutions before reaching a feasible one.

Conceptually:

```text
Hard violation count = 0
        ↓
Feasible schedule

Hard violation count > 0
        ↓
Infeasible schedule
```

Hard constraints therefore have a strong influence on the quality of a candidate schedule.

---

## 5. Soft Constraints

A `SOFT` constraint represents a preference rather than an absolute requirement.

Violating a soft constraint does not make a schedule invalid, but it reduces its quality.

Examples include:

* Prefer morning assignments.
* Minimize consecutive assignments.
* Balance workload between resources.
* Prefer specific time periods.

Conceptually:

```text
SOFT constraint satisfied
        ↓
Better schedule

SOFT constraint violated
        ↓
Valid but less desirable schedule
```

The Genetic Engine should therefore prefer schedules with fewer or less severe soft constraint violations once hard feasibility has been achieved.

---

## 6. Penalties

Constraint violations are represented through penalties.

A penalty is a non-negative numeric value that expresses how strongly a schedule violates a constraint.

Conceptually:

```text
0.0
    No violation

> 0.0
    Constraint violation
```

A constraint may produce:

* No penalty.
* One penalty.
* Multiple penalties.
* A penalty proportional to the severity of the violation.

For example:

```text
NoOverlap

0 overlaps
    → penalty = 0

1 overlap
    → penalty = 1

3 overlaps
    → penalty = 3
```

Another constraint may use a different severity model.

For example:

```text
MaximumAssignments

Maximum allowed = 5

Actual assignments = 5
    → penalty = 0

Actual assignments = 6
    → penalty = 1

Actual assignments = 8
    → penalty = 3
```

The exact penalty calculation belongs to each concrete constraint.

The global combination of penalties into the final fitness value will be defined separately in the fitness and weighting design.

---

## 7. Configurable Weight

Every constraint has a configurable `weight`.

```kotlin
val weight: Double
```

The weight represents the relative importance of the constraint when its penalty contributes to the optimization process.

Conceptually:

```text
raw penalty × weight = weighted penalty
```

For example:

```text
Raw penalty = 2
Weight      = 5

Weighted penalty = 10
```

The weight allows two constraints of the same type to have different importance.

Example:

```text
Preferred morning
weight = 2

Avoid consecutive assignments
weight = 5
```

In this case, avoiding consecutive assignments has a greater influence on optimization.

---

## 8. Weight Rules

The following rules apply:

```text
weight >= 0
```

A negative weight is not allowed because penalties must not improve a solution.

A weight of:

```text
0
```

effectively disables the influence of the constraint while preserving its configuration.

For the MVP, sensible default weights should be provided by templates so users do not need to manually configure every value.

The detailed weighting strategy will be defined in the separate task:

```text
Design constraint evaluation and weighting
```

---

## 9. Hard vs Soft Weighting

Both hard and soft constraints expose a `weight` property for consistency and configurability.

However, their semantic role differs.

For `SOFT` constraints, the weight represents preference importance.

For `HARD` constraints, the weight represents the severity applied during optimization when the rule is violated.

A hard constraint remains hard regardless of its weight.

Therefore:

```text
type = HARD
weight = 1
```

does not make the constraint less mandatory than:

```text
type = HARD
weight = 100
```

The `type` determines feasibility.

The `weight` determines optimization pressure.

---

## 10. Constraint Evaluation Result

Constraint evaluation should return more information than a simple Boolean.

A Boolean such as:

```kotlin
true
false
```

would indicate whether the rule is satisfied, but would not provide enough information for:

* Fitness calculation.
* Error explanations.
* Results visualization.
* Debugging.
* Experimental evaluation.

The proposed result is therefore:

```kotlin
data class ConstraintResult(
    val constraintId: String,
    val type: ConstraintType,
    val violations: List<ConstraintViolation>,
    val rawPenalty: Double,
    val weightedPenalty: Double
)
```

---

## 11. Constraint Violation

A `ConstraintViolation` represents one concrete violation detected during evaluation.

Conceptually:

```kotlin
data class ConstraintViolation(
    val message: String,
    val penalty: Double,
    val relatedEntityIds: Set<String> = emptySet()
)
```

The exact implementation may evolve, but each violation should provide enough information to explain why a candidate schedule has been penalized.

Examples:

```text
Teacher Ana has overlapping assignments on Monday at 09:00.
```

```text
Employee Laura has 7 assignments but the maximum allowed is 5.
```

This information can later be used in the Results interface.

---

## 12. Result Semantics

A successful evaluation with no violations may return:

```text
violations = []
rawPenalty = 0
weightedPenalty = 0
```

A violated constraint may return:

```text
violations = [ ... ]
rawPenalty = 3
weight = 5
weightedPenalty = 15
```

The relationship is conceptually:

```text
Constraint
    │
    ▼
evaluate(schedule)
    │
    ▼
ConstraintResult
    │
    ├── Violations
    ├── Raw Penalty
    └── Weighted Penalty
```

---

## 13. Evaluation Scope

For the MVP, constraints evaluate the complete `Schedule`.

The public contract therefore remains:

```kotlin
fun evaluate(schedule: Schedule): ConstraintResult
```

This provides a simple and uniform interface for the Genetic Engine.

Individual constraint implementations may internally inspect:

* Assignments.
* Activities.
* Resources.
* Time slots.
* Locations.
* Planning problem configuration.

For example:

```text
NoOverlapConstraint
        ↓
Inspect assignments grouped by resource
        ↓
Compare assigned time slots
```

The Genetic Engine does not need to know how this evaluation is performed.

---

## 14. Access to Planning Problem Data

Some constraints require information that is not contained directly in `Schedule`.

For example:

```text
AvailabilityConstraint
```

needs resource availability configuration.

Similarly:

```text
MaximumAssignmentsConstraint
```

needs the configured assignment limit.

Therefore, a concrete constraint should contain the parameters required to perform its own evaluation.

For example, conceptually:

```kotlin
class MaximumAssignmentsConstraint(
    override val id: String,
    override val name: String,
    override val type: ConstraintType,
    override val weight: Double,
    val resourceId: String,
    val maximumAssignments: Int
) : Constraint
```

The constraint definition itself therefore carries the rule configuration.

This avoids forcing the Genetic Engine to interpret constraint-specific parameters.

---

## 15. Constraint Parameters

Different constraints require different parameters.

Examples:

```text
AvailabilityConstraint
    resourceId
    availableTimeSlotIds

MaximumAssignmentsConstraint
    resourceId
    maximumAssignments

PreferredTimeSlotConstraint
    resourceId
    preferredTimeSlotIds
```

The common `Constraint` interface should not contain every possible constraint parameter.

Instead, each concrete constraint implementation defines only the parameters it requires.

This keeps the model extensible.

---

## 16. Extensibility

A fundamental requirement is that new constraints can be added without changing the Genetic Engine.

The Genetic Engine should operate only against the `Constraint` abstraction.

Conceptually:

```kotlin
for (constraint in planningProblem.constraints) {
    val result = constraint.evaluate(schedule)
}
```

The engine should never contain logic such as:

```kotlin
if (constraint is NoOverlapConstraint) {
    ...
}

if (constraint is AvailabilityConstraint) {
    ...
}
```

or:

```kotlin
when (constraint.typeName) {
    "NO_OVERLAP" -> ...
    "AVAILABILITY" -> ...
}
```

Constraint-specific behaviour belongs inside each constraint implementation.

---

## 17. Adding a New Constraint

For example, a future constraint:

```text
MinimumRestTimeConstraint
```

should only require:

1. Creating the new constraint implementation.
2. Defining its parameters.
3. Implementing `evaluate(schedule)`.
4. Registering/exposing it through the application or template layer when required.

The Genetic Engine remains unchanged.

Conceptually:

```text
Constraint
   ▲
   │
   ├── NoOverlapConstraint
   ├── AvailabilityConstraint
   ├── MaximumAssignmentsConstraint
   ├── PreferredTimeSlotConstraint
   │
   └── MinimumRestTimeConstraint
```

This follows the Open/Closed Principle:

> The constraint system is open for extension but closed for modification of the Genetic Engine.

---

## 18. Genetic Engine Boundary

The Genetic Engine should only depend on the generic contract:

```text
Schedule
    │
    ▼
List<Constraint>
    │
    ▼
evaluate()
    │
    ▼
ConstraintResult
```

The engine may aggregate:

```text
Hard violations
Soft violations
Raw penalties
Weighted penalties
```

but it must not understand the internal semantics of individual constraints.

This separation makes it possible to support new domains and planning rules without coupling them to the genetic algorithm implementation.

---

## 19. Constraint Model Overview

The conceptual model is:

```text
                 Constraint
                     │
          ┌──────────┴──────────┐
          │                     │
        HARD                   SOFT
          │                     │
          └──────────┬──────────┘
                     │
                     ▼
             evaluate(Schedule)
                     │
                     ▼
             ConstraintResult
                     │
          ┌──────────┼───────────┐
          │          │           │
     Violations  Raw Penalty  Weighted Penalty
```

Each violation may contain:

```text
ConstraintViolation
├── Message
├── Penalty
└── Related entities
```

---

## 20. Conceptual Kotlin Model

The resulting conceptual model is:

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

data class ConstraintResult(
    val constraintId: String,
    val type: ConstraintType,
    val violations: List<ConstraintViolation>,
    val rawPenalty: Double,
    val weightedPenalty: Double
)

data class ConstraintViolation(
    val message: String,
    val penalty: Double,
    val relatedEntityIds: Set<String> = emptySet()
)
```

This model is conceptual and may be refined during implementation if required by the Genetic Engine or Jenetics integration.

---

## 21. Design Decisions

### C1 — Constraints evaluate schedules

All constraints expose the same evaluation operation:

```kotlin
evaluate(schedule)
```

This keeps the Genetic Engine independent from constraint-specific logic.

### C2 — Hard and soft constraints share one abstraction

Both types implement the same `Constraint` interface.

Their different semantics are represented by `ConstraintType`.

### C3 — Violations produce penalties

Constraints are not evaluated using only a Boolean result.

The penalty provides information about the amount or severity of the violation.

### C4 — Weight is configurable

Each constraint contains a configurable weight that controls its influence during optimization.

### C5 — Type and weight have different meanings

`ConstraintType` determines whether a violation affects feasibility.

`weight` determines the optimization importance of the violation.

### C6 — Evaluation results are explainable

`ConstraintResult` contains individual violations rather than only a numeric score.

This supports fitness calculation, debugging, evaluation, and user-facing explanations.

### C7 — Constraint-specific parameters belong to concrete constraints

The generic interface does not know parameters such as resource availability or assignment limits.

### C8 — Genetic Engine depends only on Constraint

Adding a new constraint implementation must not require modifying the Genetic Engine.

---

## 22. Scope Boundary

This document defines:

* The `Constraint` abstraction.
* HARD and SOFT semantics.
* Penalties.
* Configurable weights.
* Evaluation results.
* Extensibility principles.

It does **not** define:

* The complete catalogue of concrete constraints.
* Exact penalty values for each constraint.
* Global weighting formulas.
* The final fitness function.

These aspects are addressed separately in:

```text
Define initial constraint catalogue
Design constraint evaluation and weighting
Design fitness function
```
