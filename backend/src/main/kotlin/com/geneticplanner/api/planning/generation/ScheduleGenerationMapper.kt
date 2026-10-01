package com.geneticplanner.api.planning.generation

import com.geneticplanner.domain.Assignment
import com.geneticplanner.domain.constraint.ConstraintResult
import com.geneticplanner.domain.constraint.ConstraintViolation
import com.geneticplanner.genetic.OptimizationResult
import org.springframework.stereotype.Component

@Component
class ScheduleGenerationMapper {

    fun toResponse(
        result: OptimizationResult
    ): ScheduleGenerationResponse =
        ScheduleGenerationResponse(
            planningId =
                result.schedule.planningProblemId,
            feasible =
                result.feasible,
            fitness =
                result.fitness,
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
                result.schedule.assignments
                    .map(::toAssignmentResponse),
            constraintResults =
                result.constraintResults
                    .map(::toConstraintEvaluationResponse)
        )

    private fun toAssignmentResponse(
        assignment: Assignment
    ): AssignmentResponse =
        AssignmentResponse(
            activityId =
                assignment.activityId,
            timeSlotId =
                assignment.timeSlotId,
            resourceAssignments =
                assignment.resourceAssignments,
            locationId =
                assignment.locationId
        )

    private fun toConstraintEvaluationResponse(
        result: ConstraintResult
    ): ConstraintEvaluationResponse =
        ConstraintEvaluationResponse(
            constraintId =
                result.constraintId,
            type =
                result.type.name,
            violations =
                result.violations,
            rawPenalty =
                result.rawPenalty,
            weight =
                result.weight,
            weightedPenalty =
                result.weightedPenalty,
            violationDetails =
                result.violationDetails
                    .map(::toConstraintViolationResponse)
        )

    private fun toConstraintViolationResponse(
        violation: ConstraintViolation
    ): ConstraintViolationResponse =
        ConstraintViolationResponse(
            message =
                violation.message,
            penalty =
                violation.penalty,
            relatedEntityIds =
                violation.relatedEntityIds
        )
}