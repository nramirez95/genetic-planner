# Genetic Planner — Frontend Technology and Component Alternatives

## 1. Purpose

This document defines the frontend technology and component strategy for Genetic Planner.

Before selecting the final technology stack, several alternatives were evaluated according to the following criteria:

* Development speed.
* Type safety.
* Ease of integration with the Spring Boot REST API.
* Availability of reusable UI components.
* Suitability for complex configuration forms.
* Schedule visualization capabilities.
* Maintainability.
* Learning and implementation cost.
* Suitability for the limited MVP development timeframe.

The objective is not to select the most sophisticated frontend architecture, but the simplest combination of technologies that adequately supports the Genetic Planner requirements.

---

# 2. Alternatives Considered

## 2.1 Frontend Framework

The main alternatives considered were:

| Alternative | Advantages                                                                                | Disadvantages                                                 | Decision     |
| ----------- | ----------------------------------------------------------------------------------------- | ------------------------------------------------------------- | ------------ |
| React       | Large ecosystem, component-oriented, strong TypeScript support, many compatible libraries | Requires selecting complementary libraries                    | **Selected** |
| Angular     | Complete framework with routing, forms and HTTP included                                  | More structure and complexity than required for the MVP       | Rejected     |
| Vue         | Simple learning curve and good developer experience                                       | Less aligned with the selected ecosystem and project approach | Rejected     |

### Decision

**React** was selected because Genetic Planner requires a highly interactive interface composed of reusable forms, configuration steps and schedule visualization components.

It also provides a wide ecosystem of compatible libraries for the remaining frontend requirements.

---

# 3. Language

The alternatives considered were:

```text
JavaScript
TypeScript
```

## JavaScript

Advantages:

* Minimal initial setup.
* Flexible.
* Native React ecosystem support.

Disadvantages:

* API contracts are not statically typed.
* Refactoring is less safe.
* Complex planning structures can easily produce runtime errors.

## TypeScript

Advantages:

* Compile-time type checking.
* Better editor support.
* Safer API contracts.
* Better maintainability for complex planning structures.
* Strong compatibility with React libraries.

Disadvantages:

* Slightly more initial modelling work.

### Decision

**TypeScript** was selected.

The project contains relatively complex structures such as:

```text
PlanningProblem
Resources
Activities
Constraints
Schedules
PlanningResult
```

and static typing provides significant value when exchanging this information with the backend.

---

# 4. Build Tool

The alternatives considered were:

```text
Vite
Webpack
Next.js
```

## Webpack

Advantages:

* Mature.
* Highly configurable.

Disadvantages:

* Requires significantly more configuration.
* Unnecessary complexity for the project.

## Next.js

Advantages:

* Integrated routing.
* Server-side rendering.
* Full-stack capabilities.

Disadvantages:

* Server-side rendering is not required.
* Genetic Planner already has a dedicated Spring Boot backend.
* Introduces functionality outside the project needs.

## Vite

Advantages:

* Simple React + TypeScript setup.
* Fast development server.
* Minimal configuration.
* Good production build support.

### Decision

**Vite** was selected.

The frontend is a client-side SPA consuming the Spring Boot REST API, making a lightweight build tool preferable to a full-stack React framework.

---

# 5. Component Library Alternatives

The principal alternatives considered were:

```text
Material UI
Ant Design
Chakra UI
Custom components
```

## Material UI

Advantages:

* Large set of production-ready components.
* Strong TypeScript support.
* Forms, tables, dialogs, navigation and stepper components.
* Built-in theming.
* Well suited to application interfaces.

Potential disadvantage:

* Strong Material Design visual identity unless customised.

## Ant Design

Advantages:

* Very comprehensive component catalogue.
* Particularly strong tables and enterprise interfaces.

Disadvantages:

* Strong enterprise visual style.
* More opinionated appearance than required for Genetic Planner.

## Chakra UI

Advantages:

* Simple API.
* Flexible styling.
* Good accessibility orientation.

Disadvantages:

* Smaller set of complex application components compared with Material UI.

## Custom Components

Advantages:

* Maximum visual control.

Disadvantages:

* Considerably more implementation effort.
* Requires manually solving accessibility and consistency.
* Little value for the MVP.

### Decision

**Material UI (MUI)** was selected.

The application needs components such as:

```text
Stepper
Cards
Tables
Dialogs
Form controls
Alerts
Tabs
Chips
Progress indicators
```

which MUI already provides.

This allows development effort to focus on the planning functionality rather than implementing a component system from scratch.

---

# 6. Calendar and Schedule Visualization Alternatives

The principal alternatives considered were:

```text
FullCalendar
React Big Calendar
Custom timetable component
Table-only representation
```

## FullCalendar

Advantages:

* Native React integration.
* Multiple calendar views.
* Time-based visualization.
* Mature ecosystem.
* Suitable for academic timetables and shift planning.

Disadvantages:

* Some advanced resource-oriented views are premium features.

## React Big Calendar

Advantages:

* React-oriented.
* Relatively lightweight.
* Suitable for traditional calendar visualization.

Disadvantages:

* Smaller feature set.
* Less flexible for future advanced planning visualization.

## Custom Schedule Component

Advantages:

* Full control over visualization.

Disadvantages:

* High development cost.
* Calendar layout, collisions and responsive behaviour would need to be implemented manually.

## Table Only

Advantages:

* Very simple to implement.
* Easy to understand.

Disadvantages:

* Does not satisfy the desired graphical schedule visualization particularly well.
* Less intuitive for timetable and work-shift scenarios.

### Decision

**FullCalendar** was selected for the graphical view.

It provides an official React integration and fits the two main scenarios considered by Genetic Planner.

The MVP will not depend on premium FullCalendar functionality.

A complementary table representation will also be available.

---

# 7. Form Handling Alternatives

The alternatives considered were:

```text
React Hook Form
Formik
Manual React state
```

## Manual React State

Example:

```typescript
const [name, setName] = useState("");
const [type, setType] = useState("");
```

Advantages:

* No additional dependency.

Disadvantages:

* Becomes verbose for large forms.
* Difficult to manage complex validation.
* Genetic Planner contains many configuration forms.

## Formik

Advantages:

* Mature.
* Established React form solution.
* Integrated form state and validation patterns.

Disadvantages:

* Adds more component state/update overhead than required.
* Less attractive for the planned strongly typed form approach.

## React Hook Form

Advantages:

* Lightweight.
* Good TypeScript integration.
* Works naturally with schema validation.
* Well suited to large configuration forms.
* Avoids manually managing every input field.

### Decision

**React Hook Form** was selected.

Genetic Planner will contain several complex forms:

```text
Resources
Activities
Time slots
Locations
Constraints
Optimization parameters
```

Using a form-specific library reduces repetitive state management.

---

# 8. Validation Alternatives

The main alternatives considered were:

```text
Zod
Yup
Manual validation
```

## Manual Validation

Advantages:

* No dependency.

Disadvantages:

* Validation rules become scattered.
* More code.
* Harder to maintain consistent form schemas.

## Yup

Advantages:

* Mature schema validation library.
* Frequently used with React forms.

Disadvantages:

* TypeScript inference is less central to its design than in Zod.

## Zod

Advantages:

* TypeScript-first schema definition.
* Static type inference.
* Good integration with React Hook Form.
* Allows validation schemas and TypeScript types to remain aligned.

### Decision

**Zod** was selected.

It fits particularly well with the TypeScript-first frontend strategy.

The intended flow becomes:

```text
React Hook Form
       ↓
      Zod
       ↓
Validated TypeScript data
       ↓
REST API
```

---

# 9. Server State Alternatives

Several approaches were considered:

```text
TanStack Query
Redux Toolkit + RTK Query
Zustand
React Context
Manual fetch/useEffect
```

This decision requires distinguishing:

```text
Server state
```

from:

```text
Client/UI state
```

Server state includes data obtained from Spring Boot such as:

```text
Planning problems
Templates
Generated schedules
Generation results
```

---

## 9.1 Manual fetch/useEffect

Example:

```typescript
useEffect(() => {
    fetch(...)
}, []);
```

Advantages:

* No extra dependency.

Disadvantages:

* Manual loading state.
* Manual error handling.
* Manual caching.
* Manual synchronization.
* Manual refetching.

This would recreate functionality already solved by dedicated libraries.

---

## 9.2 Redux Toolkit / RTK Query

Advantages:

* Mature global state solution.
* RTK Query provides server-state fetching and caching.
* Suitable when a large Redux store is already required.

RTK Query itself provides querying, mutations and caching capabilities.

Disadvantages for Genetic Planner:

* Requires introducing Redux architecture.
* Most of the application's shared data will originate from the backend.
* A large global client-side store is not currently required.

---

## 9.3 Zustand

Advantages:

* Lightweight.
* Simple global state.
* Much less boilerplate than Redux.

Disadvantages:

* Primarily solves client-side global state.
* Does not provide the same dedicated server-state caching model as TanStack Query.

It could still be added later if significant global client-only state appears.

---

## 9.4 React Context

Advantages:

* Built into React.
* No extra library.
* Appropriate for relatively small shared state.

Disadvantages:

* Does not provide server caching, invalidation or asynchronous query management.

Therefore it is useful for UI state but not as the main server-state solution.

---

## 9.5 TanStack Query

Advantages:

* Specifically designed for asynchronous server state.
* Query caching.
* Mutations.
* Query invalidation.
* Loading/error states.
* Refetching.
* Does not require a general global store.

TanStack explicitly distinguishes server state from client-state managers such as Redux or Zustand. Queries are keyed asynchronous data sources and the library handles caching and reuse of those results.

### Decision

**TanStack Query** was selected for server state.

This matches the Genetic Planner architecture particularly well because the backend remains the source of truth.

---

# 10. Local State Alternatives

For client-only state, the alternatives considered were:

```text
React useState/useReducer
React Context
Zustand
Redux
```

Expected local state is limited to:

```text
Current wizard step
Open dialogs
Selected tabs
Filters
Calendar view
Temporary configuration state
```

### Decision

Use:

```text
useState / useReducer
```

for component and feature state, and:

```text
React Context
```

only when state must be shared across several wizard steps.

Neither Redux nor Zustand is required initially.

TanStack Query does not replace local state management; its role is specifically server state.

If the implementation later reveals extensive cross-application client-only state, Zustand can be reconsidered.

---

# 11. HTTP Client Alternatives

The alternatives considered were:

```text
Fetch API
Axios
```

## Fetch API

Advantages:

* Native browser API.
* No dependency.
* Fully sufficient for basic REST operations.

Disadvantages:

* Requires more repeated handling around response status, configuration and common behaviour.

## Axios

Advantages:

* Centralized client configuration.
* Interceptors.
* Automatic JSON handling.
* Convenient error handling.
* Strong TypeScript support.
* Familiar REST-oriented API.

Disadvantage:

* Additional dependency.

### Decision

**Axios** was selected.

A centralized client can be configured as:

```typescript
const apiClient = axios.create({
    baseURL: import.meta.env.VITE_API_URL
});
```

and reused by all feature APIs.

---

# 12. API Strategy Alternatives

Two broad approaches were considered:

### Components call HTTP directly

```text
Component
   ↓
Axios
```

### Dedicated API layer

```text
Component
   ↓
TanStack Query
   ↓
API module
   ↓
Axios client
```

### Decision

A dedicated API layer was selected.

For example:

```text
api/
├── client.ts
├── planningApi.ts
├── templatesApi.ts
└── scheduleApi.ts
```

This keeps HTTP details outside presentation components.

---

# 13. Final Technology Selection

After evaluating the alternatives, the selected frontend stack is:

```text
React + TypeScript + Vite
        │
        ├── Material UI
        │      → Component library
        │
        ├── FullCalendar
        │      → Schedule/calendar visualization
        │
        ├── React Hook Form
        │      → Form management
        │
        ├── Zod
        │      → Form/schema validation
        │
        ├── TanStack Query
        │      → Server state
        │
        ├── React state/context
        │      → Local UI and wizard state
        │
        └── Axios
               → REST API client
```

---

# 14. Decision Summary

| Requirement       | Alternatives considered                                   | Selected                |
| ----------------- | --------------------------------------------------------- | ----------------------- |
| Framework         | React, Angular, Vue                                       | **React**               |
| Language          | JavaScript, TypeScript                                    | **TypeScript**          |
| Build tool        | Vite, Webpack, Next.js                                    | **Vite**                |
| Component library | MUI, Ant Design, Chakra UI, custom                        | **Material UI**         |
| Calendar          | FullCalendar, React Big Calendar, custom, table           | **FullCalendar**        |
| Forms             | React Hook Form, Formik, manual state                     | **React Hook Form**     |
| Validation        | Zod, Yup, manual                                          | **Zod**                 |
| Server state      | TanStack Query, RTK Query, Zustand, Context, manual fetch | **TanStack Query**      |
| Local state       | React, Context, Zustand, Redux                            | **React state/context** |
| HTTP              | Fetch, Axios                                              | **Axios**               |

---

# 15. Selection Rationale

The final stack was selected according to one main principle:

> Prefer specialized and lightweight tools with a clear responsibility rather than introducing a large framework for every frontend concern.

The resulting responsibility model is:

```text
React
→ Application UI

TypeScript
→ Type safety

Vite
→ Build tooling

Material UI
→ UI components

FullCalendar
→ Schedule visualization

React Hook Form
→ Forms

Zod
→ Validation

TanStack Query
→ Server state

React state/context
→ Local state

Axios
→ HTTP communication
```

This combination provides the functionality required by the Genetic Planner MVP while keeping the frontend architecture relatively simple.
