package com.geneticplanner.api.planning.result

data class ResultConstraintResponse(
    val constraintId: String,
    val type: String,
    val violations: Int,
    val rawPenalty: Double,
    val weight: Double,
    val weightedPenalty: Double,
    val violationDetails: List<ResultConstraintViolationResponse>
)

data class ResultConstraintViolationResponse(
    val message: String,
    val penalty: Double,
    val relatedEntityIds: Set<String>
)