package com.geneticplanner.domain.constraint.soft

import com.geneticplanner.domain.Schedule
import com.geneticplanner.domain.constraint.Constraint
import com.geneticplanner.domain.constraint.ConstraintResult
import com.geneticplanner.domain.constraint.ConstraintType
import com.geneticplanner.domain.constraint.ConstraintViolation

class BalancedWorkloadConstraint(
    override val id: String,
    override val name: String,
    override val weight: Double,
    val resourceIds: Set<String>,
    val allowedDifference: Int = 0
) : Constraint {

    override val type = ConstraintType.SOFT

    override fun evaluate(schedule: Schedule): ConstraintResult {
        if (resourceIds.size < 2) {
            return emptyResult()
        }

        val workloadByResource =
            resourceIds.associateWith { 0 }.toMutableMap()

        schedule.assignments.forEach { assignment ->
            assignment.resourceAssignments.values
                .flatten()
                .distinct()
                .filter { it in resourceIds }
                .forEach { resourceId ->
                    workloadByResource[resourceId] =
                        workloadByResource.getValue(resourceId) + 1
                }
        }

        val maximum =
            workloadByResource.values.maxOrNull() ?: 0

        val minimum =
            workloadByResource.values.minOrNull() ?: 0

        val difference = maximum - minimum
        val excess = difference - allowedDifference

        if (excess <= 0) {
            return emptyResult()
        }

        val violation = ConstraintViolation(
            message =
                "Workload difference is $difference assignment(s), " +
                        "exceeding the preferred maximum difference of " +
                        "$allowedDifference.",
            penalty = excess.toDouble(),
            relatedEntityIds = resourceIds
        )

        return ConstraintResult(
            constraintId = id,
            type = type,
            violationDetails = listOf(violation),
            weight = weight
        )
    }

    private fun emptyResult() =
        ConstraintResult(
            constraintId = id,
            type = type,
            violationDetails = emptyList(),
            weight = weight
        )
}