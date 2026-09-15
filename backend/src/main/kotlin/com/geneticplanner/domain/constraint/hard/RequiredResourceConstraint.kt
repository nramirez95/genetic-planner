package com.geneticplanner.domain.constraint.hard

import com.geneticplanner.domain.Activity
import com.geneticplanner.domain.Resource
import com.geneticplanner.domain.Schedule
import com.geneticplanner.domain.constraint.Constraint
import com.geneticplanner.domain.constraint.ConstraintResult
import com.geneticplanner.domain.constraint.ConstraintType
import com.geneticplanner.domain.constraint.ConstraintViolation

class RequiredResourceConstraint(
    override val id: String,
    override val name: String,
    override val weight: Double,
    activities: List<Activity>,
    resources: List<Resource>
) : Constraint {

    override val type = ConstraintType.HARD

    private val activitiesById = activities.associateBy { it.id }
    private val resourcesById = resources.associateBy { it.id }

    override fun evaluate(schedule: Schedule): ConstraintResult {
        val violations = mutableListOf<ConstraintViolation>()

        schedule.assignments.forEach { assignment ->
            val activity =
                activitiesById[assignment.activityId] ?: return@forEach

            activity.resourceRequirements.forEach { requirement ->
                val assignedResourceIds =
                    assignment.resourceAssignments[requirement.id]
                        ?: emptyList()

                val uniqueAssignedResourceIds =
                    assignedResourceIds.distinct()

                // First validate the exact number of distinct resources.
                if (uniqueAssignedResourceIds.size != requirement.quantity) {
                    violations += ConstraintViolation(
                        message =
                            "Activity '${activity.id}' requires " +
                                    "${requirement.quantity} distinct resource(s) " +
                                    "for requirement '${requirement.id}', but " +
                                    "${uniqueAssignedResourceIds.size} distinct " +
                                    "resource(s) were assigned.",
                        penalty = 1.0,
                        relatedEntityIds =
                            setOf(activity.id, requirement.id) +
                                    uniqueAssignedResourceIds
                    )

                    return@forEach
                }

                // Quantity is correct. Validate each assigned resource.
                uniqueAssignedResourceIds.forEach { resourceId ->
                    val resource = resourcesById[resourceId]

                    when {
                        resource == null -> {
                            violations += ConstraintViolation(
                                message =
                                    "Resource '$resourceId' assigned to " +
                                            "requirement '${requirement.id}' " +
                                            "does not exist.",
                                penalty = 1.0,
                                relatedEntityIds = setOf(
                                    activity.id,
                                    requirement.id,
                                    resourceId
                                )
                            )
                        }

                        resource.typeId != requirement.resourceTypeId -> {
                            violations += ConstraintViolation(
                                message =
                                    "Resource '$resourceId' does not match " +
                                            "expected resource type " +
                                            "'${requirement.resourceTypeId}' " +
                                            "for requirement '${requirement.id}'.",
                                penalty = 1.0,
                                relatedEntityIds = setOf(
                                    activity.id,
                                    requirement.id,
                                    resourceId
                                )
                            )
                        }

                        requirement.candidateResourceIds != null &&
                                resourceId !in requirement.candidateResourceIds -> {

                            violations += ConstraintViolation(
                                message =
                                    "Resource '$resourceId' is not an allowed " +
                                            "candidate for requirement " +
                                            "'${requirement.id}'.",
                                penalty = 1.0,
                                relatedEntityIds = setOf(
                                    activity.id,
                                    requirement.id,
                                    resourceId
                                )
                            )
                        }
                    }
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