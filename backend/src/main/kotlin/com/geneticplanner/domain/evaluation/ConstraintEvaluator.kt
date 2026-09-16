package com.geneticplanner.domain.evaluation

import com.geneticplanner.domain.Schedule
import com.geneticplanner.domain.constraint.Constraint
import com.geneticplanner.domain.constraint.ConstraintType

class ConstraintEvaluator(
    private val constraints: List<Constraint>
) {

    fun evaluate(schedule: Schedule): ScheduleEvaluation {
        val results = constraints.map { constraint ->
            constraint.evaluate(schedule)
        }

        val hardPenalty = results
            .filter { it.type == ConstraintType.HARD }
            .sumOf { it.weightedPenalty }

        val softPenalty = results
            .filter { it.type == ConstraintType.SOFT }
            .sumOf { it.weightedPenalty }

        return ScheduleEvaluation(
            hardPenalty = hardPenalty,
            softPenalty = softPenalty,
            constraintResults = results
        )
    }
}