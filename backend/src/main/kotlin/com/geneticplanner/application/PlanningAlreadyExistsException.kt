package com.geneticplanner.application

class PlanningAlreadyExistsException(
    id: String
) : RuntimeException(
    "Planning '$id' already exists."
)