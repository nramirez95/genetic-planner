package com.geneticplanner.domain.evaluation

import com.geneticplanner.domain.constraint.ConstraintResult

data class ScheduleEvaluation(
    val hardPenalty: Double,
    val softPenalty: Double,
    val constraintResults: List<ConstraintResult>
) {
    val totalPenalty: Double
        get() = hardPenalty + softPenalty

    val fitness: Double
        get() = totalPenalty

    val feasible: Boolean
        get() = constraintResults
            .filter { it.type == com.geneticplanner.domain.constraint.ConstraintType.HARD }
            .none { it.violations > 0 }
}