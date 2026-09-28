package com.geneticplanner.api

import java.time.LocalDateTime

data class ApiErrorResponse(
    val status: Int,
    val error: String,
    val message: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)