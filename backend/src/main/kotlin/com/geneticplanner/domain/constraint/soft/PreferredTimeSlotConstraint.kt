package com.geneticplanner.domain.constraint.soft

import com.geneticplanner.domain.Schedule
import com.geneticplanner.domain.constraint.Constraint
import com.geneticplanner.domain.constraint.ConstraintResult
import com.geneticplanner.domain.constraint.ConstraintType
import com.geneticplanner.domain.constraint.ConstraintViolation

class PreferredTimeSlotConstraint(
    override val id: String,
    override val name: String,
    override val weight: Double,
    val preferredTimeSlotIdsByActivityId: Map<String, Set<String>>
) : Constraint {

    override val type = ConstraintType.SOFT

    override fun evaluate(schedule: Schedule): ConstraintResult {
        val violations = mutableListOf<ConstraintViolation>()

        schedule.assignments.forEach { assignment ->
            val preferredTimeSlotIds =
                preferredTimeSlotIdsByActivityId[assignment.activityId]
                    ?: return@forEach

            if (assignment.timeSlotId !in preferredTimeSlotIds) {
                violations += ConstraintViolation(
                    message =
                        "Activity '${assignment.activityId}' is assigned to " +
                                "time slot '${assignment.timeSlotId}', which is not " +
                                "one of its preferred time slots.",
                    penalty = 1.0,
                    relatedEntityIds = setOf(
                        assignment.activityId,
                        assignment.timeSlotId
                    )
                )
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