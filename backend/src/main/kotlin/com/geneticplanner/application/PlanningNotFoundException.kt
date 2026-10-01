package com.geneticplanner.application

class PlanningNotFoundException(
    val planningId: String
) : RuntimeException(
    "Planning '$planningId' was not found."
)