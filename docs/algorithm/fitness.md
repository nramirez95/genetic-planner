# Genetic Planner — Fitness Function

## 1. Purpose

This document describes the fitness function implemented by Genetic
Planner to evaluate candidate schedules during genetic optimization.

The fitness model combines:

- HARD constraint penalties.
- SOFT constraint penalties.
- Constraint-specific weights.
- Detailed constraint violations.

The main optimization principle is:

> Lower fitness values represent better schedules.

Fitness evaluation remains independent from Jenetics.

Jenetics receives a numeric fitness value, while planning semantics and
constraint evaluation remain in the domain layer.

---

## 2. Fitness Model

Every configured constraint evaluates a `Schedule` and produces a
`ConstraintResult`.

The total fitness is:

```text
Fitness =
    HARD penalty
    +
    SOFT penalty
```

More formally:

```text
Fitness(S) =
    Σ HARD weighted penalties
    +
    Σ SOFT weighted penalties
```

For each constraint:

```text
weightedPenalty =
    rawPenalty × weight
```

Therefore:

```text
Fitness(S) =
    Σ(rawPenaltyHard × weightHard)
    +
    Σ(rawPenaltySoft × weightSoft)
```

There is no additional global HARD multiplier in the fitness function.

HARD and SOFT constraints use the same weighting mechanism.

Their semantic difference is that HARD violations determine schedule
feasibility.

---

## 3. ConstraintResult

Each constraint returns a `ConstraintResult`:

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

The result preserves both:

```text
number of violations
```

and:

```text
penalty severity
```

These concepts are intentionally separate.

A constraint may therefore produce several violations with different
penalty values.

---

## 4. ConstraintViolation

Individual violations are represented by:

```kotlin
data class ConstraintViolation(
    val message: String,
    val penalty: Double,
    val relatedEntityIds: Set<String> = emptySet()
)
```

Each violation provides:

- A human-readable description.
- A raw penalty.
- The identifiers of related planning entities when applicable.

This allows the fitness system to preserve explanatory information
instead of reducing evaluation immediately to a scalar number.

---

## 5. Raw and Weighted Penalties

For one constraint:

```text
rawPenalty
=
sum of individual violation penalties
```

and:

```text
weightedPenalty
=
rawPenalty × constraint weight
```

Example:

```text
PreferredTimeSlotConstraint

Violation 1 → penalty 1
Violation 2 → penalty 1
Violation 3 → penalty 1

rawPenalty = 3
weight = 10

weightedPenalty = 30
```

Another constraint may assign penalties according to severity rather
than simply counting violations.

For example, a capacity constraint can use the missing capacity as the
penalty value.

Therefore:

> Violation count and penalty are related but are not interchangeable.

---

## 6. HARD Constraints

HARD constraints represent mandatory planning rules.

The current implementation includes:

```text
NoOverlapConstraint
AvailabilityConstraint
RequiredResourceConstraint
LocationCapacityConstraint
```

A HARD violation does not make a genotype structurally invalid.

Instead:

```text
Genotype
    ↓
structurally valid
    ↓
Schedule
    ↓
HARD violation
    ↓
infeasible Schedule
    +
weighted penalty
```

This allows infeasible schedules to remain part of the evolutionary
search space.

The Genetic Algorithm can therefore evolve from infeasible schedules
toward feasible ones.

---

## 7. HARD Constraint Weighting

HARD constraints do not receive an automatic or hidden global
multiplier.

Their weighted penalty is calculated exactly like any other constraint:

```text
weightedPenalty =
rawPenalty × weight
```

For example, a configured HARD constraint may use:

```text
weight = 1000
```

If it produces two violations with raw penalty `1` each:

```text
rawPenalty = 2

weightedPenalty =
2 × 1000
=
2000
```

Large HARD weights such as `1000` are useful when the configuration
should strongly guide evolution away from mandatory-rule violations.

However:

```text
1000
```

is a configuration choice, not a constant built into the fitness
algorithm.

---

## 8. SOFT Constraints

SOFT constraints represent preferences and optimization objectives.

The current implementation includes:

```text
PreferredTimeSlotConstraint
MaxConsecutiveConstraint
BalancedWorkloadConstraint
```

SOFT violations are allowed and do not make the Schedule infeasible.

They increase the fitness according to:

```text
rawPenalty × weight
```

Example:

```text
PreferredTimeSlotConstraint

3 violations
raw penalty = 3
weight = 10

weighted penalty = 30
```

Different SOFT constraints can use different weights according to their
relative importance.

---

## 9. ConstraintEvaluator

The aggregation of constraint results is implemented by:

```text
ConstraintEvaluator
```

Conceptually:

```text
Schedule
    │
    ▼
Configured Constraints
    │
    ▼
ConstraintResult[]
    │
    ├── HARD results
    │       ↓
    │   hardPenalty
    │
    └── SOFT results
            ↓
        softPenalty
            │
            ▼
    ScheduleEvaluation
```

The evaluator:

1. Evaluates every configured constraint.
2. Collects all `ConstraintResult` objects.
3. Sums weighted HARD penalties.
4. Sums weighted SOFT penalties.
5. Creates a `ScheduleEvaluation`.

The Genetic Algorithm does not implement individual planning rules.

---

## 10. ScheduleEvaluation

The implemented aggregate model is:

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

Only the fundamental aggregate values and detailed results are stored.

The following values are derived:

```text
totalPenalty
fitness
feasible
```

This prevents duplicated state and keeps `ScheduleEvaluation` as the
single source of truth for optimization evaluation.

---

## 11. Fitness

The fitness value is directly derived from the evaluation:

```text
fitness =
totalPenalty
```

and:

```text
totalPenalty =
hardPenalty + softPenalty
```

Therefore:

```text
fitness =
hardPenalty + softPenalty
```

The Genetic Engine obtains fitness through:

```text
ScheduleEvaluation.fitness
```

rather than implementing a second fitness calculation.

---

## 12. Feasibility

Schedule feasibility is determined exclusively by HARD constraint
violations.

The implemented rule is conceptually:

```text
feasible =
no HARD ConstraintResult contains violations
```

This is intentionally not:

```text
hardPenalty == 0
```

because penalty and feasibility represent different concepts.

For example, consider a HARD constraint configured with:

```text
weight = 0
```

that produces one violation.

Its numeric contribution is:

```text
rawPenalty × weight
=
1 × 0
=
0
```

but the Schedule remains:

```text
feasible = false
```

because a HARD rule was violated.

This separation ensures that constraint semantics are not accidentally
changed by their numeric weight.

---

## 13. Feasible Schedule with SOFT Penalties

Consider:

```text
HARD violations = 0
hardPenalty = 0

SOFT penalty = 35
```

Then:

```text
fitness = 35
feasible = true
```

The Schedule satisfies all mandatory rules but does not satisfy all
preferences.

---

## 14. Infeasible Schedule

Consider:

```text
HARD violations > 0
hardPenalty = 1000

SOFT penalty = 20
```

Then:

```text
fitness = 1020
feasible = false
```

The Schedule remains part of the search space, but its HARD violations
make it infeasible.

---

## 15. Minimization

Genetic Planner models fitness as a penalty.

Therefore:

```text
lower fitness
=
better optimization result
```

The Jenetics Engine is configured with:

```text
Optimize.MINIMUM
```

Conceptually:

```text
Schedule A → fitness 3050
Schedule B → fitness 1020
Schedule C → fitness   35
Schedule D → fitness   10
Schedule E → fitness    0
```

The Genetic Algorithm attempts to minimize this value.

---

## 16. Ideal Fitness

When all configured constraint penalties are zero:

```text
hardPenalty = 0
softPenalty = 0
```

then:

```text
fitness = 0
```

For the normal positive-weight configurations used by Genetic Planner,
this represents a candidate without penalized HARD or SOFT violations.

However, feasibility is still derived independently from HARD
violations rather than inferred from the numeric fitness value.

The current Genetic Engine does not use fitness zero as an early
termination condition.

Evolution currently stops according to the configured fixed generation
limit.

---

## 17. Fitness Evaluation During Evolution

Jenetics operates on:

```text
Genotype<IntegerGene>
```

Planning constraints operate on:

```text
Schedule
```

The adaptation process is therefore:

```text
Genotype
    │
    ▼
ScheduleGenotypeCodec.decode()
    │
    ▼
Schedule
    │
    ▼
ConstraintEvaluator.evaluate()
    │
    ▼
ScheduleEvaluation
    │
    ▼
fitness
    │
    ▼
Jenetics
```

Conceptually:

```kotlin
val schedule = codec.decode(genotype)

val fitness =
    constraintEvaluator
        .evaluate(schedule)
        .fitness
```

This is the fitness function supplied to the Jenetics Engine.

---

## 18. Separation of Responsibilities

The implemented architecture separates four responsibilities.

### Constraint

A Constraint knows how to identify violations of one planning rule.

```text
Schedule
    ↓
Constraint
    ↓
ConstraintResult
```

### ConstraintResult

A ConstraintResult stores detailed violations and calculates:

```text
violations
rawPenalty
weightedPenalty
```

### ConstraintEvaluator

The evaluator executes all configured constraints and aggregates their
weighted penalties.

### ScheduleEvaluation

The aggregate result exposes:

```text
hardPenalty
softPenalty
totalPenalty
fitness
feasible
constraintResults
```

### JeneticsEngine

Jenetics only consumes:

```text
fitness: Double
```

and minimizes it.

It does not know how planning constraints are implemented.

---

## 19. Penalty Breakdown

The system preserves the complete evaluation instead of returning only
the scalar fitness value.

For example:

```text
Fitness: 3050
Feasible: false

HARD
├── NoOverlap:        2000
└── Availability:     1000

SOFT
├── PreferredTime:      40
└── MaxConsecutive:     10
```

This information can be used for:

- Debugging.
- Experimentation.
- Result explanation.
- UI visualization.
- Comparing generated schedules.

`OptimizationResult` preserves the complete `ScheduleEvaluation`, so
this information remains available after optimization finishes.

---

## 20. Manual Example

Consider the following constraint results.

### HARD

```text
NoOverlapConstraint

2 violations
raw penalty = 2
weight = 1000
weighted penalty = 2000
```

```text
AvailabilityConstraint

1 violation
raw penalty = 1
weight = 1000
weighted penalty = 1000
```

### SOFT

```text
PreferredTimeSlotConstraint

4 violations
raw penalty = 4
weight = 10
weighted penalty = 40
```

```text
MaxConsecutiveConstraint

2 violations
raw penalty = 2
weight = 5
weighted penalty = 10
```

The aggregate values are:

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
fitness =
3000 + 50
=
3050
```

and:

```text
feasible = false
```

because HARD violations are present.

---

## 21. Fitness and OptimizationResult

After evolution, the best genotype is decoded and evaluated again.

The Genetic Engine returns:

```text
OptimizationResult
```

containing the resulting:

```text
Schedule
ScheduleEvaluation
```

and execution statistics.

The result exposes derived properties including:

```text
fitness
feasible
hardPenalty
softPenalty
constraintResults
```

The evaluation therefore remains available outside the Jenetics
evolution process without duplicating the fitness calculation.

---

## 22. Fitness Diagram

```text
                      Genotype
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
              ┌──────────┴──────────┐
              │                     │
              ▼                     ▼
      HARD ConstraintResult  SOFT ConstraintResult
              │                     │
              ▼                     ▼
        hardPenalty           softPenalty
              │                     │
              └──────────┬──────────┘
                         │
                         ▼
                ScheduleEvaluation
                         │
              ┌──────────┴──────────┐
              │                     │
              ▼                     ▼
           fitness               feasible
              │
              ▼
       Jenetics MINIMUM
```

---

## 23. Implementation Decisions

### F1 — Fitness represents penalty

```text
Lower = better
```

The Genetic Algorithm minimizes the total weighted penalty.

### F2 — HARD and SOFT use the same penalty formula

Both use:

```text
rawPenalty × weight
```

There is no hidden HARD multiplier.

### F3 — HARD constraints determine feasibility

A Schedule is feasible only when no HARD constraint reports violations.

### F4 — Feasibility is not derived from hardPenalty

The implementation examines HARD violations directly.

This preserves correct semantics even when a HARD constraint has zero
weight.

### F5 — Violation count and penalty are separate concepts

`violations` counts violation details.

`rawPenalty` sums their individual penalty values.

### F6 — ScheduleEvaluation is the fitness source of truth

Fitness is exposed as:

```text
ScheduleEvaluation.fitness
```

and is derived from:

```text
hardPenalty + softPenalty
```

### F7 — Detailed results are preserved

The system keeps every `ConstraintResult` and `ConstraintViolation`
instead of retaining only the scalar fitness.

### F8 — Constraint evaluation remains outside Jenetics

Jenetics receives the final numeric fitness but contains no planning
constraint logic.

### F9 — Optimization uses minimization

The Jenetics Engine uses:

```text
Optimize.MINIMUM
```

### F10 — Zero fitness is not a stopping criterion

Although zero represents the ideal numeric penalty, the current engine
runs until its configured generation limit.

---

## 24. Deviations from Initial Design

The initial fitness design was largely preserved during implementation,
but several details were refined.

### No global HARD_WEIGHT constant

The initial design described:

```text
HARD_WEIGHT = 1000
```

as the default HARD weighting mechanism.

The implementation does not contain a global HARD multiplier.

Instead, every constraint owns its own:

```text
weight
```

and both HARD and SOFT penalties use:

```text
rawPenalty × weight
```

Values such as `1000` remain useful configuration choices for HARD
constraints but are not embedded in the fitness algorithm.

### ScheduleEvaluation was simplified

The initial design proposed storing values such as:

```text
feasible
hardViolationCount
softViolationCount
totalPenalty
```

directly in the aggregate evaluation object.

The implemented model stores:

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

This reduces duplicated state.

### Feasibility semantics were strengthened

The initial design conceptually described feasibility as:

```text
hardViolationCount == 0
```

The implementation derives the same semantic result directly from
HARD `ConstraintResult` objects.

It does not use numeric penalty as a feasibility proxy.

### FitnessBreakdown was not introduced

The initial design proposed a separate conceptual:

```text
FitnessBreakdown
```

The implemented `ScheduleEvaluation` already provides the required
aggregate and detailed information.

A separate model was therefore unnecessary.

### Zero-fitness early termination was not implemented

The initial design identified:

```text
fitness = 0
```

as a possible early termination condition.

The current Jenetics Engine uses a fixed generation limit instead.

This keeps execution behavior predictable and directly aligned with the
configured GA presets.

### Implemented constraint catalogue changed

Some constraints mentioned during the initial design were not part of
the final M2 implementation, while others were introduced.

The implemented HARD constraints are:

```text
NoOverlapConstraint
AvailabilityConstraint
RequiredResourceConstraint
LocationCapacityConstraint
```

The implemented SOFT constraints are:

```text
PreferredTimeSlotConstraint
MaxConsecutiveConstraint
BalancedWorkloadConstraint
```

---

## 25. Current Limitations

The current fitness model intentionally does not implement:

- Multi-objective optimization.
- Pareto-front optimization.
- Automatic constraint-weight tuning.
- Adaptive weights during evolution.
- Lexicographic HARD-before-SOFT optimization.
- Dynamic fitness normalization.

The current MVP uses a single scalar weighted penalty because it keeps
the optimization model simple, explainable, and compatible with the
generic constraint architecture.

---

## 26. Summary

The implemented fitness function is:

```text
Fitness(S) =
    Σ HARD weighted penalties
    +
    Σ SOFT weighted penalties
```

where:

```text
weightedPenalty =
rawPenalty × weight
```

and:

```text
rawPenalty =
Σ individual ConstraintViolation penalties
```

The optimization objective is:

```text
MINIMIZE fitness
```

The complete evaluation flow is:

```text
Genotype
    ↓
ScheduleGenotypeCodec
    ↓
Schedule
    ↓
ConstraintEvaluator
    ↓
ConstraintResult[]
    ↓
ScheduleEvaluation
    ├── hardPenalty
    ├── softPenalty
    ├── totalPenalty
    ├── fitness
    └── feasible
    ↓
Jenetics
    ↓
Optimize.MINIMUM
```

The central principle is:

> Fitness measures optimization penalty, while HARD constraint
> violations determine feasibility.

This separation keeps the Genetic Algorithm generic while preserving
the semantic distinction between mandatory planning rules and
optimization preferences.