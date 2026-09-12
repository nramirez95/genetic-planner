# Genetic Planner — Fitness Function Design

## 1. Purpose

This document defines the fitness function used by Genetic Planner to evaluate candidate schedules during the genetic optimization process.

The fitness function combines:

* HARD constraint violations.
* SOFT constraint violations.
* Constraint weights.

The Genetic Algorithm uses this value to compare candidate schedules and progressively search for better solutions.

The main principle is:

> **Lower fitness values represent better schedules.**

---

# 2. Fitness Function

Every constraint produces a penalty according to:

* Number of violations.
* Severity of each violation.
* Configured constraint weight.

The total fitness is calculated as:

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
Constraint penalty =
    raw penalty × weight
```

Therefore:

```text
Fitness(S) =
    Σ(rawPenaltyHard × weightHard)
    +
    Σ(rawPenaltySoft × weightSoft)
```

---

# 3. HARD Constraints

HARD constraints represent mandatory planning rules.

Examples include:

```text
NoOverlapConstraint
AvailabilityConstraint
RequiredResourceConstraint
MaximumAssignmentsConstraint
```

Violating a HARD constraint does not make the chromosome structurally invalid.

Instead:

```text
HARD violation
      ↓
Large penalty
      ↓
Worse fitness
```

This allows infeasible candidates to remain in the population while the Genetic Algorithm evolves toward feasible schedules.

---

# 4. HARD Constraint Weighting

HARD constraints receive a significantly larger penalty than SOFT constraints.

For the MVP, the default HARD weight is:

```text
HARD_WEIGHT = 1000
```

Example:

```text
NoOverlapConstraint

violations = 2
weight = 1000

penalty =
2 × 1000
=
2000
```

Another example:

```text
AvailabilityConstraint

violations = 1
weight = 1000

penalty =
1 × 1000
=
1000
```

The purpose of this high value is to strongly guide evolution toward feasible schedules.

---

# 5. SOFT Constraints

SOFT constraints represent preferences or quality objectives.

Examples include:

```text
PreferredTimeSlotConstraint
MaxConsecutiveConstraint
MinimumAssignmentsConstraint
DifferentDayConstraint
```

A violation is allowed, but increases the fitness value.

Example:

```text
PreferredTimeSlotConstraint

violations = 4
weight = 10

penalty =
4 × 10
=
40
```

SOFT weights can differ depending on their importance.

---

# 6. Fitness Calculation

The complete calculation follows:

```text
HARD penalties
      +
SOFT penalties
      ↓
Total Fitness
```

Example:

```text
Fitness

HARD:
  overlap             2 × 1000 = 2000
  unavailable         1 × 1000 = 1000

SOFT:
  preference          4 × 10   =   40
  consecutive         2 × 5    =   10

TOTAL = 3050
```

Therefore:

```text
Fitness = 3050
```

---

# 7. Fitness Breakdown

The system must preserve the complete penalty breakdown instead of returning only the final numeric value.

Conceptually:

```kotlin
data class FitnessBreakdown(
    val hardPenalty: Double,
    val softPenalty: Double,
    val totalPenalty: Double,
    val constraintResults: List<ConstraintResult>
)
```

This allows Genetic Planner to show:

```text
Total Fitness
3050

HARD
├── No overlap:        2000
└── Availability:      1000

SOFT
├── Preference:          40
└── Consecutive:         10
```

This breakdown is useful for:

* Fitness calculation.
* Debugging.
* Experimental evaluation.
* Result explanation.
* UI visualization.
* Comparing generated schedules.

---

# 8. Relationship with ScheduleEvaluation

The Constraint Engine already provides:

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

The Genetic Algorithm can therefore obtain its fitness directly from:

```text
ScheduleEvaluation.totalPenalty
```

Conceptually:

```kotlin
fun fitness(schedule: Schedule): Double {
    val evaluation = constraintEvaluator.evaluate(schedule)

    return evaluation.totalPenalty
}
```

The detailed information remains available through:

```text
ScheduleEvaluation.constraintResults
```

---

# 9. Calculation Flow

The complete evaluation flow is:

```text
Genotype
    ↓
Decoder
    ↓
Schedule
    ↓
ConstraintEvaluator
    ↓
ConstraintResult[]
    ↓
Aggregate penalties
    ↓
ScheduleEvaluation
    ↓
totalPenalty
    ↓
Fitness
```

Therefore the Genetic Algorithm itself does not need to know the implementation of individual constraints.

---

# 10. Minimization vs Maximization

Genetic Planner uses:

```text
MINIMIZATION
```

because penalties naturally express undesirable properties.

Therefore:

```text
Lower fitness
=
Better schedule
```

For example:

```text
Schedule A → Fitness 3050
Schedule B → Fitness 1020
Schedule C → Fitness   20
Schedule D → Fitness    0
```

The ordering is:

```text
D > C > B > A
```

in terms of schedule quality.

The Genetic Algorithm therefore seeks:

```text
MIN Fitness
```

---

# 11. Ideal Fitness

The ideal fitness is:

```text
0
```

because this means:

```text
HARD violations = 0
SOFT violations = 0
```

Therefore:

```text
Fitness = 0
```

represents a schedule that satisfies every configured HARD and SOFT constraint.

Conceptually:

```text
Fitness = 0
      ↓
No HARD violations
      +
No SOFT penalties
      ↓
Ideal solution
```

A fitness of zero can therefore also be used as an early termination condition.

---

# 12. Feasibility

Schedule feasibility remains determined specifically by HARD constraints:

```text
feasible =
hardViolationCount == 0
```

This is independent from the total fitness.

Example:

```text
Schedule A

HARD penalty = 0
SOFT penalty = 50

Fitness = 50

feasible = true
```

while:

```text
Schedule B

HARD penalty = 1000
SOFT penalty = 0

Fitness = 1000

feasible = false
```

Thus:

```text
Fitness
```

represents optimization quality, while:

```text
feasible
```

explicitly indicates whether all mandatory constraints are satisfied.

---

# 13. Constraint Weights

Every constraint has a configurable weight.

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

The resulting penalty is:

```text
weightedPenalty =
rawPenalty × weight
```

The default weighting strategy for the MVP is:

```text
HARD constraints
→ large weights, typically 1000

SOFT constraints
→ smaller weights according to preference importance
```

Example:

```text
NoOverlap
weight = 1000

Availability
weight = 1000

PreferredTimeSlot
weight = 10

MaxConsecutive
weight = 5
```

---

# 14. Manual Example 1 — Infeasible Schedule

Suppose a candidate schedule produces:

```text
HARD

NoOverlapConstraint
2 violations
weight = 1000

AvailabilityConstraint
1 violation
weight = 1000
```

and:

```text
SOFT

PreferredTimeSlotConstraint
4 violations
weight = 10

MaxConsecutiveConstraint
2 violations
weight = 5
```

Calculation:

```text
NoOverlap
2 × 1000
= 2000

Availability
1 × 1000
= 1000

PreferredTimeSlot
4 × 10
= 40

MaxConsecutive
2 × 5
= 10
```

Therefore:

```text
HARD penalty = 3000

SOFT penalty = 50

TOTAL FITNESS = 3050
```

The schedule is:

```text
feasible = false
```

because it contains HARD violations.

---

# 15. Manual Example 2 — Feasible Schedule with Preferences Violated

Consider:

```text
HARD

NoOverlapConstraint
0 violations

AvailabilityConstraint
0 violations
```

and:

```text
SOFT

PreferredTimeSlotConstraint
3 violations
weight = 10

MaxConsecutiveConstraint
1 violation
weight = 5
```

Calculation:

```text
HARD penalty
= 0

Preference
3 × 10
= 30

Consecutive
1 × 5
= 5
```

Therefore:

```text
TOTAL FITNESS = 35
```

and:

```text
feasible = true
```

The schedule is valid but not ideal.

---

# 16. Manual Example 3 — Ideal Schedule

Consider:

```text
HARD violations = 0
SOFT violations = 0
```

Then:

```text
HARD penalty = 0
SOFT penalty = 0
```

Therefore:

```text
TOTAL FITNESS = 0
```

and:

```text
feasible = true
```

This is the ideal candidate.

---

# 17. Manual Comparison

Suppose the population contains:

| Schedule | HARD penalty | SOFT penalty | Fitness | Feasible |
| -------- | -----------: | -----------: | ------: | -------- |
| A        |         3000 |           50 |    3050 | No       |
| B        |         1000 |           20 |    1020 | No       |
| C        |            0 |           35 |      35 | Yes      |
| D        |            0 |           10 |      10 | Yes      |
| E        |            0 |            0 |       0 | Yes      |

Since the objective is minimization:

```text
E
↓
D
↓
C
↓
B
↓
A
```

Schedule E is the ideal solution.

---

# 18. Fitness Diagram

```text
                        SCHEDULE
                           │
                           ▼
                  ConstraintEvaluator
                           │
              ┌────────────┴────────────┐
              │                         │
              ▼                         ▼
       HARD Constraints          SOFT Constraints
              │                         │
              ▼                         ▼
        Large penalties          Preference penalties
              │                         │
              └────────────┬────────────┘
                           │
                           ▼
                    TOTAL FITNESS
                           │
                           ▼
                       MINIMIZE
                           │
                           ▼
                     Best Schedule

                 Ideal Fitness = 0
```

---

# 19. Separation of Responsibilities

The architecture remains:

```text
Constraint
    ↓
calculates violation penalty

ConstraintEvaluator
    ↓
aggregates constraint results

ScheduleEvaluation
    ↓
stores detailed evaluation

Fitness Function
    ↓
returns totalPenalty

Jenetics
    ↓
minimizes fitness
```

Therefore Jenetics does not contain planning-domain logic.

---

# 20. Genetic Engine Integration

The Genetic Engine evaluates a candidate by:

```text
Genotype
      ↓
decode
      ↓
Schedule
      ↓
evaluate constraints
      ↓
ScheduleEvaluation
      ↓
totalPenalty
      ↓
fitness value
```

Conceptually:

```kotlin
fun fitness(genotype: Genotype<IntegerGene>): Double {
    val schedule = decoder.decode(genotype)
    val evaluation = constraintEvaluator.evaluate(schedule)

    return evaluation.totalPenalty
}
```

This preserves the separation between:

```text
Genetic representation
```

and:

```text
Planning evaluation
```

---

# 21. Fitness and Result Presentation

The value used internally by the Genetic Algorithm is:

```text
totalPenalty
```

However, when presenting a generated schedule to the user, Genetic Planner should expose more information:

```text
Fitness: 35

Feasible: Yes

HARD violations: 0
HARD penalty: 0

SOFT violations: 4
SOFT penalty: 35

Breakdown:
- Preferred Time Slot: 30
- Maximum Consecutive: 5
```

This makes optimization results explainable.

---

# 22. Scope

The fitness function defines:

* HARD penalty integration.
* SOFT penalty integration.
* Constraint weighting.
* Total fitness.
* Minimization.
* Ideal fitness.
* Detailed penalty breakdown.
* Manual fitness examples.

It does not define:

* Population size.
* Selection strategy.
* Mutation probability.
* Crossover probability.
* Genetic operators.
* Maximum number of generations.
* Other termination conditions.

Those decisions belong to the Genetic Algorithm configuration task.

---

# 23. Design Decisions

## F1 — Fitness represents penalty

```text
Lower = better
```

## F2 — Optimization uses minimization

The Genetic Algorithm minimizes the total penalty.

## F3 — HARD constraints receive large weights

The MVP uses weights such as:

```text
1000
```

to strongly penalize mandatory-rule violations.

## F4 — SOFT constraints use smaller configurable weights

Their weight represents preference importance.

## F5 — Total fitness combines HARD and SOFT penalties

```text
Fitness =
HardPenalty + SoftPenalty
```

## F6 — Ideal fitness is zero

```text
Fitness = 0
```

means that no configured constraint produces a penalty.

## F7 — Feasibility remains explicit

```text
feasible =
hardViolationCount == 0
```

## F8 — Penalty breakdown is preserved

The system does not store only the final scalar value.

## F9 — Constraint evaluation remains outside Jenetics

Jenetics receives the calculated fitness but does not implement planning rules.

---

# 24. Summary

The Genetic Planner fitness function is:

```text
Fitness(S) =
    Σ HARD weighted penalties
    +
    Σ SOFT weighted penalties
```

Example:

```text
Fitness

HARD:
  overlap             2 × 1000 = 2000
  unavailable         1 × 1000 = 1000

SOFT:
  preference          4 × 10   =   40
  consecutive         2 × 5    =   10

TOTAL = 3050
```

The optimization objective is:

```text
MINIMIZE Fitness
```

and:

```text
Ideal Fitness = 0
```

The complete flow is:

```text
Genotype
    ↓
Schedule
    ↓
Constraint Evaluation
    ↓
HARD penalties
+
SOFT penalties
    ↓
Fitness
    ↓
MINIMIZE
```

This design provides a simple, explainable and domain-independent fitness function suitable for the Genetic Planner MVP.
