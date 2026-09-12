# Genetic Planner — Jenetics Proof of Concept

## 1. Purpose

This experiment validates whether Jenetics is suitable as the Genetic Algorithm library for Genetic Planner.

The Proof of Concept must demonstrate that Jenetics can:

* Be integrated into the project environment.
* Create a genetic representation.
* Generate an initial population.
* Evaluate individuals using a fitness function.
* Execute multiple generations of evolution.
* Return the best individual.
* Support heterogeneous candidate domains.
* Remain isolated from the planning domain model.
* Support the chromosome representation designed for Genetic Planner.

The experiment intentionally uses a trivial optimization problem.

Its purpose is to validate the technology and architecture before implementing the real scheduling fitness function.

---

# 2. Experimental Branch

The experiment is developed in:

```text
experiment/jenetics-poc
```

This follows the project development workflow, where temporary branches are allowed for technical experiments.

If the experiment is accepted, the validated configuration and architecture can later be integrated into `main`.

---

# 3. Technology Baseline

The PoC was executed using:

```text
Java          25.0.4.1 LTS
Gradle        9.7.0
Kotlin        2.4.0
Jenetics      9.1.0
Operating OS  Windows 10
```

The Gradle project uses Java 25 through the toolchain configuration:

```kotlin
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}
```

Jenetics 9.1.0 is used because it is the current stable version selected for Genetic Planner and supports Java 25.

---

# 4. Dependency

The following dependency was added to the experimental Gradle build:

```kotlin
dependencies {
    implementation("io.jenetics:jenetics:9.1.0")

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
```

No additional Jenetics modules were required for the PoC.

---

# 5. PoC Strategy

The experiment was divided into two steps.

## Step 1 — Basic evolution

Validate:

```text
Genotype
    ↓
Population
    ↓
Fitness
    ↓
Evolution
    ↓
Best individual
```

using a homogeneous integer chromosome.

## Step 2 — Heterogeneous candidate domains

Validate the actual representation required by Genetic Planner:

```text
Activity A → 3 AssignmentOptions
Activity B → 9 AssignmentOptions
Activity C → 5 AssignmentOptions
```

Each Activity must therefore have its own valid integer range.

This second experiment is the critical architectural validation.

---

# 6. Basic PoC

The first experiment uses four integer values.

Each value can range from:

```text
0..4
```

The fitness function is deliberately trivial:

```text
fitness = sum of all gene values
```

Therefore:

```text
[0,2,1,4]
fitness = 7
```

while:

```text
[4,4,4,4]
fitness = 16
```

The theoretical optimum is:

```text
[4,4,4,4]
```

with:

```text
fitness = 16
```

---

# 7. Basic Genetic Representation

The initial genotype factory is based on an integer chromosome.

Conceptually:

```text
Genotype
    │
    └── IntegerChromosome
            │
            ├── Gene
            ├── Gene
            ├── Gene
            └── Gene
```

At application level:

```text
[2,0,4,1]
```

This resembles the Genetic Planner conceptual chromosome:

```text
Activity 0 → AssignmentOption 2
Activity 1 → AssignmentOption 0
Activity 2 → AssignmentOption 4
Activity 3 → AssignmentOption 1
```

---

# 8. Population

The Jenetics `Engine` was configured with:

```kotlin
.populationSize(50)
```

Jenetics generates the initial population using the supplied genotype factory.

Conceptually:

```text
Genotype Factory
       ↓
Individual
Individual
Individual
...
       ↓
Initial Population
```

This validates that Genetic Planner does not need to implement population creation manually.

---

# 9. Trivial Fitness Function

The PoC fitness function sums the selected integer values.

Conceptually:

```kotlin
fun fitness(
    genotype: Genotype<IntegerGene>
): Int =
    genotype.chromosome()
        .sumOf { it.allele() }
```

This fitness function contains no scheduling knowledge.

Its only purpose is to validate:

```text
Genotype
    ↓
Fitness evaluation
    ↓
Comparable result
```

---

# 10. Evolution

Evolution is executed using a Jenetics `Engine`.

The PoC uses:

```text
Population size: 50
Maximum generations: 100
```

Conceptually:

```text
Initial Population
        ↓
Fitness evaluation
        ↓
Selection
        ↓
Alteration
        ↓
New Population
        ↓
...
        ↓
Best Individual
```

The experiment uses the Jenetics evolution stream and retrieves the best phenotype after evolution.

---

# 11. Basic PoC Result

The experiment executed successfully.

Actual result:

```text
Best genotype: [[[4],[4],[4],[4]]]
Best fitness: 16
```

Therefore, Jenetics successfully:

* Generated the population.
* Evaluated fitness.
* Executed evolution.
* Reached the theoretical optimum.
* Returned the best individual.

This validates the basic genetic workflow.

---

# 12. Heterogeneous Candidate Domains

The chromosome design defined in Genetic Planner allows every Activity to have a different number of `AssignmentOptions`.

For example:

```text
Activity A → 3 options
Activity B → 9 options
Activity C → 5 options
```

Therefore the valid values are:

```text
Activity A → 0..2
Activity B → 0..8
Activity C → 0..4
```

This means that a single homogeneous chromosome range is not sufficient.

---

# 13. Jenetics Representation for Heterogeneous Domains

The PoC validates the following physical Jenetics representation:

```text
Genotype
│
├── IntegerChromosome
│       └── Gene → Activity A
│
├── IntegerChromosome
│       └── Gene → Activity B
│
└── IntegerChromosome
        └── Gene → Activity C
```

Each `IntegerChromosome` contains one gene and defines the valid range for its corresponding Activity.

Example:

```kotlin
val genotypeFactory =
    Genotype.of(
        IntegerChromosome.of(0, 3, 1),
        IntegerChromosome.of(0, 9, 1),
        IntegerChromosome.of(0, 5, 1)
    )
```

Conceptually:

```text
Chromosome 0 → one gene in 0..2
Chromosome 1 → one gene in 0..8
Chromosome 2 → one gene in 0..4
```

---

# 14. Conceptual vs Physical Representation

This experiment introduces an important distinction.

## Genetic Planner conceptual representation

At architecture level:

```text
Planning Chromosome

[2,5,1]
```

means:

```text
Activity A → AssignmentOption 2
Activity B → AssignmentOption 5
Activity C → AssignmentOption 1
```

## Jenetics physical representation

Internally:

```text
Genotype
│
├── IntegerChromosome [2]
├── IntegerChromosome [5]
└── IntegerChromosome [1]
```

Therefore:

> The conceptual Genetic Planner chromosome is an ordered sequence of Activity decisions, while its Jenetics implementation uses one single-gene `IntegerChromosome` per Activity.

The semantic model defined in the chromosome design remains unchanged.

---

# 15. Heterogeneous PoC Fitness

The second PoC also uses a trivial sum:

```text
fitness =
Activity A allele
+
Activity B allele
+
Activity C allele
```

The theoretical optimum is:

```text
[2,8,4]
```

because:

```text
Activity A max = 2
Activity B max = 8
Activity C max = 4
```

Therefore:

```text
fitness = 2 + 8 + 4 = 14
```

---

# 16. Heterogeneous PoC Result

The experiment successfully reached the expected optimum.

The decoded result was equivalent to:

```text
[2,8,4]
```

with:

```text
Best fitness: 14
```

This confirms that:

* Every Activity can have an independent candidate range.
* Jenetics preserves those ranges.
* A genotype can represent heterogeneous Activity domains.
* The resulting genotype can be decoded back into the application-level sequence of selected options.

This is the key technical validation of the experiment.

---

# 17. Mapping to Genetic Planner

The final mapping is:

| Jenetics                        | Genetic Planner                            |
| ------------------------------- | ------------------------------------------ |
| `IntegerGene`                   | Selected AssignmentOption index            |
| Single-gene `IntegerChromosome` | Genetic decision for one Activity          |
| `Genotype`                      | Complete candidate Schedule representation |
| Allele                          | AssignmentOption index                     |
| Population                      | Candidate schedules                        |
| Fitness function                | Schedule quality evaluation                |
| Evolution                       | Schedule optimization                      |
| Best phenotype                  | Best generated planning solution           |

For example:

```text
Genotype

[[2], [5], [1]]
```

maps conceptually to:

```text
Activity A → AssignmentOption 2
Activity B → AssignmentOption 5
Activity C → AssignmentOption 1
```

and is decoded into:

```text
Schedule
```

---

# 18. Encoding

The future production encoding can follow:

```text
PlanningProblem
      ↓
ProblemValidator
      ↓
CandidateSpaceBuilder
      ↓
For each Activity:
create AssignmentOptions
      ↓
For each Activity:
create single-gene IntegerChromosome
with range based on option count
      ↓
Genotype
```

For an Activity containing `N` options:

```text
valid allele range = 0 .. N-1
```

---

# 19. Decoding

The reverse process is:

```text
Genotype
      ↓
Read one allele from each chromosome
      ↓
Use Activity position
      ↓
Resolve AssignmentOption
      ↓
Create Assignment
      ↓
Schedule
```

Conceptually:

```text
[[2], [5], [1]]

      ↓

[2,5,1]

      ↓

A → Option 2
B → Option 5
C → Option 1

      ↓

Schedule
```

---

# 20. Codec

Jenetics provides a `Codec` abstraction for connecting the genotype representation with the application domain.

This matches the Genetic Planner architecture:

```text
Genotype
    ↓
Codec / Decoder
    ↓
Schedule
    ↓
ConstraintEvaluator
    ↓
ScheduleEvaluation
    ↓
Fitness
```

A production implementation can therefore expose the fitness function in terms of `Schedule` rather than exposing Jenetics types to the planning domain.

The domain model remains independent from Jenetics.

---

# 21. Domain Independence

The experiment confirms the desired dependency direction:

```text
Planning Domain
      ▲
      │
Genetic Adapter
      │
      ▼
Jenetics
```

Classes such as:

```text
PlanningProblem
Activity
Assignment
Schedule
Constraint
```

do not need to depend on:

```text
Gene
Chromosome
Genotype
Phenotype
Engine
```

This preserves the architecture designed before selecting the genetic library.

---

# 22. Mutation Validation

The representation is compatible with mutation because each Activity decision belongs to an `IntegerChromosome` with its own range.

Example:

```text
Activity A
options = 3

valid values:
0..2
```

A mutation for this chromosome remains within this candidate domain.

Therefore mutation cannot introduce an invalid AssignmentOption index for another Activity domain.

Mutation may still:

* Improve a schedule.
* Worsen a schedule.
* Create a HARD constraint violation.
* Remove a HARD constraint violation.
* Change SOFT constraint penalties.

This is expected.

Structural validity and planning feasibility remain separate concepts.

---

# 23. Crossover Consideration

The representation also provides a natural boundary for heterogeneous Activity domains.

Each chromosome belongs to one Activity.

Therefore:

```text
Chromosome index
       ↕
Activity index
```

must remain stable.

The PoC confirms that Jenetics can represent the required heterogeneous genotype.

The exact crossover operator and crossover probability are intentionally not selected in this task.

Those decisions belong to the Genetic Algorithm configuration task and must preserve the Activity-to-chromosome relationship.

---

# 24. Architectural Flow

The validated architecture becomes:

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
Jenetics Encoding
      │
      ▼
Genotype
│
├── Chromosome 0 → Activity 0
├── Chromosome 1 → Activity 1
├── Chromosome 2 → Activity 2
└── ...
      │
      ▼
Jenetics Engine
      │
      ▼
Evolved Genotype
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

# 25. Risks Identified

The experiment identifies several implementation considerations.

## Candidate-space explosion

Jenetics manages evolution but does not solve the possible combinatorial growth of precomputed `AssignmentOptions`.

Candidate generation must therefore remain efficient.

## Genetic operator configuration

Mutation, crossover and selection strategies may significantly affect optimization quality.

They require later experimentation.

## Fitness design

Jenetics optimizes the fitness supplied by Genetic Planner.

Correct HARD/SOFT prioritization remains an application responsibility.

## Large planning problems

The representation has been technically validated, but performance with large real planning datasets must be measured during experimental evaluation.

---

# 27. Decisions

## JP1 — Adopt Jenetics

Jenetics is accepted as the Genetic Algorithm library for Genetic Planner 1.0.

## JP2 — Use Jenetics 9.1.0

The project uses Jenetics 9.1.0 with Java 25.

## JP3 — Use Java 25 baseline

The Genetic Planner backend uses Java 25.

## JP4 — One physical Jenetics chromosome per Activity

Each Activity is represented by one single-gene `IntegerChromosome`.

## JP5 — One Genotype represents one complete solution

The complete ordered set of Activity chromosomes forms one candidate planning solution.

## JP6 — Preserve conceptual chromosome abstraction

At application/design level, the candidate can still be represented as:

```text
[2,5,1,...]
```

The Jenetics-specific nested structure is an infrastructure detail.

## JP7 — Keep Jenetics outside the domain model

Jenetics types remain inside the genetic-engine infrastructure.

## JP8 — Use decoding boundary

A decoder or Jenetics `Codec` will transform the genetic representation into a domain `Schedule`.

---

# 28. Conclusion

The Proof of Concept successfully validates Jenetics for Genetic Planner.

Both experiments executed correctly:

```text
Basic homogeneous experiment
        ↓
Best genotype [[[4],[4],[4],[4]]]
Best fitness 16
```

and:

```text
Heterogeneous Activity experiment
        ↓
Independent Activity ranges
        ↓
Expected optimum reached
        ↓
Decoded candidate valid
```

The critical architectural assumption from the chromosome design has therefore been validated.

The final recommended implementation is:

```text
Activity
    ↓
single-gene IntegerChromosome
    ↓

Activity
    ↓
single-gene IntegerChromosome
    ↓

...

    ↓

Genotype
    ↓
candidate Schedule
```

Consequently:

> **Jenetics 9.1.0 is considered technically and architecturally suitable for Genetic Planner 1.0.**

The project can proceed to the fitness-function design without modifying the generic planning domain model.
