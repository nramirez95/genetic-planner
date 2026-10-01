package com.geneticplanner.application.mapper

import com.geneticplanner.application.model.StoredAssignment
import com.geneticplanner.application.model.StoredConstraintResult
import com.geneticplanner.application.model.StoredConstraintViolation
import com.geneticplanner.application.model.StoredOptimizationResult
import com.geneticplanner.genetic.OptimizationResult
import org.springframework.stereotype.Component

@Component
class OptimizationResultMapper {

    fun toStored(
        result: OptimizationResult
    ): StoredOptimizationResult =
        StoredOptimizationResult(
            planningProblemId =
                result.schedule.planningProblemId,
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
                result.executionTime.toMillis(),
            randomSeed =
                result.randomSeed,
            assignments =
                result.schedule.assignments.map { assignment ->
                    StoredAssignment(
                        activityId =
                            assignment.activityId,
                        timeSlotId =
                            assignment.timeSlotId,
                        resourceAssignments =
                            assignment.resourceAssignments,
                        locationId =
                            assignment.locationId
                    )
                },
            constraintResults =
                result.constraintResults.map { constraintResult ->
                    StoredConstraintResult(
                        constraintId =
                            constraintResult.constraintId,
                        type =
                            constraintResult.type.name,
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
                                StoredConstraintViolation(
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
        )
}