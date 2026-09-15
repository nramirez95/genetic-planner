package com.geneticplanner.domain.constraint.hard

import com.geneticplanner.domain.Assignment
import com.geneticplanner.domain.Schedule
import com.geneticplanner.domain.TimeSlot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class NoOverlapConstraintTest {

    @Test
    fun `same resource in non overlapping time slots produces no violation`() {
        val constraint = NoOverlapConstraint(
            id = "no-overlap",
            name = "No resource overlap",
            weight = 1000.0,
            timeSlots = listOf(
                slot("slot-1", 9, 0, 10, 0),
                slot("slot-2", 10, 0, 11, 0)
            )
        )

        val schedule = Schedule(
            planningProblemId = "problem-1",
            assignments = listOf(
                assignment(
                    activityId = "activity-1",
                    timeSlotId = "slot-1",
                    resourceId = "resource-1"
                ),
                assignment(
                    activityId = "activity-2",
                    timeSlotId = "slot-2",
                    resourceId = "resource-1"
                )
            )
        )

        val result = constraint.evaluate(schedule)

        assertEquals(0, result.violations)
        assertEquals(0.0, result.rawPenalty)
        assertEquals(0.0, result.weightedPenalty)
    }

    @Test
    fun `same resource in overlapping time slots produces violation`() {
        val constraint = NoOverlapConstraint(
            id = "no-overlap",
            name = "No resource overlap",
            weight = 1000.0,
            timeSlots = listOf(
                slot("slot-1", 9, 0, 10, 30),
                slot("slot-2", 10, 0, 11, 0)
            )
        )

        val schedule = Schedule(
            planningProblemId = "problem-1",
            assignments = listOf(
                assignment(
                    activityId = "activity-1",
                    timeSlotId = "slot-1",
                    resourceId = "resource-1"
                ),
                assignment(
                    activityId = "activity-2",
                    timeSlotId = "slot-2",
                    resourceId = "resource-1"
                )
            )
        )

        val result = constraint.evaluate(schedule)

        assertEquals(1, result.violations)
        assertEquals(1.0, result.rawPenalty)
        assertEquals(1000.0, result.weightedPenalty)

        val violation = result.violationDetails.first()

        assertTrue(
            violation.relatedEntityIds.containsAll(
                listOf(
                    "resource-1",
                    "activity-1",
                    "activity-2"
                )
            )
        )
    }

    @Test
    fun `different resources in overlapping time slots produce no violation`() {
        val constraint = NoOverlapConstraint(
            id = "no-overlap",
            name = "No resource overlap",
            weight = 1000.0,
            timeSlots = listOf(
                slot("slot-1", 9, 0, 10, 30),
                slot("slot-2", 10, 0, 11, 0)
            )
        )

        val schedule = Schedule(
            planningProblemId = "problem-1",
            assignments = listOf(
                assignment(
                    "activity-1",
                    "slot-1",
                    "resource-1"
                ),
                assignment(
                    "activity-2",
                    "slot-2",
                    "resource-2"
                )
            )
        )

        val result = constraint.evaluate(schedule)

        assertEquals(0, result.violations)
    }

    @Test
    fun `adjacent time slots do not overlap`() {
        val constraint = NoOverlapConstraint(
            id = "no-overlap",
            name = "No resource overlap",
            weight = 1000.0,
            timeSlots = listOf(
                slot("slot-1", 9, 0, 10, 0),
                slot("slot-2", 10, 0, 11, 0)
            )
        )

        val schedule = Schedule(
            planningProblemId = "problem-1",
            assignments = listOf(
                assignment("activity-1", "slot-1", "resource-1"),
                assignment("activity-2", "slot-2", "resource-1")
            )
        )

        val result = constraint.evaluate(schedule)

        assertEquals(0, result.violations)
    }

    private fun assignment(
        activityId: String,
        timeSlotId: String,
        resourceId: String
    ) = Assignment(
        activityId = activityId,
        timeSlotId = timeSlotId,
        resourceAssignments = mapOf(
            "resource-requirement" to listOf(resourceId)
        )
    )

    private fun slot(
        id: String,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int
    ) = TimeSlot(
        id = id,
        start = LocalDateTime.of(
            2026,
            9,
            21,
            startHour,
            startMinute
        ),
        end = LocalDateTime.of(
            2026,
            9,
            21,
            endHour,
            endMinute
        )
    )
}