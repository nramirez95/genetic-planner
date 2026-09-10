# Genetic Planner — Initial Constraint Catalogue

## 1. Purpose

This document defines the initial constraint catalogue for **Genetic Planner 1.0**.

The catalogue identifies the rules that may be applied to a planning problem and classifies them according to their implementation priority:

* **Must** — required for the MVP.
* **Should** — desirable for version 1.0 if time permits.
* **Could** — optional extensions.

All constraints follow the generic `Constraint` abstraction and must remain independent from specific planning domains.

The same constraint should therefore be reusable for academic scheduling, work shift scheduling, and future planning scenarios.

---

# 2. Catalogue Overview

## Must — MVP

The following constraints are required for the MVP:

```text
NoOverlap
Availability
RequiredResource
MaximumAssignments
```

## Should

The following constraints are desirable for version 1.0:

```text
MinimumAssignments
MaxConsecutive
PreferredTimeSlot
```

## Could

The following constraints are optional:

```text
Capacity
DifferentDay
```

The complete catalogue is:

| Constraint                     | Type | Priority |
| ------------------------------ | ---- | -------- |
| `NoOverlapConstraint`          | HARD | MUST     |
| `AvailabilityConstraint`       | HARD | MUST     |
| `RequiredResourceConstraint`   | HARD | MUST     |
| `MaximumAssignmentsConstraint` | HARD | MUST     |
| `MinimumAssignmentsConstraint` | SOFT | SHOULD   |
| `MaxConsecutiveConstraint`     | SOFT | SHOULD   |
| `PreferredTimeSlotConstraint`  | SOFT | SHOULD   |
| `CapacityConstraint`           | HARD | COULD    |
| `DifferentDayConstraint`       | SOFT | COULD    |

---

# 3. NoOverlapConstraint

## Description

`NoOverlapConstraint` prevents the same resource from being assigned to two activities whose time slots overlap.

This constraint protects one of the fundamental validity rules of a schedule: a resource cannot participate in two simultaneous assignments.

## Type

```text
HARD
```

## Priority

```text
MUST
```

It is part of the MVP.

## Parameters

The constraint may optionally target:

```text
resourceIds
resourceTypeIds
```

If no specific target is configured, the constraint applies to all resources.

Conceptually:

```kotlin
NoOverlapConstraint(
    resourceIds: Set<String>? = null,
resourceTypeIds: Set<String>? = null
)
```

Two slots overlap when:

```text
slotA.start < slotB.end
AND
slotB.start < slotA.end
```

## Academic Example

Teacher Ana is assigned to:

```text
Mathematics 1A
Monday 09:00–10:00
```

and simultaneously to:

```text
Physics 2A
Monday 09:30–10:30
```

The assignments overlap.

Result:

```text
NoOverlapConstraint → violation
```

The same rule may also apply to student groups.

For example, group `1A` cannot attend Mathematics and English simultaneously.

## Work Shift Example

Employee Laura is assigned to:

```text
Reception
Monday 08:00–14:00
```

and also:

```text
Support Desk
Monday 12:00–18:00
```

Because both assignments overlap between 12:00 and 14:00:

```text
NoOverlapConstraint → violation
```

---

# 4. AvailabilityConstraint

## Description

`AvailabilityConstraint` ensures that a resource is only assigned during periods in which it is available.

Availability is represented as a constraint rather than an intrinsic property of `Resource`, allowing the same resource model to remain generic.

## Type

```text
HARD
```

## Priority

```text
MUST
```

It is part of the MVP.

## Parameters

Typical parameters are:

```text
resourceId
availableTimeSlotIds
```

Conceptually:

```kotlin
AvailabilityConstraint(
    resourceId: String,
    availableTimeSlotIds: Set<String>
)
```

An alternative representation may define unavailable periods instead. The final implementation should use one consistent approach.

## Academic Example

Teacher Ana is available:

```text
Monday 09:00–12:00
Tuesday 09:00–11:00
```

but an activity assigns her to:

```text
Tuesday 12:00–13:00
```

Result:

```text
AvailabilityConstraint → violation
```

## Work Shift Example

Employee Pedro is available only for morning shifts.

The generated schedule assigns Pedro to:

```text
Tuesday Afternoon
14:00–20:00
```

Result:

```text
AvailabilityConstraint → violation
```

---

# 5. RequiredResourceConstraint

## Description

`RequiredResourceConstraint` ensures that a specific resource is assigned to an activity when that assignment is mandatory.

It complements `ResourceRequirement`.

`ResourceRequirement` defines the structural resource needs of an activity:

```text
Activity requires:
1 TEACHER
```

while `RequiredResourceConstraint` expresses a concrete planning rule:

```text
This activity must specifically use Teacher Ana.
```

This distinction avoids using domain-specific logic in the Genetic Engine.

## Type

```text
HARD
```

## Priority

```text
MUST
```

It is part of the MVP.

## Parameters

Typical parameters are:

```text
activityId
resourceId
resourceRequirementId
```

Conceptually:

```kotlin
RequiredResourceConstraint(
    activityId: String,
    resourceId: String,
    resourceRequirementId: String? = null
)
```

`resourceRequirementId` may be used when an activity contains multiple resource requirements and the required resource must satisfy one particular requirement.

## Academic Example

The activity:

```text
Advanced Mathematics — 2A
```

requires a teacher.

Several teachers may normally be valid candidates, but this specific class must be taught by:

```text
Teacher Ana
```

If the generated assignment selects Pedro instead:

```text
RequiredResourceConstraint → violation
```

## Work Shift Example

A particular work assignment:

```text
Monday Morning — Reception
```

must be covered by:

```text
Employee Laura
```

because Laura is responsible for opening reception.

If another employee is assigned:

```text
RequiredResourceConstraint → violation
```

---

# 6. MaximumAssignmentsConstraint

## Description

`MaximumAssignmentsConstraint` limits the number of assignments that a resource may receive during a planning period.

The constraint can be used to enforce workload limits.

## Type

```text
HARD
```

## Priority

```text
MUST
```

It is part of the MVP.

## Parameters

Typical parameters are:

```text
resourceId
maximumAssignments
```

Conceptually:

```kotlin
MaximumAssignmentsConstraint(
    resourceId: String,
    maximumAssignments: Int
)
```

Future versions could optionally introduce a scope such as day, week, or complete planning horizon.

For the MVP, the limit should be interpreted over the complete configured planning horizon unless explicitly defined otherwise.

## Academic Example

Teacher Ana may teach at most:

```text
5 activities
```

during the planning horizon.

The generated schedule assigns her:

```text
7 activities
```

The excess is:

```text
7 - 5 = 2
```

Result:

```text
MaximumAssignmentsConstraint → violation
```

## Work Shift Example

Employee Pedro may receive at most:

```text
5 shifts
```

during the planning horizon.

The generated schedule assigns:

```text
6 shifts
```

Result:

```text
MaximumAssignmentsConstraint → violation
```

---

# 7. MinimumAssignmentsConstraint

## Description

`MinimumAssignmentsConstraint` encourages a resource to receive at least a configured number of assignments.

It can be used to promote workload distribution.

Because receiving fewer assignments does not necessarily make the schedule invalid, it is initially modelled as a soft constraint.

## Type

```text
SOFT
```

## Priority

```text
SHOULD
```

It is not required for the minimum MVP.

## Parameters

Typical parameters are:

```text
resourceId
minimumAssignments
```

Conceptually:

```kotlin
MinimumAssignmentsConstraint(
    resourceId: String,
    minimumAssignments: Int
)
```

## Academic Example

The planning configuration prefers Teacher Ana to have at least:

```text
4 teaching sessions
```

The generated schedule assigns her only:

```text
2 sessions
```

The schedule remains feasible but receives a soft penalty.

## Work Shift Example

Employee Laura should receive at least:

```text
3 shifts
```

during the planning horizon.

The generated schedule assigns only:

```text
2 shifts
```

Result:

```text
MinimumAssignmentsConstraint → soft violation
```

---

# 8. MaxConsecutiveConstraint

## Description

`MaxConsecutiveConstraint` discourages assigning a resource to too many consecutive activities or time slots.

It can improve schedule quality by reducing fatigue or overly concentrated workloads.

## Type

```text
SOFT
```

## Priority

```text
SHOULD
```

It is not required for the minimum MVP.

## Parameters

Typical parameters are:

```text
resourceId
maximumConsecutiveAssignments
```

Conceptually:

```kotlin
MaxConsecutiveConstraint(
    resourceId: String,
    maximumConsecutiveAssignments: Int
)
```

The implementation must determine consecutive assignments using the chronological order and boundaries of the configured `TimeSlot` objects.

## Academic Example

Teacher Pedro should teach no more than:

```text
3 consecutive classes
```

The generated timetable assigns:

```text
09:00–10:00
10:00–11:00
11:00–12:00
12:00–13:00
```

This produces four consecutive classes.

Result:

```text
MaxConsecutiveConstraint → soft violation
```

## Work Shift Example

Employee Ana should not receive more than:

```text
3 consecutive scheduled work periods
```

If four consecutive periods are assigned, the solution receives a penalty but remains feasible.

---

# 9. PreferredTimeSlotConstraint

## Description

`PreferredTimeSlotConstraint` expresses a preference for assigning an activity or resource to particular time slots.

It allows users to model preferences without making them mandatory.

## Type

```text
SOFT
```

## Priority

```text
SHOULD
```

It is not required for the minimum MVP.

## Parameters

Typical parameters are:

```text
resourceId or activityId
preferredTimeSlotIds
```

Conceptually:

```kotlin
PreferredTimeSlotConstraint(
    resourceId: String? = null,
activityId: String? = null,
preferredTimeSlotIds: Set<String>
)
```

At least one target should be provided.

## Academic Example

Teacher Ana prefers teaching during:

```text
09:00–12:00
```

A generated activity is assigned at:

```text
16:00–17:00
```

The schedule remains feasible but receives a soft penalty.

Another example may associate the preference directly with an activity:

```text
Physical Education prefers afternoon periods.
```

## Work Shift Example

Employee Laura prefers:

```text
Morning shifts
```

but receives an afternoon shift.

Result:

```text
PreferredTimeSlotConstraint → soft violation
```

The scheduler may still choose the afternoon assignment if it produces a better overall solution.

---

# 10. CapacityConstraint

## Description

`CapacityConstraint` ensures that a selected location has enough capacity for the activity assigned to it.

This rule is useful for planning problems involving rooms or physical spaces.

## Type

```text
HARD
```

## Priority

```text
COULD
```

It is not part of the MVP.

## Parameters

Typical parameters are:

```text
activityId
requiredCapacity
```

The selected `Location` provides:

```text
capacity
```

Conceptually:

```kotlin
CapacityConstraint(
    activityId: String,
    requiredCapacity: Int
)
```

The rule evaluates:

```text
location.capacity >= requiredCapacity
```

## Academic Example

Student group `1A` contains:

```text
30 students
```

but the assigned classroom has capacity for:

```text
20 people
```

Result:

```text
CapacityConstraint → violation
```

## Work Shift Example

A training activity requires space for:

```text
15 employees
```

but the selected training room supports only:

```text
10 people
```

Result:

```text
CapacityConstraint → violation
```

Although more common in academic planning, the constraint remains generic and may apply to other planning scenarios.

---

# 11. DifferentDayConstraint

## Description

`DifferentDayConstraint` encourages related activities to be scheduled on different days.

It is useful when repeated or related activities should be distributed across the planning horizon rather than concentrated on the same day.

## Type

```text
SOFT
```

## Priority

```text
COULD
```

It is not part of the MVP.

## Parameters

Typical parameters are:

```text
activityIds
```

Conceptually:

```kotlin
DifferentDayConstraint(
    activityIds: Set<String>
)
```

The constraint evaluates whether the targeted activities occur on different calendar days.

## Academic Example

Two Mathematics sessions for group `1A` should preferably occur on different days.

The generated schedule places both sessions on:

```text
Monday
```

Result:

```text
DifferentDayConstraint → soft violation
```

A schedule with:

```text
Monday
Wednesday
```

would satisfy the preference.

## Work Shift Example

Two training assignments for the same employee should preferably occur on different days.

If both are scheduled on Tuesday:

```text
DifferentDayConstraint → soft violation
```

The same generic rule can therefore apply outside academic scheduling.

---

# 12. MVP Constraint Set

The MVP constraint catalogue is deliberately small.

The mandatory constraints are:

```text
NoOverlapConstraint
AvailabilityConstraint
RequiredResourceConstraint
MaximumAssignmentsConstraint
```

These four rules provide enough expressive power to generate meaningful schedules while keeping implementation complexity manageable.

Together they ensure that:

```text
Resources are not double-booked
        +
Resources are assigned only when available
        +
Mandatory resource assignments are respected
        +
Resource workloads do not exceed configured limits
```

The MVP therefore focuses primarily on basic feasibility.

---

# 13. Version 1.0 Extended Constraints

If development time allows, the following SHOULD constraints may be included:

```text
MinimumAssignmentsConstraint
MaxConsecutiveConstraint
PreferredTimeSlotConstraint
```

These constraints improve schedule quality and user preferences without being required for basic feasibility.

The optional COULD constraints are:

```text
CapacityConstraint
DifferentDayConstraint
```

These provide useful additional modelling capabilities but are not required to demonstrate the core Genetic Planner functionality.

---

# 14. Priority Summary

The final priority structure is:

```text
MUST — MVP
│
├── NoOverlapConstraint
├── AvailabilityConstraint
├── RequiredResourceConstraint
└── MaximumAssignmentsConstraint


SHOULD
│
├── MinimumAssignmentsConstraint
├── MaxConsecutiveConstraint
└── PreferredTimeSlotConstraint


COULD
│
├── CapacityConstraint
└── DifferentDayConstraint
```

---

# 15. HARD and SOFT Summary

## HARD

```text
NoOverlapConstraint
AvailabilityConstraint
RequiredResourceConstraint
MaximumAssignmentsConstraint
CapacityConstraint
```

A violation of these constraints affects schedule feasibility.

Conceptually:

```text
Hard violations = 0
        ↓
Feasible schedule

Hard violations > 0
        ↓
Infeasible schedule
```

## SOFT

```text
MinimumAssignmentsConstraint
MaxConsecutiveConstraint
PreferredTimeSlotConstraint
DifferentDayConstraint
```

Violations reduce solution quality but do not make the schedule infeasible.

---

# 16. Genericity Across Templates

The constraint catalogue must remain independent from the selected planning template.

For example:

| Constraint         | Academic Scheduling                             | Work Shift Scheduling                            |
| ------------------ | ----------------------------------------------- | ------------------------------------------------ |
| NoOverlap          | Teacher cannot teach two classes simultaneously | Employee cannot work two overlapping assignments |
| Availability       | Teacher only teaches when available             | Employee only works when available               |
| RequiredResource   | A class must use a specific teacher             | A shift must use a specific employee             |
| MaximumAssignments | Maximum teaching sessions                       | Maximum assigned shifts                          |
| MinimumAssignments | Minimum teaching load                           | Minimum number of shifts                         |
| MaxConsecutive     | Limit consecutive classes                       | Limit consecutive work periods                   |
| PreferredTimeSlot  | Preferred teaching periods                      | Preferred shifts                                 |
| Capacity           | Classroom capacity                              | Workplace/training-room capacity                 |
| DifferentDay       | Separate repeated classes                       | Separate related work activities                 |

The Genetic Engine does not need to understand any of these domain-specific interpretations.

It operates only with:

```text
Constraint
       ↓
evaluate(Schedule)
       ↓
ConstraintResult
```

---

# 17. Relationship with ResourceRequirement

`RequiredResourceConstraint` and `ResourceRequirement` represent different concepts.

### ResourceRequirement

Defines the structural resources required by an activity.

Example:

```text
Mathematics 1A
requires:
    1 TEACHER
    1 STUDENT_GROUP
```

It answers:

> What type and quantity of resources does this activity need?

### RequiredResourceConstraint

Defines a mandatory assignment rule for a specific resource.

Example:

```text
Mathematics 1A
must use:
    Teacher Ana
```

It answers:

> Which particular resource must be selected?

Therefore:

```text
ResourceRequirement
        ↓
Defines valid assignment structure

RequiredResourceConstraint
        ↓
Restricts which concrete assignment is acceptable
```

This distinction allows both concepts to coexist without duplicating responsibilities.

---

# 18. Design Boundary

This catalogue defines:

* Supported constraint names.
* Their purpose.
* HARD or SOFT classification.
* Implementation priority.
* Required configuration parameters.
* Academic examples.
* Work Shift examples.
* The MVP constraint subset.

It does not yet define:

* Exact penalty formulas.
* Weight values.
* Penalty aggregation.
* Hard vs soft global weighting.
* Fitness calculation.

Those decisions belong to:

```text
#13 Design constraint evaluation and weighting
#15 Design fitness function
```

---

# 19. Catalogue Summary

The initial Genetic Planner constraint catalogue contains nine generic constraints:

```text
                    Constraint
                        │
          ┌─────────────┴─────────────┐
          │                           │
         HARD                        SOFT
          │                           │
    ┌─────┼──────────┐        ┌──────┼────────────┐
    │     │          │        │      │            │
NoOverlap Availability ...  Minimum  Max      Preferred
                            Assign. Consecutive TimeSlot
```

The MVP requires four constraints:

```text
NoOverlap
Availability
RequiredResource
MaximumAssignments
```

Additional constraints can be introduced without modifying the Genetic Engine because all implementations follow the common `Constraint` abstraction.
