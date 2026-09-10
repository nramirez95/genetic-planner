# Genetic Planner — Chromosome Representation

## 1. Purpose

This document defines how candidate planning solutions are represented inside the Genetic Algorithm.

The chromosome representation must satisfy the following goals:

* Remain independent from specific planning domains.
* Represent complete schedules.
* Work with the existing generic domain model.
* Support mutation and crossover operations.
* Allow constraint violations during evolution.
* Avoid generating structurally invalid schedules whenever possible.
* Remain independent from Jenetics implementation details.
* Allow conversion between the genetic representation and the domain `Schedule`.

The central design decision is:

> **Each gene represents the assignment of exactly one Activity.**

Therefore:

```text
1 Activity
    ↓
1 Assignment Gene
```

and:

```text
N Activities
    ↓
N Genes
    ↓
1 Candidate Schedule
```

---

# 2. Relationship with the Domain Model

The previously defined domain model establishes:

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
1 Activity = 1 atomic schedulable unit
```

and every complete `Schedule` contains exactly one `Assignment` for every `Activity`.

The genetic representation follows the same structure:

```text
Activity 1 → Gene 1 → Assignment 1
Activity 2 → Gene 2 → Assignment 2
Activity 3 → Gene 3 → Assignment 3
...
Activity N → Gene N → Assignment N
```

---

# 3. Gene

A gene represents the scheduling decision associated with one `Activity`.

Conceptually:

```text
Gene
    ↓
Selected assignment configuration
for one Activity
```

Each gene determines:

```text
TimeSlot
Resources
Location
```

for its associated Activity.

The Activity itself is not variable during evolution.

Therefore:

```text
Activity
    = fixed

TimeSlot
    = evolvable

Resources
    = evolvable when alternatives exist

Location
    = evolvable when alternatives exist
```

---

# 4. Assignment Option

For the MVP, each gene does not directly contain complete domain objects.

Instead, it selects one precomputed `AssignmentOption`.

Conceptually:

```kotlin
data class AssignmentOption(
    val timeSlotId: String,
    val resourceAssignments: Map<String, List<String>>,
    val locationId: String?
)
```

Each `Activity` therefore has a list of structurally valid alternatives:

```text
Activity
    ↓
List<AssignmentOption>
```

Example:

```text
Activity A1

Option 0
Monday 09:00 / Ana / Room 1

Option 1
Monday 10:00 / Pedro / Room 1

Option 2
Tuesday 09:00 / Ana / Room 2
```

A gene selects one option index:

```text
Gene value = 2
```

meaning:

```text
Activity A1
    ↓
AssignmentOption 2
```

---

# 5. Time Slot Representation

Each `AssignmentOption` contains exactly one `TimeSlot`.

Example:

```text
Activity:
Mathematics 1A

Candidate TimeSlots:

0 → Monday 09:00–10:00
1 → Monday 10:00–11:00
2 → Tuesday 09:00–10:00
3 → Wednesday 11:00–12:00
```

If an Activity defines:

```text
allowedTimeSlotIds
```

only those slots belong to its candidate set.

Otherwise, all structurally compatible TimeSlots may be considered.

---

# 6. Resource Representation

An Activity may contain multiple `ResourceRequirement` objects.

Example:

```text
Mathematics 1A

Resource Requirements:

teacher
    quantity = 1
    candidates = [Ana, Pedro]

student-group
    quantity = 1
    candidates = [1A]
```

The Assignment Option selects resources for every requirement.

Example:

```text
resourceAssignments:

teacher
    → Ana

student-group
    → 1A
```

Conceptually:

```kotlin
Map<ResourceRequirementId, List<ResourceId>>
```

The number of selected resources must satisfy:

```text
selectedResources.size =
ResourceRequirement.quantity
```

This is a structural requirement.

---

# 7. Fixed and Variable Resources

The same representation supports both fixed and variable assignments.

Example:

```text
Requirement:
TEACHER

candidateResourceIds:
[Ana]
```

There is only one structurally possible assignment:

```text
Teacher → Ana
```

By contrast:

```text
Requirement:
TEACHER

candidateResourceIds:
[Ana, Pedro, Laura]
```

allows the Genetic Algorithm to choose between multiple alternatives.

The same structure works in the Work Shift domain:

```text
Requirement:
EMPLOYEE

candidateResourceIds:
[Laura, Pedro, Marta]
```

---

# 8. Location Representation

If an Activity may be assigned to a Location, the Assignment Option contains one candidate location.

Example:

```text
Activity:
Mathematics 1A

Allowed Locations:

Room A
Room B
Room C
```

One Assignment Option may contain:

```text
Room B
```

If an Activity does not require a Location:

```text
locationId = null
```

Location therefore remains optional.

---

# 9. Complete Gene Example

Consider:

```text
Activity:
Mathematics 1A
```

with:

```text
Teacher candidates:
Ana
Pedro

Student Group:
1A

Time slots:
Monday 09:00
Monday 10:00
Tuesday 09:00

Locations:
Room 1
Room 2
```

One Assignment Option may be:

```text
TimeSlot:
Monday 10:00

Resources:
TEACHER → Ana
STUDENT_GROUP → 1A

Location:
Room 2
```

Another option may be:

```text
TimeSlot:
Tuesday 09:00

Resources:
TEACHER → Pedro
STUDENT_GROUP → 1A

Location:
Room 1
```

The gene stores only which option is selected.

---

# 10. Chromosome

A chromosome represents the complete ordered collection of assignment decisions for all Activities.

Conceptually:

```text
Chromosome
│
├── Gene 0 → Activity A
├── Gene 1 → Activity B
├── Gene 2 → Activity C
│
└── Gene N → Activity N
```

Therefore:

```text
chromosomeLength = numberOfActivities
```

For example:

```text
Activity order:

0 → Mathematics 1A
1 → Physics 2A
2 → English 1B
3 → Chemistry 2B
```

One chromosome may be represented as:

```text
[2, 0, 4, 1]
```

where:

```text
Gene 0 = 2
    ↓
Activity 0 uses AssignmentOption 2

Gene 1 = 0
    ↓
Activity 1 uses AssignmentOption 0
```

The numeric values have no domain meaning by themselves.

They are indexes into the candidate options of the corresponding Activity.

---

# 11. Stable Activity Ordering

The Activity-to-gene-position mapping must remain stable during one optimization execution.

Example:

```text
activityIndex[0] → activity-001
activityIndex[1] → activity-002
activityIndex[2] → activity-003
```

This mapping is immutable throughout evolution.

It must not depend on unstable collection iteration order.

The implementation should use either:

```text
PlanningProblem.activities order
```

or deterministic sorting.

This is important for:

* Encoding.
* Decoding.
* Mutation.
* Crossover.
* Reproducibility.

---

# 12. Genotype

A `Genotype` represents one complete candidate solution handled by the Genetic Algorithm.

Conceptually:

```text
Genotype
    ↓
Complete genetic representation
of one candidate Schedule
```

For Genetic Planner 1.0:

```text
Genotype
    │
    └── Planning Chromosome
            │
            ├── Gene 0
            ├── Gene 1
            ├── Gene 2
            └── ...
```

The terminology is:

```text
Gene
    = one scheduling decision for one Activity

Chromosome
    = ordered sequence of scheduling decisions

Genotype
    = complete genetic representation
      of one candidate solution
```

For the MVP, the genotype conceptually contains one planning chromosome.

The exact Jenetics internal representation may differ if required by the library, but the architectural rule remains:

> **One Genotype must always be decodable into exactly one complete Schedule.**

---

# 13. Candidate Space

Before creating genetic candidates, the Genetic Engine builds the possible assignment domain for every Activity.

Conceptually:

```text
Activity
   │
   ├── Candidate TimeSlots
   ├── Candidate Resources
   └── Candidate Locations
   │
   ▼
Assignment Options
```

For example:

```text
3 possible TimeSlots
×
2 possible Teachers
×
2 possible Rooms

= 12 possible Assignment Options
```

The possible options for every Activity form the candidate space.

Conceptually:

```kotlin
data class ActivityGeneDomain(
    val activityId: String,
    val options: List<AssignmentOption>
)
```

---

# 14. Separation of Candidate Space and Chromosome

The complete Assignment Options should not be duplicated inside every individual chromosome.

The candidate space is created once:

```text
Candidate Space

Activity A
→ Option 0
→ Option 1
→ Option 2

Activity B
→ Option 0
→ Option 1
```

Candidate chromosomes only store selections:

```text
Chromosome A
[0, 1]

Chromosome B
[2, 0]

Chromosome C
[1, 1]
```

This keeps the genetic representation compact.

Conceptually:

```kotlin
data class PlanningChromosome(
    val selectedOptionIndexes: List<Int>
)
```

---

# 15. Encoding

Encoding transforms a `PlanningProblem` into the genetic representation used during optimization.

The complete process is:

```text
PlanningProblem
      │
      ▼
Problem validation
      │
      ▼
Create deterministic Activity order
      │
      ▼
Build Assignment Options
      │
      ▼
Create candidate domains
      │
      ▼
Create Chromosome / Genotype
```

---

# 16. Encoding Step 1 — Validate PlanningProblem

Before encoding, the problem must satisfy the previously defined structural validation rules.

For example:

```text
Activities exist.

TimeSlots exist.

Resource references are valid.

ResourceRequirements can be satisfied.

Allowed Location references are valid.
```

If an Activity has no structurally possible assignment, the Genetic Algorithm must not start.

---

# 17. Encoding Step 2 — Establish Activity Order

The encoder creates a deterministic Activity index.

Example:

```text
Index 0 → activity-A
Index 1 → activity-B
Index 2 → activity-C
```

This mapping is preserved during the complete optimization process.

---

# 18. Encoding Step 3 — Generate Assignment Options

For each Activity, structurally valid combinations are generated.

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

The result becomes:

```text
Activity A → [Option 0, Option 1, Option 2]
```

---

# 19. Encoding Step 4 — Create Gene Domains

Each gene receives the valid range of Assignment Option indexes for its Activity.

For example:

```text
Activity A → 3 options → [0..2]
Activity B → 5 options → [0..4]
Activity C → 2 options → [0..1]
```

A candidate genotype may therefore be:

```text
[2, 4, 0]
```

meaning:

```text
Activity A → Option 2
Activity B → Option 4
Activity C → Option 0
```

---

# 20. Encoding Example

Consider:

```text
PlanningProblem

Activities:
A1
A2
A3
```

Candidate generation produces:

```text
A1 → [O10, O11, O12]
A2 → [O20, O21]
A3 → [O30, O31, O32, O33]
```

The gene domains are:

```text
Gene 0 → [0..2]
Gene 1 → [0..1]
Gene 2 → [0..3]
```

One generated candidate:

```text
[1, 0, 3]
```

means:

```text
A1 → O11
A2 → O20
A3 → O33
```

This genotype represents one complete candidate schedule.

---

# 21. Structural Validity

The encoding must prevent structurally invalid genetic states.

The following must always be true:

```text
Every Activity has exactly one gene.

Every gene corresponds to an existing Activity.

Every gene selects exactly one AssignmentOption.

Every selected AssignmentOption contains
a valid TimeSlot.

Every ResourceRequirement receives
the required number of Resources.

Selected Resources exist.

Selected Resources match
the required ResourceType.

Selected Locations exist.

All references belong
to the same PlanningProblem.
```

These are structural invariants.

---

# 22. Structural Validity vs Constraint Validity

Structural validity is different from planning feasibility.

For example:

```text
Teacher Ana
    ↓
Mathematics
Monday 09:00

Teacher Ana
    ↓
Physics
Monday 09:00
```

Both assignment options may be structurally valid.

However:

```text
NoOverlapConstraint
    ↓
HARD violation
```

The chromosome remains valid as genetic data.

The schedule is simply infeasible according to its constraints.

This behaviour is intentional because the Genetic Algorithm must be able to explore infeasible candidate solutions.

---

# 23. Constraints Are Not Encoding Rules

The encoding should guarantee structural consistency, but it should not normally enforce planning constraints such as:

```text
Availability
NoOverlap
MaximumAssignments
PreferredTimeSlot
```

Otherwise, chromosome generation would become coupled with constraint implementations.

The distinction is:

```text
Structural impossibility
    ↓
Do not create Assignment Option

Planning constraint violation
    ↓
Allow candidate
    ↓
Apply penalty
```

---

# 24. RequiredResource Consideration

`RequiredResourceConstraint` deserves special consideration.

Suppose:

```text
Activity A

Teacher candidates:
Ana
Pedro

Constraint:
RequiredResource(A, Ana)
```

Both options can remain in the genetic search space:

```text
Ana
Pedro
```

Selecting Pedro produces:

```text
RequiredResourceConstraint
    ↓
HARD violation
```

The alternative would be to remove Pedro from the candidate space.

However, the preferred generic rule is:

> A configured planning Constraint should be evaluated by the Constraint Engine rather than silently transformed into chromosome structure.

This preserves the separation between:

```text
Structural validity
```

and:

```text
Planning feasibility
```

---

# 25. Decoding

Decoding transforms the evolved genetic representation back into the generic domain `Schedule`.

The process is:

```text
Genotype
    ↓
Read gene selections
    ↓
Resolve AssignmentOption
for each Activity
    ↓
Create Assignments
    ↓
Create Schedule
```

---

# 26. Decoding Algorithm

For every gene at position `i`:

```text
activity =
activityOrder[i]

selectedOptionIndex =
chromosome[i]

option =
candidateSpace[i][selectedOptionIndex]
```

The decoder then creates:

```kotlin
Assignment(
    activityId = activity.id,
    timeSlotId = option.timeSlotId,
    resourceAssignments = option.resourceAssignments,
    locationId = option.locationId
)
```

Once all genes are decoded:

```kotlin
Schedule(
    planningProblemId = problem.id,
    assignments = assignments
)
```

is produced.

---

# 27. Decoder Responsibility

Conceptually:

```kotlin
interface ScheduleDecoder {

    fun decode(
        chromosome: PlanningChromosome,
        problem: PlanningProblem
    ): Schedule
}
```

The decoder is responsible only for translating genetic selections into domain assignments.

It does not evaluate constraints.

Therefore:

```text
Genotype
    ↓
Decoder
    ↓
Schedule
    ↓
ConstraintEvaluator
```

---

# 28. Complete Encoding and Decoding Flow

The complete transformation is:

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
                       ENCODING
                          │
                          ▼
                   ┌─────────────┐
                   │  Genotype   │
                   │             │
                   │ [2,0,4,1]   │
                   └─────────────┘
                          │
                          │
                      Evolution
                          │
                          ▼
                   ┌─────────────┐
                   │  Genotype   │
                   │             │
                   │ [2,3,1,0]   │
                   └─────────────┘
                          │
                          ▼
                       DECODING
                          │
                          ▼
                       Schedule
                          │
                          ▼
                 ConstraintEvaluator
```

Therefore:

```text
PlanningProblem
        ↓ encoding

Chromosome / Genotype
        ↓ evolution

Chromosome / Genotype
        ↓ decoding

Schedule
```

---

# 29. Chromosome Completeness

Every chromosome represents a complete candidate Schedule.

Partial schedules are not supported in the MVP.

Therefore:

```text
numberOfGenes
=
numberOfActivities
```

and:

```text
Every Activity
    ↓
Exactly one selected AssignmentOption
```

This simplifies decoding and fitness evaluation.

---

# 30. Mutation

Mutation introduces genetic variation by changing the selected Assignment Option of one or more Activities.

Example:

```text
Before

[2, 0, 4, 1]
```

Suppose gene 1 mutates:

```text
0 → 3
```

The result is:

```text
After

[2, 3, 4, 1]
```

Only the scheduling decision corresponding to Activity 1 changes.

---

# 31. Effect of Mutation on Solutions

Conceptually:

```text
Activity B

Before:
Monday 09:00
Ana
Room 1
```

After mutation:

```text
Activity B

Tuesday 11:00
Pedro
Room 2
```

Mutation can therefore affect:

* TimeSlot.
* Selected Resource.
* Location.
* Several elements simultaneously if the new Assignment Option differs in multiple values.

---

# 32. Mutation and Structural Validity

Mutation must only select options belonging to the candidate domain of the affected Activity.

Therefore it cannot produce:

```text
Unknown Resource

Invalid ResourceType

Unknown TimeSlot

Invalid Location

Incomplete ResourceRequirement
```

Structural validity is preserved by construction.

---

# 33. Mutation and Constraints

Although mutation preserves structural validity, it may introduce or remove constraint violations.

For example:

```text
Mutation
    ↓
New TimeSlot
    ↓
NoOverlap violation introduced
```

Another mutation may:

```text
Mutation
    ↓
Different TimeSlot
    ↓
Availability violation removed
```

This is expected.

Mutation should explore alternative schedules rather than guarantee feasibility.

---

# 34. Crossover

Crossover combines scheduling decisions from two parent candidate solutions.

Example:

```text
Parent A

[2, 0, 4, 1]

Parent B

[1, 3, 2, 0]
```

A crossover may produce:

```text
Child

[2, 0, 2, 0]
```

The child inherits some Activity assignments from one parent and others from the second.

---

# 35. Effect of Crossover on Solutions

Assume:

```text
Parent A

Activity A → Monday 09:00 / Ana
Activity B → Tuesday 09:00 / Ana
```

and:

```text
Parent B

Activity A → Tuesday 09:00 / Ana
Activity B → Monday 09:00 / Ana
```

Both parents may be feasible.

A child may inherit:

```text
Activity A → Monday 09:00 / Ana
Activity B → Monday 09:00 / Ana
```

producing:

```text
NoOverlapConstraint
    ↓
HARD violation
```

Therefore:

> Two feasible parents do not necessarily produce a feasible child.

This is normal Genetic Algorithm behaviour.

The Constraint Engine evaluates the child and the Fitness Function determines its probability of surviving subsequent generations.

---

# 36. Crossover and Structural Validity

Each chromosome position always represents the same Activity.

For example:

```text
position 0 → Activity A
position 1 → Activity B
position 2 → Activity C
position 3 → Activity D
```

The crossover operator must preserve this relationship.

Genes must not move between positions corresponding to different Activities if their candidate domains are incompatible.

---

# 37. Heterogeneous Gene Domains

Different Activities may have different numbers of Assignment Options.

Example:

```text
Activity A
options = [0..2]

Activity B
options = [0..8]

Activity C
options = [0..4]
```

Therefore:

```text
Gene 0
valid values = 0..2

Gene 1
valid values = 0..8

Gene 2
valid values = 0..4
```

A value valid for Activity B may be invalid for Activity A.

The final Jenetics implementation must preserve these heterogeneous domains during mutation and crossover.

This must be explicitly validated in the Jenetics proof of concept.

---

# 38. Academic Scheduling Example

Consider three Activities:

```text
A1 → Mathematics 1A
A2 → English 1A
A3 → Physics 2A
```

The candidate space may contain:

```text
A1
├── Option 0 → Monday 09:00 / Ana / Group 1A / Room 1
├── Option 1 → Tuesday 10:00 / Pedro / Group 1A / Room 2
└── Option 2 → Wednesday 09:00 / Ana / Group 1A / Room 1

A2
├── Option 0 → Monday 10:00 / Laura / Group 1A / Room 2
└── Option 1 → Tuesday 11:00 / Laura / Group 1A / Room 1

A3
├── Option 0 → Tuesday 09:00 / Pedro / Group 2A / Room 1
└── Option 1 → Wednesday 10:00 / Pedro / Group 2A / Room 2
```

A chromosome:

```text
[2, 0, 1]
```

means:

```text
A1 → Option 2
A2 → Option 0
A3 → Option 1
```

The decoder creates the corresponding Academic Schedule.

---

# 39. Work Shift Scheduling Example

Consider:

```text
A1 → Reception Morning
A2 → Support Morning
A3 → Reception Afternoon
```

The candidate space may contain:

```text
A1
├── Option 0 → Monday 08:00–14:00 / Laura / Reception
└── Option 1 → Monday 08:00–14:00 / Pedro / Reception

A2
├── Option 0 → Monday 08:00–14:00 / Pedro / Support Desk
├── Option 1 → Tuesday 08:00–14:00 / Laura / Support Desk
└── Option 2 → Tuesday 08:00–14:00 / Marta / Support Desk

A3
├── Option 0 → Monday 14:00–20:00 / Marta / Reception
└── Option 1 → Tuesday 14:00–20:00 / Laura / Reception
```

A chromosome:

```text
[1, 2, 0]
```

has exactly the same genetic meaning as in Academic Scheduling:

```text
Activity 0 → Option 1
Activity 1 → Option 2
Activity 2 → Option 0
```

Only the domain data contained in the Assignment Options changes.

---

# 40. Cross-Domain Validation

The encoding is independent from planning terminology.

The chromosome never contains:

```text
Teacher
Student
Subject
Class
Employee
Shift
```

It only represents choices associated with generic:

```text
Activity
Resource
TimeSlot
Location
```

Therefore:

```text
Academic PlanningProblem
          │
          ▼
    Candidate Space
          │
          ▼
      Same Encoding
          │
          ▼
       Genotype
          ▲
          │
      Same Encoding
          ▲
          │
    Candidate Space
          ▲
          │
Work Shift PlanningProblem
```

Likewise:

```text
Genotype
    ↓
Same Decoder
    ↓
Schedule
```

This confirms that the representation works with both target domains.

---

# 41. Search Space

If Activity `i` has:

```text
Oi
```

possible Assignment Options, the theoretical number of candidate schedules is:

```text
O1 × O2 × O3 × ... × On
```

For example:

```text
Activity A → 4 options
Activity B → 5 options
Activity C → 3 options
```

produces:

```text
4 × 5 × 3
=
60 possible schedules
```

For realistic problems, this value grows rapidly.

This combinatorial search space is what motivates the use of a Genetic Algorithm.

---

# 42. Assignment Option Explosion

Materializing every possible Assignment Option may itself become expensive.

For example:

```text
20 TimeSlots
×
10 Resource candidates
×
5 Locations

=
1,000 Assignment Options
for one Activity
```

With multiple ResourceRequirements, the number may be even larger.

Therefore candidate generation should apply structural filters first:

```text
allowedTimeSlotIds
allowedLocationIds
candidateResourceIds
ResourceType compatibility
ResourceRequirement quantity
```

before creating Assignment Options.

---

# 43. MVP Representation Decision

For Genetic Planner 1.0, the preferred representation is:

```text
One gene per Activity

        +

One selected AssignmentOption per gene

        +

Stable Activity-to-gene-position mapping

        +

One complete chromosome per candidate Schedule

        +

Decoder from chromosome to Schedule
```

In compact form:

```text
PlanningProblem
      ↓

A1 → [0,1,2,3]
A2 → [0,1]
A3 → [0,1,2,3,4]

      ↓ encoding

Genotype

[2,0,4]

      ↓ decoding

Schedule
```

This provides a good balance between:

* Simplicity.
* Genericity.
* Structural validity.
* Mutation simplicity.
* Crossover simplicity.
* Explainability.
* Implementation effort.

---

# 44. Future Representation Alternative

If experimental evaluation shows that complete Assignment Option enumeration becomes too expensive, the representation may evolve toward separate genetic dimensions.

For example:

```text
Activity
│
├── TimeSlot selection
├── Resource selection
└── Location selection
```

This could reduce precomputed candidate-space size but would introduce additional complexity in:

* Structural validation.
* Mutation.
* Crossover.
* Decoding.
* Genetic operator configuration.

For the MVP, the Assignment Option representation remains preferable.

---

# 45. Jenetics Boundary

The domain model must remain independent from Jenetics.

Therefore domain classes such as:

```text
PlanningProblem
Schedule
Assignment
Activity
Constraint
```

must not expose Jenetics concepts such as:

```text
Gene
Chromosome
Genotype
Phenotype
```

The dependency direction is:

```text
Domain Model
      │
      ▼
Genetic Encoding Adapter
      │
      ▼
Jenetics
```

The Genetic Engine infrastructure adapts the generic planning model to the genetic library.

---

# 46. Jenetics Mapping to Validate

Conceptually, the mapping is:

```text
PlanningProblem
      ↓
CandidateSpace
      ↓
Genetic Encoding
      ↓
Jenetics Genotype
      ↓
Decoder / Codec
      ↓
Schedule
```

The exact Jenetics classes used to implement this representation are intentionally left open.

In particular, the Jenetics validation task must verify:

* Representation of different gene ranges per Activity.
* Safe mutation with heterogeneous candidate domains.
* Safe crossover without breaking Activity-to-position correspondence.
* Efficient decoding into `Schedule`.
* Suitable use of a Jenetics `Codec` or equivalent adapter.

---

# 47. Representation Architecture

The complete architecture is:

```text
PlanningProblem
      │
      ▼
ProblemValidator
      │
      ▼
CandidateSpaceBuilder
      │
      ├── Activity 1 → AssignmentOptions
      ├── Activity 2 → AssignmentOptions
      ├── Activity 3 → AssignmentOptions
      └── Activity N → AssignmentOptions
      │
      ▼
Encoding
      │
      ▼
Chromosome / Genotype
      │
      ▼
Genetic Evolution
      │
      ├── Mutation
      └── Crossover
      │
      ▼
Chromosome / Genotype
      │
      ▼
Decoding
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
Fitness Function
```

---

# 48. Design Decisions

## CH1 — One Gene represents one Activity

Each gene stores the selected Assignment Option for one Activity.

---

## CH2 — One Chromosome represents a complete candidate Schedule

The chromosome contains one gene per Activity.

Partial schedules are not supported in the MVP.

---

## CH3 — One Genotype represents one candidate solution

The Genotype contains the complete genetic representation required to decode one Schedule.

---

## CH4 — Activity ordering is stable

Gene position permanently identifies its corresponding Activity during one optimization run.

---

## CH5 — Genes select Assignment Options

The chromosome stores compact option indexes rather than complete domain objects.

---

## CH6 — Assignment Options contain scheduling decisions

Each Assignment Option contains:

```text
TimeSlot
Resource assignments
Location
```

for one Activity.

---

## CH7 — Candidate space is separate from individuals

Assignment Options are generated once and reused by candidate chromosomes.

---

## CH8 — Encoding guarantees structural validity

Genetic operators may only select structurally valid Assignment Options.

---

## CH9 — Constraint validity is evaluated separately

HARD and SOFT constraints are not generally encoded into chromosome structure.

---

## CH10 — Mutation changes Activity assignments

Mutation selects a different valid Assignment Option for one or more Activities.

It preserves structural validity but may change feasibility.

---

## CH11 — Crossover combines Activity assignments

Crossover inherits assignment decisions from different parents.

It preserves structural representation but may create new constraint violations.

---

## CH12 — Heterogeneous candidate domains must be preserved

Different Activities may have different numbers of valid Assignment Options.

The Jenetics implementation must respect these domains.

---

## CH13 — Encoding and decoding are domain-independent

The same mechanism supports both Academic Scheduling and Work Shift Scheduling.

---

## CH14 — Domain classes remain independent from Jenetics

Jenetics is contained within the Genetic Engine infrastructure.

---

## CH15 — Assignment Options are the MVP strategy

A more granular genetic representation may be explored later only if scalability requires it.

---

# 49. Scope Boundary

This document defines:

* Gene semantics.
* Chromosome semantics.
* Genotype semantics.
* Candidate space.
* Encoding.
* Decoding.
* Structural validity.
* Mutation effects.
* Crossover effects.
* Academic-domain applicability.
* Work Shift-domain applicability.
* Jenetics integration boundary.

It does not define:

* Final Jenetics implementation classes.
* Mutation probability.
* Crossover probability.
* Concrete crossover operator.
* Concrete mutation operator.
* Population size.
* Selection strategy.
* Fitness formula.
* Termination criteria.

These decisions belong to:

```text
Design fitness function
Research and validate Jenetics
Define genetic algorithm configuration
```

---

# 50. Summary

The Genetic Planner chromosome representation is:

```text
              PlanningProblem
                    │
                    ▼
           CandidateSpaceBuilder
                    │
                    ▼
                 ENCODING
                    │
                    ▼
              ┌────────────┐
              │  Genotype  │
              │            │
              │ Chromosome │
              │            │
              │ G G G G G  │
              └────────────┘
                    │
                    │ Evolution
                    │
             ┌──────┴──────┐
             │             │
          Mutation      Crossover
             │             │
             └──────┬──────┘
                    │
                    ▼
              ┌────────────┐
              │  Genotype  │
              └────────────┘
                    │
                    ▼
                 DECODING
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

The core representation is:

```text
Gene
    = selected AssignmentOption
      for one Activity

Chromosome
    = ordered collection of all
      Activity assignment decisions

Genotype
    = complete genetic representation
      of one candidate Schedule
```

And the fundamental transformation is:

```text
PlanningProblem
        ↓ encoding

Chromosome / Genotype
        ↓ evolution

Chromosome / Genotype
        ↓ decoding

Schedule
```

The key principle is:

> **The chromosome represents scheduling decisions, while the domain model defines the planning problem and the constraint system determines the quality and feasibility of those decisions.**
