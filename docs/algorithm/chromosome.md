# Genetic Planner — Chromosome Representation

## 1. Purpose

This document defines how candidate planning solutions are represented inside the Genetic Algorithm.

The representation must:

* Remain independent from specific planning domains.
* Represent complete schedules.
* Work with the generic planning model.
* Support mutation and crossover.
* Permit constraint violations during evolution.
* Preserve structural validity.
* Remain independent from Jenetics at domain level.
* Allow deterministic conversion between genetic representation and `Schedule`.

The central conceptual decision is:

> **Each genetic decision represents the assignment of exactly one Activity.**

Therefore:

```text
1 Activity
    ↓
1 genetic decision
```

and:

```text
N Activities
    ↓
N genetic decisions
    ↓
1 candidate Schedule
```

---

# 2. Relationship with the Domain Model

The generic domain model establishes:

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

Every complete `Schedule` contains exactly one `Assignment` for every `Activity`.

The genetic representation follows the same semantic structure:

```text
Activity 0 → Decision 0 → Assignment 0
Activity 1 → Decision 1 → Assignment 1
Activity 2 → Decision 2 → Assignment 2
...
Activity N → Decision N → Assignment N
```

---

# 3. Gene

Conceptually, a gene represents the scheduling decision associated with one `Activity`.

It determines the selected:

```text
TimeSlot
Resources
Location
```

through an `AssignmentOption`.

The Activity itself is fixed during evolution.

Therefore:

```text
Activity
    = fixed

AssignmentOption
    = evolvable
```

---

# 4. Assignment Option

Each genetic decision selects one precomputed `AssignmentOption`.

Conceptually:

```kotlin
data class AssignmentOption(
    val timeSlotId: String,
    val resourceAssignments: Map<String, List<String>>,
    val locationId: String?
)
```

Each `Activity` therefore owns an ordered list of structurally valid alternatives:

```text
Activity
    ↓
List<AssignmentOption>
```

Example:

```text
Activity A

Option 0
Monday 09:00 / Ana / Room 1

Option 1
Monday 10:00 / Pedro / Room 1

Option 2
Tuesday 09:00 / Ana / Room 2
```

The gene stores the selected option index:

```text
Gene value = 2
```

meaning:

```text
Activity A
    ↓
AssignmentOption 2
```

---

# 5. TimeSlot Representation

Every `AssignmentOption` contains exactly one `TimeSlot`.

If the Activity defines:

```text
allowedTimeSlotIds
```

only these slots are considered structurally possible.

Otherwise, all structurally compatible TimeSlots may participate in candidate generation.

---

# 6. Resource Representation

An Activity can contain multiple `ResourceRequirement` objects.

Example:

```text
Mathematics 1A

Teacher
    quantity = 1
    candidates = [Ana, Pedro]

Student Group
    quantity = 1
    candidates = [1A]
```

An Assignment Option contains the selected resources for every requirement.

Conceptually:

```kotlin
Map<ResourceRequirementId, List<ResourceId>>
```

The selected resource count must satisfy the structural requirement quantity.

---

# 7. Fixed and Variable Resources

The representation supports both fixed and variable resource decisions.

Example:

```text
Teacher candidates:
[Ana]
```

produces only Ana-compatible alternatives.

By contrast:

```text
Teacher candidates:
[Ana, Pedro, Laura]
```

allows different Assignment Options to contain different teachers.

The same mechanism works with employees or any other Resource type.

---

# 8. Location Representation

If an Activity uses a Location, each Assignment Option contains one candidate `locationId`.

If no Location is required:

```text
locationId = null
```

Location therefore remains optional.

---

# 9. Complete Gene Example

Suppose an Activity has:

```text
Teachers:
Ana
Pedro

TimeSlots:
Monday 09:00
Monday 10:00
Tuesday 09:00

Locations:
Room 1
Room 2
```

Possible Assignment Options may be:

```text
Option 0
Monday 09:00 / Ana / Room 1

Option 1
Monday 10:00 / Ana / Room 2

Option 2
Tuesday 09:00 / Pedro / Room 1
```

A gene with value:

```text
2
```

selects `Option 2`.

---

# 10. Conceptual Planning Chromosome

At application/design level, the Planning Chromosome is the ordered collection of Activity decisions.

Example:

```text
Activity order:

0 → Mathematics
1 → Physics
2 → English
3 → Chemistry
```

A conceptual chromosome:

```text
[2,0,4,1]
```

means:

```text
Activity 0 → AssignmentOption 2
Activity 1 → AssignmentOption 0
Activity 2 → AssignmentOption 4
Activity 3 → AssignmentOption 1
```

Therefore:

```text
conceptual chromosome length
=
number of Activities
```

---

# 11. Stable Activity Ordering

The Activity-to-position relationship must remain stable during one optimization execution.

Example:

```text
position 0 → activity-001
position 1 → activity-002
position 2 → activity-003
```

The mapping must be deterministic.

It is required for:

* Encoding.
* Decoding.
* Mutation.
* Crossover.
* Reproducibility.
* Fitness evaluation.

---

# 12. Genotype

A `Genotype` represents one complete candidate planning solution.

Conceptually:

```text
Genotype
    ↓
Complete genetic representation
of one candidate Schedule
```

After the Jenetics Proof of Concept, the selected physical representation is:

```text
Genotype
│
├── IntegerChromosome 0
│       └── one IntegerGene → Activity 0
│
├── IntegerChromosome 1
│       └── one IntegerGene → Activity 1
│
├── IntegerChromosome 2
│       └── one IntegerGene → Activity 2
│
└── ...
```

Therefore:

> One Genotype contains one single-gene `IntegerChromosome` for every Activity.

This allows every Activity to define an independent candidate range.

---

# 13. Conceptual vs Jenetics Representation

The application-level representation remains:

```text
[2,5,1]
```

Jenetics physically represents the same candidate as:

```text
Genotype

├── [2]
├── [5]
└── [1]
```

The semantic interpretation remains:

```text
Activity 0 → Option 2
Activity 1 → Option 5
Activity 2 → Option 1
```

This distinction prevents Jenetics implementation details from leaking into the domain model.

---

# 14. Candidate Space

Before creating genetic candidates, the Genetic Engine builds the valid structural alternatives for every Activity.

```text
Activity
   │
   ├── Candidate TimeSlots
   ├── Candidate Resources
   └── Candidate Locations
   │
   ▼
AssignmentOptions
```

Conceptually:

```kotlin
data class ActivityGeneDomain(
    val activityId: String,
    val options: List<AssignmentOption>
)
```

For an Activity with `N` Assignment Options:

```text
valid allele values
=
0 .. N-1
```

---

# 15. Heterogeneous Candidate Domains

Different Activities can have different numbers of Assignment Options.

Example:

```text
Activity A → 3 options
Activity B → 9 options
Activity C → 5 options
```

Therefore:

```text
Activity A gene → 0..2
Activity B gene → 0..8
Activity C gene → 0..4
```

This requirement was explicitly validated during the Jenetics PoC.

The corresponding Jenetics representation is:

```text
Genotype
│
├── IntegerChromosome [0..2] → Activity A
├── IntegerChromosome [0..8] → Activity B
└── IntegerChromosome [0..4] → Activity C
```

Each chromosome contains exactly one gene.

---

# 16. Separation of Candidate Space and Genotype

The complete Assignment Options are not copied into every genotype.

The candidate space is generated once:

```text
Activity A
→ Option 0
→ Option 1
→ Option 2

Activity B
→ Option 0
→ ...
→ Option 8
```

The genotype stores only indexes:

```text
[[2], [5]]
```

Conceptually:

```text
[2,5]
```

This keeps individuals compact.

---

# 17. Encoding

Encoding transforms a `PlanningProblem` into the genetic representation.

```text
PlanningProblem
      │
      ▼
Problem validation
      │
      ▼
Deterministic Activity order
      │
      ▼
CandidateSpaceBuilder
      │
      ▼
AssignmentOptions per Activity
      │
      ▼
One IntegerChromosome per Activity
      │
      ▼
Genotype
```

---

# 18. Encoding Step 1 — Validate PlanningProblem

Before encoding:

* Activities must exist.
* TimeSlots must exist.
* Resource references must be valid.
* ResourceRequirements must be structurally satisfiable.
* Location references must be valid.
* Every Activity must have at least one structurally possible assignment.

If any Activity has zero options, optimization must not start.

---

# 19. Encoding Step 2 — Establish Activity Order

The encoder creates a deterministic ordering:

```text
0 → Activity A
1 → Activity B
2 → Activity C
```

This mapping is immutable for the complete optimization run.

---

# 20. Encoding Step 3 — Generate Assignment Options

Example:

```text
Activity A

Option 0
Monday 09:00 / Ana / Room 1

Option 1
Monday 10:00 / Pedro / Room 1

Option 2
Tuesday 09:00 / Ana / Room 2
```

Result:

```text
Activity A → [Option 0, Option 1, Option 2]
```

---

# 21. Encoding Step 4 — Create Jenetics Domains

For:

```text
Activity A → 3 options
Activity B → 9 options
Activity C → 5 options
```

Jenetics receives conceptually:

```kotlin
Genotype.of(
    IntegerChromosome.of(0, 3, 1),
    IntegerChromosome.of(0, 9, 1),
    IntegerChromosome.of(0, 5, 1)
)
```

The valid allele ranges are therefore:

```text
Activity A → 0..2
Activity B → 0..8
Activity C → 0..4
```

---

# 22. Encoding Example

Suppose:

```text
A1 → [O10, O11, O12]
A2 → [O20, O21]
A3 → [O30, O31, O32, O33]
```

A candidate may physically be:

```text
[[1], [0], [3]]
```

and conceptually:

```text
[1,0,3]
```

meaning:

```text
A1 → O11
A2 → O20
A3 → O33
```

---

# 23. Structural Validity

Encoding guarantees:

* One genetic decision per Activity.
* Every allele references an existing AssignmentOption.
* Every selected TimeSlot exists.
* Resources exist and match required ResourceTypes.
* Resource quantities are valid.
* Locations exist when required.
* All references belong to the same PlanningProblem.

These are structural invariants.

---

# 24. Structural Validity vs Constraint Validity

A structurally valid candidate may violate planning constraints.

Example:

```text
Mathematics
Monday 09:00
Teacher Ana

Physics
Monday 09:00
Teacher Ana
```

Both Assignments can be structurally valid.

However:

```text
NoOverlapConstraint
    ↓
HARD violation
```

The genotype remains valid.

The decoded Schedule is simply infeasible.

---

# 25. Constraints Are Not Encoding Rules

The generic distinction is:

```text
Structural impossibility
    ↓
Do not generate AssignmentOption

Planning constraint violation
    ↓
Allow candidate
    ↓
Evaluate and penalize
```

Constraints such as:

* Availability.
* NoOverlap.
* MaximumAssignments.
* PreferredTimeSlot.

must generally remain inside the Constraint Engine.

---

# 26. RequiredResource Consideration

Suppose:

```text
Teacher candidates:
Ana
Pedro

RequiredResourceConstraint:
Ana
```

Both Ana and Pedro may remain structurally possible.

Selecting Pedro creates:

```text
RequiredResourceConstraint
    ↓
HARD violation
```

This keeps configured constraint semantics centralized in the Constraint Engine.

---

# 27. Decoding

Decoding performs the inverse transformation:

```text
Genotype
      ↓
Read one allele per chromosome
      ↓
Resolve Activity by position
      ↓
Resolve AssignmentOption
      ↓
Create Assignment
      ↓
Schedule
```

---

# 28. Decoding Algorithm

For each chromosome position `i`:

```text
activity =
activityOrder[i]

selectedOptionIndex =
genotype.chromosome[i].gene.allele

option =
candidateSpace[i][selectedOptionIndex]
```

Then:

```kotlin
Assignment(
    activityId = activity.id,
    timeSlotId = option.timeSlotId,
    resourceAssignments = option.resourceAssignments,
    locationId = option.locationId
)
```

All Assignments form:

```kotlin
Schedule(
    planningProblemId = problem.id,
    assignments = assignments
)
```

---

# 29. Codec Boundary

Jenetics provides a `Codec` abstraction that can encapsulate:

```text
Genotype
    ↓
decoder
    ↓
Schedule
```

The intended production flow is therefore:

```text
Jenetics Genotype
      ↓
Codec / Decoder
      ↓
Domain Schedule
```

The Constraint Engine evaluates the decoded `Schedule`, not Jenetics objects.

---

# 30. Complete Flow

```text
PlanningProblem
        ↓

ProblemValidator
        ↓

CandidateSpaceBuilder
        ↓

ENCODING
        ↓

Genotype
├── [gene for Activity 0]
├── [gene for Activity 1]
├── [gene for Activity 2]
└── ...

        ↓ evolution

Evolved Genotype

        ↓ DECODING

Schedule

        ↓

ConstraintEvaluator

        ↓

ScheduleEvaluation

        ↓

Fitness
```

---

# 31. Complete Candidate Requirement

Every Genotype represents a complete candidate Schedule.

Partial schedules are not supported in the MVP.

Therefore:

```text
number of Jenetics chromosomes
=
number of Activities
```

and each Jenetics chromosome contains:

```text
1 IntegerGene
```

---

# 32. Mutation

Mutation changes the selected option of an Activity.

Conceptually:

```text
Before:
[2,0,4,1]

After:
[2,3,4,1]
```

Only the Activity associated with position 1 changes.

---

# 33. Mutation and Structural Validity

Because every Activity chromosome has its own valid integer range, mutation remains within that range.

Example:

```text
Activity A
3 AssignmentOptions

valid alleles:
0..2
```

An invalid allele such as `8` cannot represent a valid value for that chromosome.

This supports structural validity by construction.

---

# 34. Mutation and Constraints

Mutation can still:

* Create HARD violations.
* Remove HARD violations.
* Increase SOFT penalties.
* Reduce SOFT penalties.

This is expected.

Genetic validity does not imply planning feasibility.

---

# 35. Crossover

Crossover combines Activity decisions from parent candidate solutions.

Conceptually:

```text
Parent A:
[2,0,4,1]

Parent B:
[1,3,2,0]

Child:
[2,0,2,0]
```

The child inherits assignment decisions from both parents.

---

# 36. Crossover and Feasibility

Two feasible parents can produce an infeasible child.

Example:

```text
Parent A:
A → Monday / Ana
B → Tuesday / Ana

Parent B:
A → Tuesday / Ana
B → Monday / Ana
```

A child could contain:

```text
A → Monday / Ana
B → Monday / Ana
```

creating:

```text
NoOverlapConstraint
    ↓
HARD violation
```

This is normal Genetic Algorithm behaviour.

---

# 37. Crossover and Heterogeneous Domains

The Jenetics PoC confirmed that heterogeneous domains can be represented through one chromosome per Activity.

The invariant is:

```text
chromosome position
        ↕
Activity
        ↕
candidate domain
```

The selected production crossover operator must preserve this relationship.

The exact operator belongs to the Genetic Algorithm configuration task.

---

# 38. Academic Scheduling Validation

Example:

```text
Activity:
Mathematics lesson

Resources:
Teacher Ana
Student Group 1A

TimeSlot:
Monday 09:00

Location:
Room 2
```

is encoded as:

```text
Activity
    ↓
AssignmentOption index
    ↓
IntegerGene
```

No academic-specific type appears in the genetic representation.

---

# 39. Work Shift Scheduling Validation

Example:

```text
Activity:
Reception assignment

Resource:
Employee Laura

TimeSlot:
Monday 08:00–14:00

Location:
Reception
```

uses exactly the same representation:

```text
Activity
    ↓
AssignmentOption index
    ↓
IntegerGene
```

No work-specific type appears in the Genetic Engine.

---

# 40. Cross-Domain Validation

The Genetic Engine only knows generic concepts:

```text
Activity
Resource
ResourceRequirement
TimeSlot
Location
AssignmentOption
Schedule
```

It does not know:

```text
Teacher
Student
Subject
Employee
Shift
```

Therefore the same encoder, genotype and decoder can support both target domains.

---

# 41. Search Space

If Activity `i` contains `Oi` Assignment Options, the theoretical search space is:

```text
O1 × O2 × ... × On
```

Example:

```text
A → 4 options
B → 5 options
C → 3 options

4 × 5 × 3
=
60 schedules
```

The rapid growth of this value motivates the Genetic Algorithm.

---

# 42. AssignmentOption Explosion

Materializing Assignment Options can itself become expensive.

Example:

```text
20 TimeSlots
×
10 Resources
×
5 Locations

=
1,000 alternatives
```

Candidate generation must therefore apply structural filters before creating combinations.

This risk remains independent from Jenetics.

---

# 43. MVP Representation

The validated MVP representation is:

```text
PlanningProblem
      ↓
CandidateSpace
      ↓
One option domain per Activity
      ↓
Genotype
├── one single-gene IntegerChromosome per Activity
      ↓
Evolution
      ↓
Decoder / Codec
      ↓
Schedule
```

Conceptually the same candidate is still represented as:

```text
[2,0,4,...]
```

---

# 44. Future Alternatives

If AssignmentOption enumeration becomes a scalability bottleneck, future versions may separate:

```text
TimeSlot decision
Resource decision
Location decision
```

into different genetic dimensions.

This is not required for the MVP.

---

# 45. Jenetics Boundary

Domain classes remain free of Jenetics types.

The dependency direction is:

```text
Domain Model
      ▲
      │
Genetic Engine Adapter
      │
      ▼
Jenetics
```

Jenetics-specific code belongs to the Genetic Engine infrastructure.

---

# 46. Jenetics Validation Result

The Proof of Concept was executed using:

```text
Java       25
Gradle     9.7.0
Kotlin     2.4.0
Jenetics   9.1.0
```

The homogeneous test reached:

```text
Best genotype: [[[4],[4],[4],[4]]]
Best fitness: 16
```

The heterogeneous test also successfully represented independent Activity domains and reached its expected optimum.

Therefore the representation defined in this document is technically viable with Jenetics.

---

# 47. Representation Architecture

```text
PlanningProblem
      │
      ▼
ProblemValidator
      │
      ▼
CandidateSpaceBuilder
      │
      ▼
Encoding
      │
      ▼
Genotype
│
├── Activity 0 IntegerChromosome
├── Activity 1 IntegerChromosome
├── Activity 2 IntegerChromosome
└── ...
      │
      ▼
Jenetics Evolution
      │
      ▼
Genotype
      │
      ▼
Codec / Decoder
      │
      ▼
Schedule
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

---

# 48. Design Decisions

## CH1 — One genetic decision per Activity

Each Activity selects exactly one AssignmentOption.

## CH2 — Complete candidate solutions

Every Genotype represents a complete candidate Schedule.

## CH3 — Stable Activity ordering

Activity position remains fixed during one optimization run.

## CH4 — AssignmentOption indexes are evolved

Genes store compact option indexes rather than domain objects.

## CH5 — AssignmentOptions contain the scheduling decision

Each option contains TimeSlot, Resources and optional Location.

## CH6 — Candidate space is external to individuals

Options are built once and reused.

## CH7 — Encoding preserves structural validity

Only structurally valid AssignmentOptions are generated.

## CH8 — Planning feasibility is evaluated separately

HARD and SOFT constraints are not generally encoded into chromosome structure.

## CH9 — One Jenetics IntegerChromosome per Activity

The Jenetics implementation uses a single-gene chromosome for each Activity.

## CH10 — Heterogeneous domains are supported

Every Activity chromosome has an independent integer range.

## CH11 — Conceptual chromosome remains domain-level notation

The application may continue to describe candidates as:

```text
[2,5,1]
```

even though Jenetics physically stores:

```text
[[2],[5],[1]]
```

## CH12 — Mutation preserves candidate range

Mutation must remain inside the corresponding Activity domain.

## CH13 — Crossover must preserve Activity/domain correspondence

The final crossover operator must respect the heterogeneous chromosome structure.

## CH14 — Encoding and decoding are domain-independent

Academic and Work Shift planning use the same mechanisms.

## CH15 — Domain model remains independent from Jenetics

Jenetics types are isolated in the Genetic Engine infrastructure.

## CH16 — Codec/Decoder bridges genetic and domain representations

The evolved Genotype is decoded into `Schedule` before planning evaluation.

---

# 49. Scope Boundary

This document defines:

* Gene semantics.
* Conceptual chromosome semantics.
* Jenetics physical representation.
* Genotype semantics.
* Candidate-space representation.
* Encoding.
* Decoding.
* Structural validity.
* Mutation implications.
* Crossover implications.
* Cross-domain applicability.
* Jenetics integration boundary.

It does not define:

* Final fitness formula.
* Population size for production.
* Mutation probability.
* Crossover probability.
* Selection strategy.
* Final alterers.
* Termination criteria.

These belong to the following Genetic Algorithm design tasks.

---

# 50. Summary

The final validated representation is:

```text
PlanningProblem
        ↓
CandidateSpaceBuilder
        ↓
ENCODING
        ↓

Genotype
│
├── [Gene Activity 0]
├── [Gene Activity 1]
├── [Gene Activity 2]
└── ...

        ↓
Genetic Evolution
        ↓

Evolved Genotype

        ↓
DECODING
        ↓

Schedule

        ↓
ConstraintEvaluator
        ↓
ScheduleEvaluation
        ↓
Fitness
```

At conceptual level:

```text
Gene
    = selected AssignmentOption for one Activity

Planning Chromosome
    = ordered sequence of Activity decisions

Genotype
    = complete candidate solution
```

At Jenetics implementation level:

```text
Activity
    ↓
one IntegerGene
    ↓
one single-gene IntegerChromosome

N Activities
    ↓
N IntegerChromosomes
    ↓
one Genotype
```

The key principle remains:

> **The genetic representation stores scheduling decisions, the domain model defines the planning problem, and the Constraint Engine determines the quality and feasibility of the decoded Schedule.**

The Jenetics Proof of Concept confirms that this representation is technically viable for Genetic Planner 1.0.