package com.geneticplanner.domain.constraint.soft

import com.geneticplanner.domain.Assignment
import com.geneticplanner.domain.Schedule
import com.geneticplanner.domain.constraint.ConstraintType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PreferredTimeSlotConstraintTest {

    @Test
    fun `all activities in preferred time slots produce no violation`() {
        val constraint = PreferredTimeSlotConstraint(
            id = "preferred-time-slot",
            name = "Preferred time slots",
            weight = 5.0,
            preferredTimeSlotIdsByActivityId = mapOf(
                "activity-1" to setOf("slot-1"),
                "activity-2" to setOf("slot-2"),
                "activity-3" to setOf("slot-3")
            )
        )

        val schedule = schedule(
            "activity-1" to "slot-1",
            "activity-2" to "slot-2",
            "activity-3" to "slot-3"
        )

        val result = constraint.evaluate(schedule)

        assertEquals(ConstraintType.SOFT, result.type)
        assertEquals(0, result.violations)
        assertEquals(0.0, result.rawPenalty)
        assertEquals(0.0, result.weightedPenalty)
        assertTrue(result.violationDetails.isEmpty())
    }

    @Test
    fun `some activities outside preferred time slots produce proportional penalty`() {
        val constraint = PreferredTimeSlotConstraint(
            id = "preferred-time-slot",
            name = "Preferred time slots",
            weight = 5.0,
            preferredTimeSlotIdsByActivityId = mapOf(
                "activity-1" to setOf("slot-1"),
                "activity-2" to setOf("slot-2"),
                "activity-3" to setOf("slot-3")
            )
        )

        val schedule = schedule(
            "activity-1" to "slot-1",
            "activity-2" to "slot-4",
            "activity-3" to "slot-3"
        )

        val result = constraint.evaluate(schedule)

        assertEquals(ConstraintType.SOFT, result.type)
        assertEquals(1, result.violations)
        assertEquals(1.0, result.rawPenalty)
        assertEquals(5.0, result.weightedPenalty)

        assertTrue(
            result.violationDetails.first()
                .relatedEntityIds
                .containsAll(
                    listOf(
                        "activity-2",
                        "slot-4"
                    )
                )
        )
    }

    @Test
    fun `all activities outside preferred time slots produce proportional penalty`() {
        val constraint = PreferredTimeSlotConstraint(
            id = "preferred-time-slot",
            name = "Preferred time slots",
            weight = 5.0,
            preferredTimeSlotIdsByActivityId = mapOf(
                "activity-1" to setOf("slot-1"),
                "activity-2" to setOf("slot-2"),
                "activity-3" to setOf("slot-3")
            )
        )

        val schedule = schedule(
            "activity-1" to "slot-4",
            "activity-2" to "slot-4",
            "activity-3" to "slot-4"
        )

        val result = constraint.evaluate(schedule)

        assertEquals(3, result.violations)
        assertEquals(3.0, result.rawPenalty)
        assertEquals(15.0, result.weightedPenalty)
    }

    @Test
    fun `activity without preferred time slot configuration produces no violation`() {
        val constraint = PreferredTimeSlotConstraint(
            id = "preferred-time-slot",
            name = "Preferred time slots",
            weight = 5.0,
            preferredTimeSlotIdsByActivityId = mapOf(
                "activity-1" to setOf("slot-1")
            )
        )

        val schedule = schedule(
            "activity-1" to "slot-1",
            "activity-2" to "slot-99"
        )

        val result = constraint.evaluate(schedule)

        assertEquals(0, result.violations)
        assertEquals(0.0, result.rawPenalty)
    }

    @Test
    fun `weight changes impact without changing number of violations`() {
        val preferences = mapOf(
            "activity-1" to setOf("slot-1"),
            "activity-2" to setOf("slot-2")
        )

        val schedule = schedule(
            "activity-1" to "slot-3",
            "activity-2" to "slot-4"
        )

        val lowWeightConstraint = PreferredTimeSlotConstraint(
            id = "preferred-low",
            name = "Preferred time slots",
            weight = 2.0,
            preferredTimeSlotIdsByActivityId = preferences
        )

        val highWeightConstraint = PreferredTimeSlotConstraint(
            id = "preferred-high",
            name = "Preferred time slots",
            weight = 10.0,
            preferredTimeSlotIdsByActivityId = preferences
        )

        val lowWeightResult =
            lowWeightConstraint.evaluate(schedule)

        val highWeightResult =
            highWeightConstraint.evaluate(schedule)

        assertEquals(2, lowWeightResult.violations)
        assertEquals(2, highWeightResult.violations)

        assertEquals(2.0, lowWeightResult.rawPenalty)
        assertEquals(2.0, highWeightResult.rawPenalty)

        assertEquals(4.0, lowWeightResult.weightedPenalty)
        assertEquals(20.0, highWeightResult.weightedPenalty)
    }

    private fun schedule(
        vararg assignments: Pair<String, String>
    ) = Schedule(
        planningProblemId = "problem-1",
        assignments = assignments.map { (activityId, timeSlotId) ->
            Assignment(
                activityId = activityId,
                timeSlotId = timeSlotId,
                resourceAssignments = emptyMap()
            )
        }
    )
}