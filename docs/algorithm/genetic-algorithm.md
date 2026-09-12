# Genetic Planner — Genetic Algorithm Configuration

## 1. Purpose

This document defines the initial configuration parameters of the Genetic Algorithm used by Genetic Planner.

The configuration must:

* Define the main parameters required to execute the Genetic Algorithm.
* Provide sensible initial values for development and experimentation.
* Offer simple presets for the user interface.
* Allow different trade-offs between execution time and optimization effort.
* Remain independent from specific planning domains.
* Be configurable without modifying the Genetic Engine implementation.

The values defined in this document are:

> **Initial values, not final optimized values.**

They will be validated and adjusted through experimentation using representative planning problems.

---

# 2. Configuration Model

The Genetic Algorithm configuration is represented conceptually as:

```kotlin
data class GeneticAlgorithmConfig(
    val populationSize: Int,
    val generationLimit: Int,
    val mutationProbability: Double,
    val crossoverProbability: Double,
    val eliteCount: Int
)
```

This object contains the parameters required for one optimization execution.

Example:

```kotlin
GeneticAlgorithmConfig(
    populationSize = 100,
    generationLimit = 300,
    mutationProbability = 0.15,
    crossoverProbability = 0.80,
    eliteCount = 5
)
```

---

# 3. Main Parameters

The initial Genetic Planner configuration exposes five parameters:

```text
Population size
Generation limit
Mutation probability
Crossover probability
Elite count
```

These parameters control the amount of search performed and the balance between:

* Exploration.
* Exploitation.
* Execution time.
* Population diversity.
* Preservation of good solutions.

---

# 4. Population Size

`populationSize` defines the number of candidate schedules maintained in each generation.

Example:

```text
Population size = 100
```

means that every generation contains approximately:

```text
100 candidate solutions
```

A larger population:

* Explores more candidate schedules.
* Increases diversity.
* Can reduce premature convergence.
* Requires more fitness evaluations.
* Increases execution time.

A smaller population:

* Executes faster.
* Requires fewer fitness evaluations.
* Explores a smaller part of the search space.
* May converge prematurely.

Initial expected range:

```text
50 – 250
```

---

# 5. Generation Limit

`generationLimit` defines the maximum number of evolutionary generations.

Example:

```text
Generation limit = 300
```

means that evolution can execute for a maximum of:

```text
300 generations
```

A larger generation limit:

* Allows more optimization iterations.
* Can improve solution quality.
* Increases execution time.

A smaller limit:

* Produces faster results.
* May terminate before convergence.

Initial expected range:

```text
100 – 1000
```

The generation limit acts as a maximum termination condition.

Future experiments may introduce additional stopping criteria.

---

# 6. Mutation Probability

`mutationProbability` controls how frequently genetic decisions are randomly modified during evolution.

Conceptually:

```text
Candidate

[2, 0, 4, 1]

       ↓ mutation

[2, 3, 4, 1]
```

Mutation helps:

* Introduce diversity.
* Explore new regions of the search space.
* Escape local optima.

If mutation is too low:

```text
Population diversity may decrease.
```

If mutation is too high:

```text
Useful genetic information may be destroyed too frequently.
```

Initial expected range:

```text
0.10 – 0.20
```

These values are experimental and may be adjusted.

---

# 7. Crossover Probability

`crossoverProbability` controls how frequently information from two parent solutions is combined.

Conceptually:

```text
Parent A
[2, 0, 4, 1]

Parent B
[1, 3, 2, 0]

      ↓ crossover

Child
[2, 0, 2, 0]
```

Crossover helps combine useful scheduling decisions from different candidate schedules.

A higher crossover probability:

* Encourages recombination.
* Explores combinations of existing good decisions.

A lower crossover probability:

* Preserves more parent structures.
* Performs less recombination.

Initial expected range:

```text
0.70 – 0.85
```

The exact crossover operator will be selected and validated during implementation and experimentation.

---

# 8. Elite Count

`eliteCount` represents the number of highest-quality individuals preserved between generations.

Conceptually:

```text
Generation N

Candidate A ← best
Candidate B ← second best
Candidate C
Candidate D
...

       ↓

Generation N + 1

Candidate A preserved
Candidate B preserved
new candidates...
```

Elitism prevents the best solutions discovered so far from being lost due to mutation or crossover.

A larger elite count:

* Preserves more good candidates.
* Can accelerate exploitation.
* May reduce diversity.

A smaller elite count:

* Preserves less information.
* Allows more exploration.

Initial expected values:

```text
2 – 10
```

Elite count must always satisfy:

```text
eliteCount < populationSize
```

---

# 9. Initial Configuration

The initial balanced configuration is:

```text
Population size        100
Generation limit       300
Mutation probability   0.15
Crossover probability  0.80
Elite count             5
```

This configuration is intended as a reasonable development baseline.

It is not considered experimentally optimal.

---

# 10. Presets

To simplify configuration, Genetic Planner defines three initial presets:

```text
FAST
BALANCED
EXHAUSTIVE
```

Each preset represents a different optimization effort.

---

# 11. FAST Preset

The FAST preset prioritizes execution speed.

Initial values:

```text
FAST

Population size         50
Generation limit       100
Mutation probability   0.10
Crossover probability  0.70
Elite count              2
```

Expected characteristics:

* Short execution time.
* Lower number of fitness evaluations.
* Useful for previews and quick experimentation.
* May produce lower-quality solutions for complex problems.

Typical use:

```text
Quick planning preview
Development testing
Small planning problems
```

---

# 12. BALANCED Preset

The BALANCED preset provides the default compromise between execution time and optimization effort.

Initial values:

```text
BALANCED

Population size        100
Generation limit       300
Mutation probability   0.15
Crossover probability  0.80
Elite count              5
```

Expected characteristics:

* Moderate search effort.
* Reasonable population diversity.
* Suitable as the default configuration.
* Expected to provide good results without excessive execution time.

Typical use:

```text
Normal schedule generation
Default UI configuration
Most MVP scenarios
```

This is the recommended default preset.

---

# 13. EXHAUSTIVE Preset

The EXHAUSTIVE preset prioritizes optimization effort over execution speed.

Initial values:

```text
EXHAUSTIVE

Population size        250
Generation limit      1000
Mutation probability   0.20
Crossover probability  0.85
Elite count             10
```

Expected characteristics:

* Larger population.
* More generations.
* Greater exploration.
* Higher computational cost.
* Longer execution time.

Typical use:

```text
Final planning generation
Complex planning problems
Experimental comparison
```

The term `EXHAUSTIVE` does not mean that the complete search space is enumerated.

It indicates a more intensive Genetic Algorithm configuration.

---

# 14. Preset Comparison

| Parameter             | FAST | BALANCED | EXHAUSTIVE |
| --------------------- | ---: | -------: | ---------: |
| Population size       |   50 |      100 |        250 |
| Generation limit      |  100 |      300 |       1000 |
| Mutation probability  | 0.10 |     0.15 |       0.20 |
| Crossover probability | 0.70 |     0.80 |       0.85 |
| Elite count           |    2 |        5 |         10 |
| Execution effort      |  Low |   Medium |       High |
| Default preset        |   No |      Yes |         No |

These values are initial hypotheses and must be experimentally validated.

---

# 15. Preset Model

The presets can be represented as:

```kotlin
enum class OptimizationPreset {
    FAST,
    BALANCED,
    EXHAUSTIVE
}
```

and mapped to configuration objects:

```kotlin
fun OptimizationPreset.toConfig(): GeneticAlgorithmConfig =
    when (this) {
        OptimizationPreset.FAST ->
            GeneticAlgorithmConfig(
                populationSize = 50,
                generationLimit = 100,
                mutationProbability = 0.10,
                crossoverProbability = 0.70,
                eliteCount = 2
            )

        OptimizationPreset.BALANCED ->
            GeneticAlgorithmConfig(
                populationSize = 100,
                generationLimit = 300,
                mutationProbability = 0.15,
                crossoverProbability = 0.80,
                eliteCount = 5
            )

        OptimizationPreset.EXHAUSTIVE ->
            GeneticAlgorithmConfig(
                populationSize = 250,
                generationLimit = 1000,
                mutationProbability = 0.20,
                crossoverProbability = 0.85,
                eliteCount = 10
            )
    }
```

The exact implementation may change when integrating these values with Jenetics.

---

# 16. User Configuration Strategy

For the MVP, users should not need to understand Genetic Algorithm internals.

The main interface should therefore expose:

```text
Optimization mode

○ Fast
● Balanced
○ Exhaustive
```

rather than requiring users to manually configure every parameter.

Advanced configuration may optionally expose:

```text
Population size
Generation limit
Mutation probability
Crossover probability
Elite count
```

This preserves usability while allowing experiments and advanced control.

---

# 17. Configuration Flow

The intended flow is:

```text
User
  ↓
Select preset
  ↓
FAST / BALANCED / EXHAUSTIVE
  ↓
GeneticAlgorithmConfig
  ↓
Genetic Engine
  ↓
Jenetics Engine
  ↓
Evolution
  ↓
Best Schedule
```

A custom configuration follows:

```text
User / Experiment
       ↓
Custom parameters
       ↓
GeneticAlgorithmConfig
       ↓
Genetic Engine
```

Therefore the Genetic Engine does not need to know whether configuration came from:

* A preset.
* The UI.
* A test.
* An experiment.

---

# 18. Configuration Validation

Before starting optimization, the configuration must be validated.

Minimum rules include:

```text
populationSize > 0

generationLimit > 0

0.0 <= mutationProbability <= 1.0

0.0 <= crossoverProbability <= 1.0

eliteCount >= 0

eliteCount < populationSize
```

Invalid configurations must be rejected before starting the Genetic Algorithm.

---

# 19. Relationship with Jenetics

The application-level configuration is:

```text
GeneticAlgorithmConfig
```

The Genetic Engine adapter translates it into Jenetics configuration.

Conceptually:

```text
GeneticAlgorithmConfig
        ↓
Jenetics configuration
        ↓
Engine
        ↓
EvolutionStream
```

For example:

```text
populationSize
        ↓
Engine population configuration

generationLimit
        ↓
Evolution stream limit

mutationProbability
        ↓
Mutation operator

crossoverProbability
        ↓
Crossover operator
```

Jenetics-specific types must remain inside the Genetic Engine infrastructure.

---

# 20. Elite Count Implementation Note

`eliteCount` is part of the application-level configuration because elitism is useful as a conceptual optimization parameter.

However, its exact mapping to the Jenetics selection strategy will be validated during implementation.

Therefore:

> `eliteCount` is currently a design parameter, not a finalized Jenetics implementation decision.

This follows the general principle of this task: define the required configuration without prematurely fixing every low-level genetic operator.

---

# 21. Estimated Optimization Effort

The number of candidate evaluations can be approximated using:

```text
Population size × Generation count
```

This does not represent the exact runtime but provides a useful comparison.

For the presets:

```text
FAST
50 × 100
≈ 5,000 candidate evaluations
```

```text
BALANCED
100 × 300
≈ 30,000 candidate evaluations
```

```text
EXHAUSTIVE
250 × 1000
≈ 250,000 candidate evaluations
```

This illustrates why the presets may have substantially different execution costs.

Actual performance depends on:

* Planning problem size.
* Number of constraints.
* Candidate-space size.
* Fitness evaluation cost.
* Hardware.
* Genetic operator configuration.

---

# 22. Parameter Tuning

The initial values in this document are not considered final.

They must be evaluated using representative planning datasets.

Experiments should compare at least:

```text
Execution time
Best fitness achieved
Feasibility
Number of generations
Constraint violations
Consistency between executions
```

The final configuration can then be adjusted according to experimental results.

Possible outcome:

```text
Initial values
      ↓
Experiments
      ↓
Compare results
      ↓
Tune parameters
      ↓
Final recommended configuration
```

This tuning belongs to the evaluation stage rather than the initial architectural design.

---

# 23. Reproducibility

Genetic Algorithms contain random operations.

During development and experimentation, Genetic Planner should allow the use of a deterministic random seed where practical.

Conceptually:

```text
Random seed
    ↓
Repeatable experiment
```

This makes it easier to:

* Debug results.
* Compare configurations.
* Repeat experiments.
* Document evaluation results.

A fixed seed is useful for experiments but is not required for normal end-user executions.

---

# 24. Domain Independence

The same genetic configuration applies to:

```text
Academic Scheduling
Work Shift Scheduling
```

and future planning templates.

No preset contains domain-specific parameters.

Therefore:

```text
FAST
BALANCED
EXHAUSTIVE
```

configure optimization effort rather than planning semantics.

---

# 25. Design Decisions

## GA1 — Configuration is explicit

Genetic Algorithm parameters are grouped into a dedicated configuration object.

## GA2 — Initial parameters are not definitive

Values defined during this task are starting points for experimentation.

## GA3 — Genetic Planner uses three presets

```text
FAST
BALANCED
EXHAUSTIVE
```

## GA4 — BALANCED is the default preset

It provides the initial compromise between execution time and optimization effort.

## GA5 — Presets configure optimization effort

They do not change the planning model or constraints.

## GA6 — Advanced parameters remain configurable

Tests and experiments can bypass presets and provide explicit values.

## GA7 — Configuration is validated before execution

Invalid probabilities, population sizes or elite counts are rejected.

## GA8 — Jenetics remains an infrastructure detail

Application configuration does not expose Jenetics-specific types.

## GA9 — Parameters will be tuned experimentally

Final values must be supported by the evaluation results.

## GA10 — Reproducibility should be supported during experimentation

A deterministic random seed may be used for repeatable experimental runs.

---

# 26. Summary

The initial Genetic Algorithm configuration is based on:

```text
Population size
Generation limit
Mutation probability
Crossover probability
Elite count
```

Three presets are provided:

```text
FAST

Population         50
Generations       100
Mutation          0.10
Crossover         0.70
Elite               2
```

```text
BALANCED

Population        100
Generations       300
Mutation          0.15
Crossover         0.80
Elite               5
```

```text
EXHAUSTIVE

Population        250
Generations      1000
Mutation          0.20
Crossover         0.85
Elite              10
```

The overall configuration flow is:

```text
FAST ────────┐
BALANCED ────┼──→ GeneticAlgorithmConfig
EXHAUSTIVE ──┘              │
                            ▼
                     Genetic Engine
                            │
                            ▼
                         Jenetics
                            │
                            ▼
                        Evolution
                            │
                            ▼
                     Best Schedule
```

These values provide an initial configuration for Genetic Planner 1.0.

They are deliberately non-final and will be validated and tuned through experimental evaluation.
