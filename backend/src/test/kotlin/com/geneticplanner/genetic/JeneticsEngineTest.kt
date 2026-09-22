package com.geneticplanner.genetic

import com.geneticplanner.dataset.SmallFeasibleDataset
import com.geneticplanner.domain.Activity
import com.geneticplanner.domain.PlanningHorizon
import com.geneticplanner.domain.PlanningProblem
import com.geneticplanner.domain.Resource
import com.geneticplanner.domain.ResourceRequirement
import com.geneticplanner.domain.ResourceType
import com.geneticplanner.domain.TimeSlot
import com.geneticplanner.domain.constraint.ConstraintType
import com.geneticplanner.domain.constraint.hard.NoOverlapConstraint
import com.geneticplanner.domain.constraint.soft.PreferredTimeSlotConstraint
import com.geneticplanner.domain.evaluation.ConstraintEvaluator
import com.geneticplanner.genetic.config.GeneticAlgorithmConfig
import com.geneticplanner.genetic.config.GeneticAlgorithmPreset
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class JeneticsEngineTest {

    private val engine = JeneticsEngine()

    /*
     * Basic engine execution
     */

    @Test
    fun `returns a complete schedule`() {
        val problem = createProblem()

        val result =
            engine.optimize(
                problem = problem,
                config = testConfig()
            )

        assertEquals(
            problem.activities.size,
            result.schedule.assignments.size
        )

        assertEquals(
            problem.activities.map { it.id }.toSet(),
            result.schedule.assignments
                .map { it.activityId }
                .toSet()
        )
    }

    @Test
    fun `returned schedule belongs to requested planning problem`() {
        val problem = createProblem()

        val result =
            engine.optimize(
                problem = problem,
                config = testConfig()
            )

        assertEquals(
            problem.id,
            result.schedule.planningProblemId
        )
    }

    @Test
    fun `returns evaluation matching returned schedule`() {
        val problem =
            createPreferredTimeSlotProblem()

        val result =
            engine.optimize(
                problem = problem,
                config = controlledSearchConfig()
            )

        val expectedEvaluation =
            ConstraintEvaluator(problem.constraints)
                .evaluate(result.schedule)

        assertEquals(
            expectedEvaluation,
            result.evaluation
        )
    }

    /*
     * Optimization behaviour
     */

    @Test
    fun `minimizes fitness on controlled problem`() {
        val problem =
            createPreferredTimeSlotProblem()

        val result =
            engine.optimize(
                problem = problem,
                config = controlledSearchConfig()
            )

        assertEquals(
            0.0,
            result.fitness
        )

        assertEquals(
            "slot-preferred",
            result.schedule.assignments
                .single()
                .timeSlotId
        )
    }

    @Test
    fun `can find feasible zero penalty solution`() {
        val problem =
            createPreferredTimeSlotProblem()

        val result =
            engine.optimize(
                problem = problem,
                config = controlledSearchConfig()
            )

        assertTrue(result.feasible)

        assertEquals(
            0.0,
            result.hardPenalty
        )

        assertEquals(
            0.0,
            result.softPenalty
        )

        assertEquals(
            0.0,
            result.fitness
        )
    }

    @Test
    fun `executes configured generations`() {
        val problem = createProblem()

        val config =
            GeneticAlgorithmConfig(
                populationSize = 20,
                generationLimit = 5,
                mutationProbability = 0.10,
                crossoverProbability = 0.70,
                eliteCount = 2
            )

        val result =
            engine.optimize(
                problem = problem,
                config = config
            )

        assertEquals(
            5L,
            result.generationsExecuted
        )
    }

    /*
     * Invalid problem / candidate generation
     */

    @Test
    fun `rejects problem when activity has no assignment candidates`() {
        val problem =
            createProblemWithActivityWithoutCandidates()

        val exception =
            assertThrows(
                IllegalArgumentException::class.java
            ) {
                engine.optimize(
                    problem = problem,
                    config = testConfig()
                )
            }

        assertTrue(
            exception.message
                .orEmpty()
                .contains("activity-no-options")
        )
    }

    /*
     * GA configuration integration
     */

    @Test
    fun `works when elite count is zero`() {
        val problem = createProblem()

        val config =
            GeneticAlgorithmConfig(
                populationSize = 20,
                generationLimit = 5,
                mutationProbability = 0.10,
                crossoverProbability = 0.70,
                eliteCount = 0
            )

        val result =
            engine.optimize(
                problem = problem,
                config = config
            )

        assertEquals(
            5L,
            result.generationsExecuted
        )

        assertEquals(
            problem.activities.size,
            result.schedule.assignments.size
        )
    }

    @Test
    fun `works with fast preset`() {
        val problem = createProblem()

        val config =
            GeneticAlgorithmPreset.FAST.toConfig()

        val result =
            engine.optimize(
                problem = problem,
                config = config
            )

        assertEquals(
            config.generationLimit.toLong(),
            result.generationsExecuted
        )

        assertEquals(
            problem.activities.size,
            result.schedule.assignments.size
        )
    }

    /*
     * Single and multiple activities
     */

    @Test
    fun `optimizes problem with single activity`() {
        val problem =
            createSingleActivityProblem()

        val result =
            engine.optimize(
                problem = problem,
                config = testConfig()
            )

        assertEquals(
            1,
            result.schedule.assignments.size
        )

        assertEquals(
            "activity-1",
            result.schedule.assignments
                .single()
                .activityId
        )
    }

    @Test
    fun `optimizes problem with multiple activities`() {
        val problem = createProblem()

        val result =
            engine.optimize(
                problem = problem,
                config = testConfig()
            )

        assertEquals(
            problem.activities.size,
            result.schedule.assignments.size
        )

        assertEquals(
            problem.activities.map { it.id }.toSet(),
            result.schedule.assignments
                .map { it.activityId }
                .toSet()
        )
    }

    /*
     * Hard constraints
     */

    @Test
    fun `hard constraint violation produces infeasible result`() {
        val problem =
            createUnavoidableHardViolationProblem()

        val result =
            engine.optimize(
                problem = problem,
                config = testConfig()
            )

        assertFalse(result.feasible)

        assertTrue(
            result.hardPenalty > 0.0
        )

        assertEquals(
            0.0,
            result.softPenalty
        )

        assertTrue(
            result.constraintResults.any {
                it.type == ConstraintType.HARD &&
                        it.violations > 0
            }
        )
    }

    /*
     * Soft constraints
     */

    @Test
    fun `soft constraint violation does not make result infeasible`() {
        val problem =
            createUnavoidableSoftViolationProblem()

        val result =
            engine.optimize(
                problem = problem,
                config = testConfig()
            )

        assertTrue(result.feasible)

        assertEquals(
            0.0,
            result.hardPenalty
        )

        assertTrue(
            result.softPenalty > 0.0
        )

        assertTrue(
            result.fitness > 0.0
        )

        assertTrue(
            result.constraintResults.any {
                it.type == ConstraintType.SOFT &&
                        it.violations > 0
            }
        )
    }

    /*
     * OptimizationResult
     */

    @Test
    fun `optimization result exposes fitness and penalty breakdown`() {
        val problem =
            createUnavoidableSoftViolationProblem()

        val result =
            engine.optimize(
                problem = problem,
                config = testConfig()
            )

        assertEquals(
            result.hardPenalty + result.softPenalty,
            result.fitness
        )

        assertEquals(
            result.evaluation.hardPenalty,
            result.hardPenalty
        )

        assertEquals(
            result.evaluation.softPenalty,
            result.softPenalty
        )
    }

    @Test
    fun `optimization result exposes constraint results`() {
        val problem =
            createUnavoidableSoftViolationProblem()

        val result =
            engine.optimize(
                problem = problem,
                config = testConfig()
            )

        assertEquals(
            problem.constraints.size,
            result.constraintResults.size
        )

        assertEquals(
            "preferred-time",
            result.constraintResults
                .single()
                .constraintId
        )

        assertTrue(
            result.constraintResults
                .single()
                .violations > 0
        )
    }

    @Test
    fun `optimization result includes execution time`() {
        val problem = createProblem()

        val result =
            engine.optimize(
                problem = problem,
                config = testConfig()
            )

        assertFalse(
            result.executionTime.isNegative
        )
    }

    @Test
    fun `returns complete optimization result`() {
        val problem =
            createUnavoidableSoftViolationProblem()

        val result =
            engine.optimize(
                problem = problem,
                config = testConfig()
            )

        val expectedEvaluation =
            ConstraintEvaluator(problem.constraints)
                .evaluate(result.schedule)

        assertEquals(
            problem.id,
            result.schedule.planningProblemId
        )

        assertEquals(
            expectedEvaluation,
            result.evaluation
        )

        assertEquals(
            expectedEvaluation.fitness,
            result.fitness
        )

        assertEquals(
            expectedEvaluation.feasible,
            result.feasible
        )

        assertEquals(
            expectedEvaluation.hardPenalty,
            result.hardPenalty
        )

        assertEquals(
            expectedEvaluation.softPenalty,
            result.softPenalty
        )

        assertEquals(
            expectedEvaluation.constraintResults,
            result.constraintResults
        )

        assertEquals(
            testConfig().generationLimit.toLong(),
            result.generationsExecuted
        )

        assertFalse(
            result.executionTime.isNegative
        )
    }

    @Test
    fun `records provided random seed`() {
        val problem =
            SmallFeasibleDataset.create()

        val config =
            testConfig().copy(
                randomSeed = 42L
            )

        val result =
            engine.optimize(
                problem = problem,
                config = config
            )

        assertEquals(
            42L,
            result.randomSeed
        )
    }

    @Test
    fun `same problem configuration and seed produce same result`() {
        val problem =
            SmallFeasibleDataset.create()

        val config =
            testConfig().copy(
                randomSeed = 123456L
            )

        val firstResult =
            engine.optimize(
                problem = problem,
                config = config
            )

        val secondResult =
            engine.optimize(
                problem = problem,
                config = config
            )

        assertEquals(
            firstResult.schedule,
            secondResult.schedule
        )

        assertEquals(
            firstResult.evaluation,
            secondResult.evaluation
        )

        assertEquals(
            firstResult.fitness,
            secondResult.fitness
        )

        assertEquals(
            firstResult.generationsExecuted,
            secondResult.generationsExecuted
        )

        assertEquals(
            firstResult.randomSeed,
            secondResult.randomSeed
        )
    }

    @Test
    fun `generated random seed can reproduce execution`() {
        val problem =
            SmallFeasibleDataset.create()

        val config =
            testConfig()

        val originalResult =
            engine.optimize(
                problem = problem,
                config = config
            )

        val reproducedResult =
            engine.optimize(
                problem = problem,
                config = config.copy(
                    randomSeed = originalResult.randomSeed
                )
            )

        assertEquals(
            originalResult.schedule,
            reproducedResult.schedule
        )

        assertEquals(
            originalResult.evaluation,
            reproducedResult.evaluation
        )

        assertEquals(
            originalResult.fitness,
            reproducedResult.fitness
        )

        assertEquals(
            originalResult.randomSeed,
            reproducedResult.randomSeed
        )
    }

    /*
     * Fixtures
     */

    private fun createProblem(): PlanningProblem =
        PlanningProblem(
            id = "problem-1",
            name = "Test problem",
            planningHorizon = horizon(),
            resourceTypes = listOf(
                workerType()
            ),
            resources = listOf(
                worker(
                    id = "resource-1",
                    name = "Resource 1"
                ),
                worker(
                    id = "resource-2",
                    name = "Resource 2"
                )
            ),
            activities = listOf(
                Activity(
                    id = "activity-1",
                    name = "Activity 1",
                    resourceRequirements = listOf(
                        ResourceRequirement(
                            id = "requirement-1",
                            resourceTypeId = "worker",
                            quantity = 1
                        )
                    )
                ),
                Activity(
                    id = "activity-2",
                    name = "Activity 2",
                    resourceRequirements = listOf(
                        ResourceRequirement(
                            id = "requirement-2",
                            resourceTypeId = "worker",
                            quantity = 1
                        )
                    )
                )
            ),
            timeSlots = listOf(
                timeSlot(
                    id = "slot-1",
                    startHour = 9,
                    endHour = 10
                ),
                timeSlot(
                    id = "slot-2",
                    startHour = 10,
                    endHour = 11
                )
            )
        )

    private fun createSingleActivityProblem(): PlanningProblem =
        PlanningProblem(
            id = "single-activity-problem",
            name = "Single activity problem",
            planningHorizon = horizon(),
            resourceTypes = listOf(
                workerType()
            ),
            resources = listOf(
                worker()
            ),
            activities = listOf(
                Activity(
                    id = "activity-1",
                    name = "Activity 1",
                    resourceRequirements = listOf(
                        ResourceRequirement(
                            id = "requirement-1",
                            resourceTypeId = "worker",
                            quantity = 1
                        )
                    )
                )
            ),
            timeSlots = listOf(
                timeSlot(
                    id = "slot-1",
                    startHour = 9,
                    endHour = 10
                )
            )
        )

    private fun createPreferredTimeSlotProblem(): PlanningProblem {
        val preferredSlot =
            timeSlot(
                id = "slot-preferred",
                startHour = 9,
                endHour = 10
            )

        val otherSlot =
            timeSlot(
                id = "slot-other",
                startHour = 10,
                endHour = 11
            )

        val preferredTimeConstraint =
            PreferredTimeSlotConstraint(
                id = "preferred-time",
                name = "Preferred time",
                weight = 10.0,
                preferredTimeSlotIdsByActivityId = mapOf(
                    "activity-1" to
                            setOf("slot-preferred")
                )
            )

        return PlanningProblem(
            id = "preferred-time-problem",
            name = "Preferred time problem",
            planningHorizon = horizon(),
            activities = listOf(
                Activity(
                    id = "activity-1",
                    name = "Activity 1",
                    resourceRequirements = emptyList()
                )
            ),
            timeSlots = listOf(
                preferredSlot,
                otherSlot
            ),
            constraints = listOf(
                preferredTimeConstraint
            )
        )
    }

    private fun createUnavoidableSoftViolationProblem(): PlanningProblem {
        val availableSlot =
            timeSlot(
                id = "slot-other",
                startHour = 10,
                endHour = 11
            )

        val preferredTimeConstraint =
            PreferredTimeSlotConstraint(
                id = "preferred-time",
                name = "Preferred time",
                weight = 10.0,
                preferredTimeSlotIdsByActivityId = mapOf(
                    "activity-1" to
                            setOf("slot-preferred")
                )
            )

        return PlanningProblem(
            id = "soft-violation-problem",
            name = "Soft violation problem",
            planningHorizon = horizon(),
            activities = listOf(
                Activity(
                    id = "activity-1",
                    name = "Activity 1",
                    resourceRequirements = emptyList()
                )
            ),
            timeSlots = listOf(
                availableSlot
            ),
            constraints = listOf(
                preferredTimeConstraint
            )
        )
    }

    private fun createUnavoidableHardViolationProblem(): PlanningProblem {
        val slot =
            timeSlot(
                id = "slot-1",
                startHour = 9,
                endHour = 10
            )

        val noOverlapConstraint =
            NoOverlapConstraint(
                id = "no-overlap",
                name = "No resource overlap",
                weight = 1000.0,
                timeSlots = listOf(slot)
            )

        return PlanningProblem(
            id = "hard-violation-problem",
            name = "Hard violation problem",
            planningHorizon = horizon(),
            resourceTypes = listOf(
                workerType()
            ),
            resources = listOf(
                worker()
            ),
            activities = listOf(
                Activity(
                    id = "activity-1",
                    name = "Activity 1",
                    resourceRequirements = listOf(
                        ResourceRequirement(
                            id = "requirement-1",
                            resourceTypeId = "worker",
                            quantity = 1,
                            candidateResourceIds =
                                setOf("resource-1")
                        )
                    ),
                    allowedTimeSlotIds =
                        setOf("slot-1")
                ),
                Activity(
                    id = "activity-2",
                    name = "Activity 2",
                    resourceRequirements = listOf(
                        ResourceRequirement(
                            id = "requirement-2",
                            resourceTypeId = "worker",
                            quantity = 1,
                            candidateResourceIds =
                                setOf("resource-1")
                        )
                    ),
                    allowedTimeSlotIds =
                        setOf("slot-1")
                )
            ),
            timeSlots = listOf(slot),
            constraints = listOf(
                noOverlapConstraint
            )
        )
    }

    private fun createProblemWithActivityWithoutCandidates():
            PlanningProblem =
        PlanningProblem(
            id = "invalid-problem",
            name = "Problem without candidates",
            planningHorizon = horizon(),
            activities = listOf(
                Activity(
                    id = "activity-no-options",
                    name = "Activity without options",
                    resourceRequirements = emptyList(),
                    allowedTimeSlotIds = emptySet()
                )
            ),
            timeSlots = listOf(
                timeSlot(
                    id = "slot-1",
                    startHour = 9,
                    endHour = 10
                )
            )
        )

    private fun testConfig() =
        GeneticAlgorithmConfig(
            populationSize = 20,
            generationLimit = 5,
            mutationProbability = 0.10,
            crossoverProbability = 0.70,
            eliteCount = 2
        )

    private fun controlledSearchConfig() =
        GeneticAlgorithmConfig(
            populationSize = 100,
            generationLimit = 10,
            mutationProbability = 0.20,
            crossoverProbability = 0.70,
            eliteCount = 5
        )

    private fun horizon() =
        PlanningHorizon(
            start = dateTime(8),
            end = dateTime(18)
        )

    private fun workerType() =
        ResourceType(
            id = "worker",
            name = "Worker"
        )

    private fun worker(
        id: String = "resource-1",
        name: String = "Resource 1"
    ) =
        Resource(
            id = id,
            name = name,
            typeId = "worker"
        )

    private fun timeSlot(
        id: String,
        startHour: Int,
        endHour: Int
    ) =
        TimeSlot(
            id = id,
            start = dateTime(startHour),
            end = dateTime(endHour)
        )

    private fun dateTime(
        hour: Int
    ): LocalDateTime =
        LocalDateTime.of(
            2026,
            1,
            1,
            hour,
            0
        )
}