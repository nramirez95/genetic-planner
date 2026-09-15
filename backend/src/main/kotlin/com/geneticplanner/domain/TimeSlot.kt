package com.geneticplanner.domain

import java.time.LocalDateTime

data class TimeSlot (
    val id: String,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val label: String? = null
)