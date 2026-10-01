package com.geneticplanner.api.planning.result

import com.geneticplanner.application.model.OptimizationResultView
import org.springframework.stereotype.Component

@Component
class OptimizationResultApiMapper {

    fun toResponse(
        view: OptimizationResultView
    ): OptimizationResultResponse {

        val problem =
            view.problem

        val result =
            view.result

        val activitiesById =
            problem.activities.associateBy { it.id }

        val timeSlotsById =
            problem.timeSlots.associateBy { it.id }

        val resourcesById =
            problem.resources.associateBy { it.id }

        val locationsById =
            problem.locations.associateBy { it.id }

        val assignments =
            result.assignments.map { assignment ->

                val activity =
                    requireNotNull(
                        activitiesById[assignment.activityId]
                    ) {
                        "Activity '${assignment.activityId}' " +
                                "referenced by optimization result " +
                                "does not exist."
                    }

                val timeSlot =
                    requireNotNull(
                        timeSlotsById[assignment.timeSlotId]
                    ) {
                        "Time slot '${assignment.timeSlotId}' " +
                                "referenced by optimization result " +
                                "does not exist."
                    }

                val resourceIds =
                    assignment.resourceAssignments
                        .values
                        .flatten()
                        .distinct()

                val resources =
                    resourceIds.map { resourceId ->
                        val resource =
                            requireNotNull(
                                resourcesById[resourceId]
                            ) {
                                "Resource '$resourceId' " +
                                        "referenced by optimization result " +
                                        "does not exist."
                            }

                        ResultResourceResponse(
                            id = resource.id,
                            name = resource.name,
                            typeId = resource.typeId
                        )
                    }

                val location =
                    assignment.locationId?.let { locationId ->
                        val domainLocation =
                            requireNotNull(
                                locationsById[locationId]
                            ) {
                                "Location '$locationId' " +
                                        "referenced by optimization result " +
                                        "does not exist."
                            }

                        ResultLocationResponse(
                            id = domainLocation.id,
                            name = domainLocation.name,
                            type = domainLocation.type
                        )
                    }

                ResultAssignmentResponse(
                    activity =
                        ResultActivityResponse(
                            id = activity.id,
                            name = activity.name,
                            type = activity.type
                        ),
                    timeSlot =
                        ResultTimeSlotResponse(
                            id = timeSlot.id,
                            start = timeSlot.start.toString(),
                            end = timeSlot.end.toString(),
                            label = timeSlot.label
                        ),
                    resources = resources,
                    location = location
                )
            }

        val constraintResults =
            result.constraintResults.map { constraintResult ->
                ResultConstraintResponse(
                    constraintId =
                        constraintResult.constraintId,
                    type =
                        constraintResult.type,
                    violations =
                        constraintResult.violations,
                    rawPenalty =
                        constraintResult.rawPenalty,
                    weight =
                        constraintResult.weight,
                    weightedPenalty =
                        constraintResult.weightedPenalty,
                    violationDetails =
                        constraintResult.violationDetails.map { violation ->
                            ResultConstraintViolationResponse(
                                message =
                                    violation.message,
                                penalty =
                                    violation.penalty,
                                relatedEntityIds =
                                    violation.relatedEntityIds
                            )
                        }
                )
            }

        return OptimizationResultResponse(
            planningId =
                result.planningProblemId,
            fitness =
                result.fitness,
            feasible =
                result.feasible,
            hardPenalty =
                result.hardPenalty,
            softPenalty =
                result.softPenalty,
            generationsExecuted =
                result.generationsExecuted,
            executionTimeMillis =
                result.executionTimeMillis,
            randomSeed =
                result.randomSeed,
            assignments =
                assignments,
            constraintResults =
                constraintResults
        )
    }
}