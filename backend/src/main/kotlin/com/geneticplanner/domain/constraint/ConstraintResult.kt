package com.geneticplanner.domain.constraint

data class ConstraintResult(
    val constraintId: String,
    val type: ConstraintType,
    val violationDetails: List<ConstraintViolation>,
    val weight: Double
) {
    val violations: Int
        get() = violationDetails.size

    val rawPenalty: Double
        get() = violationDetails.sumOf { it.penalty }

    val weightedPenalty: Double
        get() = rawPenalty * weight
}