# Genetic Planner — Frontend Technology and Component Strategy

## 1. Purpose

This document defines the frontend technology and component strategy for Genetic Planner.

The frontend must provide an intuitive and visually attractive interface for:

* Creating planning problems.
* Selecting planning templates.
* Configuring resources and activities.
* Configuring constraints.
* Selecting optimization settings.
* Generating schedules.
* Visualizing generated schedules.
* Inspecting fitness and constraint violations.

The frontend architecture should remain simple enough to implement within the MVP while providing reusable components for both Academic and Work Shift planning scenarios.

---

# 2. Technology Stack

The selected frontend stack is:

| Concern                | Technology            |
| ---------------------- | --------------------- |
| UI framework           | React                 |
| Language               | TypeScript            |
| Build tool             | Vite                  |
| Component library      | Material UI           |
| Calendar visualization | FullCalendar          |
| Server state           | TanStack Query        |
| Local UI state         | React state / Context |
| HTTP client            | Axios                 |
| Form handling          | React Hook Form       |
| Form validation        | Zod                   |

The implementation should use the current stable compatible versions when the frontend project is created.

---

# 3. React

React is the selected UI framework.

The frontend will be implemented as a Single Page Application.

React is responsible for:

* Rendering pages.
* Managing user interaction.
* Composing reusable UI components.
* Managing local interface state.
* Integrating forms.
* Displaying planning results.

The frontend must not implement:

* Constraint evaluation.
* Fitness calculation.
* Genetic optimization.
* Planning validation belonging to the backend.

These responsibilities remain in the backend.

---

# 4. TypeScript

TypeScript is used throughout the frontend.

Its main purposes are:

* Type-safe component properties.
* Type-safe REST API contracts.
* Safer form models.
* Easier refactoring.
* Reduced runtime errors.

Examples of frontend types include:

```typescript
interface PlanningSummary {
  id: string;
  name: string;
  templateType: TemplateType;
}

type TemplateType =
  | "ACADEMIC"
  | "WORK_SHIFT";
```

Frontend types represent API and presentation data.

They are not copies of backend domain classes with backend behavior.

---

# 5. Vite

Vite is selected as the frontend development and build tool.

It provides:

* Development server.
* TypeScript integration.
* Fast development builds.
* Production bundle generation.
* Straightforward React project setup.

The frontend will therefore live in:

```text
frontend/
```

as an independent application from:

```text
backend/
```

The two applications communicate through REST.

---

# 6. Material UI

Material UI is selected as the main component library.

It will provide the visual foundation for the application.

Typical components include:

```text
AppBar
Drawer
Button
Card
TextField
Select
Checkbox
Switch
Dialog
Stepper
Tabs
Table
Chip
Alert
Snackbar
Tooltip
Progress indicators
```

Using a component library avoids implementing common interface controls from scratch and helps maintain visual consistency.

---

# 7. Material UI Theme

Genetic Planner should define a small custom theme rather than heavily styling each component independently.

Conceptually:

```typescript
const theme = createTheme({
  // Genetic Planner visual identity
});
```

The theme will control:

* Typography.
* Spacing.
* Border radius.
* Primary and secondary colors.
* Component defaults.

The objective is a clean planning-oriented interface rather than extensive visual customization.

---

# 8. Component Strategy

Components should be divided into three categories:

```text
Shared UI
Feature Components
Pages
```

## Shared UI

Reusable presentation components.

Examples:

```text
PageHeader
LoadingState
ErrorState
EmptyState
ConfirmDialog
FormSection
StatusChip
MetricCard
```

These components must not contain domain-specific business logic.

---

## Feature Components

Components associated with Genetic Planner functionality.

Examples:

```text
TemplateSelector
ResourceEditor
ActivityEditor
TimeSlotEditor
LocationEditor
ConstraintEditor
OptimizationPresetSelector
ScheduleCalendar
FitnessSummary
ConstraintBreakdown
```

Feature components may understand frontend planning concepts.

---

## Pages

Pages compose features into complete application screens.

Examples:

```text
DashboardPage
NewPlanningPage
PlanningConfigurationPage
ReviewPage
GenerationPage
ResultsPage
```

Pages coordinate components but should avoid becoming large monolithic components.

---

# 9. Planning Wizard

Creating a planning problem is naturally represented as a multi-step workflow.

Material UI's stepper can be used for:

```text
1. Template
       ↓
2. Basic Configuration
       ↓
3. Resources
       ↓
4. Activities
       ↓
5. Constraints
       ↓
6. Optimization
       ↓
7. Review
```

The exact screen design will be defined in task #22.

The frontend strategy only establishes that these steps should be implemented as reusable feature sections rather than one very large form.

---

# 10. Template-Specific UI

The interface must support template-specific terminology.

For Academic Scheduling:

```text
Resource        → Teacher / Student Group
Activity        → Class
Location        → Classroom
TimeSlot        → Teaching Period
```

For Work Shift Scheduling:

```text
Resource        → Employee
Activity        → Work Assignment
Location        → Workplace
TimeSlot        → Shift
```

This terminology affects presentation only.

The backend still ultimately produces a generic:

```text
PlanningProblem
```

---

# 11. Shared Template Components

Academic and Work Shift templates should reuse as much UI infrastructure as possible.

For example:

```text
ResourceEditor
```

may receive configuration such as:

```typescript
<ResourceEditor
    singularLabel="Teacher"
    pluralLabel="Teachers"
/>
```

or:

```typescript
<ResourceEditor
    singularLabel="Employee"
    pluralLabel="Employees"
/>
```

The same underlying component can therefore serve multiple templates.

The objective is:

> Template-specific UX without duplicating the complete frontend.

---

# 12. Calendar Solution

FullCalendar is selected for graphical schedule visualization.

It will initially be used primarily for:

```text
Week view
Day/time grid
Schedule events
Activity details
```

Example schedule:

```text
Monday

09:00 ┌─────────────────────┐
      │ Mathematics         │
      │ Teacher: Ana        │
10:00 └─────────────────────┘

10:00 ┌─────────────────────┐
      │ Physics             │
      │ Teacher: Carlos     │
11:00 └─────────────────────┘
```

---

# 13. Calendar Scope

FullCalendar is primarily a visualization component.

The Genetic Engine remains responsible for generating the schedule.

Correct:

```text
Genetic Engine
      ↓
ScheduleResponse
      ↓
FullCalendar
```

Incorrect:

```text
FullCalendar
      ↓
Scheduling Algorithm
```

The calendar must not contain planning or optimization rules.

---

# 14. Premium Calendar Features

The MVP should not depend on premium resource scheduling features.

Standard calendar views are sufficient for the initial implementation.

If advanced resource timelines later provide significant value, they may be evaluated separately.

This avoids making a commercial calendar feature a requirement for the MVP.

---

# 15. Alternative Schedule Representation

Not every planning result is best understood exclusively as a calendar.

The Results interface may also provide a table.

For example:

| Activity    | Time      | Resources        | Location |
| ----------- | --------- | ---------------- | -------- |
| Mathematics | Mon 09:00 | Ana, Group 1A    | Room 101 |
| Physics     | Mon 10:00 | Carlos, Group 1A | Lab      |

Therefore:

```text
Schedule Result
     │
     ├── Calendar View
     │
     └── Table View
```

can share the same API data.

---

# 16. State Management Strategy

The frontend distinguishes between two types of state:

```text
Server State
Local / UI State
```

They should not be managed in the same way.

---

# 17. Server State

TanStack Query is selected for server state.

Server state includes:

```text
Planning problems
Templates
Saved planning configurations
Generated schedules
Generation results
```

Typical operations are:

```text
GET planning problem
POST planning problem
PUT configuration
POST generate schedule
GET generated result
```

TanStack Query manages:

* Fetching.
* Caching.
* Loading state.
* Error state.
* Mutations.
* Cache invalidation.
* Refetching.

Example:

```typescript
const query = useQuery({
  queryKey: ["planning", planningId],
  queryFn: () => planningApi.getPlanning(planningId),
});
```

---

# 18. Local UI State

Local UI state remains in React.

Examples include:

```text
Selected tab
Open dialog
Temporary filter
Stepper position
Expanded panel
Calendar view
```

Simple component state should use:

```typescript
useState()
```

For more structured local state:

```typescript
useReducer()
```

may be used.

---

# 19. Shared Wizard State

The planning creation wizard may require state shared between several steps before it is persisted.

For this limited requirement, React Context combined with `useReducer` is sufficient.

Conceptually:

```text
PlanningWizardContext
        │
        ├── template
        ├── resources
        ├── activities
        ├── time slots
        ├── locations
        ├── constraints
        └── optimization preset
```

This avoids adding another state management library solely for a small amount of temporary client state.

---

# 20. No Redux or Zustand Initially

The MVP will not initially introduce:

```text
Redux
Zustand
MobX
```

because the expected frontend state can be divided cleanly between:

```text
TanStack Query → server state

React state/context → local state
```

Adding another global store would introduce another abstraction without a demonstrated need.

This decision can be revisited if implementation reveals complex cross-page client state.

---

# 21. State Ownership Rule

State should live as close as possible to the component that uses it.

Recommended hierarchy:

```text
Component state
      ↓ if shared locally
Feature state
      ↓ if shared across wizard
Context
      ↓ if remote
TanStack Query
```

Global state should not be the default solution.

---

# 22. API Client

Axios is selected as the HTTP client.

All backend communication should pass through a centralized API layer.

Example:

```text
src/
└── api/
    ├── client.ts
    ├── planningApi.ts
    ├── templatesApi.ts
    └── scheduleApi.ts
```

Components should not call Axios directly.

Incorrect:

```text
React Component
      ↓
axios.get(...)
```

Preferred:

```text
React Component
      ↓
TanStack Query
      ↓
planningApi
      ↓
Axios client
      ↓
REST API
```

---

# 23. Axios Client

A shared Axios client should define configuration such as:

```typescript
export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
});
```

This provides a single place for:

* Base URL.
* Common headers.
* Timeouts.
* Request interceptors if required.
* Response/error handling if required.

Authentication interceptors are not required for the MVP because authentication is outside the project scope.

---

# 24. API Modules

API communication should be organized by feature.

Example:

```typescript
planningApi.create(...)
planningApi.get(...)
planningApi.update(...)

templateApi.getAvailableTemplates(...)

scheduleApi.generate(...)
scheduleApi.getResult(...)
```

This isolates REST details from React components.

---

# 25. Form Handling

React Hook Form is selected for form state and submission.

This is particularly relevant because Genetic Planner contains many configuration forms:

```text
Planning configuration
Resources
Activities
Availability
Time slots
Locations
Constraints
Optimization parameters
```

Using a consistent form library avoids manually managing every field through React state.

---

# 26. Form Validation

Zod is selected for frontend schema validation.

Example:

```typescript
const planningSchema = z.object({
  name: z.string().min(1),
});
```

Combined with React Hook Form:

```text
Form
 ↓
React Hook Form
 ↓
Zod validation
 ↓
Valid DTO
 ↓
API Client
```

---

# 27. Frontend and Backend Validation

Frontend validation improves usability but does not replace backend validation.

Correct architecture:

```text
User input
    ↓
Zod validation
    ↓
REST request
    ↓
Backend validation
    ↓
ProblemValidator
```

The backend remains authoritative.

For example, the frontend may detect:

```text
Planning name is empty
```

while the backend validates structural rules such as:

```text
Resource candidate exists
Activity references valid TimeSlot
Resource type exists
```

---

# 28. Form Component Strategy

Repeated form controls should be wrapped where useful.

Examples:

```text
ResourceForm
ActivityForm
TimeSlotForm
LocationForm
ConstraintForm
```

However, generic abstractions should only be introduced when repetition actually exists.

Avoid creating a large generic form framework before implementation demonstrates a need.

---

# 29. Navigation

The application should use client-side routing.

The expected routes are conceptually:

```text
/
    Dashboard

/planning/new
    New planning workflow

/planning/:id
    Planning configuration

/planning/:id/results
    Generated schedule
```

A React routing library can provide this navigation.

The detailed page layout is defined in #22.

---

# 30. Error Handling

The UI should distinguish:

```text
Validation errors
API errors
Generation errors
Empty states
```

Examples:

```text
Field validation
→ inline form message

PlanningProblem invalid
→ page/form alert

Server unavailable
→ application alert/snackbar

No schedule generated
→ empty result state
```

MUI components such as:

```text
Alert
Snackbar
FormHelperText
```

can provide consistent presentation.

---

# 31. Loading States

As optimization may take noticeable time, generation must provide clear feedback.

Conceptually:

```text
Generate Schedule
       ↓
Generating...
       ↓
Result
```

The UI should show an explicit generation state rather than appearing blocked.

Possible components:

```text
CircularProgress
LinearProgress
GenerationStatus
```

The MVP does not require real-time generation progress from the backend.

A simple request/loading/result flow is sufficient initially.

---

# 32. Results Components

The schedule result should be decomposed into reusable components.

For example:

```text
ResultsPage
    │
    ├── ResultSummary
    │      ├── Fitness
    │      ├── Feasible
    │      ├── Execution Time
    │      └── Generations
    │
    ├── ScheduleCalendar
    │
    ├── ScheduleTable
    │
    └── ConstraintBreakdown
```

This mirrors the backend `PlanningResult` without coupling UI components to backend implementation details.

---

# 33. Suggested Frontend Structure

The initial structure should be:

```text
frontend/
└── src/
    ├── api/
    │   ├── client.ts
    │   ├── planningApi.ts
    │   ├── templatesApi.ts
    │   └── scheduleApi.ts
    │
    ├── components/
    │   ├── common/
    │   └── layout/
    │
    ├── features/
    │   ├── planning/
    │   ├── templates/
    │   ├── resources/
    │   ├── activities/
    │   ├── constraints/
    │   ├── optimization/
    │   └── results/
    │
    ├── pages/
    │
    ├── hooks/
    │
    ├── schemas/
    │
    ├── types/
    │
    ├── context/
    │
    ├── theme/
    │
    ├── App.tsx
    └── main.tsx
```

The structure is feature-oriented without introducing excessive frontend architecture.

---

# 34. Dependency Direction

The frontend dependency flow should generally be:

```text
Pages
  ↓
Feature Components
  ↓
Shared Components
```

For server communication:

```text
Page / Feature
      ↓
TanStack Query hook
      ↓
API module
      ↓
Axios
      ↓
REST Backend
```

For forms:

```text
Feature Form
      ↓
React Hook Form
      ↓
Zod
      ↓
API DTO
```

For results:

```text
API Response
      ↓
TanStack Query
      ↓
Results Feature
      ├── Calendar
      └── Table
```

---

# 35. Frontend Architecture Overview

```text
                     React Application

                           │
              ┌────────────┴────────────┐
              │                         │
              ▼                         ▼
            Pages                  UI Components
              │                         │
              ▼                         ▼
        Feature Components         Material UI
              │
     ┌────────┼───────────┐
     │        │           │
     ▼        ▼           ▼
   Forms   Results     Local State
     │        │           │
     │        │        React
     │        │      State/Context
     │        │
     ▼        ▼
React Hook   FullCalendar
   Form
     │
     ▼
    Zod

              Feature Components
                      │
                      ▼
               TanStack Query
                      │
                      ▼
                  API Layer
                      │
                      ▼
                    Axios
                      │
                   REST/JSON
                      │
                      ▼
             Spring Boot Backend
```

---

# 36. Technology Responsibilities

| Technology            | Responsibility                  |
| --------------------- | ------------------------------- |
| React                 | UI framework                    |
| TypeScript            | Static typing                   |
| Vite                  | Development/build tooling       |
| Material UI           | Visual components/design system |
| FullCalendar          | Schedule visualization          |
| TanStack Query        | Remote/server state             |
| React state / Context | Local and wizard state          |
| Axios                 | REST HTTP client                |
| React Hook Form       | Form state/submission           |
| Zod                   | Frontend schema validation      |

Each tool has one clear responsibility.

---

# 37. Explicitly Rejected Complexity

The initial frontend will not require:

```text
Redux
Redux Toolkit
Zustand
MobX

Next.js
Server-side rendering

Micro frontends

GraphQL

Custom design system

Custom calendar implementation
```

These technologies would not provide enough value for the current Genetic Planner MVP.

---

# 38. Design Decisions

## FE1 — React

React is the frontend framework.

## FE2 — TypeScript

All frontend application code uses TypeScript.

## FE3 — Vite

Vite is the development and production build tool.

## FE4 — Material UI

Material UI provides the primary component library and visual system.

## FE5 — FullCalendar

FullCalendar provides graphical schedule visualization.

## FE6 — No premium calendar dependency for the MVP

The project should remain functional using standard calendar capabilities.

## FE7 — TanStack Query manages server state

Backend resources and mutations are not placed in a general-purpose global store.

## FE8 — React manages local state

`useState`, `useReducer` and Context are sufficient for UI and wizard state initially.

## FE9 — No Redux/Zustand initially

A dedicated global state library will only be introduced if implementation demonstrates a real requirement.

## FE10 — Axios is the centralized REST client

React components must not call HTTP endpoints directly.

## FE11 — React Hook Form manages forms

Complex planning configuration forms use a consistent form strategy.

## FE12 — Zod provides frontend validation

Schemas validate form values before sending API requests.

## FE13 — Backend remains authoritative

Frontend validation improves UX but does not replace backend validation.

## FE14 — Components should be reusable between templates

Academic and Work Shift templates primarily differ through configuration, labels and form sections rather than duplicated frontend applications.

---

# 39. Summary

The selected frontend architecture is:

```text
React + TypeScript + Vite
           │
           ├── Material UI
           │
           ├── React Hook Form
           │       ↓
           │      Zod
           │
           ├── FullCalendar
           │
           ├── React State / Context
           │
           └── TanStack Query
                    ↓
                  Axios
                    ↓
                  REST
                    ↓
             Spring Boot Backend
```

The strategy deliberately separates:

```text
Server state
→ TanStack Query

Local UI state
→ React

Forms
→ React Hook Form + Zod

HTTP
→ Axios

Presentation
→ Material UI

Schedule visualization
→ FullCalendar
```

This provides enough structure for the Genetic Planner MVP while avoiding unnecessary frontend complexity.
