package com.geneticplanner.domain.constraint.soft

import com.geneticplanner.domain.Assignment
import com.geneticplanner.domain.Schedule
import com.geneticplanner.domain.TimeSlot
import com.geneticplanner.domain.constraint.ConstraintType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class MaxConsecutiveConstraintTest {

    @Test
    fun `assignments within maximum consecutive limit produce no violation`() {
        val constraint = constraint(
            maxConsecutive = 3,
            weight = 10.0
        )

        val schedule = schedule(
            "resource-1",
            "slot-1",
            "slot-2",
            "slot-3"
        )

        val result = constraint.evaluate(schedule)

        assertEquals(ConstraintType.SOFT, result.type)
        assertEquals(0, result.violations)
        assertEquals(0.0, result.rawPenalty)
        assertEquals(0.0, result.weightedPenalty)
        assertTrue(result.violationDetails.isEmpty())
    }

    @Test
    fun `one assignment above maximum produces proportional penalty`() {
        val constraint = constraint(
            maxConsecutive = 3,
            weight = 10.0
        )

        val schedule = schedule(
            "resource-1",
            "slot-1",
            "slot-2",
            "slot-3",
            "slot-4"
        )

        val result = constraint.evaluate(schedule)

        assertEquals(ConstraintType.SOFT, result.type)
        assertEquals(1, result.violations)
        assertEquals(1.0, result.rawPenalty)
        assertEquals(10.0, result.weightedPenalty)

        assertTrue(
            result.violationDetails.first()
                .relatedEntityIds
                .contains("resource-1")
        )
    }

    @Test
    fun `multiple assignments above maximum increase raw penalty`() {
        val constraint = constraint(
            maxConsecutive = 3,
            weight = 10.0
        )

        val schedule = schedule(
            "resource-1",
            "slot-1",
            "slot-2",
            "slot-3",
            "slot-4",
            "slot-5"
        )

        val result = constraint.evaluate(schedule)

        assertEquals(1, result.violations)
        assertEquals(2.0, result.rawPenalty)
        assertEquals(20.0, result.weightedPenalty)
    }

    @Test
    fun `gap between time slots breaks consecutive sequence`() {
        val constraint = constraint(
            maxConsecutive = 3,
            weight = 10.0
        )

        val schedule = schedule(
            "resource-1",
            "slot-1",
            "slot-2",
            "slot-4",
            "slot-5"
        )

        val result = constraint.evaluate(schedule)

        assertEquals(0, result.violations)
        assertEquals(0.0, result.rawPenalty)
        assertEquals(0.0, result.weightedPenalty)
    }

    @Test
    fun `separate excessive sequences produce separate violations`() {
        val constraint = MaxConsecutiveConstraint(
            id = "max-consecutive",
            name = "Maximum consecutive assignments",
            weight = 10.0,
            maxConsecutive = 2,
            timeSlots = listOf(
                slot("morning-1", 8, 9),
                slot("morning-2", 9, 10),
                slot("morning-3", 10, 11),
                slot("afternoon-1", 15, 16),
                slot("afternoon-2", 16, 17),
                slot("afternoon-3", 17, 18)
            )
        )

        val schedule = schedule(
            "resource-1",
            "morning-1",
            "morning-2",
            "morning-3",
            "afternoon-1",
            "afternoon-2",
            "afternoon-3"
        )

        val result = constraint.evaluate(schedule)

        assertEquals(2, result.violations)
        assertEquals(2.0, result.rawPenalty)
        assertEquals(20.0, result.weightedPenalty)
    }

    @Test
    fun `different resources are evaluated independently`() {
        val constraint = constraint(
            maxConsecutive = 3,
            weight = 10.0
        )

        val schedule = Schedule(
            planningProblemId = "problem-1",
            assignments =
                assignments(
                    resourceId = "resource-1",
                    timeSlotIds = listOf(
                        "slot-1",
                        "slot-2",
                        "slot-3",
                        "slot-4"
                    )
                ) +
                        assignments(
                            resourceId = "resource-2",
                            timeSlotIds = listOf(
                                "slot-1",
                                "slot-2"
                            )
                        )
        )

        val result = constraint.evaluate(schedule)

        assertEquals(1, result.violations)
        assertEquals(1.0, result.rawPenalty)
        assertEquals(10.0, result.weightedPenalty)

        assertTrue(
            result.violationDetails.first()
                .relatedEntityIds
                .contains("resource-1")
        )
    }

    private fun constraint(
        maxConsecutive: Int,
        weight: Double
    ) = MaxConsecutiveConstraint(
        id = "max-consecutive",
        name = "Maximum consecutive assignments",
        weight = weight,
        maxConsecutive = maxConsecutive,
        timeSlots = standardTimeSlots()
    )

    private fun standardTimeSlots() = listOf(
        slot("slot-1", 8, 9),
        slot("slot-2", 9, 10),
        slot("slot-3", 10, 11),
        slot("slot-4", 11, 12),
        slot("slot-5", 12, 13)
    )

    private fun slot(
        id: String,
        startHour: Int,
        endHour: Int
    ) = TimeSlot(
        id = id,
        start = LocalDateTime.of(
            2026,
            9,
            21,
            startHour,
            0
        ),
        end = LocalDateTime.of(
            2026,
            9,
            21,
            endHour,
            0
        )
    )

    private fun schedule(
        resourceId: String,
        vararg timeSlotIds: String
    ) = Schedule(
        planningProblemId = "problem-1",
        assignments = assignments(
            resourceId = resourceId,
            timeSlotIds = timeSlotIds.toList()
        )
    )

    private fun assignments(
        resourceId: String,
        timeSlotIds: List<String>
    ) = timeSlotIds.mapIndexed { index, timeSlotId ->
        Assignment(
            activityId = "$resourceId-activity-$index",
            timeSlotId = timeSlotId,
            resourceAssignments = mapOf(
                "resource-requirement" to listOf(resourceId)
            )
        )
    }
}