package com.geneticplanner.application.model

import com.geneticplanner.domain.PlanningProblem
import com.geneticplanner.genetic.config.GeneticAlgorithmConfig

data class PlanningConfiguration(
    val problem: PlanningProblem,
    val optimizationConfiguration: GeneticAlgorithmConfig?
)