package com.geneticplanner.domain

data class Schedule (
    val planningProblemId: String,
    val assignments: List<Assignment>
)