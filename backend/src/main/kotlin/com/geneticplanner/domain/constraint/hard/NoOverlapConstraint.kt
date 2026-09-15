package com.geneticplanner.domain.constraint.hard

import com.geneticplanner.domain.Schedule
import com.geneticplanner.domain.TimeSlot
import com.geneticplanner.domain.constraint.Constraint
import com.geneticplanner.domain.constraint.ConstraintResult
import com.geneticplanner.domain.constraint.ConstraintType
import com.geneticplanner.domain.constraint.ConstraintViolation

class NoOverlapConstraint(
    override val id: String,
    override val name: String,
    override val weight: Double,
    timeSlots: List<TimeSlot>
) : Constraint {

    override val type = ConstraintType.HARD

    private val timeSlotsById = timeSlots.associateBy { it.id }

    override fun evaluate(schedule: Schedule): ConstraintResult {
        val violations = mutableListOf<ConstraintViolation>()

        schedule.assignments.forEachIndexed { index, first ->
            for (second in schedule.assignments.drop(index + 1)) {
                val firstSlot = timeSlotsById[first.timeSlotId] ?: continue
                val secondSlot = timeSlotsById[second.timeSlotId] ?: continue

                if (!overlap(firstSlot, secondSlot)) {
                    continue
                }

                val firstResources =
                    first.resourceAssignments.values.flatten().toSet()

                val secondResources =
                    second.resourceAssignments.values.flatten().toSet()

                val sharedResources = firstResources intersect secondResources

                sharedResources.forEach { resourceId ->
                    violations += ConstraintViolation(
                        message =
                            "Resource '$resourceId' is assigned to overlapping " +
                                    "activities '${first.activityId}' and '${second.activityId}'.",
                        penalty = 1.0,
                        relatedEntityIds = setOf(
                            resourceId,
                            first.activityId,
                            second.activityId,
                            first.timeSlotId,
                            second.timeSlotId
                        )
                    )
                }
            }
        }

        return result(violations)
    }

    private fun overlap(first: TimeSlot, second: TimeSlot): Boolean =
        first.start < second.end &&
                second.start < first.end

    private fun result(
        violations: List<ConstraintViolation>
    ) = ConstraintResult(
        constraintId = id,
        type = type,
        violationDetails = violations,
        weight = weight
    )
}