package com.geneticplanner.api.planning

import com.geneticplanner.application.PlanningService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.net.URI

@RestController
@RequestMapping("/api/plannings")
class PlanningController(
    private val planningService: PlanningService,
    private val mapper: PlanningApiMapper
) {

    @PostMapping
    fun create(
        @Valid @RequestBody request: PlanningRequest
    ): ResponseEntity<PlanningResponse> {

        val configuration =
            mapper.toConfiguration(request)
        val created =
            planningService.createConfiguration(configuration)
        val response =
            mapper.toResponse(created)

        return ResponseEntity
            .created(
                URI.create(
                    "/api/plannings/${created.problem.id}"
                )
            )
            .body(response)
    }

    @GetMapping
    fun findAll(): List<PlanningResponse> =
        planningService.findAllConfigurations()
            .map(mapper::toResponse)

    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: String
    ): PlanningResponse {

        val configuration =
            planningService.findConfigurationById(id)
                ?: throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Planning '$id' was not found."
                )

        return mapper.toResponse(configuration)
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: String,
        @Valid @RequestBody request: PlanningRequest
    ): PlanningResponse {

        val configuration =
            mapper.toConfiguration(request)

        val updated =
            planningService.updateConfiguration(
                id,
                configuration
            )
                ?: throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Planning '$id' was not found."
                )

        return mapper.toResponse(updated)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable id: String
    ) {
        val deleted =
            planningService.deleteConfiguration(id)

        if (!deleted) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Planning '$id' was not found."
            )
        }
    }
}