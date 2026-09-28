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
        val problem = mapper.toDomain(request)
        val created = planningService.create(problem)
        val response = mapper.toResponse(created)

        return ResponseEntity
            .created(URI.create("/api/plannings/${created.id}"))
            .body(response)
    }

    @GetMapping
    fun findAll(): List<PlanningResponse> =
        planningService
            .findAll()
            .map(mapper::toResponse)

    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: String
    ): PlanningResponse {
        val problem =
            planningService.findById(id)
                ?: throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Planning '$id' was not found."
                )

        return mapper.toResponse(problem)
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: String,
        @Valid @RequestBody request: PlanningRequest
    ): PlanningResponse {
        val problem = mapper.toDomain(request)

        val updated =
            planningService.update(id, problem)
                ?: throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Planning '$id' was not found."
                )

        return mapper.toResponse(updated)
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: String
    ): ResponseEntity<Void> {
        val deleted = planningService.delete(id)

        if (!deleted) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Planning '$id' was not found."
            )
        }

        return ResponseEntity.noContent().build()
    }
}