# Genetic Planner — Main Application Wireframes

## 1. Purpose

This document defines the main low-fidelity wireframes for the Genetic Planner MVP.

The objective is to establish:

* Main screens.
* Navigation.
* Information hierarchy.
* Planning configuration workflow.
* Primary actions.
* Schedule generation flow.
* Results visualization.

These wireframes describe structure and behaviour rather than final visual styling.

Final visual implementation will use the frontend strategy defined for Genetic Planner:

* React.
* TypeScript.
* Material UI.
* FullCalendar.
* React Hook Form.
* Zod.
* TanStack Query.
* Axios.

---

# 2. Main User Flow

The primary user journey is:

```text
Dashboard
    ↓
New Planning
    ↓
Template Selection
    ↓
Planning Wizard
    ├── Basic Information
    ├── Resources
    ├── Activities
    ├── Time Slots & Locations
    ├── Constraints
    ├── Optimization
    └── Review
            ↓
        Generate
            ↓
       Generating
            ↓
         Results
```

The Planning Wizard represents the main configuration experience.

---

# 3. Global Application Layout

The application uses a simple shared layout.

```text
┌──────────────────────────────────────────────────────────────────┐
│ Genetic Planner                                      [Help]      │
├────────────────┬─────────────────────────────────────────────────┤
│                │                                                 │
│ Dashboard      │                                                 │
│ New Planning   │                 Main content                    │
│                │                                                 │
│                │                                                 │
│                │                                                 │
└────────────────┴─────────────────────────────────────────────────┘
```

The navigation should remain minimal for the MVP.

Primary navigation:

* Dashboard.
* New Planning.

Authentication and user profile navigation are not required.

---

# 4. Dashboard

## Purpose

The Dashboard is the application entry point.

It allows the user to:

* Start a new planning problem.
* Access existing planning problems when persistence is enabled.
* Quickly understand the application purpose.

---

## Wireframe

```text
┌──────────────────────────────────────────────────────────────────┐
│ Genetic Planner                                                  │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│ Dashboard                                                        │
│                                                                  │
│ Create and optimise schedules using genetic algorithms.          │
│                                                                  │
│                                    [ + New Planning ]             │
│                                                                  │
├──────────────────────────────────────────────────────────────────┤
│ Recent Planning Problems                                         │
│                                                                  │
│ ┌──────────────────────────────────────────────────────────────┐ │
│ │ Computer Science Timetable                                  │ │
│ │ Academic Scheduling                                        │ │
│ │ Last modified: ...                           [ Open ]        │ │
│ └──────────────────────────────────────────────────────────────┘ │
│                                                                  │
│ ┌──────────────────────────────────────────────────────────────┐ │
│ │ Office Weekly Shifts                                       │ │
│ │ Work Shift Scheduling                                     │ │
│ │ Last modified: ...                           [ Open ]        │ │
│ └──────────────────────────────────────────────────────────────┘ │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

---

## Empty State

If no planning problems exist:

```text
┌───────────────────────────────────────────────┐
│                                               │
│        No planning problems yet               │
│                                               │
│ Create your first planning problem and let    │
│ Genetic Planner optimise the schedule.        │
│                                               │
│             [ + New Planning ]                │
│                                               │
└───────────────────────────────────────────────┘
```

---

# 5. Template Selection

## Purpose

The first step when creating a planning problem is selecting the planning scenario.

Initial options:

* Academic Scheduling.
* Work Shift Scheduling.

---

## Wireframe

```text
┌──────────────────────────────────────────────────────────────────┐
│ New Planning                                                     │
│                                                                  │
│ Choose a planning template                                       │
│                                                                  │
│ Select the scenario that best describes your planning problem.   │
│                                                                  │
│ ┌───────────────────────────┐   ┌───────────────────────────────┐ │
│ │ 🎓 Academic Scheduling    │   │ 🕒 Work Shift Scheduling    │ │
│ │                           │   │                               │ │
│ │ Generate class and        │   │ Generate employee work       │ │
│ │ teaching schedules.       │   │ shift schedules.             │ │
│ │                           │   │                               │ │
│ │ Teachers                  │   │ Employees                     │ │
│ │ Student groups            │   │ Work assignments              │ │
│ │ Classes                   │   │ Shifts                        │ │
│ │ Classrooms                │   │ Workplaces                    │ │
│ │                           │   │                               │ │
│ │       [ Select ]          │   │        [ Select ]             │ │
│ └───────────────────────────┘   └───────────────────────────────┘ │
│                                                                  │
│ [ Cancel ]                                                       │
└──────────────────────────────────────────────────────────────────┘
```

Academic Scheduling is the primary MVP scenario.

Work Shift Scheduling validates the generic planning architecture.

---

# 6. Planning Wizard

## Purpose

The Planning Wizard guides the user through creation of a `PlanningProblem`.

The wizard avoids presenting all configuration options simultaneously.

---

## Steps

```text
1 Template
2 Basics
3 Resources
4 Activities
5 Time Slots
6 Constraints
7 Optimization
8 Review
```

Locations are configured together with Time Slots for the MVP.

---

## Global Wizard Layout

```text
┌──────────────────────────────────────────────────────────────────┐
│ New Planning — Academic Scheduling                               │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ✓ Template ─ ✓ Basics ─ ● Resources ─ Activities ─ Time Slots  │
│                                  ─ Constraints ─ Optimization    │
│                                  ─ Review                        │
│                                                                  │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│                         Step Content                             │
│                                                                  │
│                                                                  │
│                                                                  │
├──────────────────────────────────────────────────────────────────┤
│ [ Back ]                                        [ Save & Next ]  │
└──────────────────────────────────────────────────────────────────┘
```

The current step must always be visible.

The user can navigate backwards without losing entered information.

---

# 7. Basic Information

Although not explicitly a separate acceptance criterion, basic planning information is required before configuring entities.

---

## Wireframe

```text
┌──────────────────────────────────────────────────────────────────┐
│ Basic Information                                                │
│                                                                  │
│ Planning name *                                                  │
│ ┌──────────────────────────────────────────────────────────────┐ │
│ │ Computer Science Timetable                                  │ │
│ └──────────────────────────────────────────────────────────────┘ │
│                                                                  │
│ Planning period                                                  │
│                                                                  │
│ Start date                     End date                           │
│ [ 15/09/2026 ]                 [ 19/09/2026 ]                    │
│                                                                  │
│ Description                                                      │
│ ┌──────────────────────────────────────────────────────────────┐ │
│ │ Optional description...                                    │ │
│ └──────────────────────────────────────────────────────────────┘ │
│                                                                  │
│ [ Back ]                                        [ Save & Next ]  │
└──────────────────────────────────────────────────────────────────┘
```

---

# 8. Resources

## Purpose

Configure the resources participating in the planning problem.

The terminology depends on the selected template.

Academic example:

* Teachers.
* Student Groups.

Work Shift example:

* Employees.

---

## Academic Wireframe

```text
┌──────────────────────────────────────────────────────────────────┐
│ Resources                                                        │
│                                                                  │
│ Configure the people or groups required by activities.           │
│                                                                  │
│ [ Teachers ] [ Student Groups ]                                  │
│                                                                  │
│ Teachers                                      [ + Add Teacher ]   │
│                                                                  │
│ ┌──────────────────────────────────────────────────────────────┐ │
│ │ Name             Availability                  Actions       │ │
│ ├──────────────────────────────────────────────────────────────┤ │
│ │ Ana López        Mon–Fri                       Edit  Delete   │ │
│ │ Carlos Ruiz      Mon, Tue, Thu                 Edit  Delete   │ │
│ └──────────────────────────────────────────────────────────────┘ │
│                                                                  │
│ 2 teachers                                                       │
│                                                                  │
│ [ Back ]                                        [ Save & Next ]  │
└──────────────────────────────────────────────────────────────────┘
```

---

## Add Resource Dialog

```text
┌───────────────────────────────────────┐
│ Add Teacher                           │
│                                       │
│ Name *                                │
│ [                                  ]  │
│                                       │
│ Available periods                     │
│ [ Configure availability ]            │
│                                       │
│                [ Cancel ] [ Add ]      │
└───────────────────────────────────────┘
```

Resource configuration should remain simple.

Advanced arbitrary attributes are not exposed unless required.

---

# 9. Activities

## Purpose

Define the activities that must be scheduled.

Academic examples:

* Mathematics — Group 1A.
* Programming — Group 2B.

Work Shift examples:

* Reception Morning.
* Night Support.

---

## Wireframe

```text
┌──────────────────────────────────────────────────────────────────┐
│ Activities                                      [ + Add Activity ]│
│                                                                  │
│ Define what must be placed into the schedule.                    │
│                                                                  │
│ ┌──────────────────────────────────────────────────────────────┐ │
│ │ Activity       Required Resources       Sessions    Actions  │ │
│ ├──────────────────────────────────────────────────────────────┤ │
│ │ Mathematics   Teacher + Group 1A          3        Edit  ×   │ │
│ │ Programming   Teacher + Group 2A          2        Edit  ×   │ │
│ │ Databases     Teacher + Group 1B          2        Edit  ×   │ │
│ └──────────────────────────────────────────────────────────────┘ │
│                                                                  │
│ 7 schedulable activities will be generated.                      │
│                                                                  │
│ [ Back ]                                        [ Save & Next ]  │
└──────────────────────────────────────────────────────────────────┘
```

---

## Add Activity

```text
┌────────────────────────────────────────────────────┐
│ Add Activity                                       │
│                                                    │
│ Name *                                             │
│ [ Mathematics                                   ] │
│                                                    │
│ Number of sessions                                 │
│ [ 3 ]                                              │
│                                                    │
│ Required teacher                                   │
│ [ Any compatible teacher ▼ ]                       │
│                                                    │
│ Student group                                      │
│ [ Group 1A ▼ ]                                     │
│                                                    │
│ Allowed classrooms                                 │
│ [ Any compatible classroom ▼ ]                     │
│                                                    │
│                        [ Cancel ] [ Add Activity ]  │
└────────────────────────────────────────────────────┘
```

When several sessions are requested, the template transforms them into separate atomic `Activity` instances.

---

# 10. Time Slots and Locations

## Purpose

Define when activities may occur and, where relevant, the available locations.

---

## Wireframe

```text
┌──────────────────────────────────────────────────────────────────┐
│ Time Slots & Locations                                           │
│                                                                  │
│ [ Time Slots ] [ Locations ]                                     │
│                                                                  │
│ Time Slots                                      [ + Add Period ]  │
│                                                                  │
│ ┌──────────────────────────────────────────────────────────────┐ │
│ │ Day          Start       End                     Actions     │ │
│ ├──────────────────────────────────────────────────────────────┤ │
│ │ Monday       09:00       10:00                   Edit   ×    │ │
│ │ Monday       10:00       11:00                   Edit   ×    │ │
│ │ Monday       11:00       12:00                   Edit   ×    │ │
│ │ Tuesday      09:00       10:00                   Edit   ×    │ │
│ └──────────────────────────────────────────────────────────────┘ │
│                                                                  │
│ [ + Generate recurring periods ]                                 │
│                                                                  │
│ [ Back ]                                        [ Save & Next ]  │
└──────────────────────────────────────────────────────────────────┘
```

A helper for recurring periods prevents the user from manually creating dozens of slots.

---

## Recurring Period Dialog

```text
┌──────────────────────────────────────────────┐
│ Generate Teaching Periods                    │
│                                              │
│ Days                                         │
│ ☑ Monday   ☑ Tuesday   ☑ Wednesday           │
│ ☑ Thursday ☑ Friday                          │
│                                              │
│ From       [ 09:00 ]                         │
│ To         [ 14:00 ]                         │
│                                              │
│ Period duration                              │
│ [ 60 minutes ▼ ]                             │
│                                              │
│                 [ Cancel ] [ Generate ]       │
└──────────────────────────────────────────────┘
```

---

## Locations Tab

```text
┌──────────────────────────────────────────────────────────────┐
│ Locations                                  [ + Add Classroom ]│
│                                                              │
│ Name                Capacity                    Actions       │
│ Room 101              30                        Edit   ×      │
│ Computer Lab          25                        Edit   ×      │
│ Physics Lab           20                        Edit   ×      │
└──────────────────────────────────────────────────────────────┘
```

Locations remain optional when the selected planning scenario does not require them.

---

# 11. Constraints

## Purpose

Allow the user to review and configure planning rules.

Constraints must clearly distinguish:

```text
HARD
SOFT
```

---

## Wireframe

```text
┌──────────────────────────────────────────────────────────────────┐
│ Constraints                                                      │
│                                                                  │
│ Define the rules used to evaluate generated schedules.           │
│                                                                  │
│ HARD CONSTRAINTS                                                 │
│ Required for a valid schedule                                    │
│                                                                  │
│ ┌──────────────────────────────────────────────────────────────┐ │
│ │ ☑ No overlapping assignments                   Weight 1000  │ │
│ │ ☑ Respect resource availability                Weight 1000  │ │
│ │ ☑ Required resources                           Weight 1000  │ │
│ └──────────────────────────────────────────────────────────────┘ │
│                                                                  │
│ SOFT CONSTRAINTS                                                 │
│ Used to improve schedule quality                                 │
│                                                                  │
│ ┌──────────────────────────────────────────────────────────────┐ │
│ │ ☑ Preferred time slots                         Weight [10]   │ │
│ │ ☑ Maximum consecutive activities               Weight [ 5]   │ │
│ │ ☐ Minimum assignments                           Weight [10]   │ │
│ └──────────────────────────────────────────────────────────────┘ │
│                                                                  │
│ [ Back ]                                        [ Save & Next ]  │
└──────────────────────────────────────────────────────────────────┘
```

Default constraints come from the selected `PlanningTemplate`.

---

# 12. Constraint Editing

A constraint can expose parameters when required.

Example:

```text
┌─────────────────────────────────────────────┐
│ Maximum Consecutive Activities              │
│                                             │
│ Maximum consecutive assignments            │
│ [ 3 ]                                       │
│                                             │
│ Weight                                      │
│ [ 5 ]                                       │
│                                             │
│                [ Cancel ] [ Save ]          │
└─────────────────────────────────────────────┘
```

The UI does not determine whether the constraint is HARD or SOFT dynamically.

Its semantic type comes from the constraint catalogue.

---

# 13. Optimization

The user selects how intensively the Genetic Algorithm should search.

Advanced parameters are optional.

---

## Wireframe

```text
┌──────────────────────────────────────────────────────────────────┐
│ Optimization                                                     │
│                                                                  │
│ Choose optimization effort                                      │
│                                                                  │
│ ┌──────────────────┐ ┌──────────────────┐ ┌───────────────────┐  │
│ │ FAST             │ │ BALANCED        │ │ EXHAUSTIVE        │  │
│ │                  │ │                  │ │                   │  │
│ │ Faster results   │ │ Recommended      │ │ Deeper search     │  │
│ │ Lower effort     │ │                  │ │ Higher effort     │  │
│ │                  │ │       ✓          │ │                   │  │
│ └──────────────────┘ └──────────────────┘ └───────────────────┘  │
│                                                                  │
│ ▸ Advanced settings                                              │
│                                                                  │
│ [ Back ]                                        [ Save & Next ]  │
└──────────────────────────────────────────────────────────────────┘
```

`BALANCED` is selected by default.

---

## Advanced Settings

When expanded:

```text
Population size          [ 100 ]
Generation limit         [ 300 ]
Mutation probability     [ 0.15 ]
Crossover probability    [ 0.80 ]
Elite count              [ 5 ]
```

Most users should not need this section.

---

# 14. Review

## Purpose

Allow users to verify the complete planning configuration before launching optimization.

---

## Wireframe

```text
┌──────────────────────────────────────────────────────────────────┐
│ Review                                                           │
│                                                                  │
│ Computer Science Timetable                                       │
│ Academic Scheduling                                              │
│                                                                  │
│ ┌────────────────────┐ ┌────────────────────┐                    │
│ │ Resources          │ │ Activities         │                    │
│ │ 8 teachers         │ │ 24 activities      │                    │
│ │ 4 groups           │ │                    │                    │
│ │           [ Edit ] │ │           [ Edit ] │                    │
│ └────────────────────┘ └────────────────────┘                    │
│                                                                  │
│ ┌────────────────────┐ ┌────────────────────┐                    │
│ │ Time Slots         │ │ Locations          │                    │
│ │ 30 periods         │ │ 6 classrooms       │                    │
│ │           [ Edit ] │ │           [ Edit ] │                    │
│ └────────────────────┘ └────────────────────┘                    │
│                                                                  │
│ Constraints                                                      │
│ 3 HARD   •   2 SOFT                                  [ Edit ]    │
│                                                                  │
│ Optimization                                                     │
│ BALANCED                                             [ Edit ]    │
│                                                                  │
│ Validation                                                       │
│ ✓ Planning problem is valid                                      │
│                                                                  │
│ [ Back ]                              [ Generate Schedule ]       │
└──────────────────────────────────────────────────────────────────┘
```

---

# 15. Validation Errors

Generation cannot start when structural validation fails.

Example:

```text
┌──────────────────────────────────────────────────────────────┐
│ ⚠ Planning problem requires attention                        │
│                                                              │
│ • Mathematics has no valid time slot.                        │
│ • Group 1A does not exist.                                   │
│                                                              │
│ [ Review Activities ]                                        │
└──────────────────────────────────────────────────────────────┘
```

The Review step therefore acts as the final validation boundary before optimization.

---

# 16. Generating

## Purpose

Clearly indicate that optimization is running.

The initial MVP does not require real-time generation progress.

---

## Wireframe

```text
┌──────────────────────────────────────────────────────────────────┐
│ Generating Schedule                                              │
│                                                                  │
│                                                                  │
│                         ◌                                        │
│                                                                  │
│               Optimising your schedule...                        │
│                                                                  │
│      Genetic Planner is evaluating candidate schedules           │
│      and searching for the best solution.                        │
│                                                                  │
│              Optimization mode: BALANCED                         │
│                                                                  │
│                  Please keep this page open.                      │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

The spinner/progress indicator represents an indeterminate operation.

We should not show fictitious percentages if the backend does not provide real progress.

---

# 17. Generation Error

If optimization fails:

```text
┌───────────────────────────────────────────────┐
│ Unable to generate schedule                   │
│                                               │
│ The optimization process could not complete.  │
│                                               │
│ [ Back to configuration ]     [ Try Again ]   │
└───────────────────────────────────────────────┘
```

Technical details should not be exposed directly to the user.

---

# 18. Results

## Purpose

The Results screen is the main output of Genetic Planner.

It must show both:

* The generated schedule.
* The quality of the solution.

---

## Main Wireframe

```text
┌──────────────────────────────────────────────────────────────────┐
│ Computer Science Timetable                         [ Regenerate ] │
│                                                                  │
│ ✓ Feasible Schedule                                               │
│                                                                  │
│ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ │
│ │ Fitness     │ │ HARD        │ │ SOFT        │ │ Time        │ │
│ │    35       │ │ 0 violations│ │ 5 violations│ │ 1.8 s       │ │
│ └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘ │
│                                                                  │
│ [ Calendar ] [ Table ] [ Constraint Details ]                    │
│                                                                  │
│ ┌──────────────────────────────────────────────────────────────┐ │
│ │          MON       TUE       WED       THU       FRI        │ │
│ │ 09:00  Math      Physics    ...       ...        ...        │ │
│ │                                                              │ │
│ │ 10:00  Prog.     Math       ...       ...        ...        │ │
│ │                                                              │ │
│ │ 11:00  DB        Prog.      ...       ...        ...        │ │
│ │                                                              │ │
│ └──────────────────────────────────────────────────────────────┘ │
│                                                                  │
│                       [ Back to Dashboard ]                       │
└──────────────────────────────────────────────────────────────────┘
```

The actual calendar will be implemented with FullCalendar.

---

# 19. Calendar Event

Selecting an activity should reveal its details.

```text
┌────────────────────────────────────┐
│ Mathematics                        │
│                                    │
│ Monday                             │
│ 09:00 – 10:00                      │
│                                    │
│ Teacher                            │
│ Ana López                          │
│                                    │
│ Student Group                      │
│ Group 1A                           │
│                                    │
│ Classroom                          │
│ Room 101                           │
│                                    │
│                           [ Close ] │
└────────────────────────────────────┘
```

---

# 20. Table View

The same result can be represented as:

```text
┌──────────────────────────────────────────────────────────────────┐
│ Activity      Day       Time         Resources         Location  │
├──────────────────────────────────────────────────────────────────┤
│ Mathematics  Monday    09–10        Ana, Group 1A     Room 101  │
│ Programming  Monday    10–11        Carlos, Group 2A  Lab 1     │
│ Physics      Tuesday   09–10        Ana, Group 1B     Lab 2     │
└──────────────────────────────────────────────────────────────────┘
```

This provides an accessible alternative to calendar-only visualization.

---

# 21. Constraint Breakdown

The Results page should explain the fitness rather than showing only a number.

Example:

```text
┌───────────────────────────────────────────────────────────────┐
│ Constraint Breakdown                                          │
│                                                               │
│ HARD                                                          │
│ ✓ No overlapping assignments                    0 violations │
│ ✓ Resource availability                        0 violations │
│ ✓ Required resources                            0 violations │
│                                                               │
│ SOFT                                                          │
│ Preferred time slots                          3 × 10 = 30    │
│ Maximum consecutive activities                 1 × 5 = 5     │
│                                                               │
│ Total Fitness                                      35         │
└───────────────────────────────────────────────────────────────┘
```

This directly reflects the previously defined fitness model.

---

# 22. Infeasible Result

The Genetic Algorithm may return its best candidate even if it is not feasible.

The UI must make this clear.

```text
┌───────────────────────────────────────────────────────────────┐
│ ⚠ Best solution found is not fully feasible                   │
│                                                               │
│ Fitness: 1035                                                 │
│                                                               │
│ HARD violations: 1                                            │
│ SOFT violations: 3                                            │
│                                                               │
│ Review the constraint details before using this schedule.      │
└───────────────────────────────────────────────────────────────┘
```

An infeasible result must never be visually presented as a valid schedule.

---

# 23. Responsive Behaviour

Desktop is the primary target for the MVP because planning configuration and schedule visualization benefit from larger screens.

The interface should nevertheless remain usable on narrower screens.

On smaller widths:

```text
Cards
→ stack vertically

Tables
→ horizontal scroll if necessary

Wizard step labels
→ compact representation

Calendar
→ reduced day/time view
```

A dedicated mobile application is outside the project scope.

---

# 24. Component Mapping

The wireframes map naturally to the selected frontend strategy.

| Wireframe Element   | Suggested Component         |
| ------------------- | --------------------------- |
| Global layout       | MUI AppBar / Drawer         |
| Template cards      | MUI Card                    |
| Wizard              | MUI Stepper                 |
| Configuration forms | React Hook Form + MUI       |
| Validation          | Zod + MUI form errors       |
| Resource lists      | MUI Table                   |
| Activity lists      | MUI Table                   |
| Constraints         | MUI Cards / Switch / Inputs |
| Optimization modes  | MUI Cards                   |
| Review metrics      | MUI Cards                   |
| Loading             | MUI CircularProgress        |
| Result metrics      | MetricCard                  |
| Schedule            | FullCalendar                |
| Schedule table      | MUI Table                   |
| Result tabs         | MUI Tabs                    |
| Dialogs             | MUI Dialog                  |

---

# 25. Navigation Summary

```text
Dashboard
    │
    └── New Planning
             │
             ▼
      Template Selection
             │
             ▼
       Planning Wizard
             │
             ├── Basics
             ├── Resources
             ├── Activities
             ├── Time Slots & Locations
             ├── Constraints
             ├── Optimization
             └── Review
                    │
                    ▼
                 Generate
                    │
                    ▼
                Generating
                    │
                    ▼
                  Results
                    │
              ┌─────┴───────┐
              ▼             ▼
          Calendar         Table
              │
              ▼
      Constraint Details
```

---

# 26. Design Decisions

## WF1 — Dashboard is the application entry point

The user can create a new planning problem or reopen an existing one.

## WF2 — Template selection occurs before configuration

The selected template determines terminology and defaults.

## WF3 — Planning configuration uses a wizard

The configuration process is divided into manageable steps.

## WF4 — Academic terminology is shown for the Academic Template

The user interacts with Teachers, Classes and Classrooms rather than generic internal concepts where appropriate.

## WF5 — Resources and Activities use table-based editors

This allows multiple planning entities to be reviewed and edited efficiently.

## WF6 — Time Slots support bulk creation

Users should not need to manually create every recurring teaching or shift period.

## WF7 — HARD and SOFT constraints are visually separated

The semantic distinction must be visible to the user.

## WF8 — Genetic Algorithm complexity is hidden by default

Users primarily select FAST, BALANCED or EXHAUSTIVE.

Advanced parameters remain optional.

## WF9 — Review validates the planning problem

Structural errors must be resolved before schedule generation.

## WF10 — Generation uses an indeterminate progress state

No fictitious percentage is displayed unless genuine progress information exists.

## WF11 — Results combine schedule and quality information

A generated calendar alone is insufficient.

Fitness and constraint violations must also be visible.

## WF12 — Calendar and table views coexist

The calendar provides graphical understanding while the table provides precise and accessible information.

## WF13 — Infeasible schedules are clearly identified

The best genetic candidate must not be presented as a valid schedule when HARD violations remain.

---

# 27. Summary

The main Genetic Planner user experience is intentionally linear:

```text
Select problem type
        ↓
Configure planning data
        ↓
Configure constraints
        ↓
Select optimization effort
        ↓
Review
        ↓
Generate
        ↓
Understand the result
```

The interface exposes domain-oriented terminology to users while maintaining the generic `PlanningProblem` architecture internally.

The wireframes deliberately favour clarity and implementation speed over advanced UI functionality, keeping the interface aligned with the Genetic Planner MVP.
