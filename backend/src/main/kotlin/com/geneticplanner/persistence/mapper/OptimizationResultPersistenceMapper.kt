package com.geneticplanner.persistence.mapper

import com.geneticplanner.application.model.StoredOptimizationResult
import com.geneticplanner.persistence.entity.OptimizationResultEntity
import org.springframework.stereotype.Component

@Component
class OptimizationResultPersistenceMapper {

    fun toEntity(
        result: StoredOptimizationResult
    ): OptimizationResultEntity =
        OptimizationResultEntity(
            planningProblemId =
                result.planningProblemId,
            fitness =
                result.fitness,
            feasible =
                result.feasible,
            hardPenalty =
                result.hardPenalty,
            softPenalty =
                result.softPenalty,
            generationsExecuted =
                result.generationsExecuted,
            executionTimeMillis =
                result.executionTimeMillis,
            randomSeed =
                result.randomSeed,
            assignments =
                result.assignments,
            constraintResults =
                result.constraintResults
        )

    fun toModel(
        entity: OptimizationResultEntity
    ): StoredOptimizationResult =
        StoredOptimizationResult(
            planningProblemId =
                entity.planningProblemId,
            fitness =
                entity.fitness,
            feasible =
                entity.feasible,
            hardPenalty =
                entity.hardPenalty,
            softPenalty =
                entity.softPenalty,
            generationsExecuted =
                entity.generationsExecuted,
            executionTimeMillis =
                entity.executionTimeMillis,
            randomSeed =
                entity.randomSeed,
            assignments =
                entity.assignments,
            constraintResults =
                entity.constraintResults
        )
}