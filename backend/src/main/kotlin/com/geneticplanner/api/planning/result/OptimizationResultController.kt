package com.geneticplanner.api.planning.result

import com.geneticplanner.application.OptimizationResultService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/plannings")
class OptimizationResultController(
    private val optimizationResultService:
    OptimizationResultService,
    private val mapper:
    OptimizationResultApiMapper
) {

    @GetMapping("/{id}/result")
    fun getResult(
        @PathVariable id: String
    ): OptimizationResultResponse {

        val view =
            optimizationResultService
                .findByPlanningId(id)

        return mapper.toResponse(view)
    }
}