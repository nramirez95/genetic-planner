package com.geneticplanner.domain.evaluation

import com.geneticplanner.domain.constraint.ConstraintResult
import com.geneticplanner.domain.constraint.ConstraintType
import com.geneticplanner.domain.constraint.ConstraintViolation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ScheduleEvaluationTest {

    @Test
    fun `total penalty is sum of hard and soft penalties`() {
        val evaluation = ScheduleEvaluation(
            hardPenalty = 2000.0,
            softPenalty = 50.0,
            constraintResults = emptyList()
        )

        assertEquals(2050.0, evaluation.totalPenalty)
    }

    @Test
    fun `fitness equals total penalty`() {
        val evaluation = ScheduleEvaluation(
            hardPenalty = 1000.0,
            softPenalty = 25.0,
            constraintResults = emptyList()
        )

        assertEquals(1025.0, evaluation.totalPenalty)
        assertEquals(1025.0, evaluation.fitness)
    }

    @Test
    fun `evaluation without violations is feasible`() {
        val evaluation = ScheduleEvaluation(
            hardPenalty = 0.0,
            softPenalty = 0.0,
            constraintResults = listOf(
                result(
                    id = "hard-constraint",
                    type = ConstraintType.HARD,
                    violations = emptyList(),
                    weight = 1000.0
                ),
                result(
                    id = "soft-constraint",
                    type = ConstraintType.SOFT,
                    violations = emptyList(),
                    weight = 5.0
                )
            )
        )

        assertTrue(evaluation.feasible)
        assertEquals(0.0, evaluation.totalPenalty)
        assertEquals(0.0, evaluation.fitness)
    }

    @Test
    fun `soft violations do not make evaluation infeasible`() {
        val evaluation = ScheduleEvaluation(
            hardPenalty = 0.0,
            softPenalty = 10.0,
            constraintResults = listOf(
                result(
                    id = "preferred-time-slot",
                    type = ConstraintType.SOFT,
                    violations = listOf(
                        violation(
                            message = "Preference not satisfied",
                            penalty = 2.0
                        )
                    ),
                    weight = 5.0
                )
            )
        )

        assertTrue(evaluation.feasible)
        assertEquals(0.0, evaluation.hardPenalty)
        assertEquals(10.0, evaluation.softPenalty)
        assertEquals(10.0, evaluation.totalPenalty)
        assertEquals(10.0, evaluation.fitness)
    }

    @Test
    fun `hard violation makes evaluation infeasible`() {
        val evaluation = ScheduleEvaluation(
            hardPenalty = 1000.0,
            softPenalty = 0.0,
            constraintResults = listOf(
                result(
                    id = "no-overlap",
                    type = ConstraintType.HARD,
                    violations = listOf(
                        violation(
                            message = "Resource overlap",
                            penalty = 1.0
                        )
                    ),
                    weight = 1000.0
                )
            )
        )

        assertFalse(evaluation.feasible)
        assertEquals(1000.0, evaluation.hardPenalty)
        assertEquals(1000.0, evaluation.totalPenalty)
        assertEquals(1000.0, evaluation.fitness)
    }

    @Test
    fun `hard violation determines feasibility independently of soft violations`() {
        val evaluation = ScheduleEvaluation(
            hardPenalty = 1000.0,
            softPenalty = 20.0,
            constraintResults = listOf(
                result(
                    id = "no-overlap",
                    type = ConstraintType.HARD,
                    violations = listOf(
                        violation(
                            message = "Resource overlap",
                            penalty = 1.0
                        )
                    ),
                    weight = 1000.0
                ),
                result(
                    id = "preferred-time-slot",
                    type = ConstraintType.SOFT,
                    violations = listOf(
                        violation(
                            message = "Preference not satisfied",
                            penalty = 2.0
                        )
                    ),
                    weight = 10.0
                )
            )
        )

        assertFalse(evaluation.feasible)
        assertEquals(1000.0, evaluation.hardPenalty)
        assertEquals(20.0, evaluation.softPenalty)
        assertEquals(1020.0, evaluation.totalPenalty)
        assertEquals(1020.0, evaluation.fitness)
    }

    private fun result(
        id: String,
        type: ConstraintType,
        violations: List<ConstraintViolation>,
        weight: Double
    ) = ConstraintResult(
        constraintId = id,
        type = type,
        violationDetails = violations,
        weight = weight
    )

    private fun violation(
        message: String,
        penalty: Double
    ) = ConstraintViolation(
        message = message,
        penalty = penalty
    )
}