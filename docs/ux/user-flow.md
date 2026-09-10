# Genetic Planner — User Journey and Application Structure

## 1. Purpose

This document defines the main user journey and application structure of **Genetic Planner**.

The objective is to provide a simple guided workflow that allows users to define a planning problem, configure its constraints, generate an optimized schedule, and review the resulting solution without requiring knowledge of genetic algorithms.

The application follows the main flow:

```text
Dashboard
    ↓
New Planning
    ↓
Choose Template
    ↓
Configure Problem
    ↓
Configure Constraints
    ↓
Review
    ↓
Generate
    ↓
Results
```

The same workflow is shared by all planning scenarios. Templates adapt terminology, default values, forms, and constraints without changing the underlying application structure.

---

## 2. UX Principles

The application should follow the following principles:

### Guided configuration

Planning problems can contain many entities and constraints. The application should guide the user through the configuration instead of presenting all options at once.

### Domain-friendly terminology

Users should interact with concepts familiar to their planning scenario.

For example, the Academic template may display:

```text
Teachers
Student Groups
Classes
Classrooms
Teaching Periods
```

while internally these concepts are mapped to:

```text
Resources
Activities
Locations
TimeSlots
```

The Work Shift template may display:

```text
Employees
Work Assignments
Workplaces
Shifts
```

The generic domain model remains hidden from users when domain-specific terminology improves usability.

### Progressive disclosure

Advanced options should only be displayed when necessary.

The default workflow should allow a schedule to be generated without requiring the user to understand genetic algorithm parameters.

### Early validation

Configuration errors should be detected as early as possible instead of waiting until schedule generation.

### Explainable results

The application should not only show the generated schedule but also provide information about its quality, especially hard and soft constraint violations.

---

# 3. Happy Path

The main happy path is:

```text
Dashboard
    │
    ▼
New Planning
    │
    ▼
Choose Template
    │
    ▼
Configure Problem
    │
    ▼
Configure Constraints
    │
    ▼
Review
    │
    ▼
Generate
    │
    ▼
Results
```

The expected user actions are:

1. User opens the application.
2. User selects **New Planning**.
3. User selects a planning template.
4. User enters the planning problem information.
5. User configures hard and soft constraints.
6. User reviews the complete configuration.
7. User starts schedule generation.
8. Genetic Planner executes the optimization process.
9. User receives the generated schedule.
10. User reviews the schedule and its quality information.

---

# 4. Step 1 — Dashboard

## Purpose

Provide the entry point to Genetic Planner and allow the user to start a new planning problem.

## Information displayed

The Dashboard should contain:

* Application name and short description.
* **New Planning** primary action.
* Available or recent planning problems, if persistence is implemented.
* Quick access to previously generated results, if available.

For the initial MVP, the Dashboard may remain intentionally simple.

Example:

```text
┌─────────────────────────────────────────────┐
│ Genetic Planner                             │
│                                             │
│ Generate optimized schedules using          │
│ genetic algorithms.                         │
│                                             │
│          [ + New Planning ]                 │
│                                             │
│ Recent Planning                             │
│ -----------------------------------------   │
│ Academic Schedule 2026       [Open]         │
│                                             │
└─────────────────────────────────────────────┘
```

If persistence is not implemented in the first MVP iteration, the Recent Planning section can be omitted.

---

# 5. Step 2 — New Planning / Choose Template

## Purpose

Allow the user to select the type of planning problem.

## Templates

Initially:

### Academic Scheduling

Priority:

```text
MUST
```

Used to generate academic timetables.

### Work Shift Scheduling

Priority:

```text
SHOULD
```

Used to generate employee work schedules.

The architecture should allow additional templates in the future.

## Information displayed

Each template should show:

* Template name.
* Short description.
* Main concepts involved.
* Example use case.

Example:

```text
┌──────────────────────┐
│ Academic Scheduling  │
│                      │
│ Create optimized     │
│ academic timetables. │
│                      │
│ Teachers             │
│ Student groups       │
│ Classes              │
│ Classrooms           │
│                      │
│      [ Select ]      │
└──────────────────────┘
```

```text
┌──────────────────────┐
│ Work Shift           │
│ Scheduling           │
│                      │
│ Generate employee    │
│ work schedules.      │
│                      │
│ Employees            │
│ Work assignments     │
│ Shifts               │
│ Workplaces           │
│                      │
│      [ Select ]      │
└──────────────────────┘
```

---

# 6. Step 3 — Configure Problem

## Purpose

Collect the information required to create the `PlanningProblem`.

The exact terminology and fields depend on the selected template.

The configuration should be divided into manageable sections rather than displaying one large form.

---

## 6.1 Academic Scheduling Configuration

The Academic template should guide the user through the following information.

### General Information

* Planning name.
* Start date.
* End date.

Example:

```text
Planning name: First Semester Schedule
Start date:    14/09/2026
End date:      18/09/2026
```

### Teachers

For each teacher:

* Name.
* Identifier.
* Optional metadata.

Example:

```text
Ana López
Pedro García
Laura Martín
```

Internally:

```text
ResourceType = TEACHER
```

### Student Groups

For each group:

* Name.
* Identifier.

Example:

```text
1A
1B
2A
```

Internally:

```text
ResourceType = STUDENT_GROUP
```

### Classrooms

For each classroom:

* Name.
* Capacity, when relevant.

Example:

```text
Classroom 101
Classroom 102
Laboratory
```

Internally:

```text
Location
```

### Teaching Periods

The user defines the available scheduling periods.

Example:

```text
Monday    09:00 – 10:00
Monday    10:00 – 11:00
Monday    11:00 – 12:00

Tuesday   09:00 – 10:00
...
```

Internally:

```text
TimeSlot
```

### Classes / Activities

The user defines the activities that must be scheduled.

For each activity:

* Name.
* Teacher requirement.
* Student group.
* Allowed classrooms, when applicable.
* Allowed teaching periods, when applicable.

Example:

```text
Mathematics — 1A — Ana
English     — 1A — Pedro
Physics     — 2A — Laura
```

Repeated lessons are represented internally as independent atomic activities.

---

# 7. Academic Happy Path

The complete Academic flow is:

```text
Dashboard
    ↓
New Planning
    ↓
Academic Scheduling
    ↓
General Information
    ↓
Teachers
    ↓
Student Groups
    ↓
Classrooms
    ↓
Teaching Periods
    ↓
Classes
    ↓
Constraints
    ↓
Review
    ↓
Generate
    ↓
Academic Timetable
```

Example scenario:

```text
Teachers
    Ana
    Pedro

Student Groups
    1A

Classrooms
    Classroom 101
    Classroom 102

Teaching Periods
    Monday 09:00
    Monday 10:00
    Tuesday 09:00
    Tuesday 10:00

Classes
    Mathematics 1A — Ana
    Mathematics 1A — Ana
    English 1A — Pedro
    English 1A — Pedro
```

The template transforms this configuration into the generic `PlanningProblem`.

---

# 8. Work Shift Scheduling Configuration

The Work Shift template uses the same application workflow but adapts its terminology.

## General Information

* Planning name.
* Start date.
* End date.

Example:

```text
Planning name: Reception Weekly Schedule
Start date:    14/09/2026
End date:      20/09/2026
```

## Employees

For each employee:

* Name.
* Identifier.
* Optional metadata.

Internally:

```text
ResourceType = EMPLOYEE
```

## Workplaces

Optional locations where work assignments occur.

Examples:

```text
Reception
Support Desk
Office
```

Internally:

```text
Location
```

## Shifts / Time Periods

Available work periods.

Examples:

```text
Monday Morning     08:00 – 14:00
Monday Afternoon   14:00 – 20:00
Tuesday Morning    08:00 – 14:00
Tuesday Afternoon  14:00 – 20:00
```

Internally:

```text
TimeSlot
```

## Work Assignments

The activities that need to be covered.

Examples:

```text
Monday Morning Reception
Monday Afternoon Reception
Tuesday Morning Reception
Tuesday Afternoon Reception
```

Internally:

```text
Activity
```

Each activity defines the required employee resources and optional workplace.

---

# 9. Work Shift Happy Path

The Work Shift flow is:

```text
Dashboard
    ↓
New Planning
    ↓
Work Shift Scheduling
    ↓
General Information
    ↓
Employees
    ↓
Workplaces
    ↓
Shifts
    ↓
Work Assignments
    ↓
Constraints
    ↓
Review
    ↓
Generate
    ↓
Work Schedule
```

Example:

```text
Employees
    Ana
    Laura
    Pedro

Workplaces
    Reception

Shifts
    Monday Morning
    Monday Afternoon
    Tuesday Morning
    Tuesday Afternoon

Work Assignments
    Monday Morning Reception
    Monday Afternoon Reception
    Tuesday Morning Reception
    Tuesday Afternoon Reception
```

Again, the template transforms these concepts into the same generic `PlanningProblem` used by the Academic template.

---

# 10. Step 4 — Configure Constraints

## Purpose

Allow the user to define the rules that the generated schedule should respect.

Constraints are divided into:

```text
Hard Constraints
Soft Constraints
```

The UI should clearly explain the difference.

### Hard Constraints

Rules that should not be violated.

Examples:

```text
✓ Avoid resource overlaps
✓ Respect resource availability
✓ Respect required resources
✓ Maximum assignments
```

### Soft Constraints

Preferences that improve schedule quality but may be violated if necessary.

Examples:

```text
○ Prefer morning periods
○ Minimize consecutive assignments
○ Balance workload
```

Templates should provide sensible default constraints so the user does not need to configure every rule manually.

For each constraint, the interface should show:

* Name.
* Short description.
* Hard or Soft classification.
* Affected entity or entities.
* Configuration parameters, when necessary.
* Weight or importance for configurable soft constraints, when exposed to the user.

Advanced weighting options may remain hidden in the MVP unless required.

---

# 11. Step 5 — Review

## Purpose

Allow the user to verify the planning problem before executing the Genetic Engine.

The Review page should summarize the complete configuration.

Example for Academic Scheduling:

```text
Academic Schedule

Planning Horizon
14/09/2026 — 18/09/2026

Resources
2 Teachers
1 Student Group

Activities
4 Classes

Time Slots
20 available periods

Locations
2 Classrooms

Constraints
4 Hard Constraints
2 Soft Constraints
```

The page should provide access to each configuration section for corrections.

Example:

```text
Teachers          2      [Edit]
Student Groups    1      [Edit]
Classes           4      [Edit]
Time Slots       20      [Edit]
Classrooms        2      [Edit]
Constraints       6      [Edit]
```

The main action is:

```text
[ Generate Schedule ]
```

Generation should only be enabled when the planning problem passes structural validation.

---

# 12. Step 6 — Generate

## Purpose

Execute the Genetic Engine.

The user should not need to configure genetic algorithm parameters in the normal workflow.

The application should use a predefined optimization configuration, such as:

```text
Balanced
```

Advanced configurations such as:

```text
Fast
Balanced
Exhaustive
```

may be exposed as an optional setting.

## Information displayed during generation

The UI should communicate that optimization is in progress.

Example:

```text
Generating schedule...

Optimizing assignments and constraints.

[ Progress indicator ]
```

Technical details such as chromosome representation, crossover, mutation, or Jenetics internals should not be required for normal users.

---

# 13. Step 7 — Results

## Purpose

Present the generated schedule and allow the user to understand its quality.

The primary element should be the schedule itself.

For Academic Scheduling, the preferred visualization is a timetable/calendar view.

Example:

```text
          Monday          Tuesday

09:00     Mathematics     English
          1A              1A
          Room 101        Room 102

10:00     English         Mathematics
          1A              1A
          Room 102        Room 101
```

For Work Shift Scheduling, the result may use a shift-oriented table:

```text
                Morning       Afternoon

Monday          Ana           Pedro
Tuesday         Laura         Ana
Wednesday       Pedro         Laura
```

---

## 13.1 Solution Quality

The Results page should also display basic optimization information.

Example:

```text
Solution Quality

Hard violations: 0
Soft violations: 2
Fitness: 0.94
```

The exact fitness representation will depend on the final fitness model.

Hard constraint violations should be clearly highlighted because they may indicate that the generated schedule is not fully feasible.

---

## 13.2 Result Actions

Depending on the implemented MVP scope, possible actions include:

```text
[ Generate Again ]
[ Edit Configuration ]
[ Back to Dashboard ]
```

Export functionality is not required for the initial MVP and may be added later.

---

# 14. Main Navigation

The application should use a simple global navigation structure.

Recommended primary navigation:

```text
Genetic Planner

Dashboard
New Planning
```

Additional sections such as saved planning problems or history should only be introduced if persistence is implemented.

During planning configuration, a stepper should show the current workflow:

```text
Template
   ↓
Problem
   ↓
Constraints
   ↓
Review
   ↓
Generate
```

A visual representation could be:

```text
[1 Template] ── [2 Problem] ── [3 Constraints] ── [4 Review]
```

The current step should be visually identifiable.

---

# 15. Navigation Rules

The following navigation rules apply:

### Forward navigation

The user may continue to the next step when the minimum required information for the current step is valid.

### Back navigation

The user may return to previous configuration steps without losing previously entered information.

### Review navigation

The Review page should provide direct access to editable configuration sections.

### Results navigation

From Results, the user should be able to:

* Return to the planning configuration.
* Generate another solution.
* Return to the Dashboard.

### Browser navigation

When technically feasible, browser Back/Forward navigation should not unexpectedly destroy the current planning configuration.

---

# 16. Validation and Main Error Scenarios

Validation should occur before schedule generation whenever possible.

Errors are divided into configuration errors, feasibility problems, generation errors, and unexpected system errors.

---

## 16.1 Missing Required Information

Example:

```text
No activities have been defined.
```

The application should:

* Identify the affected section.
* Explain what information is missing.
* Prevent generation.
* Provide a direct way to return to the relevant configuration step.

Example:

```text
At least one class is required before generating a schedule.

[ Go to Classes ]
```

---

## 16.2 Invalid Planning Horizon

Example:

```text
End date is earlier than start date.
```

The application should display the validation error close to the affected fields and prevent progression until corrected.

---

## 16.3 Invalid References

Example:

An Activity references a Teacher or Location that has been removed.

The application should detect the invalid reference and request that the activity be corrected.

Example:

```text
Mathematics 1A references teacher "Ana", which is no longer available.

[ Edit Activity ]
```

---

## 16.4 No Valid Time Slots

An activity may have no valid TimeSlot candidates.

Example:

```text
Mathematics 1A cannot be assigned to any available teaching period.
```

Generation should be prevented when this can be determined during structural validation.

---

## 16.5 Impossible Resource Requirement

Example:

An activity requires one resource of type `TEACHER`, but no valid teacher is available.

The user should receive a domain-friendly message:

```text
Mathematics 1A requires a teacher, but no eligible teacher is available.
```

The UI should avoid exposing internal model terminology unless useful.

---

## 16.6 Conflicting Constraints

The configuration may be structurally valid but impossible to satisfy because of conflicting hard constraints.

Example:

```text
Teacher Ana must teach Mathematics on Monday morning,
but Ana is unavailable during all Monday morning periods.
```

When the conflict can be detected before optimization, the application should report it before generation.

When it cannot be determined beforehand, the Genetic Engine may return a solution containing hard violations.

---

## 16.7 No Feasible Schedule Found

The Genetic Engine may fail to find a schedule satisfying all hard constraints within the configured optimization limits.

The application should not simply display:

```text
Generation failed.
```

Instead, it should explain:

```text
No fully feasible schedule was found.

Best solution:
3 hard constraint violations
2 soft constraint violations
```

When possible, the violations should be listed so the user can modify the configuration.

Possible actions:

```text
[ Review Constraints ]
[ Edit Planning ]
[ Generate Again ]
```

---

## 16.8 Generation Error

If the optimization process cannot complete because of an application error:

```text
The schedule could not be generated due to an unexpected error.

Please review the configuration and try again.
```

Technical stack traces should not be shown in the user interface.

---

# 17. Error Handling Principles

Error messages should:

* Explain what happened.
* Use terminology appropriate to the selected template.
* Identify the affected entity or configuration section when possible.
* Explain how the user can resolve the problem.
* Avoid exposing internal implementation details.
* Preserve entered configuration whenever possible.

The preferred pattern is:

```text
What happened
      +
Where the problem is
      +
How to correct it
```

For example:

```text
English 1A has no available teacher.

Add an eligible teacher or modify the activity requirements.

[ Edit English 1A ]
```

---

# 18. Template Independence

Although Academic Scheduling and Work Shift Scheduling present different terminology, both follow the same application journey:

```text
Template
   │
   ▼
Domain-specific configuration
   │
   ▼
Generic PlanningProblem
   │
   ▼
Constraints
   │
   ▼
Genetic Engine
   │
   ▼
Schedule
   │
   ▼
Domain-specific visualization
```

The frontend may therefore adapt labels and forms while preserving the same underlying application structure.

For example:

| Generic Concept | Academic                | Work Shift      |
| --------------- | ----------------------- | --------------- |
| Resource        | Teacher / Student Group | Employee        |
| Activity        | Class                   | Work Assignment |
| TimeSlot        | Teaching Period         | Shift           |
| Location        | Classroom               | Workplace       |
| Schedule        | Timetable               | Work Schedule   |

This separation supports the core product principle:

> **Genericity in the model and genetic engine; specificity in templates and UX.**

---

# 19. Application Structure

The resulting high-level application structure is:

```text
Genetic Planner
│
├── Dashboard
│
├── New Planning
│   │
│   ├── Template Selection
│   │
│   ├── Problem Configuration
│   │   │
│   │   ├── General Information
│   │   ├── Resources
│   │   ├── Locations
│   │   ├── Time Slots
│   │   └── Activities
│   │
│   ├── Constraint Configuration
│   ├── Review
│   └── Generate
│
└── Results
    │
    ├── Schedule
    ├── Solution Quality
    ├── Constraint Violations
    └── Result Actions
```

Templates customize the contents of the Problem Configuration and Results sections without modifying this overall structure.

---

# 20. User Journey Summary

The main journey can be summarized as:

```text
                         ┌───────────────────┐
                         │     Dashboard     │
                         └─────────┬─────────┘
                                   │
                                   ▼
                         ┌───────────────────┐
                         │   New Planning    │
                         └─────────┬─────────┘
                                   │
                                   ▼
                         ┌───────────────────┐
                         │  Choose Template  │
                         └─────────┬─────────┘
                                   │
                      ┌────────────┴────────────┐
                      │                         │
                      ▼                         ▼
             Academic Scheduling       Work Shift Scheduling
                      │                         │
                      └────────────┬────────────┘
                                   │
                                   ▼
                         Configure Problem
                                   │
                                   ▼
                       Configure Constraints
                                   │
                                   ▼
                                Review
                                   │
                             validation
                              ┌────┴────┐
                         invalid       valid
                            │             │
                            │             ▼
                            │          Generate
                            │             │
                            └── edit ◄────┤
                                          ▼
                                        Results
                                          │
                            ┌─────────────┼─────────────┐
                            ▼             ▼             ▼
                          Edit       Generate Again   Dashboard
```
