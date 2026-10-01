package com.geneticplanner.application

class OptimizationResultNotFoundException(
    val planningId: String
) : RuntimeException(
    "No optimization result was found for planning '$planningId'."
)