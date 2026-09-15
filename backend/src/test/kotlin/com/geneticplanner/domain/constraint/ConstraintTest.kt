package com.geneticplanner.domain.constraint

import com.geneticplanner.domain.Schedule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ConstraintTest {

    @Test
    fun `hard and soft constraints use the same interface`() {
        val hardConstraint: Constraint =
            TestConstraint(
                id = "hard-test",
                type = ConstraintType.HARD,
                weight = 1000.0
            )

        val softConstraint: Constraint =
            TestConstraint(
                id = "soft-test",
                type = ConstraintType.SOFT,
                weight = 5.0
            )

        val schedule = Schedule(
            planningProblemId = "problem-1",
            assignments = emptyList()
        )

        assertEquals(
            ConstraintType.HARD,
            hardConstraint.evaluate(schedule).type
        )

        assertEquals(
            ConstraintType.SOFT,
            softConstraint.evaluate(schedule).type
        )
    }

    private data class TestConstraint(
        override val id: String,
        override val type: ConstraintType,
        override val weight: Double,
        override val name: String = "Test constraint"
    ) : Constraint {

        override fun evaluate(schedule: Schedule): ConstraintResult =
            ConstraintResult(
                constraintId = id,
                type = type,
                violations = 1,
                rawPenalty = 1.0,
                weight = weight
            )
    }
}