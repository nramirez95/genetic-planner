package com.geneticplanner.api

data class PlanningValidationErrorResponse(
    val status: Int,
    val error: String,
    val message: String,
    val errors: List<PlanningValidationIssueResponse>
)

data class PlanningValidationIssueResponse(
    val code: String,
    val message: String,
    val entityType: String?,
    val entityId: String?,
    val field: String?
)