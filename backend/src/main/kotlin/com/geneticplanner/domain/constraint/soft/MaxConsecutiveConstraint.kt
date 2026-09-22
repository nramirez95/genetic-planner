package com.geneticplanner.domain.constraint.soft

import com.geneticplanner.domain.Schedule
import com.geneticplanner.domain.TimeSlot
import com.geneticplanner.domain.constraint.Constraint
import com.geneticplanner.domain.constraint.ConstraintResult
import com.geneticplanner.domain.constraint.ConstraintType
import com.geneticplanner.domain.constraint.ConstraintViolation

class MaxConsecutiveConstraint(
    override val id: String,
    override val name: String,
    override val weight: Double,
    val maxConsecutive: Int,
    timeSlots: List<TimeSlot>
) : Constraint {

    override val type = ConstraintType.SOFT

    private val timeSlotsById =
        timeSlots.associateBy { it.id }

    override fun evaluate(schedule: Schedule): ConstraintResult {
        val violations = mutableListOf<ConstraintViolation>()

        val assignmentsByResource =
            schedule.assignments
                .flatMap { assignment ->
                    assignment.resourceAssignments.values
                        .flatten()
                        .distinct()
                        .map { resourceId ->
                            resourceId to assignment
                        }
                }
                .groupBy(
                    keySelector = { it.first },
                    valueTransform = { it.second }
                )

        assignmentsByResource.forEach { (resourceId, assignments) ->
            val slots = assignments
                .mapNotNull { timeSlotsById[it.timeSlotId] }
                .distinctBy { it.id }
                .sortedBy { it.start }

            if (slots.isEmpty()) {
                return@forEach
            }

            var currentSequence = mutableListOf(slots.first())

            fun evaluateSequence() {
                if (currentSequence.size > maxConsecutive) {
                    val excess =
                        currentSequence.size - maxConsecutive

                    violations += ConstraintViolation(
                        message =
                            "Resource '$resourceId' has " +
                                    "${currentSequence.size} consecutive assignments, " +
                                    "exceeding the preferred maximum of " +
                                    "$maxConsecutive.",
                        penalty = excess.toDouble(),
                        relatedEntityIds =
                            setOf(resourceId) +
                                    currentSequence.map { it.id }
                    )
                }
            }

            for (index in 1 until slots.size) {
                val previous = slots[index - 1]
                val current = slots[index]

                if (previous.end == current.start) {
                    currentSequence.add(current)
                } else {
                    evaluateSequence()
                    currentSequence =
                        mutableListOf(current)
                }
            }

            evaluateSequence()
        }

        return ConstraintResult(
            constraintId = id,
            type = type,
            violationDetails = violations,
            weight = weight
        )
    }
}