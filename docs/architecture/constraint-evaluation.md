# Genetic Planner — Constraint Evaluation

## 1. Purpose

This document describes the implemented constraint evaluation
architecture used by Genetic Planner.

The evaluation model must:

- Support HARD and SOFT constraints.
- Preserve the semantic difference between HARD and SOFT rules.
- Support weighted penalties.
- Preserve detailed violation information.
- Determine schedule feasibility.
- Provide the information required by the fitness function.
- Remain independent from Jenetics.
- Remain independent from specific planning domains.

The central principle is:

> Every constraint produces violations and penalties, while HARD
> constraints additionally determine whether a Schedule is feasible.

---

## 2. Evaluation Architecture

Constraint evaluation operates on domain `Schedule` objects.

The complete relationship with genetic optimization is:

```text
Jenetics Genotype
        │
        ▼
ScheduleGenotypeCodec
        │
        ▼
      Schedule
        │
        ▼
ConstraintEvaluator
        │
        ▼
ConstraintResult[]
        │
        ▼
ScheduleEvaluation
        │
        ├── fitness
        └── feasible
```

The Constraint Engine has no dependency on Jenetics.

This separation allows constraints to be tested and executed directly
against domain schedules.

---

## 3. ConstraintType

Constraints are classified as:

```kotlin
enum class ConstraintType {
    HARD,
    SOFT
}
```

The type determines the semantic role of the constraint.

### HARD

A HARD constraint represents a mandatory planning rule.

A violation:

```text
affects fitness
+
makes the Schedule infeasible
```

### SOFT

A SOFT constraint represents a preference or quality objective.

A violation:

```text
affects fitness
+
does not make the Schedule infeasible
```

Constraint type and weight therefore represent different concepts.

---

## 4. Constraint Interface

Every planning constraint implements:

```kotlin
interface Constraint {
    val id: String
    val name: String
    val type: ConstraintType
    val weight: Double

    fun evaluate(schedule: Schedule): ConstraintResult
}
```

Each concrete constraint is responsible for:

- Inspecting the Schedule.
- Detecting violations of its rule.
- Creating `ConstraintViolation` objects.
- Assigning a raw penalty to each violation.

The common evaluation infrastructure is responsible for aggregating the
resulting penalties.

---

## 5. ConstraintViolation

Each detected violation is represented by:

```kotlin
data class ConstraintViolation(
    val message: String,
    val penalty: Double,
    val relatedEntityIds: Set<String> = emptySet()
)
```

A violation contains:

```text
message
```

describing the problem,

```text
penalty
```

representing its unweighted severity, and:

```text
relatedEntityIds
```

identifying the planning entities involved when applicable.

Example:

```text
ConstraintViolation
├── message = "Resource worker-1 has overlapping assignments"
├── penalty = 1.0
└── relatedEntityIds =
    [worker-1, activity-1, activity-2]
```

This detailed representation supports explainability, testing,
experimentation, and later result visualization.

---

## 6. ConstraintResult

Every constraint returns:

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

The aggregate values are derived.

This avoids duplicated state and ensures that violation details remain
the source of truth.

---

## 7. Violation Count

The number of violations is:

```text
violations =
number of ConstraintViolation objects
```

For example:

```text
violationDetails =
[
    violation A,
    violation B,
    violation C
]
```

produces:

```text
violations = 3
```

Violation count does not necessarily equal penalty severity.

---

## 8. Raw Penalty

The raw penalty is calculated as:

```text
rawPenalty =
Σ ConstraintViolation.penalty
```

Example:

```text
Violation A → penalty 1
Violation B → penalty 1
Violation C → penalty 2
```

produces:

```text
violations = 3
rawPenalty = 4
```

This distinction allows constraints to express different violation
severities.

---

## 9. Weighted Penalty

Every constraint has a configurable:

```text
weight
```

The final optimization contribution of that constraint is:

```text
weightedPenalty =
rawPenalty × weight
```

Example:

```text
rawPenalty = 4
weight = 10

weightedPenalty =
4 × 10
=
40
```

This formula is identical for HARD and SOFT constraints.

There is no additional global HARD multiplier.

---

## 10. Type vs Weight

A constraint is HARD because:

```text
type = HARD
```

not because it has a large weight.

For example:

```text
NoOverlapConstraint

type = HARD
weight = 1000
```

means:

```text
HARD
→ violations affect feasibility

1000
→ violations have a high optimization cost
```

These concepts remain independent.

Therefore:

```text
ConstraintType
=
planning semantics
```

while:

```text
weight
=
optimization impact
```

---

## 11. Zero Weights

The evaluation architecture does not use the numeric penalty to
determine feasibility.

This distinction is important when:

```text
weight = 0
```

For example:

```text
HARD constraint
1 violation
rawPenalty = 1
weight = 0
```

produces:

```text
weightedPenalty = 0
```

but the Schedule is still:

```text
infeasible
```

because a HARD violation exists.

Therefore:

> HARD feasibility is determined from violations, not from weighted
> penalty.

A zero weight may remove a constraint's influence on numeric
optimization, but it does not change the semantic type of the
constraint.

---

## 12. ConstraintEvaluator

Constraint aggregation is implemented by:

```text
ConstraintEvaluator
```

The evaluator receives the configured constraints and evaluates them
against a Schedule.

Conceptually:

```text
Schedule
    │
    ▼
Constraint 1 ──→ ConstraintResult 1
Constraint 2 ──→ ConstraintResult 2
Constraint 3 ──→ ConstraintResult 3
    │
    ▼
ConstraintEvaluator
    │
    ├── aggregate HARD weighted penalties
    └── aggregate SOFT weighted penalties
    │
    ▼
ScheduleEvaluation
```

The evaluator does not contain the rule-specific logic implemented by
individual constraints.

---

## 13. ConstraintEvaluator Algorithm

The implemented aggregation semantics are equivalent to:

```kotlin
fun evaluate(schedule: Schedule): ScheduleEvaluation {

    val results =
        constraints.map { constraint ->
            constraint.evaluate(schedule)
        }

    val hardPenalty =
        results
            .filter { it.type == ConstraintType.HARD }
            .sumOf { it.weightedPenalty }

    val softPenalty =
        results
            .filter { it.type == ConstraintType.SOFT }
            .sumOf { it.weightedPenalty }

    return ScheduleEvaluation(
        hardPenalty = hardPenalty,
        softPenalty = softPenalty,
        constraintResults = results
    )
}
```

The evaluator therefore performs aggregation only.

Constraint-specific violation detection remains inside each concrete
constraint.

---

## 14. ScheduleEvaluation

The aggregate evaluation is represented by:

```kotlin
data class ScheduleEvaluation(
    val hardPenalty: Double,
    val softPenalty: Double,
    val constraintResults: List<ConstraintResult>
) {
    val totalPenalty: Double
        get() = hardPenalty + softPenalty

    val fitness: Double
        get() = totalPenalty

    val feasible: Boolean
        get() = constraintResults
            .filter { it.type == ConstraintType.HARD }
            .none { it.violations > 0 }
}
```

Stored state is limited to:

```text
hardPenalty
softPenalty
constraintResults
```

while:

```text
totalPenalty
fitness
feasible
```

are derived.

This keeps the detailed constraint results as the semantic source of
truth.

---

## 15. HARD Penalty Aggregation

The HARD penalty is:

```text
hardPenalty =
Σ weightedPenalty
for HARD constraints
```

Example:

```text
NoOverlap
weightedPenalty = 2000

Availability
weightedPenalty = 1000
```

produces:

```text
hardPenalty =
2000 + 1000
=
3000
```

---

## 16. SOFT Penalty Aggregation

Similarly:

```text
softPenalty =
Σ weightedPenalty
for SOFT constraints
```

Example:

```text
PreferredTimeSlot
weightedPenalty = 40

MaxConsecutive
weightedPenalty = 10
```

produces:

```text
softPenalty =
40 + 10
=
50
```

---

## 17. Total Penalty and Fitness

The total penalty is derived as:

```text
totalPenalty =
hardPenalty + softPenalty
```

and fitness is:

```text
fitness =
totalPenalty
```

For example:

```text
hardPenalty = 3000
softPenalty = 50

totalPenalty = 3050
fitness = 3050
```

The Genetic Algorithm minimizes this value.

The complete fitness semantics are documented in:

```text
docs/algorithm/fitness.md
```

---

## 18. Feasibility

Feasibility is determined from HARD violations.

Conceptually:

```text
feasible =
no HARD ConstraintResult has violations
```

Therefore:

```text
HARD violations = 0
→ feasible = true
```

while:

```text
HARD violations > 0
→ feasible = false
```

SOFT violations never make a Schedule infeasible.

Importantly:

```text
hardPenalty = 0
```

does not necessarily imply:

```text
feasible = true
```

because a zero-weight HARD constraint may still contain violations.

---

# 19. Implemented HARD Constraints

The M2 implementation contains four HARD constraints.

## 19.1 NoOverlapConstraint

`NoOverlapConstraint` detects overlapping assignments that use the same
Resource.

Two TimeSlots overlap when:

```text
A.start < B.end
AND
B.start < A.end
```

For each conflicting Resource and assignment pair, the constraint
creates one violation.

Conceptually:

```text
Activity A
Resource R1
09:00–10:00

Activity B
Resource R1
09:30–10:30
```

produces a HARD violation.

Adjacent TimeSlots such as:

```text
09:00–10:00
10:00–11:00
```

do not overlap.

---

## 19.2 AvailabilityConstraint

`AvailabilityConstraint` verifies whether assigned Resources are
available during their selected TimeSlot.

Availability belongs to the constraint configuration rather than to the
`Resource` domain entity.

The implemented semantics are:

```text
Resource absent from availability configuration
→ unrestricted
```

while:

```text
Resource configured with empty availability
→ never available
```

An assignment outside the configured availability produces a HARD
violation.

This keeps Resource itself generic and allows availability rules to vary
between planning problems.

---

## 19.3 RequiredResourceConstraint

`RequiredResourceConstraint` verifies that every Activity
`ResourceRequirement` is satisfied by its Assignment.

For each requirement, the constraint validates:

- Required quantity.
- Distinct Resource identifiers.
- Resource type.
- Configured candidate Resources.

The required quantity represents the exact number of distinct Resources.

For example:

```text
quantity = 2
```

requires exactly two different Resource identifiers.

Therefore:

```text
[worker-1, worker-1]
```

does not satisfy the requirement.

Likewise, assigning more Resources than the required quantity is also a
violation.

Type and candidate-set checks are evaluated without reporting duplicate
semantic errors for the same invalid Resource where possible.

---

## 19.4 LocationCapacityConstraint

`LocationCapacityConstraint` verifies that the selected Location
provides enough capacity for the Activity requirement.

If:

```text
required capacity = 30
location capacity = 25
```

the shortage is:

```text
5
```

and the raw penalty reflects that shortage.

Therefore capacity violations can express severity rather than simply
counting one penalty point per violation.

Location capacity remains a constraint concern and is not used to prune
AssignmentOptions during candidate generation.

---

# 20. Implemented SOFT Constraints

The M2 implementation contains three SOFT constraints.

## 20.1 PreferredTimeSlotConstraint

`PreferredTimeSlotConstraint` stores preferred TimeSlot identifiers by
Activity.

Conceptually:

```text
Activity A
→ preferred [slot-1, slot-2]
```

If Activity A is assigned to one of those TimeSlots:

```text
no violation
```

Otherwise:

```text
one SOFT violation
raw penalty = 1
```

An Activity absent from the preference map has no configured preference
and therefore produces no preference violation.

---

## 20.2 MaxConsecutiveConstraint

`MaxConsecutiveConstraint` limits consecutive assignments for a
Resource.

Two assignments are consecutive when:

```text
previous TimeSlot end
=
next TimeSlot start
```

The constraint evaluates assignment sequences for each relevant
Resource.

When the configured maximum is exceeded, the excess contributes to the
SOFT penalty.

This allows the Genetic Algorithm to prefer schedules with better
continuity characteristics without making longer sequences structurally
invalid.

---

## 20.3 BalancedWorkloadConstraint

`BalancedWorkloadConstraint` encourages assignments to be distributed
more evenly across configured Resources.

Workload is measured using assignment counts.

Conceptually:

```text
Resource A → 5 assignments
Resource B → 3 assignments
Resource C → 3 assignments
```

The constraint evaluates the difference between the highest and lowest
configured workload.

Penalty is produced when that difference exceeds the accepted balance.

This remains a SOFT objective: an unbalanced Schedule can still be
feasible.

---

## 21. Complete Evaluation Example

Suppose constraint evaluation produces:

### HARD

```text
NoOverlapConstraint

2 violations
rawPenalty = 2
weight = 1000
weightedPenalty = 2000
```

```text
AvailabilityConstraint

1 violation
rawPenalty = 1
weight = 1000
weightedPenalty = 1000
```

### SOFT

```text
PreferredTimeSlotConstraint

4 violations
rawPenalty = 4
weight = 10
weightedPenalty = 40
```

```text
MaxConsecutiveConstraint

2 violations
rawPenalty = 2
weight = 5
weightedPenalty = 10
```

The evaluator produces:

```text
hardPenalty =
2000 + 1000
=
3000
```

```text
softPenalty =
40 + 10
=
50
```

Therefore:

```text
totalPenalty = 3050
fitness = 3050
feasible = false
```

The Schedule is infeasible because HARD violations exist.

---

## 22. Explainability

The evaluation architecture preserves every violation instead of
returning only:

```text
fitness = 3050
```

For example, the system can retain:

```text
HARD

NoOverlap
├── Activity A / Activity B / Resource R1
└── Activity C / Activity D / Resource R2

Availability
└── Activity E / Resource R3
```

This information can later support:

- User-facing explanations.
- Constraint summaries.
- Debugging.
- Experimental analysis.
- Visualization of problematic assignments.

The Genetic Engine preserves these results through
`OptimizationResult.constraintResults`.

---

## 23. Relationship with Candidate Generation

A candidate option represents a structurally possible assignment.

Constraint evaluation represents planning semantics across the decoded
Schedule.

Therefore:

```text
Structural impossibility
        ↓
AssignmentCandidateGenerator
        ↓
option is not generated
```

while:

```text
Planning rule violation
        ↓
option / Schedule remains representable
        ↓
ConstraintEvaluator
        ↓
violation + penalty
```

For example:

```text
Activity A → Slot 1 → Resource R1
Activity B → Slot 1 → Resource R1
```

can consist of two structurally valid AssignmentOptions.

When combined into a Schedule:

```text
NoOverlapConstraint
        ↓
HARD violation
```

This separation allows the Genetic Algorithm to explore infeasible
solutions while maintaining structural validity.

---

## 24. Relationship with the Fitness Function

Constraint evaluation produces:

```text
ScheduleEvaluation
```

containing the detailed planning evaluation.

The fitness function uses:

```text
ScheduleEvaluation.fitness
```

as the scalar value supplied to Jenetics.

Therefore:

```text
Constraint
      ↓
ConstraintViolation
      ↓
ConstraintResult
      ↓
ConstraintEvaluator
      ↓
ScheduleEvaluation
      ↓
fitness
      ↓
Jenetics
```

Planning constraints never operate on Jenetics objects.

---

## 25. Domain Independence

The constraint architecture operates on generic planning concepts:

```text
Schedule
Assignment
Resource
Activity
ResourceRequirement
TimeSlot
Location
```

It does not require domain-specific entities such as:

```text
Teacher
Employee
Subject
WorkShift
```

For example, `NoOverlapConstraint` operates on generic Resources.

The same implementation can therefore detect:

```text
teacher overlap
employee overlap
machine overlap
room-related resource overlap
```

without changing the Genetic Engine or constraint evaluation
architecture.

---

## 26. Implementation Decisions

### CE1 — Constraints evaluate domain Schedules

Constraint evaluation is independent from Jenetics.

### CE2 — Violations are first-class evaluation data

Constraints return detailed `ConstraintViolation` objects instead of
only booleans or scalar penalties.

### CE3 — ConstraintResult derives aggregate values

`violations`, `rawPenalty`, and `weightedPenalty` are derived from
violation details and weight rather than duplicated as stored state.

### CE4 — HARD and SOFT use the same weighting formula

```text
weightedPenalty =
rawPenalty × weight
```

No global HARD multiplier is applied.

### CE5 — Constraint type determines feasibility semantics

`ConstraintType.HARD` determines whether violations make a Schedule
infeasible.

Weight determines only numeric optimization impact.

### CE6 — Feasibility is derived from violations

The implementation does not use:

```text
hardPenalty == 0
```

as a feasibility test.

### CE7 — HARD and SOFT penalties are aggregated separately

The system preserves:

```text
hardPenalty
softPenalty
```

before deriving total fitness.

### CE8 — ScheduleEvaluation avoids duplicated state

Only penalties and detailed results are stored.

`totalPenalty`, `fitness`, and `feasible` are derived.

### CE9 — Constraint evaluation remains generic

No constraint-evaluation infrastructure depends on Academic or Work
Shift templates.

### CE10 — Structural validity and constraint validity remain separate

Candidate generation handles structural alternatives.

Constraints evaluate planning rules over Schedules.

### CE11 — Constraint-specific severity is supported

Individual violations contain their own penalty values, allowing
constraints such as capacity to express the magnitude of a violation.

---

## 27. Deviations from Initial Design

The initial evaluation architecture was preserved conceptually but
refined during M2 implementation.

### ConstraintResult was simplified

The initial model stored:

```text
violations
rawPenalty
weightedPenalty
```

directly in `ConstraintResult`.

The implemented model stores:

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

This removes duplicated state.

### ScheduleEvaluation was simplified

The initial model proposed storing:

```text
feasible
hardViolationCount
softViolationCount
hardPenalty
softPenalty
totalPenalty
constraintResults
```

The implementation stores only:

```text
hardPenalty
softPenalty
constraintResults
```

and derives:

```text
totalPenalty
fitness
feasible
```

Violation counts can still be calculated from the detailed
`ConstraintResult` objects when required.

### Zero-weight constraints are allowed by evaluation semantics

The initial design proposed:

```text
weight > 0
```

for active constraints and suggested rejecting zero-weight constraints.

The final evaluation semantics do not depend on positive weight for
feasibility.

A zero-weight HARD constraint can still make a Schedule infeasible.

This is one reason feasibility is derived from violations rather than
penalty values.

### Implemented constraint catalogue changed

The initial design referenced constraints that were candidates for the
MVP but were not implemented during M2, including examples such as:

```text
MaximumAssignmentsConstraint
MinimumAssignmentsConstraint
DifferentDayConstraint
```

The implemented catalogue is:

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

### Fitness became a derived ScheduleEvaluation property

The initial architecture described constraint evaluation and fitness as
two more distinct stages.

The implemented `ScheduleEvaluation` exposes:

```text
fitness
```

directly as a derived property of:

```text
hardPenalty + softPenalty
```

The conceptual separation remains: constraints evaluate planning rules,
while the Genetic Engine consumes the resulting scalar fitness.

---

## 28. Current Scope

The current constraint evaluation architecture supports:

- Generic HARD and SOFT constraints.
- Weighted penalties.
- Multiple detailed violations per constraint.
- Constraint-specific violation severity.
- Independent HARD and SOFT aggregation.
- Explicit feasibility.
- Explainable optimization results.
- Generic application across planning templates.

It intentionally does not provide:

- Automatic weight tuning.
- Dynamic constraint activation during evolution.
- Multi-objective constraint optimization.
- Constraint priorities beyond type and weight.
- Domain-specific evaluation infrastructure.

These features can be considered after the MVP if required by
experimental results.

---

## 29. Summary

The implemented evaluation pipeline is:

```text
Schedule
    │
    ▼
Constraint
    │
    ▼
ConstraintViolation[]
    │
    ▼
ConstraintResult
    │
    ├── violations
    ├── rawPenalty
    └── weightedPenalty
    │
    ▼
ConstraintEvaluator
    │
    ├── hardPenalty
    └── softPenalty
    │
    ▼
ScheduleEvaluation
    │
    ├── totalPenalty
    ├── fitness
    └── feasible
```

The central formulas are:

```text
rawPenalty =
Σ ConstraintViolation.penalty
```

```text
weightedPenalty =
rawPenalty × weight
```

```text
hardPenalty =
Σ HARD weightedPenalty
```

```text
softPenalty =
Σ SOFT weightedPenalty
```

```text
fitness =
hardPenalty + softPenalty
```

and:

```text
feasible =
no HARD constraint contains violations
```

The key architectural distinction remains:

> Constraint type determines planning semantics; weight determines
> optimization impact.

This keeps constraint evaluation generic, explainable, and independent
from the Jenetics implementation.