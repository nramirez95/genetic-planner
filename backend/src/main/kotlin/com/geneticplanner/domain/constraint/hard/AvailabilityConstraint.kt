package com.geneticplanner.domain.constraint.hard

import com.geneticplanner.domain.Schedule
import com.geneticplanner.domain.constraint.Constraint
import com.geneticplanner.domain.constraint.ConstraintResult
import com.geneticplanner.domain.constraint.ConstraintType
import com.geneticplanner.domain.constraint.ConstraintViolation

class AvailabilityConstraint(
    override val id: String,
    override val name: String,
    override val weight: Double,
    val availableTimeSlotIdsByResourceId: Map<String, Set<String>>
) : Constraint {

    override val type = ConstraintType.HARD

    override fun evaluate(schedule: Schedule): ConstraintResult {
        val violations = mutableListOf<ConstraintViolation>()

        schedule.assignments.forEach { assignment ->
            val assignedResourceIds =
                assignment.resourceAssignments.values
                    .flatten()
                    .toSet()

            assignedResourceIds.forEach { resourceId ->
                val availableSlots =
                    availableTimeSlotIdsByResourceId[resourceId]
                        ?: return@forEach

                if (assignment.timeSlotId !in availableSlots) {
                    violations += ConstraintViolation(
                        message =
                            "Resource '$resourceId' is not available in " +
                                    "time slot '${assignment.timeSlotId}'.",
                        penalty = 1.0,
                        relatedEntityIds = setOf(
                            resourceId,
                            assignment.activityId,
                            assignment.timeSlotId
                        )
                    )
                }
            }
        }

        return ConstraintResult(
            constraintId = id,
            type = type,
            violationDetails = violations,
            weight = weight
        )
    }
}