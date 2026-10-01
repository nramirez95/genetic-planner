package com.geneticplanner.api.planning

data class OptimizationConfigurationResponse(
    val populationSize: Int,
    val generationLimit: Int,
    val mutationProbability: Double,
    val crossoverProbability: Double,
    val eliteCount: Int,
    val randomSeed: Long?
)