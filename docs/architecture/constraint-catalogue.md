# Genetic Planner — Constraint Catalogue

## 1. Purpose

This document describes the generic constraint catalogue implemented by
Genetic Planner.

Constraints express planning rules evaluated against a generated
`Schedule`.

All constraints implement the common `Constraint` abstraction and remain
independent from specific planning domains.

The same constraint implementations can therefore be reused across
Academic Scheduling, Work Shift Scheduling, and future planning
templates.

The catalogue distinguishes between:

- **HARD constraints**, which define mandatory planning rules and
  determine schedule feasibility.
- **SOFT constraints**, which express preferences or quality objectives
  and influence optimization without determining feasibility.

---

## 2. Implemented Constraint Catalogue

The current core catalogue contains seven generic constraints:

| Constraint | Type | Status |
| --- | --- | --- |
| `NoOverlapConstraint` | HARD | Implemented |
| `AvailabilityConstraint` | HARD | Implemented |
| `RequiredResourceConstraint` | HARD | Implemented |
| `LocationCapacityConstraint` | HARD | Implemented |
| `PreferredTimeSlotConstraint` | SOFT | Implemented |
| `MaxConsecutiveConstraint` | SOFT | Implemented |
| `BalancedWorkloadConstraint` | SOFT | Implemented |

The catalogue deliberately focuses on a small set of reusable rules that
cover both schedule feasibility and schedule quality.

Additional constraints can be introduced without modifying the Genetic
Engine.

---

## 3. Common Constraint Model

All constraints follow:

```kotlin
interface Constraint {
    val id: String
    val name: String
    val type: ConstraintType
    val weight: Double

    fun evaluate(schedule: Schedule): ConstraintResult
}
```

where:

```kotlin
enum class ConstraintType {
    HARD,
    SOFT
}
```

A constraint evaluates a domain `Schedule` and produces detailed
violations through a `ConstraintResult`.

Constraint evaluation is independent from Jenetics.

---

## 4. HARD Constraints

HARD constraints represent mandatory planning rules.

If any HARD constraint produces one or more violations:

```text
Schedule
    ↓
infeasible
```

HARD constraints also contribute their weighted penalties to fitness.

The current HARD catalogue contains:

```text
NoOverlapConstraint
AvailabilityConstraint
RequiredResourceConstraint
LocationCapacityConstraint
```

---

## 5. NoOverlapConstraint

### Description

`NoOverlapConstraint` prevents the same Resource from being assigned to
overlapping activities.

It protects the fundamental scheduling rule that one Resource cannot
participate in two simultaneous assignments.

### Type

```text
HARD
```

### Overlap Rule

Two TimeSlots overlap when:

```text
slotA.start < slotB.end
AND
slotB.start < slotA.end
```

Adjacent TimeSlots therefore do not overlap.

For example:

```text
09:00–10:00
10:00–11:00
```

are valid consecutive periods.

However:

```text
09:00–10:00
09:30–10:30
```

overlap.

### Violation Semantics

The constraint evaluates Resources shared by overlapping assignments.

One violation is produced for each conflicting Resource and assignment
pair.

### Academic Example

Teacher Ana is assigned to:

```text
Mathematics
Monday 09:00–10:00
```

and:

```text
Physics
Monday 09:30–10:30
```

The assignments overlap and use the same Resource.

Result:

```text
NoOverlapConstraint
→ HARD violation
```

### Work Shift Example

Employee Laura is assigned to:

```text
Reception
08:00–14:00
```

and:

```text
Support Desk
12:00–18:00
```

The assignments overlap between 12:00 and 14:00.

The same generic constraint detects the conflict.

---

## 6. AvailabilityConstraint

### Description

`AvailabilityConstraint` ensures that Resources are assigned only to
TimeSlots in which they are available.

Availability is represented as a constraint rather than an intrinsic
property of `Resource`.

This keeps the generic Resource model independent from scheduling rules.

### Type

```text
HARD
```

### Configuration Semantics

Availability is configured by Resource.

The implementation distinguishes between:

```text
Resource absent from availability configuration
→ unrestricted
```

and:

```text
Resource configured with an empty set
→ never available
```

A Resource configured with specific TimeSlots may only be assigned to
those TimeSlots.

### Academic Example

Teacher Ana is configured as available during:

```text
Monday 09:00–10:00
Monday 10:00–11:00
```

but the generated Schedule assigns her to:

```text
Monday 12:00–13:00
```

Result:

```text
AvailabilityConstraint
→ HARD violation
```

### Work Shift Example

Employee Pedro is available only during configured morning TimeSlots.

An afternoon assignment therefore produces the same generic violation.

---

## 7. RequiredResourceConstraint

### Description

`RequiredResourceConstraint` verifies that the Resource requirements
defined by each Activity are correctly satisfied by its Assignment.

Activities express their structural Resource needs through
`ResourceRequirement`.

For example:

```text
Activity
    ↓
ResourceRequirement
    ├── resourceTypeId
    ├── quantity
    └── candidateResourceIds
```

The Assignment then provides:

```text
ResourceRequirement.id
        ↓
List<Resource.id>
```

through:

```text
Assignment.resourceAssignments
```

### Type

```text
HARD
```

### Evaluation

For every ResourceRequirement, the constraint verifies:

- The required quantity.
- That assigned Resources are distinct.
- That assigned Resources have the required Resource type.
- That assigned Resources belong to the configured candidate set when
  one is provided.

### Exact Quantity

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

### Candidate Resources

If:

```text
candidateResourceIds = null
```

any Resource of the required type may satisfy the requirement.

If a candidate set is configured, the selected Resources must belong to
that set.

### Academic Example

An Activity requires:

```text
resource type = TEACHER
quantity = 1
```

If its Assignment contains no teacher, the requirement is violated.

If it contains a Resource of a different type, the requirement is also
violated.

### Work Shift Example

A work Activity requires:

```text
resource type = WORKER
quantity = 2
```

The generated Assignment must contain exactly two distinct compatible
workers.

The same constraint therefore works without knowing whether Resources
represent teachers, employees, machines, or another domain concept.

---

## 8. LocationCapacityConstraint

### Description

`LocationCapacityConstraint` ensures that the selected Location provides
enough capacity for the assigned Activity.

### Type

```text
HARD
```

An Activity may define:

```text
requiredLocationCapacity
```

while a Location may define:

```text
capacity
```

The constraint evaluates whether the selected Location satisfies the
required capacity.

### Penalty

Capacity violations use the capacity shortage as their raw penalty.

For example:

```text
required capacity = 30
location capacity = 25
```

produces:

```text
shortage = 5
raw penalty = 5
```

This allows the evaluation to distinguish between small and large
capacity violations.

### Academic Example

A class requires capacity for:

```text
30
```

but the selected classroom has:

```text
capacity = 20
```

Result:

```text
LocationCapacityConstraint
→ HARD violation
→ raw penalty = 10
```

### Work Shift Example

A training Activity requires capacity for 15 participants but is
assigned to a Location with capacity 10.

The same generic constraint detects the shortage.

---

## 9. SOFT Constraints

SOFT constraints represent preferences and schedule quality objectives.

Their violations contribute penalties to fitness but do not make the
Schedule infeasible.

The current SOFT catalogue contains:

```text
PreferredTimeSlotConstraint
MaxConsecutiveConstraint
BalancedWorkloadConstraint
```

---

## 10. PreferredTimeSlotConstraint

### Description

`PreferredTimeSlotConstraint` expresses preferences for assigning
Activities to particular TimeSlots.

### Type

```text
SOFT
```

Preferences are configured by Activity.

Conceptually:

```text
Activity A
→ preferred TimeSlots
   [slot-1, slot-2]
```

### Evaluation

If Activity A is assigned to:

```text
slot-1
```

or:

```text
slot-2
```

no violation is produced.

If it is assigned to another TimeSlot:

```text
one SOFT violation
raw penalty = 1
```

An Activity absent from the preference configuration has no preference
and therefore produces no violation.

### Academic Example

A Physical Education Activity prefers:

```text
afternoon-1
afternoon-2
```

but is assigned to:

```text
morning-1
```

The Schedule remains feasible but receives a SOFT penalty.

### Work Shift Example

A particular work Activity is preferably scheduled during one of a set
of configured morning TimeSlots.

Scheduling it outside those preferred TimeSlots produces the same
generic SOFT violation.

---

## 11. MaxConsecutiveConstraint

### Description

`MaxConsecutiveConstraint` discourages assigning a Resource to too many
consecutive activities.

### Type

```text
SOFT
```

Two assignments are consecutive when:

```text
previousTimeSlot.end
=
nextTimeSlot.start
```

### Evaluation

Assignments are evaluated chronologically for each relevant Resource.

If the number of consecutive assignments exceeds the configured
maximum, the excess contributes to the SOFT penalty.

### Example

Suppose:

```text
maximum consecutive assignments = 3
```

and a Resource receives:

```text
09:00–10:00
10:00–11:00
11:00–12:00
12:00–13:00
```

The Resource has four consecutive assignments.

The configured maximum is exceeded, producing a SOFT penalty.

### Domain Independence

In Academic Scheduling this may represent consecutive classes for a
teacher.

In Work Shift Scheduling it may represent consecutive work periods for
an employee.

The constraint itself only operates on generic Resources, Assignments,
and TimeSlots.

---

## 12. BalancedWorkloadConstraint

### Description

`BalancedWorkloadConstraint` encourages assignments to be distributed
more evenly between configured Resources.

### Type

```text
SOFT
```

### Workload Measure

For the current implementation, workload is measured using the number
of assignments associated with each configured Resource.

For example:

```text
Resource A → 5 assignments
Resource B → 3 assignments
Resource C → 3 assignments
```

The workload difference is based on the maximum and minimum assignment
counts.

### Evaluation

The constraint produces a SOFT penalty when the workload difference
exceeds the configured accepted balance.

The constraint does not make an uneven Schedule infeasible.

### Academic Example

Teaching activities should preferably be distributed reasonably evenly
between a configured group of teachers.

A strongly unbalanced distribution receives a penalty.

### Work Shift Example

Work assignments should preferably be distributed between a configured
group of employees.

Again, the same constraint is reused without domain-specific logic.

---

## 13. Constraint Evaluation

Every implemented constraint produces a `ConstraintResult`.

Conceptually:

```text
Constraint
    ↓
evaluate(Schedule)
    ↓
ConstraintViolation[]
    ↓
ConstraintResult
```

A result provides:

```text
violationDetails
violations
rawPenalty
weightedPenalty
```

where:

```text
rawPenalty =
Σ individual violation penalties
```

and:

```text
weightedPenalty =
rawPenalty × constraint weight
```

HARD and SOFT constraints use the same weighting formula.

There is no hidden global HARD multiplier.

Detailed evaluation semantics are documented in:

```text
docs/architecture/constraint-evaluation.md
```

---

## 14. Constraint Catalogue and Fitness

The implemented catalogue contributes to fitness as follows:

```text
HARD
│
├── NoOverlapConstraint
├── AvailabilityConstraint
├── RequiredResourceConstraint
└── LocationCapacityConstraint
        │
        ▼
    hardPenalty


SOFT
│
├── PreferredTimeSlotConstraint
├── MaxConsecutiveConstraint
└── BalancedWorkloadConstraint
        │
        ▼
    softPenalty
```

The Genetic Engine minimizes:

```text
fitness =
hardPenalty + softPenalty
```

while feasibility is determined separately:

```text
HARD violations = 0
→ feasible

HARD violations > 0
→ infeasible
```

Therefore fitness and feasibility are related but distinct concepts.

---

## 15. Genericity Across Templates

The constraint catalogue remains independent from planning templates.

| Constraint | Academic Scheduling | Work Shift Scheduling |
| --- | --- | --- |
| `NoOverlapConstraint` | Teacher cannot teach overlapping classes | Employee cannot perform overlapping assignments |
| `AvailabilityConstraint` | Teacher only teaches when available | Employee only works when available |
| `RequiredResourceConstraint` | Required teaching resources must be assigned | Required work resources must be assigned |
| `LocationCapacityConstraint` | Classroom must have sufficient capacity | Training/work location must have sufficient capacity |
| `PreferredTimeSlotConstraint` | Activity prefers particular teaching periods | Activity prefers particular work periods |
| `MaxConsecutiveConstraint` | Limit consecutive teaching periods | Limit consecutive work periods |
| `BalancedWorkloadConstraint` | Balance activities between teachers | Balance assignments between employees |

These descriptions are template-level interpretations only.

The constraint implementations operate exclusively on generic planning
concepts.

---

## 16. Relationship with ResourceRequirement

`ResourceRequirement` and `RequiredResourceConstraint` have different
responsibilities.

### ResourceRequirement

`ResourceRequirement` describes the Resources structurally required by
an Activity.

For example:

```text
Activity
requires
    resource type = TEACHER
    quantity = 1
```

It defines:

```text
What Resources does this Activity require?
```

Candidate generation uses this information when constructing possible
AssignmentOptions.

### RequiredResourceConstraint

`RequiredResourceConstraint` evaluates whether the final Assignment
actually satisfies those requirements.

It answers:

```text
Does this Assignment correctly satisfy the Activity's
ResourceRequirements?
```

Therefore:

```text
ResourceRequirement
        │
        ├── describes structural requirements
        │
        ▼
AssignmentCandidateGenerator
        │
        ▼
Assignment
        │
        ▼
RequiredResourceConstraint
        │
        └── verifies the final assignment
```

This keeps structural candidate generation and Schedule validation as
separate responsibilities.

---

## 17. Structural Validity vs Constraint Validity

Not every constraint is used to remove candidate options before genetic
optimization.

Candidate generation creates structurally possible AssignmentOptions.

The Genetic Algorithm may combine those options into a Schedule that
violates planning constraints.

For example:

```text
Activity A
→ Resource R1
→ Monday 09:00

Activity B
→ Resource R1
→ Monday 09:00
```

Both AssignmentOptions may individually be structurally valid.

Together they produce:

```text
Schedule
    ↓
NoOverlapConstraint
    ↓
HARD violation
```

This separation is intentional.

> Structural validity belongs to candidate generation; planning
> feasibility belongs to constraint evaluation.

It allows the Genetic Algorithm to explore infeasible schedules while
using penalties to guide evolution toward better solutions.

---

## 18. Extensibility

New constraints can be introduced by implementing the common
`Constraint` abstraction.

Conceptually:

```text
New Constraint
      │
      ▼
implements Constraint
      │
      ▼
evaluate(Schedule)
      │
      ▼
ConstraintResult
```

The Genetic Engine does not require changes when a new constraint is
introduced.

This is an important extensibility property of the architecture.

Possible future constraints include:

```text
MaximumAssignmentsConstraint
MinimumAssignmentsConstraint
DifferentDayConstraint
```

These are not part of the current implemented core catalogue.

They should only be introduced if required by future planning scenarios
or experimental evaluation.

---

## 19. Changes from the Initial Catalogue

The constraint catalogue was refined during M2 as the generic planning
domain and Genetic Engine were implemented.

### MaximumAssignmentsConstraint

The initial catalogue classified:

```text
MaximumAssignmentsConstraint
```

as a mandatory HARD constraint.

It was not included in the final M2 core catalogue.

The implemented workload-related SOFT objective is instead:

```text
BalancedWorkloadConstraint
```

A future `MaximumAssignmentsConstraint` may still be introduced if a
validation scenario requires an explicit upper workload limit.

### MinimumAssignmentsConstraint

The initial catalogue considered:

```text
MinimumAssignmentsConstraint
```

as a SHOULD constraint.

It was not implemented during M2.

Workload distribution is currently represented through
`BalancedWorkloadConstraint`.

### CapacityConstraint

The initial catalogue classified:

```text
CapacityConstraint
```

as an optional constraint.

During domain and constraint implementation, capacity became part of
the core catalogue as:

```text
LocationCapacityConstraint
```

The explicit name clarifies that the constraint evaluates the capacity
of the selected Location.

### MaxConsecutiveConstraint

Initially classified as SHOULD, it was implemented during M2 and now
forms part of the core SOFT catalogue.

### PreferredTimeSlotConstraint

Initially classified as SHOULD, it was implemented during M2 and now
forms part of the core SOFT catalogue.

### BalancedWorkloadConstraint

`BalancedWorkloadConstraint` was introduced during implementation as a
generic SOFT objective for improving workload distribution.

It replaces the need for minimum-assignment balancing in the initial
core implementation.

### DifferentDayConstraint

The initial catalogue identified:

```text
DifferentDayConstraint
```

as a possible extension.

It remains unimplemented and outside the current core catalogue.

---

## 20. Current Catalogue Summary

The implemented Genetic Planner constraint architecture is:

```text
                       Constraint
                           │
             ┌─────────────┴─────────────┐
             │                           │
            HARD                        SOFT
             │                           │
    ┌────────┼─────────┐        ┌────────┼─────────┐
    │        │         │        │        │         │
NoOverlap Availability Required  Preferred Max    Balanced
                      Resource   TimeSlot Consecutive Workload
    │
    └── LocationCapacity
```

More explicitly:

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

All seven constraints:

- Operate on the generic planning model.
- Produce detailed `ConstraintResult` information.
- Participate in weighted fitness evaluation.
- Remain independent from Jenetics.
- Remain independent from planning templates.
- Can be reused across different scheduling domains.

This catalogue provides the constraint foundation required by the
Genetic Planner MVP while preserving the extensibility needed for
future planning scenarios.