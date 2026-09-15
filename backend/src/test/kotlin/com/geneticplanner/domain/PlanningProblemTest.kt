package com.geneticplanner.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class PlanningProblemTest {

    @Test
    fun `creates a generic planning problem`() {
        val teacherType = ResourceType(
            id = "teacher",
            name = "Teacher"
        )

        val teacher = Resource(
            id = "teacher-1",
            name = "Teacher 1",
            typeId = teacherType.id
        )

        val timeSlot = TimeSlot(
            id = "slot-1",
            start = LocalDateTime.of(2026, 9, 21, 9, 0),
            end = LocalDateTime.of(2026, 9, 21, 10, 0),
            label = "Monday 09:00-10:00"
        )

        val classroom = Location(
            id = "room-1",
            name = "Room 1",
            type = "classroom",
            capacity = 30
        )

        val requirement = ResourceRequirement(
            id = "teacher-requirement",
            resourceTypeId = teacherType.id,
            quantity = 1,
            candidateResourceIds = setOf(teacher.id)
        )

        val activity = Activity(
            id = "math-session-1",
            name = "Mathematics 1A - Session 1",
            resourceRequirements = listOf(requirement),
            allowedTimeSlotIds = setOf(timeSlot.id),
            allowedLocationIds = setOf(classroom.id)
        )

        val problem = PlanningProblem(
            id = "academic-problem-1",
            name = "Academic Planning Example",
            templateType = "academic",
            planningHorizon = PlanningHorizon(
                start = LocalDateTime.of(2026, 9, 21, 8, 0),
                end = LocalDateTime.of(2026, 9, 25, 18, 0)
            ),
            resourceTypes = listOf(teacherType),
            resources = listOf(teacher),
            activities = listOf(activity),
            timeSlots = listOf(timeSlot),
            locations = listOf(classroom)
        )

        assertEquals("academic-problem-1", problem.id)
        assertEquals("academic", problem.templateType)
        assertEquals(1, problem.resourceTypes.size)
        assertEquals(1, problem.resources.size)
        assertEquals(1, problem.activities.size)
        assertEquals(1, problem.timeSlots.size)
        assertEquals(1, problem.locations.size)

        assertEquals(
            teacher.id,
            problem.activities.first()
                .resourceRequirements.first()
                .candidateResourceIds
                ?.first()
        )

        assertTrue(
            problem.activities.first()
                .allowedTimeSlotIds
                ?.contains(timeSlot.id) == true
        )
    }
}