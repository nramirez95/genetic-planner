package com.geneticplanner.api.planning.generation

data class ScheduleGenerationResponse(
    val planningId: String,
    val feasible: Boolean,
    val fitness: Double,
    val hardPenalty: Double,
    val softPenalty: Double,
    val generationsExecuted: Long,
    val executionTimeMillis: Long,
    val randomSeed: Long,
    val assignments: List<AssignmentResponse>,
    val constraintResults: List<ConstraintEvaluationResponse>
)