package com.geneticplanner.persistence.mapper

import com.geneticplanner.genetic.config.GeneticAlgorithmConfig
import com.geneticplanner.persistence.entity.OptimizationConfigurationEntity
import org.springframework.stereotype.Component

@Component
class OptimizationConfigurationPersistenceMapper {

    fun toEntity(
        planningProblemId: String,
        configuration: GeneticAlgorithmConfig
    ): OptimizationConfigurationEntity =
        OptimizationConfigurationEntity(
            planningProblemId = planningProblemId,
            populationSize = configuration.populationSize,
            generationLimit = configuration.generationLimit,
            mutationProbability = configuration.mutationProbability,
            crossoverProbability = configuration.crossoverProbability,
            eliteCount = configuration.eliteCount,
            randomSeed = configuration.randomSeed
        )

    fun toDomain(
        entity: OptimizationConfigurationEntity
    ): GeneticAlgorithmConfig =
        GeneticAlgorithmConfig(
            populationSize = entity.populationSize,
            generationLimit = entity.generationLimit,
            mutationProbability = entity.mutationProbability,
            crossoverProbability = entity.crossoverProbability,
            eliteCount = entity.eliteCount,
            randomSeed = entity.randomSeed
        )
}