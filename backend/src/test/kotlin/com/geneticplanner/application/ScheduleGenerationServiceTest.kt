package com.geneticplanner.application

import com.geneticplanner.application.mapper.OptimizationResultMapper
import com.geneticplanner.application.model.StoredOptimizationResult
import com.geneticplanner.application.port.out.OptimizationConfigurationPersistencePort
import com.geneticplanner.application.port.out.OptimizationResultPersistencePort
import com.geneticplanner.application.port.out.PlanningProblemPersistencePort
import com.geneticplanner.dataset.TrivialDataset
import com.geneticplanner.domain.PlanningProblem
import com.geneticplanner.domain.validation.ProblemValidator
import com.geneticplanner.genetic.GeneticEngine
import com.geneticplanner.genetic.OptimizationResult
import com.geneticplanner.genetic.config.GeneticAlgorithmConfig
import com.geneticplanner.genetic.config.GeneticAlgorithmPreset
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ScheduleGenerationServiceTest {

    private lateinit var planningPersistence:
            FakePlanningProblemPersistencePort

    private lateinit var optimizationPersistence:
            FakeOptimizationConfigurationPersistencePort

    private lateinit var geneticEngine:
            FakeGeneticEngine

    private lateinit var service:
            ScheduleGenerationService

    private lateinit var optimizationResultPersistence:
            FakeOptimizationResultPersistencePort

    private lateinit var optimizationResultMapper:
            OptimizationResultMapper

    @BeforeEach
    fun setUp() {
        planningPersistence =
            FakePlanningProblemPersistencePort()

        optimizationPersistence =
            FakeOptimizationConfigurationPersistencePort()

        optimizationResultPersistence =
            FakeOptimizationResultPersistencePort()

        optimizationResultMapper =
            OptimizationResultMapper()

        geneticEngine =
            FakeGeneticEngine()

        service =
            ScheduleGenerationService(
                planningProblemPersistencePort =
                    planningPersistence,
                optimizationConfigurationPersistencePort =
                    optimizationPersistence,
                geneticEngine =
                    geneticEngine,
                problemValidator =
                    ProblemValidator(),
                optimizationResultPersistencePort =
                    optimizationResultPersistence,
                optimizationResultMapper =
                    optimizationResultMapper
            )
    }

    @Test
    fun `should reject unknown planning`() {
        val exception =
            assertThrows(
                PlanningNotFoundException::class.java
            ) {
                service.generate(PLANNING_ID)
            }

        assertEquals(
            PLANNING_ID,
            exception.planningId
        )

        assertFalse(
            optimizationPersistence.findCalled
        )

        assertFalse(
            geneticEngine.optimizeCalled
        )

        assertFalse(
            optimizationResultPersistence.saveCalled
        )
    }

    @Test
    fun `should reject invalid planning before executing engine`() {
        val invalidProblem =
            validProblem().copy(
                timeSlots = emptyList()
            )

        planningPersistence.problem =
            invalidProblem

        val exception =
            assertThrows(
                InvalidPlanningProblemException::class.java
            ) {
                service.generate(PLANNING_ID)
            }

        assertEquals(
            PLANNING_ID,
            exception.planningId
        )

        assertFalse(
            exception.validationResult.isValid
        )

        assertTrue(
            exception.validationResult.errors.isNotEmpty()
        )

        assertFalse(
            optimizationPersistence.findCalled
        )

        assertFalse(
            geneticEngine.optimizeCalled
        )

        assertFalse(
            optimizationResultPersistence.saveCalled
        )
    }

    @Test
    fun `should use persisted optimization configuration`() {
        val problem =
            validProblem()

        val config =
            GeneticAlgorithmConfig(
                populationSize = 75,
                generationLimit = 150,
                mutationProbability = 0.12,
                crossoverProbability = 0.75,
                eliteCount = 3,
                randomSeed = 12345L
            )

        val expectedResult =
            createOptimizationResult()

        planningPersistence.problem =
            problem

        optimizationPersistence.configuration =
            config

        geneticEngine.result =
            expectedResult

        val result =
            service.generate(PLANNING_ID)

        assertSame(
            expectedResult,
            result
        )

        assertTrue(
            geneticEngine.optimizeCalled
        )

        assertEquals(
            problem,
            geneticEngine.receivedProblem
        )

        assertEquals(
            config,
            geneticEngine.receivedConfig
        )

        val storedResult =
            optimizationResultPersistence.savedResult

        assertTrue(
            optimizationResultPersistence.saveCalled
        )

        assertNotNull(
            storedResult
        )

        assertEquals(
            PLANNING_ID,
            storedResult!!.planningProblemId
        )

        assertEquals(
            expectedResult.fitness,
            storedResult.fitness
        )

        assertEquals(
            expectedResult.feasible,
            storedResult.feasible
        )

        assertEquals(
            expectedResult.generationsExecuted,
            storedResult.generationsExecuted
        )

        assertEquals(
            expectedResult.schedule.assignments.size,
            storedResult.assignments.size
        )

        assertEquals(
            expectedResult.constraintResults.size,
            storedResult.constraintResults.size
        )
    }

    @Test
    fun `should use default preset when optimization configuration is missing`() {
        val problem =
            validProblem()

        val expectedResult =
            createOptimizationResult()

        planningPersistence.problem =
            problem

        optimizationPersistence.configuration =
            null

        geneticEngine.result =
            expectedResult

        val result =
            service.generate(PLANNING_ID)

        assertSame(
            expectedResult,
            result
        )

        assertEquals(
            problem,
            geneticEngine.receivedProblem
        )

        assertEquals(
            GeneticAlgorithmPreset.DEFAULT.toConfig(),
            geneticEngine.receivedConfig
        )
    }

    @Test
    fun `should wrap genetic engine failure`() {
        val problem =
            validProblem()

        val cause =
            IllegalStateException(
                "Genetic engine failed."
            )

        planningPersistence.problem =
            problem

        optimizationPersistence.configuration =
            null

        geneticEngine.exception =
            cause

        val exception =
            assertThrows(
                ScheduleGenerationException::class.java
            ) {
                service.generate(PLANNING_ID)
            }

        assertEquals(
            PLANNING_ID,
            exception.planningId
        )

        assertSame(
            cause,
            exception.cause
        )

        assertTrue(
            geneticEngine.optimizeCalled
        )

        assertFalse(
            optimizationResultPersistence.saveCalled
        )
    }

    private fun validProblem(): PlanningProblem =
        TrivialDataset.create()
            .copy(
                id = PLANNING_ID
            )

    private fun createOptimizationResult(): OptimizationResult =
        com.geneticplanner.genetic.JeneticsEngine()
            .optimize(
                problem = validProblem(),
                config =
                    GeneticAlgorithmPreset.FAST
                        .toConfig()
                        .copy(
                            randomSeed = 12345L
                        )
            )

    companion object {

        private const val PLANNING_ID =
            "generation-planning-1"
    }

    private class FakePlanningProblemPersistencePort :
        PlanningProblemPersistencePort {

        var problem: PlanningProblem? =
            null

        override fun findById(
            id: String
        ): PlanningProblem? =
            problem?.takeIf {
                it.id == id
            }

        override fun save(
            problem: PlanningProblem
        ): PlanningProblem =
            problem

        override fun findAll():
                List<PlanningProblem> =
            listOfNotNull(problem)

        override fun deleteById(
            id: String
        ) {
            if (problem?.id == id) {
                problem = null
            }
        }

        override fun existsById(
            id: String
        ): Boolean =
            problem?.id == id
    }

    private class FakeOptimizationConfigurationPersistencePort :
        OptimizationConfigurationPersistencePort {

        var configuration:
                GeneticAlgorithmConfig? =
            null

        var findCalled:
                Boolean =
            false

        override fun findByPlanningProblemId(
            planningProblemId: String
        ): GeneticAlgorithmConfig? {
            findCalled = true
            return configuration
        }

        override fun save(
            planningProblemId: String,
            configuration: GeneticAlgorithmConfig
        ): GeneticAlgorithmConfig {
            this.configuration =
                configuration

            return configuration
        }

        override fun deleteByPlanningProblemId(
            planningProblemId: String
        ) {
            configuration = null
        }
    }

    private class FakeGeneticEngine :
        GeneticEngine {

        var optimizeCalled:
                Boolean =
            false

        var receivedProblem:
                PlanningProblem? =
            null

        var receivedConfig:
                GeneticAlgorithmConfig? =
            null

        var result:
                OptimizationResult? =
            null

        var exception:
                RuntimeException? =
            null

        override fun optimize(
            problem: PlanningProblem,
            config: GeneticAlgorithmConfig
        ): OptimizationResult {

            optimizeCalled = true
            receivedProblem = problem
            receivedConfig = config

            exception?.let {
                throw it
            }

            return requireNotNull(result) {
                "FakeGeneticEngine result was not configured."
            }
        }
    }

    private class FakeOptimizationResultPersistencePort :
        OptimizationResultPersistencePort {

        var savedResult:
                StoredOptimizationResult? =
            null

        var saveCalled:
                Boolean =
            false

        override fun save(
            result: StoredOptimizationResult
        ): StoredOptimizationResult {

            saveCalled = true
            savedResult = result

            return result
        }

        override fun findByPlanningProblemId(
            planningProblemId: String
        ): StoredOptimizationResult? =
            savedResult?.takeIf {
                it.planningProblemId ==
                        planningProblemId
            }

        override fun deleteByPlanningProblemId(
            planningProblemId: String
        ) {
            if (
                savedResult?.planningProblemId ==
                planningProblemId
            ) {
                savedResult = null
            }
        }
    }
}