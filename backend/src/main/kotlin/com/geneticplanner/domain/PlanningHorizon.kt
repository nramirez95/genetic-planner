package com.geneticplanner.domain

import java.time.LocalDateTime

data class PlanningHorizon(
    val start: LocalDateTime,
    val end: LocalDateTime
)