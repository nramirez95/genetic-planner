package com.geneticplanner.api.planning.generation

data class ConstraintViolationResponse(
    val message: String,
    val penalty: Double,
    val relatedEntityIds: Set<String>
)