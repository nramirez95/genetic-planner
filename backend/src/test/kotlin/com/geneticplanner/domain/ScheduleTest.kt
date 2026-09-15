package com.geneticplanner.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ScheduleTest {

    @Test
    fun `creates schedule with assignments`() {
        val assignment = Assignment(
            activityId = "math-session-1",
            timeSlotId = "slot-1",
            resourceAssignments = mapOf(
                "teacher-requirement" to listOf("teacher-1")
            ),
            locationId = "room-1"
        )

        val schedule = Schedule(
            planningProblemId = "problem-1",
            assignments = listOf(assignment)
        )

        assertEquals("problem-1", schedule.planningProblemId)
        assertEquals(1, schedule.assignments.size)
        assertEquals("math-session-1", schedule.assignments.first().activityId)
        assertEquals("slot-1", schedule.assignments.first().timeSlotId)
        assertEquals("room-1", schedule.assignments.first().locationId)
    }

    @Test
    fun `assigns multiple resources to a resource requirement`() {
        val assignment = Assignment(
            activityId = "activity-1",
            timeSlotId = "slot-1",
            resourceAssignments = mapOf(
                "assistant-requirement" to listOf(
                    "assistant-1",
                    "assistant-2"
                )
            )
        )

        val assignedResources =
            assignment.resourceAssignments["assistant-requirement"]

        assertEquals(
            listOf("assistant-1", "assistant-2"),
            assignedResources
        )
    }

    @Test
    fun `creates assignment without location`() {
        val assignment = Assignment(
            activityId = "online-session-1",
            timeSlotId = "slot-1",
            resourceAssignments = mapOf(
                "teacher-requirement" to listOf("teacher-1")
            )
        )

        assertNull(assignment.locationId)
    }
}