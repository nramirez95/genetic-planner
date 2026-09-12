# Genetic Planner — Constraint Evaluation and Weighting

## 1. Purpose

This document defines how Genetic Planner evaluates constraints and aggregates their penalties when assessing a candidate `Schedule`.

The evaluation model must:

* Support both HARD and SOFT constraints.
* Preserve the semantic difference between HARD and SOFT constraints.
* Allow weighted penalties.
* Produce an explainable result.
* Separate constraint evaluation from genetic optimization.
* Provide the information required by the fitness function.
* Remain independent from specific planning domains.

The central principle is:

> **Every constraint produces a penalty, while HARD constraints additionally determine whether a Schedule is feasible.**

---

# 2. Evaluation Flow

Constraint evaluation occurs after a candidate genotype has been decoded into a domain `Schedule`.

The general flow is:

```text id="62fgcz"
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
ScheduleEvaluation
    ↓
Fitness Function
```

The Constraint Engine does not depend on Jenetics.

It evaluates domain objects only.

---

# 3. Constraint Model

The common constraint interface is:

```kotlin id="gwnd8w"
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
```

Each concrete constraint is responsible for:

* Detecting its own violations.
* Calculating the raw penalty.
* Returning the detected violations.

The generic evaluation layer is responsible for:

* Applying the constraint weight.
* Aggregating HARD penalties.
* Aggregating SOFT penalties.
* Determining Schedule feasibility.
* Producing the complete `ScheduleEvaluation`.

---

# 4. ConstraintResult

Every evaluated constraint returns a result:

```kotlin id="fnqcih"
data class ConstraintResult(
    val constraintId: String,
    val type: ConstraintType,
    val violations: List<ConstraintViolation>,
    val rawPenalty: Double,
    val weightedPenalty: Double
)
```

Each individual violation is represented as:

```kotlin id="1d7mt4"
data class ConstraintViolation(
    val message: String,
    val penalty: Double,
    val relatedEntityIds: Set<String> = emptySet()
)
```

This provides more information than a simple:

```text id="zmuaq1"
true / false
```

and supports later:

* Fitness calculation.
* Debugging.
* User explanations.
* Experimental analysis.
* Visualization of generated schedules.

---

# 5. Raw Penalty

The `rawPenalty` represents the unweighted severity produced by a constraint.

In the simplest case:

```text id="ycc0f7"
rawPenalty =
number of violations
```

Example:

```text id="rxj4sk"
NoOverlapConstraint

2 overlaps detected

rawPenalty = 2
```

However, a concrete constraint may assign different penalties to different violations if necessary.

For example:

```text id="402rfq"
Violation 1 penalty = 1
Violation 2 penalty = 2

rawPenalty = 3
```

Therefore:

```text id="4ss5s9"
rawPenalty =
Σ violation penalties
```

---

# 6. Constraint Weight

Every constraint has a numeric `weight`.

The weight determines how strongly its violations affect optimization.

The generic formula is:

```text id="cu3tad"
weightedPenalty =
rawPenalty × weight
```

This formula applies equally to HARD and SOFT constraints.

Example:

```text id="kx1pz4"
rawPenalty = 4
weight = 10

weightedPenalty =
4 × 10
=
40
```

---

# 7. HARD and SOFT Semantics

HARD and SOFT constraints use the same penalty formula:

```text id="r35dk7"
weightedPenalty =
rawPenalty × weight
```

However, their meaning is different.

## HARD constraint

A HARD constraint represents a mandatory planning rule.

If a HARD constraint has one or more violations:

```text id="rw5bfm"
Schedule
    ↓
infeasible
```

Example:

```text id="0x2fyx"
NoOverlapConstraint
type = HARD
weight = 1000
```

A violation affects:

* Fitness.
* Feasibility.

## SOFT constraint

A SOFT constraint represents a preference or quality objective.

If a SOFT constraint is violated:

```text id="vwd8q1"
Schedule
    ↓
still feasible
    ↓
lower quality
```

Example:

```text id="2j4r7u"
PreferredTimeSlotConstraint
type = SOFT
weight = 10
```

A violation affects fitness but not feasibility.

---

# 8. HARD Is Not Defined by Weight

A constraint is HARD because of:

```text id="7np1ph"
type = HARD
```

not because it has a weight of `1000`.

For example:

```text id="mqg07m"
NoOverlapConstraint

type = HARD
weight = 1000
```

means:

```text id="mjr953"
HARD
→ violation makes Schedule infeasible

1000
→ violation has high optimization cost
```

These concepts must remain separate.

Therefore:

```text id="a6l1l9"
ConstraintType
=
semantic meaning
```

while:

```text id="9lxrbp"
weight
=
optimization importance
```

---

# 9. Default Weighting Strategy

For the MVP, HARD constraints normally use much larger weights than SOFT constraints.

Typical values are:

```text id="kjsy2d"
HARD

NoOverlap               1000
Availability            1000
RequiredResource        1000
MaximumAssignments      1000
```

and:

```text id="xyf11b"
SOFT

PreferredTimeSlot         10
MaxConsecutive              5
MinimumAssignments         10
DifferentDay                5
```

These values are defaults rather than part of the definition of HARD or SOFT.

---

# 10. Active Constraint Weight Validation

An active constraint must have:

```text id="d96det"
weight > 0
```

A weight of zero would make violations invisible to the Genetic Algorithm.

For the MVP:

```text id="eihpb9"
active HARD weight = 0
```

must be rejected.

The same validation should normally apply to active SOFT constraints.

If a constraint should not affect optimization, it should be disabled instead of configured with zero weight.

---

# 11. Evaluating One Constraint

Suppose:

```text id="hhrecr"
PreferredTimeSlotConstraint

type = SOFT
weight = 10
```

and evaluation finds:

```text id="3rlu6k"
4 violations
```

with one penalty point each.

Then:

```text id="uvr2ko"
rawPenalty = 4
```

and:

```text id="wna6vd"
weightedPenalty =
4 × 10
=
40
```

The result becomes conceptually:

```text id="16mhq5"
ConstraintResult

type = SOFT
violations = 4
rawPenalty = 4
weightedPenalty = 40
```

---

# 12. Evaluating a HARD Constraint

Suppose:

```text id="g16zhx"
NoOverlapConstraint

type = HARD
weight = 1000
```

and:

```text id="xkwq1p"
2 overlaps
```

Then:

```text id="ehbta0"
rawPenalty = 2

weightedPenalty =
2 × 1000
=
2000
```

Because this is a HARD constraint:

```text id="2zwdlt"
hardViolationCount > 0
```

and therefore the final Schedule is infeasible.

---

# 13. ScheduleEvaluation

After evaluating every active constraint, Genetic Planner creates:

```kotlin id="irykfx"
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

This object is the complete evaluation of a candidate `Schedule`.

---

# 14. HARD Violation Count

The number of HARD violations is:

```text id="s27xy1"
hardViolationCount =
Σ number of violations
from HARD constraints
```

Example:

```text id="v33ri6"
NoOverlap
2 violations

Availability
1 violation
```

produces:

```text id="jg28kp"
hardViolationCount =
2 + 1
=
3
```

---

# 15. SOFT Violation Count

Similarly:

```text id="gwd1vj"
softViolationCount =
Σ number of violations
from SOFT constraints
```

Example:

```text id="y1i57t"
PreferredTimeSlot
4 violations

MaxConsecutive
2 violations
```

produces:

```text id="h5q37u"
softViolationCount =
4 + 2
=
6
```

---

# 16. HARD Penalty Aggregation

The total HARD penalty is:

```text id="r4iqyn"
hardPenalty =
Σ weightedPenalty
for every HARD constraint
```

Example:

```text id="gtcthh"
NoOverlap
weightedPenalty = 2000

Availability
weightedPenalty = 1000
```

Therefore:

```text id="68o6bt"
hardPenalty =
2000 + 1000
=
3000
```

---

# 17. SOFT Penalty Aggregation

The total SOFT penalty is:

```text id="8fxusq"
softPenalty =
Σ weightedPenalty
for every SOFT constraint
```

Example:

```text id="jjgyqg"
PreferredTimeSlot
weightedPenalty = 40

MaxConsecutive
weightedPenalty = 10
```

Therefore:

```text id="h7y1ld"
softPenalty =
40 + 10
=
50
```

---

# 18. Total Penalty

The complete Schedule penalty is:

```text id="zvp83f"
totalPenalty =
hardPenalty + softPenalty
```

Example:

```text id="3o3ccv"
hardPenalty = 3000
softPenalty = 50
```

produces:

```text id="z01i8b"
totalPenalty =
3000 + 50
=
3050
```

The Fitness Function uses this value during optimization.

---

# 19. Feasibility

Schedule feasibility depends only on HARD constraints.

The rule is:

```text id="jdt2bc"
feasible =
hardViolationCount == 0
```

Therefore:

```text id="i7rffs"
HARD violations = 0
→ feasible = true
```

while:

```text id="fur19j"
HARD violations > 0
→ feasible = false
```

SOFT violations never make a Schedule infeasible.

---

# 20. Example — Complete Evaluation

Suppose the following constraints are evaluated.

## HARD

```text id="te67dj"
NoOverlapConstraint

violations = 2
rawPenalty = 2
weight = 1000

weightedPenalty =
2 × 1000
=
2000
```

```text id="kxslf4"
AvailabilityConstraint

violations = 1
rawPenalty = 1
weight = 1000

weightedPenalty =
1 × 1000
=
1000
```

## SOFT

```text id="k0ibzm"
PreferredTimeSlotConstraint

violations = 4
rawPenalty = 4
weight = 10

weightedPenalty =
4 × 10
=
40
```

```text id="2ig5uj"
MaxConsecutiveConstraint

violations = 2
rawPenalty = 2
weight = 5

weightedPenalty =
2 × 5
=
10
```

---

# 21. Complete Example Result

The aggregated values are:

```text id="rkhnrk"
hardViolationCount =
2 + 1
=
3
```

```text id="k4imtw"
softViolationCount =
4 + 2
=
6
```

```text id="rxz594"
hardPenalty =
2000 + 1000
=
3000
```

```text id="bb5jwg"
softPenalty =
40 + 10
=
50
```

```text id="ri5927"
totalPenalty =
3000 + 50
=
3050
```

Since:

```text id="fv9rxg"
hardViolationCount = 3
```

the result is:

```text id="4x2wbr"
feasible = false
```

Therefore:

```text id="ow1fbz"
ScheduleEvaluation

feasible = false

hardViolationCount = 3
softViolationCount = 6

hardPenalty = 3000
softPenalty = 50

totalPenalty = 3050
```

---

# 22. Feasible Schedule Example

Consider:

```text id="swj7ql"
HARD violations = 0

PreferredTimeSlot
3 violations × 10 = 30

MaxConsecutive
1 violation × 5 = 5
```

Then:

```text id="4rxztt"
hardViolationCount = 0
softViolationCount = 4

hardPenalty = 0
softPenalty = 35

totalPenalty = 35

feasible = true
```

This demonstrates that:

```text id="bntd85"
fitness > 0
```

does not necessarily mean that the Schedule is infeasible.

---

# 23. Ideal Evaluation

The ideal candidate produces:

```text id="lqsb8r"
hardViolationCount = 0
softViolationCount = 0

hardPenalty = 0
softPenalty = 0

totalPenalty = 0

feasible = true
```

Therefore:

```text id="pzkk2j"
totalPenalty = 0
```

represents the ideal evaluation.

---

# 24. Evaluation Algorithm

Conceptually, evaluation can be implemented as:

```kotlin id="lcqh7c"
fun evaluate(
    schedule: Schedule,
    constraints: List<Constraint>
): ScheduleEvaluation {

    val results = constraints.map { constraint ->
        constraint.evaluate(schedule)
    }

    val hardResults =
        results.filter { it.type == ConstraintType.HARD }

    val softResults =
        results.filter { it.type == ConstraintType.SOFT }

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

The exact implementation may vary, but the evaluation semantics must remain equivalent.

---

# 25. Separation from Fitness

Constraint evaluation and fitness are related but separate concepts.

The Constraint Engine produces:

```text id="y7pt0x"
ScheduleEvaluation
```

containing:

```text id="rw4746"
feasibility
violation counts
HARD penalty
SOFT penalty
total penalty
detailed constraint results
```

The Fitness Function then uses:

```text id="hxufzk"
ScheduleEvaluation.totalPenalty
```

as the scalar value optimized by the Genetic Algorithm.

Therefore:

```text id="eewd1r"
ConstraintEvaluator
      ↓
explains Schedule quality

Fitness Function
      ↓
provides optimization value
```

---

# 26. Explainability

The evaluation must preserve individual `ConstraintResult` objects.

For example:

```text id="26vq0g"
Total penalty: 3050

HARD:
- NoOverlap:       2000
- Availability:    1000

SOFT:
- PreferredTime:     40
- MaxConsecutive:     10
```

This information can later be shown in the results interface.

It also allows experimental analysis such as:

* Most frequently violated constraints.
* HARD vs SOFT penalty distribution.
* Comparison between algorithm configurations.
* Evolution of schedule quality.

---

# 27. Domain Independence

The evaluation mechanism does not know whether a Resource is:

```text id="w5pa89"
Teacher
Employee
Student group
Machine
```

or whether an Activity represents:

```text id="m8jwil"
Lesson
Work assignment
Exam
Meeting
```

It only evaluates generic:

```text id="hrmsj3"
Schedule
Assignment
Resource
Activity
TimeSlot
Location
Constraint
```

Therefore the same evaluation mechanism supports Academic Scheduling and Work Shift Scheduling.

---

# 28. Error Conditions

Constraint evaluation should reject invalid configurations such as:

```text id="w3sh1h"
weight < 0
```

and, for active constraints:

```text id="pe89jt"
weight = 0
```

It should also reject:

```text id="3qj4hz"
rawPenalty < 0
weightedPenalty < 0
```

because Genetic Planner defines penalties as non-negative values.

Therefore:

```text id="2pp6nt"
rawPenalty >= 0
weight > 0
weightedPenalty >= 0
```

---

# 29. Relationship with Chromosome Representation

A chromosome may be structurally valid while producing constraint violations.

Example:

```text id="dd2gpq"
Activity A
→ Monday 09:00
→ Ana

Activity B
→ Monday 09:00
→ Ana
```

Both Assignment Options may be structurally valid.

After decoding:

```text id="76okbt"
Schedule
    ↓
NoOverlapConstraint
    ↓
violation
    ↓
penalty
```

Therefore:

> Structural validity belongs to encoding, while planning validity belongs to constraint evaluation.

---

# 30. Relationship with the Fitness Function

The evaluation output connects directly with the fitness design:

```text id="dd00zz"
Schedule
    ↓
ConstraintEvaluator
    ↓
ScheduleEvaluation
    ↓
totalPenalty
    ↓
Fitness
```

For example:

```text id="51yzpz"
hardPenalty = 3000
softPenalty = 50
```

therefore:

```text id="s4ttst"
Fitness =
3000 + 50
=
3050
```

The Genetic Algorithm minimizes this value.

---

# 31. Design Decisions

## CE1 — All constraints use the same weighting formula

```text id="jx1gql"
weightedPenalty =
rawPenalty × weight
```

No separate global HARD multiplier is required.

## CE2 — Constraint type and weight have different meanings

```text id="9shtqg"
ConstraintType
→ determines semantics and feasibility

Weight
→ determines optimization impact
```

## CE3 — HARD constraints determine feasibility

```text id="93yyzw"
hardViolationCount > 0
→ feasible = false
```

## CE4 — SOFT constraints do not determine feasibility

SOFT violations only reduce Schedule quality.

## CE5 — HARD constraints normally use large weights

Typical MVP value:

```text id="i4tawq"
1000
```

## CE6 — SOFT constraints use smaller configurable weights

Typical values may include:

```text id="0nt9om"
5
10
20
```

## CE7 — HARD and SOFT penalties are aggregated separately

```text id="o7y9i6"
hardPenalty
softPenalty
```

remain available independently.

## CE8 — Total penalty combines both groups

```text id="tixh4s"
totalPenalty =
hardPenalty + softPenalty
```

## CE9 — Penalties are non-negative

```text id="uq0c1m"
rawPenalty >= 0
weightedPenalty >= 0
```

## CE10 — Detailed results are preserved

Evaluation must retain each `ConstraintResult` for explainability and analysis.

## CE11 — Evaluation remains domain-independent

The same mechanism applies to every planning template.

## CE12 — Fitness calculation is a separate responsibility

Constraint evaluation produces `ScheduleEvaluation`.

The Fitness Function uses that result for Genetic Algorithm optimization.

---

# 32. Summary

Constraint evaluation follows:

```text id="xvjncf"
Constraint
    ↓
detect violations
    ↓
rawPenalty
    ↓
× weight
    ↓
weightedPenalty
```

Results are separated by type:

```text id="r17nfa"
HARD
    ↓
hardPenalty
    +
determines feasibility

SOFT
    ↓
softPenalty
    +
affects schedule quality
```

The final aggregation is:

```text id="9thuc7"
hardPenalty =
Σ HARD weighted penalties

softPenalty =
Σ SOFT weighted penalties

totalPenalty =
hardPenalty + softPenalty
```

and:

```text id="gk28x9"
feasible =
hardViolationCount == 0
```

Example:

```text id="2p0rmv"
HARD:
  overlap             2 × 1000 = 2000
  unavailable         1 × 1000 = 1000

SOFT:
  preference          4 × 10   =   40
  consecutive         2 × 5    =   10

hardPenalty = 3000
softPenalty = 50
totalPenalty = 3050

feasible = false
```

The resulting architecture is:

```text id="ykrwkp"
Schedule
    ↓
ConstraintEvaluator
    ↓
ConstraintResult[]
    ↓
ScheduleEvaluation
    │
    ├── feasible
    ├── hardViolationCount
    ├── softViolationCount
    ├── hardPenalty
    ├── softPenalty
    ├── totalPenalty
    └── detailed results
             ↓
        Fitness Function
```

This design provides a simple, explainable and reusable constraint evaluation model for Genetic Planner.
