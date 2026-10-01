package com.geneticplanner.application.model

data class StoredOptimizationResult(
    val planningProblemId: String,
    val fitness: Double,
    val feasible: Boolean,
    val hardPenalty: Double,
    val softPenalty: Double,
    val generationsExecuted: Long,
    val executionTimeMillis: Long,
    val randomSeed: Long,
    val assignments: List<StoredAssignment>,
    val constraintResults: List<StoredConstraintResult>
)

data class StoredAssignment(
    val activityId: String,
    val timeSlotId: String,
    val resourceAssignments: Map<String, List<String>>,
    val locationId: String?
)

data class StoredConstraintResult(
    val constraintId: String,
    val type: String,
    val violations: Int,
    val rawPenalty: Double,
    val weight: Double,
    val weightedPenalty: Double,
    val violationDetails: List<StoredConstraintViolation>
)

data class StoredConstraintViolation(
    val message: String,
    val penalty: Double,
    val relatedEntityIds: Set<String>
)