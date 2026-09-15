package com.geneticplanner.domain.constraint

import com.geneticplanner.domain.Schedule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ConstraintTest {

    @Test
    fun `hard and soft constraints use the same interface`() {
        val hardConstraint: Constraint = TestConstraint(
            id = "hard-test",
            name = "Hard test constraint",
            type = ConstraintType.HARD,
            weight = 1000.0
        )

        val softConstraint: Constraint = TestConstraint(
            id = "soft-test",
            name = "Soft test constraint",
            type = ConstraintType.SOFT,
            weight = 5.0
        )

        val schedule = Schedule(
            planningProblemId = "problem-1",
            assignments = emptyList()
        )

        val hardResult = hardConstraint.evaluate(schedule)
        val softResult = softConstraint.evaluate(schedule)

        assertEquals(ConstraintType.HARD, hardResult.type)
        assertEquals(ConstraintType.SOFT, softResult.type)

        assertEquals(1000.0, hardResult.weightedPenalty)
        assertEquals(5.0, softResult.weightedPenalty)
    }

    private data class TestConstraint(
        override val id: String,
        override val name: String,
        override val type: ConstraintType,
        override val weight: Double
    ) : Constraint {

        override fun evaluate(
            schedule: Schedule
        ): ConstraintResult =
            ConstraintResult(
                constraintId = id,
                type = type,
                violationDetails = listOf(
                    ConstraintViolation(
                        message = "Test violation",
                        penalty = 1.0
                    )
                ),
                weight = weight
            )
    }
}