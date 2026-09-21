package com.geneticplanner.genetic.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class GeneticAlgorithmConfigTest {

    @Test
    fun `valid configuration is created`() {
        val config = GeneticAlgorithmConfig(
            populationSize = 100,
            generationLimit = 300,
            mutationProbability = 0.15,
            crossoverProbability = 0.80,
            eliteCount = 5
        )

        assertEquals(100, config.populationSize)
        assertEquals(300, config.generationLimit)
        assertEquals(0.15, config.mutationProbability)
        assertEquals(0.80, config.crossoverProbability)
        assertEquals(5, config.eliteCount)
    }

    @Test
    fun `population size equal to zero is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            GeneticAlgorithmConfig(
                populationSize = 0,
                generationLimit = 300,
                mutationProbability = 0.15,
                crossoverProbability = 0.80,
                eliteCount = 0
            )
        }
    }

    @Test
    fun `negative population size is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            GeneticAlgorithmConfig(
                populationSize = -1,
                generationLimit = 300,
                mutationProbability = 0.15,
                crossoverProbability = 0.80,
                eliteCount = 0
            )
        }
    }

    @Test
    fun `generation limit equal to zero is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            GeneticAlgorithmConfig(
                populationSize = 100,
                generationLimit = 0,
                mutationProbability = 0.15,
                crossoverProbability = 0.80,
                eliteCount = 5
            )
        }
    }

    @Test
    fun `negative generation limit is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            GeneticAlgorithmConfig(
                populationSize = 100,
                generationLimit = -1,
                mutationProbability = 0.15,
                crossoverProbability = 0.80,
                eliteCount = 5
            )
        }
    }

    @Test
    fun `negative mutation probability is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            GeneticAlgorithmConfig(
                populationSize = 100,
                generationLimit = 300,
                mutationProbability = -0.01,
                crossoverProbability = 0.80,
                eliteCount = 5
            )
        }
    }

    @Test
    fun `mutation probability greater than one is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            GeneticAlgorithmConfig(
                populationSize = 100,
                generationLimit = 300,
                mutationProbability = 1.01,
                crossoverProbability = 0.80,
                eliteCount = 5
            )
        }
    }

    @Test
    fun `mutation probability equal to zero is accepted`() {
        val config = GeneticAlgorithmConfig(
            populationSize = 100,
            generationLimit = 300,
            mutationProbability = 0.0,
            crossoverProbability = 0.80,
            eliteCount = 5
        )

        assertEquals(0.0, config.mutationProbability)
    }

    @Test
    fun `mutation probability equal to one is accepted`() {
        val config = GeneticAlgorithmConfig(
            populationSize = 100,
            generationLimit = 300,
            mutationProbability = 1.0,
            crossoverProbability = 0.80,
            eliteCount = 5
        )

        assertEquals(1.0, config.mutationProbability)
    }

    @Test
    fun `negative crossover probability is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            GeneticAlgorithmConfig(
                populationSize = 100,
                generationLimit = 300,
                mutationProbability = 0.15,
                crossoverProbability = -0.01,
                eliteCount = 5
            )
        }
    }

    @Test
    fun `crossover probability greater than one is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            GeneticAlgorithmConfig(
                populationSize = 100,
                generationLimit = 300,
                mutationProbability = 0.15,
                crossoverProbability = 1.01,
                eliteCount = 5
            )
        }
    }

    @Test
    fun `crossover probability equal to zero is accepted`() {
        val config = GeneticAlgorithmConfig(
            populationSize = 100,
            generationLimit = 300,
            mutationProbability = 0.15,
            crossoverProbability = 0.0,
            eliteCount = 5
        )

        assertEquals(0.0, config.crossoverProbability)
    }

    @Test
    fun `crossover probability equal to one is accepted`() {
        val config = GeneticAlgorithmConfig(
            populationSize = 100,
            generationLimit = 300,
            mutationProbability = 0.15,
            crossoverProbability = 1.0,
            eliteCount = 5
        )

        assertEquals(1.0, config.crossoverProbability)
    }

    @Test
    fun `negative elite count is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            GeneticAlgorithmConfig(
                populationSize = 100,
                generationLimit = 300,
                mutationProbability = 0.15,
                crossoverProbability = 0.80,
                eliteCount = -1
            )
        }
    }

    @Test
    fun `elite count equal to population size is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            GeneticAlgorithmConfig(
                populationSize = 100,
                generationLimit = 300,
                mutationProbability = 0.15,
                crossoverProbability = 0.80,
                eliteCount = 100
            )
        }
    }

    @Test
    fun `elite count greater than population size is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            GeneticAlgorithmConfig(
                populationSize = 100,
                generationLimit = 300,
                mutationProbability = 0.15,
                crossoverProbability = 0.80,
                eliteCount = 101
            )
        }
    }

    @Test
    fun `elite count equal to zero is accepted`() {
        val config = GeneticAlgorithmConfig(
            populationSize = 100,
            generationLimit = 300,
            mutationProbability = 0.15,
            crossoverProbability = 0.80,
            eliteCount = 0
        )

        assertEquals(0, config.eliteCount)
    }

    @Test
    fun `elite count one below population size is accepted`() {
        val config = GeneticAlgorithmConfig(
            populationSize = 10,
            generationLimit = 300,
            mutationProbability = 0.15,
            crossoverProbability = 0.80,
            eliteCount = 9
        )

        assertEquals(9, config.eliteCount)
    }
}