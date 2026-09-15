package com.geneticplanner.domain.constraint.hard

import com.geneticplanner.domain.Assignment
import com.geneticplanner.domain.Schedule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AvailabilityConstraintTest {

    @Test
    fun `resource assigned to available time slot produces no violation`() {
        val constraint = AvailabilityConstraint(
            id = "availability",
            name = "Resource availability",
            weight = 1000.0,
            availableTimeSlotIdsByResourceId = mapOf(
                "resource-1" to setOf("slot-1", "slot-2")
            )
        )

        val result = constraint.evaluate(
            schedule(
                resourceId = "resource-1",
                timeSlotId = "slot-1"
            )
        )

        assertEquals(0, result.violations)
        assertEquals(0.0, result.weightedPenalty)
    }

    @Test
    fun `resource assigned to unavailable time slot produces violation`() {
        val constraint = AvailabilityConstraint(
            id = "availability",
            name = "Resource availability",
            weight = 1000.0,
            availableTimeSlotIdsByResourceId = mapOf(
                "resource-1" to setOf("slot-2")
            )
        )

        val result = constraint.evaluate(
            schedule(
                resourceId = "resource-1",
                timeSlotId = "slot-1"
            )
        )

        assertEquals(1, result.violations)
        assertEquals(1.0, result.rawPenalty)
        assertEquals(1000.0, result.weightedPenalty)

        assertTrue(
            result.violationDetails.first()
                .relatedEntityIds
                .containsAll(
                    listOf(
                        "resource-1",
                        "activity-1",
                        "slot-1"
                    )
                )
        )
    }

    @Test
    fun `resource absent from availability configuration is unrestricted`() {
        val constraint = AvailabilityConstraint(
            id = "availability",
            name = "Resource availability",
            weight = 1000.0,
            availableTimeSlotIdsByResourceId = emptyMap()
        )

        val result = constraint.evaluate(
            schedule(
                resourceId = "resource-1",
                timeSlotId = "slot-1"
            )
        )

        assertEquals(0, result.violations)
    }

    @Test
    fun `resource with empty availability produces violation`() {
        val constraint = AvailabilityConstraint(
            id = "availability",
            name = "Resource availability",
            weight = 1000.0,
            availableTimeSlotIdsByResourceId = mapOf(
                "resource-1" to emptySet()
            )
        )

        val result = constraint.evaluate(
            schedule(
                resourceId = "resource-1",
                timeSlotId = "slot-1"
            )
        )

        assertEquals(1, result.violations)
    }

    private fun schedule(
        resourceId: String,
        timeSlotId: String
    ) = Schedule(
        planningProblemId = "problem-1",
        assignments = listOf(
            Assignment(
                activityId = "activity-1",
                timeSlotId = timeSlotId,
                resourceAssignments = mapOf(
                    "resource-requirement" to listOf(resourceId)
                )
            )
        )
    )
}