package com.geneticplanner.genetic.config

enum class GeneticAlgorithmPreset(
    private val config: GeneticAlgorithmConfig
) {

    FAST(
        GeneticAlgorithmConfig(
            populationSize = 50,
            generationLimit = 100,
            mutationProbability = 0.10,
            crossoverProbability = 0.70,
            eliteCount = 2
        )
    ),

    BALANCED(
        GeneticAlgorithmConfig(
            populationSize = 100,
            generationLimit = 300,
            mutationProbability = 0.15,
            crossoverProbability = 0.80,
            eliteCount = 5
        )
    ),

    EXHAUSTIVE(
        GeneticAlgorithmConfig(
            populationSize = 250,
            generationLimit = 1000,
            mutationProbability = 0.20,
            crossoverProbability = 0.85,
            eliteCount = 10
        )
    );

    fun toConfig(): GeneticAlgorithmConfig =
        config

    companion object {
        val DEFAULT: GeneticAlgorithmPreset =
            BALANCED
    }
}