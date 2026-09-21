package com.geneticplanner.genetic

import com.geneticplanner.domain.PlanningProblem
import com.geneticplanner.genetic.config.GeneticAlgorithmConfig

interface GeneticEngine {

    fun optimize(
        problem: PlanningProblem,
        config: GeneticAlgorithmConfig
    ): OptimizationResult
}