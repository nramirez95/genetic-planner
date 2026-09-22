# Genetic Planner — Chromosome Representation

## 1. Purpose

This document describes the chromosome representation implemented by
the Genetic Planner Genetic Engine.

The representation must:

- Remain independent from specific planning domains.
- Represent complete candidate schedules.
- Support heterogeneous candidate domains.
- Support mutation and crossover.
- Preserve structural validity.
- Allow planning constraint violations during evolution.
- Keep Jenetics types outside the domain model.
- Provide deterministic conversion between Jenetics genotypes and
  domain schedules.

The central representation decision is:

> Each Activity represents exactly one atomic scheduling decision.

Therefore:

```text
1 Activity
    ↓
1 AssignmentOption decision
    ↓
1 IntegerGene
    ↓
1 single-gene IntegerChromosome
```

and:

```text
N Activities
    ↓
N IntegerChromosomes
    ↓
1 Genotype
    ↓
1 complete candidate Schedule
```

---

## 2. Relationship with the Domain Model

The generic planning model establishes:

```text
PlanningProblem
    │
    └── Activities
            │
            ▼
       Genetic Engine
            │
            ▼
         Schedule
            │
            └── Assignments
```

For the MVP:

```text
1 Activity
=
1 atomic schedulable unit
```

Repeated real-world activities are represented as separate Activity
instances.

For example:

```text
Mathematics session 1
Mathematics session 2
Mathematics session 3
```

are three independent Activities and therefore three independent
genetic decisions.

This keeps recurrence and template-specific concepts outside the
Genetic Engine.

---

## 3. AssignmentOption

The genetic representation does not encode TimeSlot, Resource, and
Location independently.

Instead, every Activity has a precomputed ordered collection of
`AssignmentOption` objects.

The implemented model is:

```kotlin
data class AssignmentOption(
    val activityId: String,
    val timeSlotId: String,
    val resourceAssignments: Map<String, List<String>>,
    val locationId: String? = null
)
```

Each option therefore represents one complete structurally possible
assignment for one Activity.

Example:

```text
Activity A

Option 0
→ Slot 1
→ Resource 1
→ Room A

Option 1
→ Slot 1
→ Resource 2
→ Room A

Option 2
→ Slot 2
→ Resource 1
→ Room B
```

The genetic representation stores only the selected option index.

For example:

```text
allele = 2
```

means:

```text
Activity A
    ↓
AssignmentOption 2
    ↓
Slot 2 / Resource 1 / Room B
```

---

## 4. Candidate Generation

Assignment options are created before the Jenetics evolution starts by:

```text
AssignmentCandidateGenerator
```

The generator receives a `PlanningProblem` and creates:

```kotlin
AssignmentCandidateGenerationResult
```

containing one:

```kotlin
ActivityCandidateOptions
```

for every Activity.

Conceptually:

```text
PlanningProblem
      │
      ▼
AssignmentCandidateGenerator
      │
      ├── Activity A → [O0, O1, O2]
      ├── Activity B → [O0, O1]
      └── Activity C → [O0, O1, O2, O3]
```

The ordering produced by candidate generation is preserved by the
genetic codec.

The codec does not sort Activities or AssignmentOptions by identifier.

---

## 5. Candidate TimeSlots

For each Activity:

```text
allowedTimeSlotIds = null
```

means that all problem TimeSlots are structurally available.

An explicit set restricts the candidate TimeSlots to the referenced
identifiers.

An empty set produces no valid TimeSlot candidate.

Each `AssignmentOption` contains exactly one selected TimeSlot.

---

## 6. Candidate Resources

An Activity can define multiple `ResourceRequirement` objects.

Example:

```text
Activity

Requirement R1
type = teacher
quantity = 1

Requirement R2
type = student-group
quantity = 1
```

An AssignmentOption stores:

```kotlin
Map<ResourceRequirementId, List<ResourceId>>
```

The generator produces the valid resource combinations for each
requirement.

For a requirement:

```text
candidateResourceIds = null
```

all Resources matching its `resourceTypeId` may participate.

An explicit candidate set restricts the Resources that can participate.

`quantity` determines the exact number of distinct Resources selected
for the requirement.

For example:

```text
Candidates = [R1, R2, R3]
Quantity = 2
```

produces combinations equivalent to:

```text
[R1, R2]
[R1, R3]
[R2, R3]
```

When an Activity contains several ResourceRequirements, their
possibilities are combined through a Cartesian product.

An Activity without ResourceRequirements produces one empty resource
assignment.

---

## 7. Candidate Locations

Location selection follows the same structural candidate approach.

If an Activity defines explicit:

```text
allowedLocationIds
```

only those Locations participate.

If `allowedLocationIds` is `null` and the PlanningProblem contains
Locations, all Locations are structurally available.

If `allowedLocationIds` is empty, no assignment option can be produced.

If the PlanningProblem contains no Locations, candidate generation
creates a valid alternative with:

```text
locationId = null
```

Location capacity is not used to remove AssignmentOptions.

Capacity is evaluated later as a planning constraint.

---

## 8. Structural Validity vs Planning Feasibility

Candidate generation preserves structural validity.

For example, generated options reference:

- Existing TimeSlots.
- Existing Resources.
- Compatible ResourceTypes.
- Valid resource quantities.
- Existing Locations when applicable.

However, candidate generation intentionally does not guarantee planning
feasibility.

Consider:

```text
Activity A
Slot 1
Resource R1

Activity B
Slot 1
Resource R1
```

Both assignments may be structurally valid independently.

Together they may violate:

```text
NoOverlapConstraint
```

Therefore:

```text
Structural possibility
        ↓
AssignmentCandidateGenerator
        ↓
Genetic representation
```

while:

```text
Schedule-level validity
        ↓
ConstraintEvaluator
        ↓
HARD / SOFT violations
```

This separation is intentional.

---

## 9. Constraints Are Not Encoding Rules

The implementation distinguishes:

```text
Structural impossibility
        ↓
Do not generate AssignmentOption
```

from:

```text
Planning constraint violation
        ↓
Allow candidate Schedule
        ↓
Evaluate constraint
        ↓
Apply penalty
```

For this reason, candidate generation does not filter complete
solutions using constraints such as:

- `NoOverlapConstraint`
- `AvailabilityConstraint`
- `PreferredTimeSlotConstraint`
- `MaxConsecutiveConstraint`
- `BalancedWorkloadConstraint`
- `LocationCapacityConstraint`

This allows the Genetic Algorithm to explore infeasible regions of the
search space and use fitness penalties to guide evolution.

---

## 10. Jenetics Representation

The Jenetics representation is implemented by:

```text
ScheduleGenotypeCodec
```

Its dependency direction is:

```text
Domain / Candidate Model
          ▲
          │
ScheduleGenotypeCodec
          │
          ▼
       Jenetics
```

No Jenetics type is exposed by the domain model.

The physical representation is:

```text
Genotype<IntegerGene>
│
├── IntegerChromosome
│       └── IntegerGene → Activity 0
│
├── IntegerChromosome
│       └── IntegerGene → Activity 1
│
├── IntegerChromosome
│       └── IntegerGene → Activity 2
│
└── ...
```

Therefore:

> One Genotype contains one single-gene IntegerChromosome for every
> Activity.

---

## 11. Why One Chromosome per Activity

Different Activities can have different numbers of AssignmentOptions.

Example:

```text
Activity A → 3 options
Activity B → 5 options
Activity C → 2 options
```

The corresponding allele domains are:

```text
Activity A → 0..2
Activity B → 0..4
Activity C → 0..1
```

A single homogeneous integer chromosome would not naturally represent
these independent ranges.

The implemented representation therefore uses:

```text
Genotype
├── IntegerChromosome → Activity A domain
├── IntegerChromosome → Activity B domain
└── IntegerChromosome → Activity C domain
```

Each chromosome contains exactly one gene and has the domain required
by its Activity.

---

## 12. Conceptual vs Physical Representation

For documentation purposes, a candidate can be written conceptually as:

```text
[2, 4, 1]
```

meaning:

```text
Activity A → Option 2
Activity B → Option 4
Activity C → Option 1
```

Jenetics physically stores the same decisions as:

```text
Genotype
├── [2]
├── [4]
└── [1]
```

or conceptually:

```text
[[2], [4], [1]]
```

The distinction is important:

```text
[2,4,1]
```

is a convenient domain-level notation, while the actual Jenetics
representation uses heterogeneous single-gene chromosomes.

---

## 13. Stable Ordering

Candidate ordering must remain stable during an optimization execution.

Suppose candidate generation produces:

```text
position 0 → Activity A
position 1 → Activity B
position 2 → Activity C
```

The codec preserves this order when creating, encoding, and decoding
genotypes.

The AssignmentOption order for each Activity is also preserved.

The codec deliberately does not sort candidates by their IDs.

This stable positional mapping is required because:

```text
chromosome position
        ↕
ActivityCandidateOptions
        ↕
AssignmentOption domain
```

must remain consistent throughout the execution.

---

## 14. Allele Domains

For an Activity with `N` AssignmentOptions:

```text
valid semantic allele values
=
0 .. N-1
```

For example:

```text
3 options → 0..2
5 options → 0..4
2 options → 0..1
```

The allele is interpreted exclusively as an index into the ordered
AssignmentOption list.

Identifiers themselves are never encoded as integer values.

---

## 15. Single-Option Activities

An implementation-specific edge case exists when an Activity contains
exactly one AssignmentOption.

Semantically:

```text
options = [O0]
```

means:

```text
only valid allele = 0
```

A normal random integer chromosome with equal lower and upper bounds
cannot be generated using the same Jenetics construction used for
multi-option domains.

Therefore `ScheduleGenotypeCodec` handles this case explicitly by
constructing a fixed gene whose only usable value is:

```text
0
```

Conceptually:

```text
Activity
   │
   └── one AssignmentOption
             │
             ▼
          allele 0
```

This is an implementation adaptation only.

It does not change the conceptual rule:

> Every Activity corresponds to one genetic decision.

---

## 16. Missing Candidate Options

A genotype can only represent a complete Schedule if every Activity has
at least one AssignmentOption.

Therefore `ScheduleGenotypeCodec` rejects candidate generation results
where:

```text
ActivityCandidateOptions.options.isEmpty()
```

The Genetic Engine also checks the candidate generation result before
starting evolution.

This is a technical precondition of the genetic representation.

Structural validation of the complete `PlanningProblem` remains a
separate responsibility of the validation/application flow.

---

## 17. Genotype Creation

`ScheduleGenotypeCodec.createGenotype()` creates the genotype template
used by Jenetics.

Conceptually:

```text
Activity A → 3 options
Activity B → 5 options
Activity C → 2 options

          ↓

Genotype
├── chromosome A → allele 0..2
├── chromosome B → allele 0..4
└── chromosome C → allele 0..1
```

Every chromosome contains exactly one `IntegerGene`.

The resulting genotype always contains:

```text
number of chromosomes
=
number of Activities
```

---

## 18. Decoding

Decoding converts a Jenetics genotype into a domain `Schedule`.

For each chromosome position:

```text
chromosome position
        ↓
ActivityCandidateOptions
        ↓
IntegerGene allele
        ↓
AssignmentOption index
        ↓
Assignment
```

The selected option is transformed into:

```kotlin
Assignment(
    activityId = option.activityId,
    timeSlotId = option.timeSlotId,
    resourceAssignments = option.resourceAssignments,
    locationId = option.locationId
)
```

All assignments are then collected into:

```kotlin
Schedule(
    planningProblemId = planningProblemId,
    assignments = assignments
)
```

The decoder verifies that the genotype contains the expected number of
chromosomes and that each chromosome contains exactly one gene.

The selected allele must also correspond to an existing
AssignmentOption.

---

## 19. Encoding

`ScheduleGenotypeCodec` also supports the inverse operation:

```text
Schedule
    ↓
Genotype
```

Encoding is useful for:

- Codec validation.
- Tests.
- Round-trip verification.
- Converting known domain solutions into their genetic representation.

For every expected Activity, the codec:

```text
Activity
    ↓
Find exactly one Assignment
    ↓
Find matching AssignmentOption
    ↓
Resolve option index
    ↓
Create IntegerGene
```

The encoded Schedule must:

- Belong to the expected PlanningProblem.
- Contain exactly one Assignment for every expected Activity.
- Not contain duplicate assignments for an Activity.
- Use an Assignment that corresponds to a known AssignmentOption.

Otherwise encoding is rejected.

---

## 20. Round-Trip Consistency

The codec supports both transformations:

```text
Genotype
    ↓ decode
Schedule
    ↓ encode
Genotype
```

and:

```text
Schedule
    ↓ encode
Genotype
    ↓ decode
Schedule
```

For valid inputs, these transformations preserve the scheduling
decisions.

This behavior is covered by codec unit tests.

---

## 21. Complete Candidate Requirement

Every genotype represents a complete candidate Schedule.

Partial schedules are not part of the MVP representation.

Therefore:

```text
N Activities
=
N chromosomes
=
N Assignments after decoding
```

This representation makes every Activity participate in every candidate
solution.

Completeness does not imply feasibility.

A complete candidate can still violate HARD constraints.

---

## 22. Mutation

The Genetic Engine uses Jenetics `Mutator`.

Mutation changes genetic decision values while respecting the chromosome
domain.

Conceptually:

```text
Before

[2, 0, 4, 1]

        ↓ mutation

After

[2, 3, 4, 1]
```

The mutated value still identifies an AssignmentOption available to the
corresponding Activity.

Mutation can:

- Introduce HARD violations.
- Remove HARD violations.
- Increase SOFT penalties.
- Reduce SOFT penalties.

This is expected.

Genetic validity does not imply planning feasibility.

---

## 23. Crossover

The implemented Genetic Engine uses:

```text
SinglePointCrossover
```

Crossover combines Activity decisions from parent genotypes.

Conceptually:

```text
Parent A

[2, 0, 4, 1]

Parent B

[1, 3, 2, 0]

        ↓ single-point crossover

Child

[2, 0, 2, 0]
```

The child still contains one decision for every Activity.

However, crossover may create a Schedule that violates planning
constraints.

For example, two independently valid inherited assignments may use the
same Resource during overlapping TimeSlots.

Such a child remains genetically valid and is evaluated by the
Constraint Engine.

---

## 24. Search Space

If Activity `i` contains `Oi` AssignmentOptions, the theoretical search
space is:

```text
O1 × O2 × ... × On
```

Example:

```text
Activity A → 4 options
Activity B → 5 options
Activity C → 3 options

4 × 5 × 3
=
60 possible schedules
```

This multiplicative growth is one of the reasons for using a
metaheuristic optimization strategy instead of exhaustive enumeration.

---

## 25. Candidate-Space Growth

Materializing AssignmentOptions can itself become expensive.

For example:

```text
20 TimeSlots
×
10 Resource alternatives
×
5 Locations
=
1,000 possible combinations
```

for a single Activity before considering other dimensions.

The current MVP deliberately precomputes AssignmentOptions because it
keeps the genetic representation and constraint evaluation simple.

If candidate generation becomes a scalability bottleneck, future
versions could encode TimeSlot, Resource, and Location decisions
separately.

That alternative is not required for the current MVP.

---

## 26. Domain Independence

The representation uses only generic planning concepts:

```text
Activity
Resource
ResourceRequirement
TimeSlot
Location
AssignmentOption
Assignment
Schedule
```

For an academic planning template:

```text
Mathematics lesson
        ↓
Activity
        ↓
AssignmentOption index
        ↓
IntegerGene
```

For a work-shift template:

```text
Reception assignment
        ↓
Activity
        ↓
AssignmentOption index
        ↓
IntegerGene
```

The Genetic Engine does not need to know whether a Resource represents
a teacher, employee, student group, or another template-specific
concept.

---

## 27. Jenetics ↔ Domain Boundary

The complete adaptation is:

```text
                    DOMAIN
                      │
                      ▼
               PlanningProblem
                      │
                      ▼
          AssignmentCandidateGenerator
                      │
                      ▼
       AssignmentCandidateGenerationResult
                      │
                      ▼
             ScheduleGenotypeCodec
                      │
              ┌───────┴────────┐
              │                │
           encode            decode
              │                │
              ▼                ▼
           JENETICS          Schedule
           Genotype             │
              │                 ▼
              │         ConstraintEvaluator
              │                 │
              └──── evolution   ▼
                         ScheduleEvaluation
```

The dependency direction remains:

```text
Domain
  ▲
  │
Genetic infrastructure
  │
  ▼
Jenetics
```

Domain classes never depend on Jenetics.

---

## 28. Implementation Decisions

### CH1 — One Activity is one genetic decision

Every Activity represents one atomic schedulable unit and selects one
AssignmentOption.

### CH2 — One single-gene chromosome per Activity

The physical Jenetics representation uses one `IntegerChromosome`
containing one `IntegerGene` for every Activity.

### CH3 — One Genotype represents one complete candidate Schedule

Partial candidate schedules are not supported.

### CH4 — Alleles are AssignmentOption indexes

Genes contain compact integer indexes instead of domain objects.

### CH5 — Candidate options are external to genotypes

Complete AssignmentOption objects are generated once and reused by the
codec.

### CH6 — Candidate domains may be heterogeneous

Every Activity can have a different number of AssignmentOptions.

### CH7 — Candidate order is preserved

Activity and AssignmentOption ordering from candidate generation is
used directly by the codec.

### CH8 — Candidate generation handles structural possibilities

Complete-schedule planning constraints are not used to prune the
genetic search space.

### CH9 — Planning feasibility is evaluated after decoding

HARD and SOFT constraints operate on domain `Schedule` objects.

### CH10 — Encoding and decoding are both supported

`ScheduleGenotypeCodec` provides deterministic conversion between the
domain and genetic representations.

### CH11 — Single-option Activities use a fixed allele

An Activity with one AssignmentOption is represented using allele zero
without changing the general chromosome structure.

### CH12 — Missing candidate domains are rejected

Evolution cannot start if any Activity has zero AssignmentOptions.

### CH13 — Domain identifiers remain opaque

IDs are not converted into numeric genetic values. Alleles represent
positions in candidate lists only.

### CH14 — The representation remains domain-independent

Academic and Work Shift planning use exactly the same codec.

### CH15 — Jenetics remains an infrastructure dependency

No Jenetics type appears in the planning domain.

---

## 29. Deviations from Initial Design

The initial chromosome design was largely validated by implementation,
but several details were refined during M2.

### CandidateSpaceBuilder was replaced by the implemented candidate model

The initial design referred conceptually to:

```text
CandidateSpaceBuilder
ActivityGeneDomain
```

The implemented types are:

```text
AssignmentCandidateGenerator
AssignmentCandidateGenerationResult
ActivityCandidateOptions
AssignmentOption
```

The underlying concept remains unchanged: every Activity receives an
ordered candidate domain before evolution begins.

### AssignmentOption includes activityId

The initial conceptual model represented an option mainly through its
TimeSlot, Resources, and Location.

The implemented `AssignmentOption` also contains:

```text
activityId
```

This makes the Activity association explicit and supports conversion to
domain Assignments.

### Problem validation was separated from genotype encoding

The initial flow showed:

```text
PlanningProblem
    ↓
ProblemValidator
    ↓
Encoding
```

The final architecture keeps full problem validation as an
application/orchestration responsibility.

`ScheduleGenotypeCodec` only enforces the technical preconditions needed
to represent the candidate domains.

In particular, activities with no AssignmentOptions are rejected before
evolution.

### Encoding terminology was refined

The initial document described encoding mainly as the transformation
from `PlanningProblem` to a genotype.

The implementation separates three responsibilities:

```text
PlanningProblem
        ↓
AssignmentCandidateGenerator
        ↓
candidate domains
        ↓
ScheduleGenotypeCodec.createGenotype()
```

while `ScheduleGenotypeCodec.encode(schedule)` specifically means:

```text
Schedule → Genotype
```

This distinction avoids ambiguity between candidate-space construction
and codec encoding.

### Single-option handling was added

The initial design did not identify the Jenetics edge case produced by
an Activity with exactly one candidate.

The implemented codec explicitly represents that case using the only
semantic allele:

```text
0
```

### Crossover is no longer unspecified

The initial design required the selected crossover operator to preserve
Activity/domain correspondence but left the operator undecided.

The implementation uses:

```text
SinglePointCrossover
```

### Jenetics range details were refined

The conceptual domain remains:

```text
N options → indexes 0..N-1
```

Implementation and tests focus on the valid allele values rather than
depending on Jenetics internal range metadata.

This was necessary because different Jenetics construction APIs expose
range bounds differently, particularly for manually constructed
single-option genes.

---

## 30. Current Limitations

The current representation intentionally:

- Materializes AssignmentOptions before evolution.
- Represents each Activity as one atomic scheduling decision.
- Does not represent partial schedules.
- Does not encode recurrence directly.
- Does not use domain-specific chromosomes.
- Does not dynamically resize candidate domains during evolution.

These decisions keep the MVP representation simple and generic.

They can be revisited if scalability experiments identify candidate
generation or chromosome representation as a bottleneck.

---

## 31. Summary

The implemented representation is:

```text
PlanningProblem
        │
        ▼
AssignmentCandidateGenerator
        │
        ▼
ActivityCandidateOptions
        │
        ▼
ScheduleGenotypeCodec
        │
        ▼
Genotype<IntegerGene>
│
├── IntegerChromosome → Activity 0
│       └── one IntegerGene
│
├── IntegerChromosome → Activity 1
│       └── one IntegerGene
│
└── ...
        │
        ▼
Jenetics evolution
        │
        ▼
Evolved Genotype
        │
        ▼
ScheduleGenotypeCodec.decode()
        │
        ▼
Schedule
        │
        ▼
ConstraintEvaluator
        │
        ▼
ScheduleEvaluation
```

At conceptual level:

```text
Gene
=
selected AssignmentOption index for one Activity
```

```text
Genotype
=
ordered collection of all Activity decisions
=
one complete candidate Schedule
```

The central implementation principle is:

> The genotype stores compact indexes into structurally valid assignment
> alternatives; the domain defines planning semantics; and the
> Constraint Engine evaluates the quality and feasibility of the decoded
> Schedule.