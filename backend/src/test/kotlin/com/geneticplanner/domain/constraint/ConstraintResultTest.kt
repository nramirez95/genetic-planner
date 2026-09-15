package com.geneticplanner.domain.constraint

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ConstraintResultTest {

    @Test
    fun `weighted penalty is raw penalty multiplied by weight`() {
        val result = ConstraintResult(
            constraintId = "no-overlap",
            type = ConstraintType.HARD,
            violations = 2,
            rawPenalty = 2.0,
            weight = 1000.0
        )

        assertEquals(2000.0, result.weightedPenalty)
    }

    @Test
    fun `soft constraint result uses the same penalty calculation`() {
        val result = ConstraintResult(
            constraintId = "preferred-time-slot",
            type = ConstraintType.SOFT,
            violations = 3,
            rawPenalty = 3.0,
            weight = 5.0
        )

        assertEquals(15.0, result.weightedPenalty)
    }

    @Test
    fun `result exposes violation count and raw penalty independently`() {
        val result = ConstraintResult(
            constraintId = "maximum-assignments",
            type = ConstraintType.SOFT,
            violations = 1,
            rawPenalty = 3.0,
            weight = 10.0
        )

        assertEquals(1, result.violations)
        assertEquals(3.0, result.rawPenalty)
        assertEquals(30.0, result.weightedPenalty)
    }

    @Test
    fun `constraint with no violations has zero penalty`() {
        val result = ConstraintResult(
            constraintId = "no-overlap",
            type = ConstraintType.HARD,
            violations = 0,
            rawPenalty = 0.0,
            weight = 1000.0
        )

        assertEquals(0, result.violations)
        assertEquals(0.0, result.rawPenalty)
        assertEquals(0.0, result.weightedPenalty)
    }
}