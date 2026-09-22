# Genetic Planner — Genetic Algorithm

## 1. Purpose

This document describes the Genetic Algorithm implementation used by
Genetic Planner.

It documents:

- The Genetic Engine abstraction.
- The Jenetics-based implementation.
- Genetic Algorithm configuration and presets.
- The execution flow from a `PlanningProblem` to an optimized `Schedule`.
- Genetic operators and selection strategies.
- Deterministic execution support.
- Optimization results and statistics.
- Implementation decisions and deviations from the initial design.

The Genetic Algorithm remains independent from specific planning domains.

The same engine can therefore optimize academic schedules, work shifts,
and future planning templates without introducing domain-specific logic
into the optimization layer.

---

## 2. Genetic Engine Architecture

The optimization layer is exposed through the application-level
`GeneticEngine` interface:

```kotlin
interface GeneticEngine {

    fun optimize(
        problem: PlanningProblem,
        config: GeneticAlgorithmConfig
    ): OptimizationResult
}
```

The current implementation is:

```text
JeneticsEngine
```

This class adapts the Genetic Planner domain model to the Jenetics
evolutionary engine.

Conceptually:

```text
PlanningProblem
      │
      ▼
AssignmentCandidateGenerator
      │
      ▼
Assignment options per Activity
      │
      ▼
ScheduleGenotypeCodec
      │
      ▼
Jenetics Genotype<IntegerGene>
      │
      ▼
Jenetics Engine
      │
      ▼
Evolution
      │
      ▼
Best Genotype
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
OptimizationResult
```

Jenetics-specific types remain inside the genetic infrastructure layer.

The domain model does not depend on Jenetics.

---

## 3. Domain Independence

The Genetic Engine operates exclusively on the generic planning model:

```text
PlanningProblem
├── Resources
├── Activities
├── TimeSlots
├── Locations
└── Constraints
```

The engine does not contain concepts such as:

```text
Teacher
Student
Subject
Employee
WorkShift
```

These concepts belong to planning templates and user-facing workflows.

The Genetic Engine therefore follows the project principle:

> Genericity in the model and Genetic Engine; specificity in templates
> and UX.

The `templateType` metadata available in `PlanningProblem` is not used
to select genetic behavior.

---

## 4. Genetic Algorithm Configuration

Each optimization execution receives a `GeneticAlgorithmConfig`:

```kotlin
data class GeneticAlgorithmConfig(
    val populationSize: Int,
    val generationLimit: Int,
    val mutationProbability: Double,
    val crossoverProbability: Double,
    val eliteCount: Int,
    val randomSeed: Long? = null
)
```

The configuration is independent from Jenetics and contains only
application-level optimization parameters.

The parameters are:

```text
Population size
Generation limit
Mutation probability
Crossover probability
Elite count
Optional random seed
```

This allows the same Genetic Engine to be executed using:

- A predefined preset.
- A custom configuration.
- A test configuration.
- An experimental configuration.

---

## 5. Configuration Validation

`GeneticAlgorithmConfig` validates its invariants when it is created.

The implemented rules are:

```text
populationSize > 0

generationLimit > 0

0.0 <= mutationProbability <= 1.0

0.0 <= crossoverProbability <= 1.0

eliteCount >= 0

eliteCount < populationSize
```

The random seed is optional and any `Long` value is valid.

Invalid Genetic Algorithm configurations are therefore rejected before
the optimization process starts.

---

## 6. Presets

Genetic Planner provides three predefined configurations:

```text
FAST
BALANCED
EXHAUSTIVE
```

They are implemented through `GeneticAlgorithmPreset`.

| Parameter | FAST | BALANCED | EXHAUSTIVE |
|---|---:|---:|---:|
| Population size | 50 | 100 | 250 |
| Generation limit | 100 | 300 | 1000 |
| Mutation probability | 0.10 | 0.15 | 0.20 |
| Crossover probability | 0.70 | 0.80 | 0.85 |
| Elite count | 2 | 5 | 10 |

`BALANCED` is the default preset.

The presets configure optimization effort only. They do not modify the
planning model, constraints, chromosome representation, or fitness
semantics.

`EXHAUSTIVE` does not mean that the complete search space is enumerated.
It represents a more computationally intensive Genetic Algorithm
configuration.

A preset can be converted into a configuration and optionally extended
for a particular execution:

```kotlin
GeneticAlgorithmPreset.BALANCED
    .toConfig()
    .copy(randomSeed = 42L)
```

The seed is deliberately not part of a preset because it identifies an
execution rather than an optimization strategy.

---

## 7. Candidate Generation

Before creating a Jenetics population, the engine uses
`AssignmentCandidateGenerator`.

For every activity, the generator produces the structurally valid
assignment options available to that activity.

Conceptually:

```text
Activity
   │
   ├── valid TimeSlots
   ├── valid Resource assignments
   └── valid Locations
   │
   ▼
List<AssignmentOption>
```

The Cartesian product of these possibilities forms the candidate
options for the activity.

Candidate generation handles structural possibilities only.

It does not evaluate complete-schedule constraints such as:

- Resource overlap.
- Availability.
- Preferred time slots.
- Consecutive assignments.
- Workload balance.
- Location capacity.

Those rules are evaluated later by the constraint and fitness
evaluation process.

If an activity has no assignment candidates, the Genetic Engine rejects
the problem because a complete genotype cannot be constructed.

---

## 8. Jenetics ↔ Domain Adaptation

The boundary between the Genetic Planner domain and Jenetics is
implemented by `ScheduleGenotypeCodec`.

The fundamental mapping is:

```text
One Activity
     =
One scheduling decision
     =
One IntegerChromosome
     =
One IntegerGene
```

The integer allele identifies one `AssignmentOption` from the candidate
list of the corresponding activity.

For example:

```text
Activity A

Candidate options:

0 → Slot 1 + Resource 1
1 → Slot 1 + Resource 2
2 → Slot 2 + Resource 1
3 → Slot 2 + Resource 2
```

An allele with value:

```text
2
```

therefore represents:

```text
Activity A
→ Slot 2
→ Resource 1
```

A complete genotype contains one such decision for every activity.

The codec supports both directions:

```text
Genotype
   ↓ decode
Schedule
```

and:

```text
Schedule
   ↓ encode
Genotype
```

The complete chromosome representation and encoding decisions are
documented in:

`docs/algorithm/chromosome.md`

---

## 9. Single-Option Activities

During implementation, a Jenetics edge case was identified for
activities containing exactly one assignment option.

Creating an integer chromosome using a random range whose lower and
upper bounds were both zero caused Jenetics random generation to fail.

The implementation therefore creates a fixed gene for this case.

Conceptually:

```text
1 candidate option
        ↓
only valid allele = 0
```

Activities with multiple options continue to use their normal integer
allele range.

This keeps the representation consistent:

> Every activity still corresponds to exactly one genetic decision,
> even when only one decision value is possible.

---

## 10. Fitness Evaluation

Jenetics requires a fitness function for every genotype.

The implemented process is:

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
```

The fitness value is:

```text
fitness = hardPenalty + softPenalty
```

Lower values are better.

Therefore the Jenetics engine is configured with:

```text
Optimize.MINIMUM
```

A fitness value of:

```text
0.0
```

represents a schedule with no weighted constraint penalties.

Feasibility is evaluated separately from the numeric fitness value.

A schedule is feasible when no HARD constraint has violations.

This distinction means that a HARD constraint violation still makes a
schedule infeasible even if its configured weight is zero.

The complete fitness model is documented in:

`docs/algorithm/fitness.md`

---

## 11. Selection Strategy

The implemented Genetic Algorithm uses different strategies for
survivors and offspring.

### Survivors

Elitism is implemented using Jenetics `EliteSelector`.

The configured:

```text
eliteCount
```

is mapped to the number of individuals preserved between generations.

Conceptually:

```text
Generation N

Best individual ─────┐
Second best ─────────┼── preserved
...                   │
                     ▼

Generation N + 1
```

The implementation configures:

```text
survivorsSize = eliteCount
survivorsSelector = EliteSelector
```

An `eliteCount` of zero is valid.

### Offspring

The remaining population is selected using tournament selection.

The implementation uses:

```text
TournamentSelector
```

with a tournament size of:

```text
3
```

This provides selection pressure while preserving competition between
candidate schedules.

---

## 12. Genetic Operators

Two genetic operators are currently used.

### Crossover

The implementation uses:

```text
SinglePointCrossover
```

configured with:

```text
crossoverProbability
```

Crossover combines genetic scheduling decisions from two parent
solutions.

Conceptually:

```text
Parent A
[A1, A2, A3, A4]

Parent B
[B1, B2, B3, B4]

        ↓

Child
[A1, A2, B3, B4]
```

### Mutation

The implementation uses:

```text
Mutator
```

configured with:

```text
mutationProbability
```

Mutation introduces random changes into scheduling decisions and helps
maintain population diversity.

Conceptually:

```text
Before mutation

[2, 0, 4, 1]

        ↓

After mutation

[2, 3, 4, 1]
```

Both operators work on the Jenetics representation. The resulting
genotypes are decoded into domain schedules when their fitness must be
evaluated.

---

## 13. Evolution and Termination

The Jenetics `Engine` is configured using the supplied
`GeneticAlgorithmConfig`.

The generation limit is applied to the evolution stream using a fixed
generation limit.

Conceptually:

```text
Initial population
       │
       ▼
Generation 1
       │
       ▼
Generation 2
       │
      ...
       │
       ▼
Generation N
       │
       ▼
Stop
```

where:

```text
N = generationLimit
```

No convergence-based or time-based stopping criterion is currently
implemented.

The number of generations actually executed is stored in the
optimization result.

---

## 14. Deterministic Execution

Genetic Algorithms depend on random operations.

The implemented engine therefore supports an optional:

```text
randomSeed
```

When a seed is explicitly provided, it is used for the complete
Jenetics execution.

Example:

```kotlin
val config =
    GeneticAlgorithmPreset.BALANCED
        .toConfig()
        .copy(randomSeed = 42L)
```

If no seed is supplied, `JeneticsEngine` generates one for the
execution.

The seed actually used is always stored in `OptimizationResult`.

Therefore an initially random execution can later be reproduced using:

```text
Original execution
      │
      ▼
Generated seed
      │
      ▼
OptimizationResult.randomSeed
      │
      ▼
New execution using same seed
```

Jenetics random operations execute inside a seeded `RandomRegistry`
scope.

The engine also uses a direct synchronous executor for evolutionary
operations. This avoids non-deterministic ordering introduced by
parallel execution and allows:

```text
same PlanningProblem
+ same GeneticAlgorithmConfig
+ same random seed
        ↓
same optimization result
```

Execution time is intentionally not expected to be deterministic.

---

## 15. Optimization Result

The Genetic Engine returns an application-level `OptimizationResult`.

The result contains:

```kotlin
data class OptimizationResult(
    val schedule: Schedule,
    val evaluation: ScheduleEvaluation,
    val generationsExecuted: Long,
    val executionTime: Duration,
    val randomSeed: Long
)
```

It also exposes derived information from the schedule evaluation:

```text
fitness
feasible
hardPenalty
softPenalty
constraintResults
```

`ScheduleEvaluation` remains the single source of truth for fitness and
constraint evaluation information.

No Jenetics-specific type is exposed through `OptimizationResult`.

---

## 16. Complete Optimization Flow

The implemented execution flow is:

```text
PlanningProblem
      │
      ▼
Generate assignment candidates
      │
      ├── missing options ──→ reject execution
      │
      ▼
Create ScheduleGenotypeCodec
      │
      ▼
Resolve random seed
      │
      ▼
Create Jenetics Engine
      │
      ├── Optimize.MINIMUM
      ├── population size
      ├── EliteSelector
      ├── TournamentSelector(3)
      ├── SinglePointCrossover
      └── Mutator
      │
      ▼
EvolutionStream
      │
      ▼
generationLimit
      │
      ▼
Best phenotype
      │
      ▼
Decode genotype
      │
      ▼
Schedule
      │
      ▼
Evaluate constraints
      │
      ▼
OptimizationResult
```

This process keeps the evolutionary implementation separated from the
planning domain.

---

## 17. Estimated Optimization Effort

A simple approximation of the search effort is:

```text
Population size × Generation count
```

For the current presets:

```text
FAST
50 × 100
≈ 5,000
```

```text
BALANCED
100 × 300
≈ 30,000
```

```text
EXHAUSTIVE
250 × 1000
≈ 250,000
```

This is only an approximation and does not represent exact runtime or
the exact number of fitness evaluations.

Actual performance also depends on:

- Planning problem size.
- Number of candidate options.
- Number and complexity of constraints.
- Genetic operators.
- Runtime environment and hardware.

---

## 18. Initial Experimental Validation

The implemented presets were executed against the controlled
`SmallFeasibleDataset` using random seed `42`.

All three presets reached:

```text
fitness = 0.0
HARD violations = 0
soft penalty = 0.0
```

The observed execution times were:

| Preset | Generations | Execution time |
|---|---:|---:|
| FAST | 100 | 294 ms |
| BALANCED | 300 | 367 ms |
| EXHAUSTIVE | 1000 | 1192 ms |

For this small dataset, the additional optimization effort did not
improve solution quality because FAST already reached the known optimum.

These measurements are an initial technical baseline rather than a
complete performance benchmark.

The complete experiment is documented under:

`docs/experiments/`

More representative datasets and repeated executions are required
before using experimental results to tune the preset values.

---

## 19. Implementation Decisions

### GA1 — Genetic Engine is domain-independent

The engine operates on the generic `PlanningProblem` and does not
branch according to planning template.

### GA2 — Jenetics is isolated behind GeneticEngine

Application and domain code do not expose Jenetics-specific types.

### GA3 — One activity represents one genetic decision

Each activity maps to one chromosome containing one integer gene.

### GA4 — Candidate generation precedes evolution

The allele domain is constructed from structurally valid
`AssignmentOption` instances before the Jenetics engine starts.

### GA5 — Fitness is minimized

Fitness represents total weighted constraint penalty, therefore lower
values are better and Jenetics uses `Optimize.MINIMUM`.

### GA6 — HARD constraints determine feasibility

Numeric fitness and feasibility are related but separate concepts.

A HARD violation makes the schedule infeasible regardless of its
configured weight.

### GA7 — Elitism has explicit semantics

`eliteCount` represents the exact number of elite individuals preserved
between generations.

### GA8 — Tournament selection is used for offspring

Offspring selection uses a tournament size of three.

### GA9 — Single-point crossover is used

The initially unspecified crossover operator was implemented using
Jenetics `SinglePointCrossover`.

### GA10 — Mutation uses Jenetics Mutator

The configured mutation probability is mapped directly to the mutation
operator.

### GA11 — Reproducibility is an execution feature

Random seeds are optional in configuration but every execution records
the seed actually used.

### GA12 — Deterministic experiments use serial evolution

A direct executor is used together with the seeded Jenetics
`RandomRegistry` to make executions reproducible.

### GA13 — Presets remain initial configurations

FAST, BALANCED, and EXHAUSTIVE are implemented and usable, but their
values have not yet been established as experimentally optimal.

---

## 20. Deviations from Initial Design

Several implementation details were intentionally left open during the
initial design and were resolved during M2.

### Random seed became part of the configuration

The initial configuration contained five parameters.

The implemented configuration adds:

```text
randomSeed: Long?
```

This was introduced to support deterministic testing and reproducible
experiments.

### Elitism mapping was finalized

The initial design defined `eliteCount` conceptually but did not specify
its exact Jenetics implementation.

It is now implemented using:

```text
survivorsSize(eliteCount)
+
EliteSelector
```

### Crossover operator was finalized

The initial design did not select a specific crossover operator.

The implementation uses:

```text
SinglePointCrossover
```

### Offspring selection was explicitly defined

The initial design did not define the final offspring selection
strategy.

The implementation uses:

```text
TournamentSelector(3)
```

### Deterministic execution required serial processing

A fixed random seed alone was not sufficient to guarantee repeatable
complete schedules when evolutionary operations could execute in
parallel.

The implementation therefore combines:

```text
RandomRegistry with seeded Random
+
direct synchronous executor
```

### Single-option chromosome handling was added

The original representation assumed normal integer allele ranges.

During implementation, activities with exactly one candidate exposed a
Jenetics range-generation edge case.

The codec now handles these activities using a fixed allele with value
zero.

### Optimization result was expanded

The initial design focused mainly on returning the best schedule.

The implemented engine also returns:

- Complete `ScheduleEvaluation`.
- Generations executed.
- Execution time.
- Random seed used.

This information supports testing, diagnostics, and experimental
evaluation.

---

## 21. Current Limitations

The current Genetic Algorithm implementation intentionally does not
include:

- Adaptive mutation or crossover probabilities.
- Convergence-based stopping.
- Time-based stopping.
- Multiple optimization objectives.
- Automatic parameter tuning.
- Parallel deterministic execution.
- Domain-specific genetic operators.

These features are outside the current MVP scope and can be evaluated
as future extensions.

---

## 22. Summary

The implemented Genetic Planner optimization pipeline is:

```text
PlanningProblem
      ↓
AssignmentCandidateGenerator
      ↓
ScheduleGenotypeCodec
      ↓
Genotype<IntegerGene>
      ↓
JeneticsEngine
      ↓
Evolution
      ↓
Best genotype
      ↓
Schedule
      ↓
ConstraintEvaluator
      ↓
OptimizationResult
```

The current Genetic Algorithm uses:

```text
Optimization             MINIMUM
Survivor selection       EliteSelector
Offspring selection      TournamentSelector(3)
Crossover                 SinglePointCrossover
Mutation                  Mutator
Termination               Fixed generation limit
Reproducibility           Random seed + serial execution
```

Three optimization presets are available:

| Preset | Population | Generations | Mutation | Crossover | Elite |
|---|---:|---:|---:|---:|---:|
| FAST | 50 | 100 | 0.10 | 0.70 | 2 |
| BALANCED | 100 | 300 | 0.15 | 0.80 | 5 |
| EXHAUSTIVE | 250 | 1000 | 0.20 | 0.85 | 10 |

The implementation preserves the central architectural requirement that
planning semantics remain in the domain and constraint layers while
Jenetics remains an implementation detail of the Genetic Engine.