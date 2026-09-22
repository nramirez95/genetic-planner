package com.geneticplanner.dataset

import com.geneticplanner.domain.constraint.ConstraintType
import com.geneticplanner.genetic.JeneticsEngine
import com.geneticplanner.genetic.config.GeneticAlgorithmConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PlanningDatasetTest {

    private val engine =
        JeneticsEngine()

    @Test
    fun `trivial dataset has known zero optimum`() {
        val problem =
            TrivialDataset.create()

        val result =
            engine.optimize(
                problem,
                testConfig()
            )

        assertEquals(
            TrivialDataset.OPTIMAL_FITNESS,
            result.fitness
        )

        assertTrue(result.feasible)
    }

    @Test
    fun `small feasible dataset contains hard constraints`() {
        val problem =
            SmallFeasibleDataset.create()

        assertTrue(
            problem.constraints.any {
                it.type == ConstraintType.HARD
            }
        )
    }

    @Test
    fun `small feasible dataset contains soft constraints`() {
        val problem =
            SmallFeasibleDataset.create()

        assertTrue(
            problem.constraints.any {
                it.type == ConstraintType.SOFT
            }
        )
    }

    @Test
    fun `infeasible dataset cannot produce feasible schedule`() {
        val problem =
            InfeasibleDataset.create()

        val result =
            engine.optimize(
                problem,
                testConfig()
            )

        assertFalse(result.feasible)

        assertTrue(
            result.hardPenalty > 0.0
        )
    }

    private fun testConfig() =
        GeneticAlgorithmConfig(
            populationSize = 50,
            generationLimit = 20,
            mutationProbability = 0.15,
            crossoverProbability = 0.80,
            eliteCount = 2
        )
}