# Initial Genetic Engine Experiment

## Objective

The objective of this experiment is to perform an initial technical
evaluation of the Genetic Planner genetic engine using the predefined
FAST, BALANCED, and EXHAUSTIVE genetic algorithm configurations.

The experiment compares solution quality and execution cost while
keeping the planning problem and random seed constant.

## Dataset

The experiment uses `SmallFeasibleDataset`.

Dataset characteristics:

- 6 activities
- 3 resources
- 4 time slots
- HARD `NoOverlapConstraint`
- SOFT `PreferredTimeSlotConstraint`
- Known optimal fitness: 0.0

The dataset has at least one feasible schedule with no HARD or SOFT
constraint violations.

## Experimental setup

The following presets were evaluated:

- FAST
- BALANCED
- EXHAUSTIVE

All executions used the same random seed:

`42`

Using a fixed random seed makes the experiment reproducible.

## Metrics

The following metrics were recorded for each execution:

- Fitness
- HARD constraint violations
- Soft penalty
- Execution time in milliseconds
- Generations executed
- Random seed

Lower fitness values represent better solutions.

A fitness value of `0.0` represents a solution without constraint
penalties for this dataset.

## Results

| Preset | Fitness | HARD violations | Soft penalty | Time (ms) | Generations | Seed |
|---|---:|---:|---:|---:|---:|---:|
| FAST | 0.0 | 0 | 0.0 | 294 | 100 | 42 |
| BALANCED | 0.0 | 0 | 0.0 | 367 | 300 | 42 |
| EXHAUSTIVE | 0.0 | 0 | 0.0 | 1192 | 1000 | 42 |

The raw results are available in:

`docs/experiments/initial-genetic-engine-results.csv`

## Initial observations

All three genetic algorithm presets reached the known optimal fitness
of 0.0 for the `SmallFeasibleDataset`.

No HARD constraint violations were present in the generated schedules,
and the SOFT penalty was also 0.0 in all three executions.

For this small controlled problem, increasing the search effort did
not improve solution quality because the FAST preset was already able
to find an optimal solution.

The main observed difference between the presets was execution cost.
FAST completed 100 generations in 294 ms, BALANCED completed 300
generations in 367 ms, and EXHAUSTIVE completed 1000 generations in
1192 ms.

These results suggest that the small dataset is not sufficiently
difficult to demonstrate differences in solution quality between the
presets. However, it provides an initial validation that the genetic
engine can produce a feasible zero-penalty schedule under both HARD
and SOFT constraints.

More demanding datasets and repeated executions should be used in
later experiments to evaluate convergence, scalability, and the
trade-off between solution quality and execution time.

## Reproducibility

The experiment can be reproduced using:

- Dataset: `SmallFeasibleDataset`
- Random seed: `42`
- Genetic engine: `JeneticsEngine`
- Presets: FAST, BALANCED, EXHAUSTIVE

The random seed used by each execution is also stored in the
optimization result.