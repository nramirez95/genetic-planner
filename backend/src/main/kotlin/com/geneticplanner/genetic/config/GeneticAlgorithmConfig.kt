package com.geneticplanner.genetic.config

data class GeneticAlgorithmConfig(
    val populationSize: Int,
    val generationLimit: Int,
    val mutationProbability: Double,
    val crossoverProbability: Double,
    val eliteCount: Int
) {

    init {
        require(populationSize > 0) {
            "Population size must be greater than zero."
        }

        require(generationLimit > 0) {
            "Generation limit must be greater than zero."
        }

        require(mutationProbability in 0.0..1.0) {
            "Mutation probability must be between 0.0 and 1.0."
        }

        require(crossoverProbability in 0.0..1.0) {
            "Crossover probability must be between 0.0 and 1.0."
        }

        require(eliteCount >= 0) {
            "Elite count cannot be negative."
        }

        require(eliteCount < populationSize) {
            "Elite count must be smaller than population size."
        }
    }
}