package com.geneticplanner.application.model

import com.geneticplanner.domain.PlanningProblem

data class OptimizationResultView(
    val problem: PlanningProblem,
    val result: StoredOptimizationResult
)