package com.geneticplanner.genetic

import com.geneticplanner.domain.Schedule
import com.geneticplanner.domain.evaluation.ScheduleEvaluation

data class OptimizationResult(
    val schedule: Schedule,
    val evaluation: ScheduleEvaluation,
    val generationsExecuted: Long
) {

    val fitness: Double
        get() = evaluation.fitness

    val feasible: Boolean
        get() = evaluation.feasible
}