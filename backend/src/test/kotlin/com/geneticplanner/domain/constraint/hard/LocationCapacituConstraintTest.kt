package com.geneticplanner.domain.constraint.hard

import com.geneticplanner.domain.Activity
import com.geneticplanner.domain.Assignment
import com.geneticplanner.domain.Location
import com.geneticplanner.domain.Schedule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LocationCapacityConstraintTest {

    @Test
    fun `location with sufficient capacity produces no violation`() {
        val constraint = LocationCapacityConstraint(
            id = "location-capacity",
            name = "Location capacity",
            weight = 1000.0,
            activities = listOf(
                activity(requiredCapacity = 20)
            ),
            locations = listOf(
                location(capacity = 30)
            )
        )

        val result = constraint.evaluate(schedule())

        assertEquals(0, result.violations)
        assertEquals(0.0, result.rawPenalty)
        assertEquals(0.0, result.weightedPenalty)
    }

    @Test
    fun `location with insufficient capacity produces violation`() {
        val constraint = LocationCapacityConstraint(
            id = "location-capacity",
            name = "Location capacity",
            weight = 1000.0,
            activities = listOf(
                activity(requiredCapacity = 30)
            ),
            locations = listOf(
                location(capacity = 20)
            )
        )

        val result = constraint.evaluate(schedule())

        assertEquals(1, result.violations)
        assertEquals(10.0, result.rawPenalty)
        assertEquals(10000.0, result.weightedPenalty)

        assertTrue(
            result.violationDetails.first()
                .relatedEntityIds
                .containsAll(
                    listOf(
                        "activity-1",
                        "location-1"
                    )
                )
        )
    }

    @Test
    fun `activity without required capacity produces no violation`() {
        val constraint = LocationCapacityConstraint(
            id = "location-capacity",
            name = "Location capacity",
            weight = 1000.0,
            activities = listOf(
                activity(requiredCapacity = null)
            ),
            locations = listOf(
                location(capacity = 1)
            )
        )

        val result = constraint.evaluate(schedule())

        assertEquals(0, result.violations)
    }

    @Test
    fun `assignment without location produces no capacity violation`() {
        val constraint = LocationCapacityConstraint(
            id = "location-capacity",
            name = "Location capacity",
            weight = 1000.0,
            activities = listOf(
                activity(requiredCapacity = 30)
            ),
            locations = listOf(
                location(capacity = 20)
            )
        )

        val scheduleWithoutLocation = Schedule(
            planningProblemId = "problem-1",
            assignments = listOf(
                Assignment(
                    activityId = "activity-1",
                    timeSlotId = "slot-1",
                    resourceAssignments = emptyMap(),
                    locationId = null
                )
            )
        )

        val result = constraint.evaluate(scheduleWithoutLocation)

        assertEquals(0, result.violations)
    }

    @Test
    fun `location without defined capacity produces no capacity violation`() {
        val constraint = LocationCapacityConstraint(
            id = "location-capacity",
            name = "Location capacity",
            weight = 1000.0,
            activities = listOf(
                activity(requiredCapacity = 30)
            ),
            locations = listOf(
                location(capacity = null)
            )
        )

        val result = constraint.evaluate(schedule())

        assertEquals(0, result.violations)
    }

    private fun activity(
        requiredCapacity: Int?
    ) = Activity(
        id = "activity-1",
        name = "Activity 1",
        resourceRequirements = emptyList(),
        requiredLocationCapacity = requiredCapacity
    )

    private fun location(
        capacity: Int?
    ) = Location(
        id = "location-1",
        name = "Location 1",
        capacity = capacity
    )

    private fun schedule() = Schedule(
        planningProblemId = "problem-1",
        assignments = listOf(
            Assignment(
                activityId = "activity-1",
                timeSlotId = "slot-1",
                resourceAssignments = emptyMap(),
                locationId = "location-1"
            )
        )
    )
}