package com.geneticplanner.domain.evaluation

import com.geneticplanner.domain.Schedule
import com.geneticplanner.domain.constraint.Constraint
import com.geneticplanner.domain.constraint.ConstraintResult
import com.geneticplanner.domain.constraint.ConstraintType
import com.geneticplanner.domain.constraint.ConstraintViolation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ConstraintEvaluatorTest {

    private val schedule = Schedule(
        planningProblemId = "problem-1",
        assignments = emptyList()
    )

    @Test
    fun `no constraints produce zero fitness and feasible evaluation`() {
        val evaluator = ConstraintEvaluator(
            constraints = emptyList()
        )

        val evaluation = evaluator.evaluate(schedule)

        assertEquals(0.0, evaluation.hardPenalty)
        assertEquals(0.0, evaluation.softPenalty)
        assertEquals(0.0, evaluation.totalPenalty)
        assertEquals(0.0, evaluation.fitness)
        assertTrue(evaluation.feasible)
        assertTrue(evaluation.constraintResults.isEmpty())
    }

    @Test
    fun `hard penalty is calculated from hard constraints`() {
        val evaluator = ConstraintEvaluator(
            constraints = listOf(
                constraint(
                    id = "no-overlap",
                    type = ConstraintType.HARD,
                    rawPenalty = 2.0,
                    weight = 1000.0
                )
            )
        )

        val evaluation = evaluator.evaluate(schedule)

        assertEquals(2000.0, evaluation.hardPenalty)
        assertEquals(0.0, evaluation.softPenalty)
        assertEquals(2000.0, evaluation.totalPenalty)
        assertEquals(2000.0, evaluation.fitness)
        assertFalse(evaluation.feasible)
    }

    @Test
    fun `soft penalty is calculated from soft constraints`() {
        val evaluator = ConstraintEvaluator(
            constraints = listOf(
                constraint(
                    id = "preferred-time-slot",
                    type = ConstraintType.SOFT,
                    rawPenalty = 3.0,
                    weight = 5.0
                )
            )
        )

        val evaluation = evaluator.evaluate(schedule)

        assertEquals(0.0, evaluation.hardPenalty)
        assertEquals(15.0, evaluation.softPenalty)
        assertEquals(15.0, evaluation.totalPenalty)
        assertEquals(15.0, evaluation.fitness)
        assertTrue(evaluation.feasible)
    }

    @Test
    fun `hard and soft penalties are aggregated separately`() {
        val evaluator = ConstraintEvaluator(
            constraints = listOf(
                constraint(
                    id = "no-overlap",
                    type = ConstraintType.HARD,
                    rawPenalty = 2.0,
                    weight = 1000.0
                ),
                constraint(
                    id = "availability",
                    type = ConstraintType.HARD,
                    rawPenalty = 1.0,
                    weight = 1000.0
                ),
                constraint(
                    id = "preferred-time-slot",
                    type = ConstraintType.SOFT,
                    rawPenalty = 4.0,
                    weight = 10.0
                ),
                constraint(
                    id = "max-consecutive",
                    type = ConstraintType.SOFT,
                    rawPenalty = 2.0,
                    weight = 5.0
                )
            )
        )

        val evaluation = evaluator.evaluate(schedule)

        assertEquals(3000.0, evaluation.hardPenalty)
        assertEquals(50.0, evaluation.softPenalty)
        assertEquals(3050.0, evaluation.totalPenalty)
        assertEquals(3050.0, evaluation.fitness)
        assertFalse(evaluation.feasible)
    }

    @Test
    fun `multiple soft violations keep solution feasible`() {
        val evaluator = ConstraintEvaluator(
            constraints = listOf(
                constraint(
                    id = "preferred-time-slot",
                    type = ConstraintType.SOFT,
                    rawPenalty = 4.0,
                    weight = 10.0
                ),
                constraint(
                    id = "max-consecutive",
                    type = ConstraintType.SOFT,
                    rawPenalty = 2.0,
                    weight = 5.0
                ),
                constraint(
                    id = "balanced-workload",
                    type = ConstraintType.SOFT,
                    rawPenalty = 3.0,
                    weight = 2.0
                )
            )
        )

        val evaluation = evaluator.evaluate(schedule)

        assertEquals(0.0, evaluation.hardPenalty)
        assertEquals(56.0, evaluation.softPenalty)
        assertEquals(56.0, evaluation.totalPenalty)
        assertEquals(56.0, evaluation.fitness)
        assertTrue(evaluation.feasible)
    }

    @Test
    fun `weights modify constraint contribution to total fitness`() {
        val evaluator = ConstraintEvaluator(
            constraints = listOf(
                constraint(
                    id = "soft-low-weight",
                    type = ConstraintType.SOFT,
                    rawPenalty = 2.0,
                    weight = 2.0
                ),
                constraint(
                    id = "soft-high-weight",
                    type = ConstraintType.SOFT,
                    rawPenalty = 2.0,
                    weight = 10.0
                )
            )
        )

        val evaluation = evaluator.evaluate(schedule)

        // First constraint: 2 * 2 = 4
        // Second constraint: 2 * 10 = 20
        // Total = 24

        assertEquals(0.0, evaluation.hardPenalty)
        assertEquals(24.0, evaluation.softPenalty)
        assertEquals(24.0, evaluation.totalPenalty)
        assertEquals(24.0, evaluation.fitness)
        assertTrue(evaluation.feasible)
    }

    @Test
    fun `constraint results provide complete breakdown`() {
        val evaluator = ConstraintEvaluator(
            constraints = listOf(
                constraint(
                    id = "no-overlap",
                    type = ConstraintType.HARD,
                    rawPenalty = 1.0,
                    weight = 1000.0
                ),
                constraint(
                    id = "preferred-time-slot",
                    type = ConstraintType.SOFT,
                    rawPenalty = 2.0,
                    weight = 5.0
                )
            )
        )

        val evaluation = evaluator.evaluate(schedule)

        assertEquals(2, evaluation.constraintResults.size)

        val hardResult = evaluation.constraintResults.first {
            it.constraintId == "no-overlap"
        }

        assertEquals(ConstraintType.HARD, hardResult.type)
        assertEquals(1, hardResult.violations)
        assertEquals(1.0, hardResult.rawPenalty)
        assertEquals(1000.0, hardResult.weightedPenalty)

        val softResult = evaluation.constraintResults.first {
            it.constraintId == "preferred-time-slot"
        }

        assertEquals(ConstraintType.SOFT, softResult.type)
        assertEquals(1, softResult.violations)
        assertEquals(2.0, softResult.rawPenalty)
        assertEquals(10.0, softResult.weightedPenalty)
    }

    @Test
    fun `constraints without violations contribute zero penalty`() {
        val evaluator = ConstraintEvaluator(
            constraints = listOf(
                constraint(
                    id = "no-overlap",
                    type = ConstraintType.HARD,
                    rawPenalty = 0.0,
                    weight = 1000.0
                ),
                constraint(
                    id = "preferred-time-slot",
                    type = ConstraintType.SOFT,
                    rawPenalty = 0.0,
                    weight = 10.0
                )
            )
        )

        val evaluation = evaluator.evaluate(schedule)

        assertEquals(0.0, evaluation.hardPenalty)
        assertEquals(0.0, evaluation.softPenalty)
        assertEquals(0.0, evaluation.totalPenalty)
        assertEquals(0.0, evaluation.fitness)
        assertTrue(evaluation.feasible)
        assertEquals(2, evaluation.constraintResults.size)
    }

    private fun constraint(
        id: String,
        type: ConstraintType,
        rawPenalty: Double,
        weight: Double
    ): Constraint = TestConstraint(
        id = id,
        name = "Test constraint $id",
        type = type,
        weight = weight,
        rawPenalty = rawPenalty
    )

    private data class TestConstraint(
        override val id: String,
        override val name: String,
        override val type: ConstraintType,
        override val weight: Double,
        val rawPenalty: Double
    ) : Constraint {

        override fun evaluate(
            schedule: Schedule
        ): ConstraintResult {
            val violations =
                if (rawPenalty > 0.0) {
                    listOf(
                        ConstraintViolation(
                            message = "Test violation",
                            penalty = rawPenalty
                        )
                    )
                } else {
                    emptyList()
                }

            return ConstraintResult(
                constraintId = id,
                type = type,
                violationDetails = violations,
                weight = weight
            )
        }
    }
}