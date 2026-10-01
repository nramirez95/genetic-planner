package com.geneticplanner.api.planning.generation

data class ConstraintEvaluationResponse(
    val constraintId: String,
    val type: String,
    val violations: Int,
    val rawPenalty: Double,
    val weight: Double,
    val weightedPenalty: Double,
    val violationDetails: List<ConstraintViolationResponse>
)