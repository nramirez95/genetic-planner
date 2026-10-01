package com.geneticplanner.persistence.adapter

import com.geneticplanner.application.model.StoredOptimizationResult
import com.geneticplanner.application.port.out.OptimizationResultPersistencePort
import com.geneticplanner.persistence.mapper.OptimizationResultPersistenceMapper
import com.geneticplanner.persistence.repository.OptimizationResultJpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class OptimizationResultJpaAdapter(
    private val repository:
    OptimizationResultJpaRepository,
    private val mapper:
    OptimizationResultPersistenceMapper
) : OptimizationResultPersistencePort {

    @Transactional
    override fun save(
        result: StoredOptimizationResult
    ): StoredOptimizationResult {

        val entity =
            mapper.toEntity(result)

        val saved =
            repository.save(entity)

        return mapper.toModel(saved)
    }

    @Transactional(readOnly = true)
    override fun findByPlanningProblemId(
        planningProblemId: String
    ): StoredOptimizationResult? =
        repository.findById(planningProblemId)
            .orElse(null)
            ?.let(mapper::toModel)

    @Transactional
    override fun deleteByPlanningProblemId(
        planningProblemId: String
    ) {
        repository.deleteById(
            planningProblemId
        )
    }
}