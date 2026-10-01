package com.geneticplanner.application

import com.geneticplanner.application.model.OptimizationResultView
import com.geneticplanner.application.port.out.OptimizationResultPersistencePort
import com.geneticplanner.application.port.out.PlanningProblemPersistencePort
import org.springframework.stereotype.Service

@Service
class OptimizationResultService(
    private val planningProblemPersistencePort:
    PlanningProblemPersistencePort,
    private val optimizationResultPersistencePort:
    OptimizationResultPersistencePort
) {

    fun findByPlanningId(
        planningId: String
    ): OptimizationResultView {

        val problem =
            planningProblemPersistencePort
                .findById(planningId)
                ?: throw PlanningNotFoundException(
                    planningId
                )

        val result =
            optimizationResultPersistencePort
                .findByPlanningProblemId(planningId)
                ?: throw OptimizationResultNotFoundException(
                    planningId
                )

        return OptimizationResultView(
            problem = problem,
            result = result
        )
    }
}