package com.geneticplanner.genetic

import com.geneticplanner.domain.Schedule
import com.geneticplanner.domain.constraint.ConstraintResult
import com.geneticplanner.domain.evaluation.ScheduleEvaluation
import java.time.Duration

data class OptimizationResult(
    val schedule: Schedule,
    val evaluation: ScheduleEvaluation,
    val generationsExecuted: Long,
    val executionTime: Duration
) {

    val fitness: Double
        get() = evaluation.fitness

    val feasible: Boolean
        get() = evaluation.feasible

    val hardPenalty: Double
        get() = evaluation.hardPenalty

    val softPenalty: Double
        get() = evaluation.softPenalty

    val constraintResults: List<ConstraintResult>
        get() = evaluation.constraintResults
}