package com.geneticplanner.api.planning.generation

import com.geneticplanner.application.ScheduleGenerationService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/plannings")
class ScheduleGenerationController(
    private val scheduleGenerationService:
    ScheduleGenerationService,
    private val mapper:
    ScheduleGenerationMapper
) {

    @PostMapping("/{id}/generate")
    @ResponseStatus(HttpStatus.OK)
    fun generate(
        @PathVariable id: String
    ): ScheduleGenerationResponse {

        val result =
            scheduleGenerationService.generate(id)

        return mapper.toResponse(result)
    }
}