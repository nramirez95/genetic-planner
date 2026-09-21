package com.geneticplanner.domain.candidate

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AssignmentCandidateGenerationResultTest {

    @Test
    fun `result without empty activity options has no missing options`() {
        val result = AssignmentCandidateGenerationResult(
            candidatesByActivity = listOf(
                ActivityCandidateOptions(
                    activityId = "activity-1",
                    options = listOf(
                        option(
                            activityId = "activity-1",
                            timeSlotId = "slot-1"
                        )
                    )
                ),
                ActivityCandidateOptions(
                    activityId = "activity-2",
                    options = listOf(
                        option(
                            activityId = "activity-2",
                            timeSlotId = "slot-2"
                        )
                    )
                )
            )
        )

        assertTrue(
            result.activitiesWithoutOptions.isEmpty()
        )

        assertFalse(result.hasMissingOptions)
    }

    @Test
    fun `result reports activity without options`() {
        val result = AssignmentCandidateGenerationResult(
            candidatesByActivity = listOf(
                ActivityCandidateOptions(
                    activityId = "activity-1",
                    options = listOf(
                        option(
                            activityId = "activity-1",
                            timeSlotId = "slot-1"
                        )
                    )
                ),
                ActivityCandidateOptions(
                    activityId = "activity-2",
                    options = emptyList()
                )
            )
        )

        assertEquals(
            listOf("activity-2"),
            result.activitiesWithoutOptions
        )

        assertTrue(result.hasMissingOptions)
    }

    @Test
    fun `result reports all activities without options`() {
        val result = AssignmentCandidateGenerationResult(
            candidatesByActivity = listOf(
                ActivityCandidateOptions(
                    activityId = "activity-1",
                    options = emptyList()
                ),
                ActivityCandidateOptions(
                    activityId = "activity-2",
                    options = listOf(
                        option(
                            activityId = "activity-2",
                            timeSlotId = "slot-1"
                        )
                    )
                ),
                ActivityCandidateOptions(
                    activityId = "activity-3",
                    options = emptyList()
                )
            )
        )

        assertEquals(
            listOf(
                "activity-1",
                "activity-3"
            ),
            result.activitiesWithoutOptions
        )

        assertTrue(result.hasMissingOptions)
    }

    private fun option(
        activityId: String,
        timeSlotId: String
    ) = AssignmentOption(
        activityId = activityId,
        timeSlotId = timeSlotId,
        resourceAssignments = emptyMap(),
        locationId = null
    )
}