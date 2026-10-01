package com.geneticplanner.application

import com.geneticplanner.domain.validation.ValidationResult

class InvalidPlanningProblemException(
    val planningId: String,
    val validationResult: ValidationResult
) : RuntimeException(
    "Planning '$planningId' is not valid for generation."
)