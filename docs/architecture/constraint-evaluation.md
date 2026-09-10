# Genetic Planner — Constraint Evaluation and Weighting

## 1. Purpose

This document defines how Genetic Planner evaluates constraints and aggregates their penalties.

The goal is to translate constraint violations into numerical values that can later be used by the Genetic Algorithm fitness function.

The evaluation strategy must:

* Strongly prioritize HARD constraint satisfaction.
* Allow SOFT constraints to influence solution quality.
* Support configurable constraint weights.
* Preserve enough information to explain violations.
* Remain independent from specific planning domains.
* Allow new constraints to be introduced without modifying the Genetic Engine.

The conceptual strategy is:

```text
HARD violation
      ↓
High penalty

SOFT violation
      ↓
Penalty × weight
```

---

# 2. Evaluation Overview

Each configured constraint evaluates a candidate `Schedule`.

```text
Schedule
   │
   ▼
Constraint.evaluate()
   │
   ▼
ConstraintResult
```

The Genetic Engine evaluates every constraint associated with the `PlanningProblem`.

Conceptually:

```kotlin
for (constraint in planningProblem.constraints) {
    val result = constraint.evaluate(schedule)
}
```

The individual results are then aggregated into a global evaluation.

```text
ConstraintResult 1 ──┐
ConstraintResult 2 ──┤
ConstraintResult 3 ──┤
ConstraintResult N ──┘
          │
          ▼
ScheduleEvaluation
```

---

# 3. Raw Penalty

Each constraint is responsible for calculating its own raw penalty.

The raw penalty represents the severity of the violation before applying global weighting rules.

The general rule is:

```text
No violation
    ↓
rawPenalty = 0

Violation
    ↓
rawPenalty > 0
```

The exact calculation depends on the constraint.

For example:

```text
MaximumAssignments

maximum = 5
actual  = 8

rawPenalty = 3
```

because the resource exceeds the configured maximum by three assignments.

Another example:

```text
NoOverlap

2 overlapping assignment pairs

rawPenalty = 2
```

The constraint implementation therefore determines the meaning of its raw penalty.

---

# 4. Weighting System

Each constraint contains a configurable weight:

```kotlin
val weight: Double
```

The weight represents the relative importance of the constraint during optimization.

The general weighted penalty is:

```text
weightedPenalty = rawPenalty × weight
```

For example:

```text
rawPenalty = 2
weight = 3

weightedPenalty = 6
```

The weight must satisfy:

```text
weight >= 0
```

Negative weights are not allowed because a violation must never improve a solution.

---

# 5. HARD Constraint Strategy

HARD constraints represent mandatory planning rules.

A violation makes the candidate schedule infeasible.

Examples include:

```text
NoOverlap
Availability
RequiredResource
MaximumAssignments
Capacity
```

The evaluation strategy must ensure that HARD violations dominate SOFT preferences.

For this reason, HARD constraints receive an additional high penalty factor.

Conceptually:

```text
hardWeightedPenalty =
    rawPenalty
    × weight
    × HARD_PENALTY_FACTOR
```

where:

```text
HARD_PENALTY_FACTOR >> 1
```

For example:

```text
HARD_PENALTY_FACTOR = 1000
```

may be used as an initial value.

Example:

```text
NoOverlapConstraint

rawPenalty = 2
weight = 1
HARD_PENALTY_FACTOR = 1000

hardWeightedPenalty = 2000
```

This makes a hard violation substantially more expensive than normal preference violations.

---

# 6. HARD Feasibility

Penalty magnitude alone does not determine whether a schedule is feasible.

Feasibility is explicitly determined using HARD violations.

```text
hardViolationCount == 0
        ↓
Feasible schedule

hardViolationCount > 0
        ↓
Infeasible schedule
```

This distinction is fundamental.

For example:

```text
Schedule A
Hard violations = 1
Soft penalty = 0

Schedule B
Hard violations = 0
Soft penalty = 500
```

Schedule B is feasible while Schedule A is not.

Even if numeric penalties later become close because of configuration choices, the feasibility state remains explicit.

---

# 7. Why HARD and SOFT Must Remain Separate

A single undifferentiated penalty value could create undesirable situations.

For example:

```text
Schedule A
1 HARD violation
0 SOFT violations

Schedule B
0 HARD violations
100 SOFT violations
```

If all penalties were treated identically, the algorithm might incorrectly prefer Schedule A.

Genetic Planner therefore keeps separate aggregated values:

```text
hardPenalty
softPenalty
```

and separate violation counters:

```text
hardViolationCount
softViolationCount
```

This preserves the semantic difference between feasibility and preference quality.

---

# 8. HARD Penalty Factor

The initial strategy introduces a global constant:

```kotlin
HARD_PENALTY_FACTOR
```

Conceptually:

```kotlin
const val HARD_PENALTY_FACTOR = 1000.0
```

The exact value is not considered final at this design stage.

It must be large enough that HARD violations strongly dominate SOFT penalties during optimization.

Its final calibration will be validated through experiments.

The important design decision is:

> HARD constraints receive a penalty multiplier significantly larger than SOFT constraints.

---

# 9. SOFT Constraint Strategy

SOFT constraints represent preferences.

Their violation does not make a schedule infeasible.

Examples include:

```text
MinimumAssignments
MaxConsecutive
PreferredTimeSlot
DifferentDay
```

Their penalty is calculated as:

```text
softWeightedPenalty =
    rawPenalty × weight
```

No additional HARD factor is applied.

Example:

```text
PreferredTimeSlotConstraint

rawPenalty = 3
weight = 2

softWeightedPenalty = 6
```

Another preference may be more important:

```text
MaxConsecutiveConstraint

rawPenalty = 2
weight = 5

softWeightedPenalty = 10
```

The Genetic Engine should therefore prefer improving the second constraint when all other conditions are equal.

---

# 10. Weight Interpretation

Weights provide relative importance between constraints.

Example:

```text
PreferredTimeSlot
weight = 1

MaxConsecutive
weight = 5
```

A violation of `MaxConsecutive` has five times the optimization influence of an equivalent raw violation of `PreferredTimeSlot`.

Weights therefore allow templates and users to express preference priorities.

---

# 11. Default Weights

For the MVP, templates should provide sensible default values.

A simple initial strategy is:

```text
HARD constraints
default weight = 1.0

SOFT constraints
default weight = 1.0
```

Users should not be required to understand weighting in order to generate a schedule.

Advanced configuration may allow users to modify SOFT weights.

For example:

```text
Preferred morning
Importance:
[ Low ] [ Medium ] [ High ]
```

which may internally map to numerical values such as:

```text
Low     → 1
Medium  → 3
High    → 5
```

The exact user-facing mapping may be refined during UX implementation.

---

# 12. HARD Weight Configuration

Although HARD constraints expose a `weight`, their type remains authoritative.

For example:

```text
NoOverlap
type = HARD
weight = 0.5
```

is still a HARD constraint.

Its violation still affects feasibility.

The weight only adjusts optimization pressure.

Therefore:

```text
ConstraintType
        ↓
determines feasibility

Weight
        ↓
determines optimization importance
```

A HARD constraint must never become SOFT simply because it has a low weight.

---

# 13. Recommended Weight Validation

The following validation rules apply:

```text
weight >= 0
```

Additionally, for active HARD constraints it is recommended that:

```text
weight > 0
```

because a zero-weight HARD constraint would still make the schedule infeasible but would provide no numerical pressure for the Genetic Algorithm to eliminate the violation.

For this reason, the MVP should reject:

```text
HARD constraint
weight = 0
```

unless constraints later support an explicit enabled/disabled state.

For SOFT constraints:

```text
weight = 0
```

may effectively disable their influence.

---

# 14. ConstraintResult

Each individual evaluation returns a `ConstraintResult`.

Conceptually:

```kotlin
data class ConstraintResult(
    val constraintId: String,
    val type: ConstraintType,
    val violations: List<ConstraintViolation>,
    val rawPenalty: Double,
    val weightedPenalty: Double
)
```

For HARD constraints:

```text
weightedPenalty =
rawPenalty × weight × HARD_PENALTY_FACTOR
```

For SOFT constraints:

```text
weightedPenalty =
rawPenalty × weight
```

---

# 15. ConstraintViolation

Each individual violation should remain identifiable.

Conceptually:

```kotlin
data class ConstraintViolation(
    val message: String,
    val penalty: Double,
    val relatedEntityIds: Set<String> = emptySet()
)
```

Example:

```text
Constraint:
NoOverlapConstraint

Violation:
Teacher Ana has overlapping assignments
between Mathematics 1A and Physics 2A.

Related entities:
teacher-ana
math-1a
physics-2a

Penalty:
1
```

This information allows Genetic Planner to explain why a schedule was penalized.

---

# 16. Aggregated Evaluation Result

After evaluating all constraints, Genetic Planner produces an aggregated result.

Conceptually:

```kotlin
data class ScheduleEvaluation(
    val feasible: Boolean,
    val hardViolationCount: Int,
    val softViolationCount: Int,
    val hardPenalty: Double,
    val softPenalty: Double,
    val totalPenalty: Double,
    val constraintResults: List<ConstraintResult>
)
```

The aggregated values are:

```text
hardViolationCount
    = total HARD violations

softViolationCount
    = total SOFT violations

hardPenalty
    = sum of weighted HARD penalties

softPenalty
    = sum of weighted SOFT penalties
```

---

# 17. Total Penalty

The global penalty is:

```text
totalPenalty =
    hardPenalty + softPenalty
```

However, the individual components must remain available.

Therefore, Genetic Planner should never retain only:

```text
totalPenalty = 2015
```

It should preserve:

```text
hardPenalty = 2000
softPenalty = 15
totalPenalty = 2015
```

This makes evaluation easier to understand and allows the future fitness function to treat the components differently if necessary.

---

# 18. Feasibility Calculation

The feasibility value is derived from HARD violations:

```kotlin
val feasible =
    hardViolationCount == 0
```

This means that a schedule with:

```text
hardPenalty = 0
softPenalty = 300
```

is feasible.

A schedule with:

```text
hardPenalty = 1000
softPenalty = 0
```

is infeasible.

---

# 19. Aggregation Example

Suppose a generated schedule produces:

```text
NoOverlapConstraint
type = HARD
rawPenalty = 2
weight = 1

AvailabilityConstraint
type = HARD
rawPenalty = 1
weight = 2

PreferredTimeSlotConstraint
type = SOFT
rawPenalty = 3
weight = 2

MaxConsecutiveConstraint
type = SOFT
rawPenalty = 1
weight = 5
```

Using:

```text
HARD_PENALTY_FACTOR = 1000
```

the results are:

```text
NoOverlap
2 × 1 × 1000
= 2000

Availability
1 × 2 × 1000
= 2000

PreferredTimeSlot
3 × 2
= 6

MaxConsecutive
1 × 5
= 5
```

Aggregated result:

```text
Hard violations = 3
Soft violations = 4

Hard penalty = 4000
Soft penalty = 11

Total penalty = 4011

Feasible = false
```

---

# 20. Feasible Schedule Example

Consider another candidate:

```text
NoOverlap
rawPenalty = 0

Availability
rawPenalty = 0

PreferredTimeSlot
rawPenalty = 4
weight = 2

MaxConsecutive
rawPenalty = 2
weight = 5
```

Result:

```text
Hard violations = 0
Soft violations = 6

Hard penalty = 0

Soft penalty =
(4 × 2) + (2 × 5)
= 18

Total penalty = 18

Feasible = true
```

This schedule is feasible even though it does not satisfy all user preferences.

---

# 21. Comparing Candidate Schedules

Evaluation should prioritize candidates conceptually in the following order:

```text
1. Fewer HARD violations
        ↓
2. Lower HARD penalty
        ↓
3. Lower SOFT penalty
```

For example:

```text
Schedule A
Hard violations = 0
Soft penalty = 30

Schedule B
Hard violations = 1
Soft penalty = 0
```

Preferred:

```text
Schedule A
```

because feasibility has priority.

Among feasible schedules:

```text
Schedule A
Hard violations = 0
Soft penalty = 30

Schedule B
Hard violations = 0
Soft penalty = 15
```

Preferred:

```text
Schedule B
```

This ordering will later guide the fitness function design.

---

# 22. Constraint Evaluation Service

Constraint aggregation should be performed by a generic component.

Conceptually:

```kotlin
class ConstraintEvaluator {

    fun evaluate(
        schedule: Schedule,
        constraints: List<Constraint>
    ): ScheduleEvaluation
}
```

Its responsibility is:

```text
Schedule
    +
Constraints
    ↓
Evaluate all constraints
    ↓
Separate HARD / SOFT
    ↓
Aggregate penalties
    ↓
Produce ScheduleEvaluation
```

It must not contain constraint-specific logic.

---

# 23. Generic Evaluation Algorithm

Conceptually:

```kotlin
fun evaluate(
    schedule: Schedule,
    constraints: List<Constraint>
): ScheduleEvaluation {

    val results = constraints.map {
        it.evaluate(schedule)
    }

    val hardResults = results.filter {
        it.type == ConstraintType.HARD
    }

    val softResults = results.filter {
        it.type == ConstraintType.SOFT
    }

    val hardViolationCount =
        hardResults.sumOf { it.violations.size }

    val softViolationCount =
        softResults.sumOf { it.violations.size }

    val hardPenalty =
        hardResults.sumOf { it.weightedPenalty }

    val softPenalty =
        softResults.sumOf { it.weightedPenalty }

    return ScheduleEvaluation(
        feasible = hardViolationCount == 0,
        hardViolationCount = hardViolationCount,
        softViolationCount = softViolationCount,
        hardPenalty = hardPenalty,
        softPenalty = softPenalty,
        totalPenalty = hardPenalty + softPenalty,
        constraintResults = results
    )
}
```

The `ConstraintEvaluator` does not know whether a result belongs to:

```text
NoOverlap
Availability
PreferredTimeSlot
or any future constraint
```

It works only with the common `Constraint` contract.

---

# 24. Violation Traceability

All `ConstraintResult` objects must be preserved in the aggregated evaluation.

This provides traceability:

```text
ScheduleEvaluation
        │
        └── ConstraintResults
                │
                ├── NoOverlap
                │     └── violations
                │
                ├── Availability
                │     └── violations
                │
                └── PreferredTimeSlot
                      └── violations
```

The application can therefore answer:

```text
Which constraints were violated?

How many times?

Which entities were involved?

How severe were the violations?

How much penalty did they contribute?
```

---

# 25. User-Facing Violation Information

The evaluation model supports the Results screen defined in the user journey.

For example:

```text
Solution Quality

Hard violations: 2
Soft violations: 3
```

Detailed information may show:

```text
Hard Constraints

✕ No Overlap
  Teacher Ana has two overlapping classes.

✕ Availability
  Teacher Pedro is assigned outside his availability.

Soft Constraints

! Preferred Time Slot
  Teacher Laura received an afternoon class.
```

The UI therefore does not need to reconstruct constraint failures from a numeric fitness value.

The necessary information is already provided by `ConstraintResult`.

---

# 26. Relationship with Fitness

Constraint evaluation and fitness calculation are separate responsibilities.

```text
Schedule
    ↓
ConstraintEvaluator
    ↓
ScheduleEvaluation
    ↓
Fitness Function
    ↓
Fitness value
```

This task defines:

```text
Schedule
    ↓
penalties
```

The later fitness design defines:

```text
penalties
    ↓
Genetic Algorithm fitness value
```

This separation prevents the constraint implementations from depending directly on Jenetics or the Genetic Algorithm.

---

# 27. Evaluation Architecture

The resulting architecture is:

```text
PlanningProblem
      │
      ├── Constraints
      │
      ▼
   Schedule
      │
      ▼
ConstraintEvaluator
      │
      ├── HARD results
      │       ├── violations
      │       └── penalties
      │
      └── SOFT results
              ├── violations
              └── penalties
      │
      ▼
ScheduleEvaluation
      │
      ├── feasible
      ├── hardViolationCount
      ├── softViolationCount
      ├── hardPenalty
      ├── softPenalty
      ├── totalPenalty
      └── constraintResults
```

---

# 28. Design Decisions

## E1 — Constraints calculate raw severity

Each concrete constraint determines how strongly it is violated.

This produces the `rawPenalty`.

---

## E2 — Weights represent relative importance

The generic weighting rule is:

```text
rawPenalty × weight
```

---

## E3 — HARD constraints receive additional optimization pressure

HARD penalties use:

```text
rawPenalty × weight × HARD_PENALTY_FACTOR
```

with a large configurable global factor.

---

## E4 — HARD violations explicitly determine feasibility

A schedule is feasible only when:

```text
hardViolationCount == 0
```

This rule does not depend on penalty magnitude.

---

## E5 — HARD and SOFT penalties remain separate

The model stores:

```text
hardPenalty
softPenalty
```

rather than only one global value.

---

## E6 — Aggregation remains generic

`ConstraintEvaluator` evaluates `Constraint` implementations without knowing their concrete classes.

---

## E7 — Violations remain identifiable

Individual `ConstraintViolation` objects are preserved after aggregation.

This supports debugging, experiments, documentation, and user-facing explanations.

---

## E8 — Evaluation remains independent from Jenetics

The constraint system produces a generic `ScheduleEvaluation`.

The Genetic Algorithm fitness layer later translates that evaluation into the numeric representation required by Jenetics.

---

# 29. Scope Boundary

This document defines:

* HARD penalty strategy.
* SOFT penalty strategy.
* Weighting system.
* Aggregated evaluation result.
* Feasibility semantics.
* Violation traceability.

It intentionally does not define:

* Final numeric fitness representation.
* Whether Jenetics minimizes or maximizes the resulting fitness value.
* Fitness normalization.
* Selection strategy.
* Genetic operators.
* Population size.
* Termination conditions.

Those decisions belong to:

```text
Design fitness function
Define genetic algorithm configuration
Research and validate Jenetics
```

---

# 30. Summary

Constraint evaluation follows the model:

```text
                        Schedule
                           │
                           ▼
                    Constraints
                           │
             ┌─────────────┴─────────────┐
             │                           │
            HARD                        SOFT
             │                           │
             ▼                           ▼
 rawPenalty × weight × factor    rawPenalty × weight
             │                           │
             └─────────────┬─────────────┘
                           ▼
                   ScheduleEvaluation
                           │
             ┌─────────────┼─────────────┐
             │             │             │
        Feasibility   Penalties     Violations
                           │
                           ▼
                  Future Fitness Function
```

The central rule is:

> **HARD constraints determine feasibility; SOFT constraints determine preference quality. Both influence optimization, but they remain explicitly separated throughout evaluation.**
