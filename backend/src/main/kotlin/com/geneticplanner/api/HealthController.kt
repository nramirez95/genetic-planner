package com.geneticplanner.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController {

    @GetMapping("/health")
    fun health(): HealthResponse =
        HealthResponse(
            status = "UP",
            application = "genetic-planner"
        )
}

data class HealthResponse(
    val status: String,
    val application: String
)