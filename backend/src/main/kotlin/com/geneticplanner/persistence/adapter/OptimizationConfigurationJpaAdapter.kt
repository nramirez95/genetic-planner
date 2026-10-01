package com.geneticplanner.persistence.adapter

import com.geneticplanner.application.port.out.OptimizationConfigurationPersistencePort
import com.geneticplanner.genetic.config.GeneticAlgorithmConfig
import com.geneticplanner.persistence.mapper.OptimizationConfigurationPersistenceMapper
import com.geneticplanner.persistence.repository.OptimizationConfigurationJpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class OptimizationConfigurationJpaAdapter(
    private val repository: OptimizationConfigurationJpaRepository,
    private val mapper: OptimizationConfigurationPersistenceMapper
) : OptimizationConfigurationPersistencePort {

    @Transactional
    override fun save(
        planningProblemId: String,
        configuration: GeneticAlgorithmConfig
    ): GeneticAlgorithmConfig {

        repository.save(
            mapper.toEntity(
                planningProblemId,
                configuration
            )
        )

        return configuration
    }

    @Transactional(readOnly = true)
    override fun findByPlanningProblemId(
        planningProblemId: String
    ): GeneticAlgorithmConfig? =
        repository.findById(planningProblemId)
            .map(mapper::toDomain)
            .orElse(null)

    @Transactional
    override fun deleteByPlanningProblemId(
        planningProblemId: String
    ) {
        if (repository.existsById(planningProblemId)) {
            repository.deleteById(planningProblemId)
        }
    }
}