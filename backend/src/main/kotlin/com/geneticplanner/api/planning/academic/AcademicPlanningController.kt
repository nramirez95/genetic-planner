package com.geneticplanner.api.planning.academic

import com.geneticplanner.api.planning.PlanningApiMapper
import com.geneticplanner.api.planning.PlanningResponse
import com.geneticplanner.application.PlanningService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/plannings/from-template/academic")
class AcademicPlanningController(
    private val planningService: PlanningService,
    private val academicMapper: AcademicPlanningApiMapper,
    private val planningApiMapper: PlanningApiMapper
) {

    @PostMapping
    fun create(
        @Valid @RequestBody request: AcademicPlanningRequest
    ): ResponseEntity<PlanningResponse> {

        val configuration =
            academicMapper.toConfiguration(request)

        val created =
            planningService.createConfiguration(configuration)

        val response =
            planningApiMapper.toResponse(created)

        return ResponseEntity
            .created(
                URI.create(
                    "/api/plannings/${created.problem.id}"
                )
            )
            .body(response)
    }
}