package com.geneticplanner.api.planning.result

data class OptimizationResultResponse(
    val planningId: String,
    val fitness: Double,
    val feasible: Boolean,
    val hardPenalty: Double,
    val softPenalty: Double,
    val generationsExecuted: Long,
    val executionTimeMillis: Long,
    val randomSeed: Long,
    val assignments: List<ResultAssignmentResponse>,
    val constraintResults: List<ResultConstraintResponse>
)