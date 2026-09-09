# Genetic Planner — Development Workflow

## 1. Purpose

This document defines the Git and GitHub development workflow used in **Genetic Planner**.

The project follows a lightweight **Trunk-Based Development** strategy adapted to a short development cycle and a single-developer project.

The main objectives of this workflow are to:

* Keep development simple and efficient.
* Integrate changes continuously.
* Avoid unnecessary long-lived branches.
* Maintain a readable Git history.
* Preserve traceability between GitHub Issues, commits, milestones, and releases.
* Isolate fixes, documentation work, and genetic algorithm experiments when necessary.

---

## 2. Trunk-Based Development

The repository uses:

```text
main
```

as its development trunk.

Normal development work is performed directly on `main`.

Changes should be:

* Small.
* Incremental.
* Coherent.
* Tested before being committed.
* Integrated frequently.

The repository avoids long-lived development branches and does not use `develop` or `feature/*` branches.

The default workflow is:

```text
GitHub Issue
      ↓
Move to In Progress
      ↓
Implement small change
      ↓
Validate / Test
      ↓
Commit to main
      ↓
Continue incrementally
      ↓
Acceptance Criteria satisfied
      ↓
Move Issue to Done
```

---

## 3. Main Branch

`main` is the central development branch of Genetic Planner.

It contains the latest integrated state of the application.

New functionality, normal refactoring, tests, configuration changes, and small documentation updates are normally committed directly to `main`.

Typical development therefore follows:

```text
main
 │
 ├── GP-X: Incremental change
 │
 ├── GP-X: Incremental change
 │
 ├── GP-X: Add tests
 │
 └── GP-X: Update documentation
```

Each commit should leave the project in a reasonable and recoverable state.

Whenever possible, `main` should:

* Compile successfully.
* Pass the available automated tests.
* Avoid incomplete changes that break existing functionality.
* Represent an integrated state of the project.

---

## 4. Branch Strategy

Temporary branches are only created when isolating a change provides a clear benefit.

The project supports three branch types:

```text
fix/*
docs/*
experiment/*
```

The project does **not** use:

```text
feature/*
develop
release/*
```

The general branch structure is:

```text
                    ┌── fix/*
                    │
main ───────────────┼── docs/*
                    │
                    └── experiment/*
```

All temporary branches must originate from `main` and should eventually be merged back into `main` or discarded.

---

## 5. Fix Branches

### Purpose

`fix/*` branches are used when a bug correction should be isolated from normal development.

Format:

```text
fix/<short-description>
```

Examples:

```text
fix/no-overlap-evaluation
fix/activity-validation
fix/calendar-rendering
fix/fitness-calculation
```

The workflow is:

```text
main
  │
  └── fix/problem-description
              │
              ├── implement fix
              ├── validate
              ├── test
              │
              ▼
            merge
              │
              ▼
             main
```

Once the fix has been successfully integrated, the branch should be deleted.

Very small corrections that can be safely implemented and tested immediately may be committed directly to `main`.

---

## 6. Documentation Branches

### Purpose

`docs/*` branches are used for substantial or independent documentation changes that benefit from being isolated from application development.

Format:

```text
docs/<short-description>
```

Examples:

```text
docs/domain-model
docs/architecture
docs/genetic-algorithm
docs/experiment-results
```

Small documentation changes associated with active development should normally be committed directly to `main`.

For example, implementing a constraint and updating its corresponding documentation may be part of the same incremental development process.

A `docs/*` branch should therefore only be created when the documentation itself represents a sufficiently independent piece of work.

---

## 7. Experiment Branches

### Purpose

`experiment/*` branches are used to isolate exploratory work, particularly work related to the Genetic Engine.

Format:

```text
experiment/<short-description>
```

Examples:

```text
experiment/mutation-rate
experiment/population-size
experiment/crossover-strategy
experiment/selection-strategy
```

Experiments may contain:

* Alternative implementations.
* Temporary instrumentation.
* Benchmark configurations.
* Changes that may not become part of the final implementation.
* Exploratory code.
* Alternative genetic operators.
* Performance experiments.

This isolation prevents experimental work from destabilizing `main`.

An `experiment/*` branch must not become a substitute for a general-purpose feature branch.

---

## 8. Experiment Lifecycle

An experiment may have two outcomes.

### Successful Experiment

If an experiment produces a change that should become part of Genetic Planner:

```text
experiment/*
      │
      ▼
Validate results
      │
      ▼
Select relevant changes
      │
      ▼
Integrate into main
```

Only the stable and relevant changes should be integrated.

Experimental or temporary code that is no longer required should not be merged.

### Discarded Experiment

If an experiment does not produce an improvement:

```text
experiment/*
      │
      ▼
Document relevant results
      │
      ▼
Discard branch
```

The branch itself does not need to be preserved.

Relevant findings should instead be documented under:

```text
docs/experiments/
```

This allows unsuccessful experiments to remain useful for the project evaluation and final report without preserving obsolete implementation code.

---

## 9. Branch Naming Convention

Temporary branch names must:

* Use lowercase characters.
* Use hyphens to separate words.
* Be short and descriptive.
* Clearly identify their purpose.

The general format is:

```text
<type>/<short-description>
```

Examples:

```text
fix/resource-overlap
docs/constraint-model
experiment/mutation-probability
```

When useful, the Genetic Planner task identifier may also be included:

```text
fix/gp-27-resource-overlap
docs/gp-4-constraint-model
experiment/gp-10-mutation-probability
```

Including the task identifier is recommended when the branch is directly associated with a specific GitHub Issue.

---

## 10. Starting Temporary Work

Temporary branches should always start from the latest version of `main`.

```bash
git switch main
git pull
git switch -c experiment/mutation-rate
```

For example:

```bash
git switch main
git pull
git switch -c docs/gp-4-constraint-model
```

Before merging a temporary branch, it should be validated against the latest state of `main`.

---

## 11. Commit Naming Convention

All commits in Genetic Planner must follow the project naming convention:

```text
GP-<issue-number>: <short description>
```

Where:

* `GP` identifies the **Genetic Planner** project.
* `<issue-number>` corresponds to the related GitHub Issue or project task.
* `<short description>` concisely describes the change introduced by the commit.

Examples:

```text
GP-2: Add generic Resource model

GP-2: Add Activity and ResourceRequirement models

GP-3: Add domain UML diagram

GP-4: Define hard and soft constraint model

GP-10: Add Jenetics proof of concept
```

Commit descriptions should:

* Be short and descriptive.
* Clearly indicate the change introduced.
* Use imperative language where possible.
* Represent one coherent change.
* Avoid vague wording.

Avoid messages such as:

```text
GP-2: Changes

GP-3: Update

GP-4: Work

GP-7: Final changes
```

---

## 12. Incremental Commits

An Issue does not need to correspond to a single commit.

Large tasks should be divided into several small and meaningful commits.

For example, a task related to the generic planning domain may result in:

```text
GP-2: Add Resource and ResourceType models

GP-2: Add Activity model

GP-2: Add ResourceRequirement model

GP-2: Add planning domain validation rules

GP-2: Document generic planning domain
```

All commits use the same `GP-X` identifier because they belong to the same task.

This provides direct traceability while following the Trunk-Based Development principle of integrating small changes frequently.

---

## 13. Keeping Main Stable

Working directly on `main` does not mean committing broken or unvalidated code.

Before committing a change, the affected functionality should be validated.

The basic development cycle is:

```text
Implement
    ↓
Compile / Build
    ↓
Run relevant tests
    ↓
Commit
```

As the project evolves, validation may include:

* Kotlin compilation.
* Backend unit tests.
* Domain tests.
* Constraint tests.
* Genetic Engine tests.
* Frontend build.
* Frontend tests.
* Integration tests.

Automated CI checks may later be introduced to execute these validations automatically.

---

## 14. Incomplete Functionality

If functionality cannot be completed in a single development session, it should preferably be divided into smaller valid increments.

For example:

```text
1. Add domain abstraction
2. Add implementation
3. Add tests
4. Integrate with existing functionality
```

Each step should leave `main` in a valid state.

If work is genuinely exploratory or too unstable to integrate incrementally, it may temporarily use an:

```text
experiment/*
```

branch.

Experimental branches must not be used simply to avoid committing normal development work to the trunk.

---

## 15. GitHub Project Integration

GitHub Issues are the main unit used to plan and track project work.

The GitHub Project follows the workflow:

```text
Backlog
   ↓
Ready
   ↓
In Progress
   ↓
Testing / Review
   ↓
Done
```

When active work begins:

```text
Ready → In Progress
```

When implementation and documentation are complete and the Acceptance Criteria are ready to be validated:

```text
In Progress → Testing / Review
```

Once all Acceptance Criteria have been satisfied:

```text
Testing / Review → Done
```

Because Genetic Planner follows Trunk-Based Development, a GitHub Issue does not normally correspond to a dedicated branch.

---

## 16. Linking Commits to Issues

Every commit associated with planned project work must reference its corresponding GitHub Issue using the Genetic Planner naming convention:

```text
GP-<issue-number>: <short description>
```

For example, if Issue `#4` corresponds to the constraint model:

```text
GP-4: Define hard and soft constraint model
```

The relationship is:

```text
GitHub Issue #4
       │
       ▼
      GP-4
       │
       ├── GP-4: Define Constraint abstraction
       ├── GP-4: Define ConstraintResult model
       ├── GP-4: Document constraint evaluation
       └── GP-4: Add constraint UML diagram
```

This convention applies to:

* New functionality.
* Bug fixes.
* Tests.
* Refactoring.
* Documentation.
* Configuration.
* Experimental work.

### Multiple Commits for the Same Issue

An Issue may produce multiple commits.

Each commit retains the same `GP-X` identifier while describing the specific incremental change:

```text
GP-7: Define chromosome representation

GP-7: Add chromosome encoding example

GP-7: Document genotype mapping
```

### Completing an Issue

The existence of a commit referencing an Issue does not mean that the Issue is complete.

An Issue may only move to `Done` when:

* All Acceptance Criteria are satisfied.
* Relevant tests pass.
* Required documentation is updated.
* The resulting changes are integrated into `main`.

The GitHub Project remains the source of truth for task status.

---

## 17. Pull Requests

Pull Requests are **not mandatory for normal development**.

Normal incremental development is committed directly to `main`.

Pull Requests may be used when:

* Integrating a `fix/*` branch.
* Integrating substantial `docs/*` work.
* Integrating selected changes from an `experiment/*` branch.
* A change would benefit from an explicit review step.

This keeps the workflow lightweight while preserving a formal review mechanism when useful.

---

## 18. Merge Strategy

Temporary branches should only be merged into `main` after their changes have been validated.

For small `fix/*` or `docs/*` branches, a normal merge or squash merge may be used depending on the quality and usefulness of the individual commits.

For exploratory `experiment/*` branches, a **squash merge** is preferred when several temporary commits were created.

For example, an experimental branch containing:

```text
GP-10: Try mutation probability 0.05
GP-10: Try mutation probability 0.10
GP-10: Add temporary benchmark logging
GP-10: Remove experimental logging
GP-10: Select mutation configuration
```

may be integrated into `main` as:

```text
GP-10: Configure validated mutation strategy
```

This prevents temporary experimental history from unnecessarily cluttering the trunk.

---

## 19. Branch Lifetime

All branches other than `main` are temporary.

After their purpose has been completed, branches under:

```text
fix/*
docs/*
experiment/*
```

should normally be deleted.

The repository should therefore contain only `main` plus a small number of temporary active branches at any given time.

---

## 20. Milestones

Project work is grouped into four milestones:

```text
M1 — Foundation & Design
M2 — Genetic Engine
M3 — Functional MVP
M4 — Release & Evaluation
```

Milestones represent delivery stages and are independent from Git branches.

Each Issue should be assigned to the milestone in which its Acceptance Criteria are expected to be completed.

---

## 21. Releases

Stable project versions are created from `main`.

The final MVP will be tagged as:

```text
v1.0.0
```

Optional intermediate versions may be created when they represent meaningful project checkpoints:

```text
v0.1.0
v0.2.0
v0.3.0
```

Release tags must always reference a stable commit on `main`.

---

## 22. Workflow Summary

The normal Genetic Planner development workflow is:

```text
GitHub Issue
      │
      ▼
Ready
      │
      ▼
In Progress
      │
      ▼
main
      │
      ├── GP-X: Small incremental change
      ├── GP-X: Add tests
      ├── GP-X: Update documentation
      │
      ▼
Testing / Review
      │
      ▼
Acceptance Criteria satisfied
      │
      ▼
Done
```

Branches are only introduced when isolation is useful:

```text
                         fix/*
                        /
main ──────────────────┼──────────────→ main
                        \
                         docs/*


main ─── experiment/* ─── validated changes ───→ main
```

The development workflow can therefore be summarized by the following principle:

> **Develop on the trunk, integrate frequently, keep commits small and traceable, and create temporary branches only when isolation provides a clear benefit.**

The standard commit convention for all Genetic Planner work is:

```text
GP-X: Short description of the task
```
