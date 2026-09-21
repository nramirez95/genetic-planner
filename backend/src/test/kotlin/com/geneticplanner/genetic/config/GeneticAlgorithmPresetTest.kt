package com.geneticplanner.genetic.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GeneticAlgorithmPresetTest {

    @Test
    fun `fast preset produces expected configuration`() {
        val config =
            GeneticAlgorithmPreset.FAST.toConfig()

        assertEquals(50, config.populationSize)
        assertEquals(100, config.generationLimit)
        assertEquals(0.10, config.mutationProbability)
        assertEquals(0.70, config.crossoverProbability)
        assertEquals(2, config.eliteCount)
    }

    @Test
    fun `balanced preset produces expected configuration`() {
        val config =
            GeneticAlgorithmPreset.BALANCED.toConfig()

        assertEquals(100, config.populationSize)
        assertEquals(300, config.generationLimit)
        assertEquals(0.15, config.mutationProbability)
        assertEquals(0.80, config.crossoverProbability)
        assertEquals(5, config.eliteCount)
    }

    @Test
    fun `exhaustive preset produces expected configuration`() {
        val config =
            GeneticAlgorithmPreset.EXHAUSTIVE.toConfig()

        assertEquals(250, config.populationSize)
        assertEquals(1000, config.generationLimit)
        assertEquals(0.20, config.mutationProbability)
        assertEquals(0.85, config.crossoverProbability)
        assertEquals(10, config.eliteCount)
    }

    @Test
    fun `balanced preset is default`() {
        assertEquals(
            GeneticAlgorithmPreset.BALANCED,
            GeneticAlgorithmPreset.DEFAULT
        )
    }

    @Test
    fun `default preset produces balanced configuration`() {
        val defaultConfig =
            GeneticAlgorithmPreset.DEFAULT.toConfig()

        val balancedConfig =
            GeneticAlgorithmPreset.BALANCED.toConfig()

        assertEquals(
            balancedConfig,
            defaultConfig
        )
    }
}