package com.geneticplanner.domain.constraint

data class ConstraintResult(
    val constraintId: String,
    val type: ConstraintType,
    val violations: Int,
    val rawPenalty: Double,
    val weight: Double
) {
    val weightedPenalty: Double
        get() = (rawPenalty * weight)
}