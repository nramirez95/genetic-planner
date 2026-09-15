package com.geneticplanner.domain.validation

data class ValidationError(
    val code: ValidationErrorCode,
    val message: String,
    val entityType: String? = null,
    val entityId: String? = null,
    val field: String? = null
)