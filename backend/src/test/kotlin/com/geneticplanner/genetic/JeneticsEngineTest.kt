package com.geneticplanner.genetic

import com.geneticplanner.domain.*
import com.geneticplanner.domain.constraint.soft.PreferredTimeSlotConstraint
import com.geneticplanner.domain.evaluation.ConstraintEvaluator
import com.geneticplanner.genetic.config.GeneticAlgorithmConfig
import com.geneticplanner.genetic.config.GeneticAlgorithmPreset
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class JeneticsEngineTest {

    private val engine = JeneticsEngine()

    @Test
    fun `returns a complete schedule`() {
        val problem = createProblem()

        val result = engine.optimize(
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

        val result = engine.optimize(
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
        val problem = createProblem()

        val result = engine.optimize(
            problem = problem,
            config = testConfig()
        )

        val expectedEvaluation =
            ConstraintEvaluator(problem.constraints)
                .evaluate(result.schedule)

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
    }

    @Test
    fun `minimizes fitness on controlled problem`() {
        val problem =
            createPreferredTimeSlotProblem()

        val result = engine.optimize(
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

        val result = engine.optimize(
            problem = problem,
            config = controlledSearchConfig()
        )

        assertTrue(result.feasible)
        assertEquals(0.0, result.fitness)
        assertEquals(0.0, result.evaluation.hardPenalty)
        assertEquals(0.0, result.evaluation.softPenalty)
    }

    @Test
    fun `executes configured generations`() {
        val problem = createProblem()

        val config = GeneticAlgorithmConfig(
            populationSize = 20,
            generationLimit = 5,
            mutationProbability = 0.10,
            crossoverProbability = 0.70,
            eliteCount = 2
        )

        val result = engine.optimize(
            problem = problem,
            config = config
        )

        assertEquals(
            5L,
            result.generationsExecuted
        )
    }

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
            exception.message.orEmpty()
                .contains("activity-no-options")
        )
    }

    @Test
    fun `works when elite count is zero`() {
        val problem = createProblem()

        val config = GeneticAlgorithmConfig(
            populationSize = 20,
            generationLimit = 5,
            mutationProbability = 0.10,
            crossoverProbability = 0.70,
            eliteCount = 0
        )

        val result = engine.optimize(
            problem = problem,
            config = config
        )

        assertEquals(
            problem.activities.size,
            result.schedule.assignments.size
        )

        assertTrue(
            result.generationsExecuted > 0
        )
    }

    @Test
    fun `works with fast preset`() {
        val problem = createProblem()

        val result = engine.optimize(
            problem = problem,
            config = GeneticAlgorithmPreset.FAST.toConfig()
        )

        assertEquals(
            problem.id,
            result.schedule.planningProblemId
        )

        assertEquals(
            problem.activities.size,
            result.schedule.assignments.size
        )

        assertTrue(
            result.generationsExecuted > 0
        )
    }

    @Test
    fun `soft constraint violation does not make result infeasible`() {
        val problem =
            createUnavoidableSoftViolationProblem()

        val result = engine.optimize(
            problem = problem,
            config = testConfig()
        )

        assertTrue(
            result.evaluation.softPenalty > 0.0
        )

        assertEquals(
            0.0,
            result.evaluation.hardPenalty
        )

        assertTrue(
            result.feasible
        )
    }

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
            result.constraintResults.single().constraintId
        )

        assertTrue(
            result.constraintResults.single().violations > 0
        )
    }

    @Test
    fun `optimization result includes execution time`() {
        val problem =
            createProblem()

        val result =
            engine.optimize(
                problem = problem,
                config = testConfig()
            )

        assertFalse(
            result.executionTime.isNegative
        )
    }

    /*
     * Generic problem with two activities and two possible time slots.
     */
    private fun createProblem(): PlanningProblem {
        val slot1 = TimeSlot(
            id = "slot-1",
            start = dateTime(9),
            end = dateTime(10)
        )

        val slot2 = TimeSlot(
            id = "slot-2",
            start = dateTime(10),
            end = dateTime(11)
        )

        return PlanningProblem(
            id = "problem-1",
            name = "Test problem",
            planningHorizon = PlanningHorizon(
                start = dateTime(8),
                end = dateTime(18)
            ),
            resourceTypes = listOf(
                ResourceType(
                    id = "worker",
                    name = "Worker"
                )
            ),
            resources = listOf(
                Resource(
                    id = "resource-1",
                    name = "Resource 1",
                    typeId = "worker"
                ),
                Resource(
                    id = "resource-2",
                    name = "Resource 2",
                    typeId = "worker"
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
                slot1,
                slot2
            ),
            locations = emptyList(),
            constraints = emptyList()
        )
    }

    /*
     * One activity can be assigned either to:
     *
     * slot-preferred -> penalty 0
     * slot-other     -> penalty 10
     *
     * This gives us a controlled optimization problem where the
     * optimum is known in advance.
     */
    private fun createPreferredTimeSlotProblem(): PlanningProblem {
        val preferredSlot = TimeSlot(
            id = "slot-preferred",
            start = dateTime(9),
            end = dateTime(10)
        )

        val otherSlot = TimeSlot(
            id = "slot-other",
            start = dateTime(10),
            end = dateTime(11)
        )

        val preferenceConstraint =
            PreferredTimeSlotConstraint(
                id = "preferred-time",
                name = "Preferred time",
                weight = 10.0,
                preferredTimeSlotIdsByActivityId = mapOf(
                    "activity-1" to setOf(
                        "slot-preferred"
                    )
                )
            )

        return PlanningProblem(
            id = "preferred-problem",
            name = "Preferred time slot problem",
            planningHorizon = PlanningHorizon(
                start = dateTime(8),
                end = dateTime(18)
            ),
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
                preferenceConstraint
            )
        )
    }

    /*
     * The only available time slot violates the soft preference.
     * The result therefore has positive soft penalty but remains
     * feasible because no HARD constraint is violated.
     */
    private fun createUnavoidableSoftViolationProblem(): PlanningProblem {
        val availableSlot = TimeSlot(
            id = "slot-other",
            start = dateTime(10),
            end = dateTime(11)
        )

        val preferenceConstraint =
            PreferredTimeSlotConstraint(
                id = "preferred-time",
                name = "Preferred time",
                weight = 10.0,
                preferredTimeSlotIdsByActivityId = mapOf(
                    "activity-1" to setOf(
                        "slot-not-available"
                    )
                )
            )

        return PlanningProblem(
            id = "soft-violation-problem",
            name = "Soft violation problem",
            planningHorizon = PlanningHorizon(
                start = dateTime(8),
                end = dateTime(18)
            ),
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
                preferenceConstraint
            )
        )
    }

    /*
     * allowedTimeSlotIds = emptySet() explicitly means that the
     * activity cannot use any time slot.
     *
     * Candidate generation therefore produces zero options.
     */
    private fun createProblemWithActivityWithoutCandidates(): PlanningProblem {
        return PlanningProblem(
            id = "invalid-problem",
            name = "Problem without candidates",
            planningHorizon = PlanningHorizon(
                start = dateTime(8),
                end = dateTime(18)
            ),
            activities = listOf(
                Activity(
                    id = "activity-no-options",
                    name = "Activity without options",
                    resourceRequirements = emptyList(),
                    allowedTimeSlotIds = emptySet()
                )
            ),
            timeSlots = listOf(
                TimeSlot(
                    id = "slot-1",
                    start = dateTime(9),
                    end = dateTime(10)
                )
            )
        )
    }

    /*
     * Small configuration keeps unit tests fast.
     */
    private fun testConfig() =
        GeneticAlgorithmConfig(
            populationSize = 20,
            generationLimit = 5,
            mutationProbability = 0.10,
            crossoverProbability = 0.70,
            eliteCount = 2
        )

    /*
     * The controlled problem only has two possible solutions.
     * A larger population makes it overwhelmingly likely that
     * both alternatives are represented in the initial population.
     */
    private fun controlledSearchConfig() =
        GeneticAlgorithmConfig(
            populationSize = 100,
            generationLimit = 10,
            mutationProbability = 0.20,
            crossoverProbability = 0.70,
            eliteCount = 5
        )

    private fun dateTime(hour: Int): LocalDateTime =
        LocalDateTime.of(
            2026,
            1,
            1,
            hour,
            0
        )
}