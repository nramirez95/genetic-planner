package com.geneticplanner.domain.validation

data class ValidationResult (
    val errors: List<ValidationError>
) {
    val isValid: Boolean
        get() = errors.isEmpty()
}