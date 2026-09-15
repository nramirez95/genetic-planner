package com.geneticplanner.domain.constraint

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ConstraintResultTest {

    @Test
    fun `weighted penalty is raw penalty multiplied by weight`() {
        val result = ConstraintResult(
            constraintId = "no-overlap",
            type = ConstraintType.HARD,
            violationDetails = listOf(
                ConstraintViolation(
                    message = "Overlap 1",
                    penalty = 1.0
                ),
                ConstraintViolation(
                    message = "Overlap 2",
                    penalty = 1.0
                )
            ),
            weight = 1000.0
        )

        assertEquals(2, result.violations)
        assertEquals(2.0, result.rawPenalty)
        assertEquals(2000.0, result.weightedPenalty)
    }

    @Test
    fun `soft constraint uses same penalty calculation`() {
        val result = ConstraintResult(
            constraintId = "preferred-time-slot",
            type = ConstraintType.SOFT,
            violationDetails = listOf(
                ConstraintViolation(
                    message = "Preference violation 1",
                    penalty = 1.0
                ),
                ConstraintViolation(
                    message = "Preference violation 2",
                    penalty = 2.0
                )
            ),
            weight = 5.0
        )

        assertEquals(2, result.violations)
        assertEquals(3.0, result.rawPenalty)
        assertEquals(15.0, result.weightedPenalty)
    }

    @Test
    fun `violation count and raw penalty are independent`() {
        val result = ConstraintResult(
            constraintId = "maximum-assignments",
            type = ConstraintType.SOFT,
            violationDetails = listOf(
                ConstraintViolation(
                    message = "Maximum exceeded by three assignments",
                    penalty = 3.0
                )
            ),
            weight = 10.0
        )

        assertEquals(1, result.violations)
        assertEquals(3.0, result.rawPenalty)
        assertEquals(30.0, result.weightedPenalty)
    }

    @Test
    fun `result without violations has zero penalties`() {
        val result = ConstraintResult(
            constraintId = "no-overlap",
            type = ConstraintType.HARD,
            violationDetails = emptyList(),
            weight = 1000.0
        )

        assertEquals(0, result.violations)
        assertEquals(0.0, result.rawPenalty)
        assertEquals(0.0, result.weightedPenalty)
        assertTrue(result.violationDetails.isEmpty())
    }

    @Test
    fun `result exposes violation details`() {
        val violation = ConstraintViolation(
            message = "Resource 'resource-1' overlaps.",
            penalty = 1.0,
            relatedEntityIds = setOf(
                "resource-1",
                "activity-1",
                "activity-2"
            )
        )

        val result = ConstraintResult(
            constraintId = "no-overlap",
            type = ConstraintType.HARD,
            violationDetails = listOf(violation),
            weight = 1000.0
        )

        assertEquals(1, result.violationDetails.size)
        assertEquals(
            "Resource 'resource-1' overlaps.",
            result.violationDetails.first().message
        )
        assertTrue(
            result.violationDetails.first()
                .relatedEntityIds
                .contains("resource-1")
        )
    }
}