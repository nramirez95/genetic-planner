package com.geneticplanner.domain.constraint.soft

import com.geneticplanner.domain.Assignment
import com.geneticplanner.domain.Schedule
import com.geneticplanner.domain.constraint.ConstraintType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BalancedWorkloadConstraintTest {

    @Test
    fun `balanced workload produces no violation`() {
        val constraint = constraint(
            allowedDifference = 0,
            weight = 5.0
        )

        val schedule = schedule(
            "resource-1" to 2,
            "resource-2" to 2,
            "resource-3" to 2
        )

        val result = constraint.evaluate(schedule)

        assertEquals(ConstraintType.SOFT, result.type)
        assertEquals(0, result.violations)
        assertEquals(0.0, result.rawPenalty)
        assertEquals(0.0, result.weightedPenalty)
        assertTrue(result.violationDetails.isEmpty())
    }

    @Test
    fun `workload difference within allowed tolerance produces no violation`() {
        val constraint = constraint(
            allowedDifference = 1,
            weight = 5.0
        )

        val schedule = schedule(
            "resource-1" to 3,
            "resource-2" to 2,
            "resource-3" to 2
        )

        val result = constraint.evaluate(schedule)

        assertEquals(0, result.violations)
        assertEquals(0.0, result.rawPenalty)
        assertEquals(0.0, result.weightedPenalty)
    }

    @Test
    fun `workload above allowed difference produces proportional penalty`() {
        val constraint = constraint(
            allowedDifference = 1,
            weight = 5.0
        )

        val schedule = schedule(
            "resource-1" to 4,
            "resource-2" to 2,
            "resource-3" to 2
        )

        val result = constraint.evaluate(schedule)

        // max = 4
        // min = 2
        // difference = 2
        // allowed = 1
        // excess = 1

        assertEquals(ConstraintType.SOFT, result.type)
        assertEquals(1, result.violations)
        assertEquals(1.0, result.rawPenalty)
        assertEquals(5.0, result.weightedPenalty)
    }

    @Test
    fun `larger workload imbalance increases penalty`() {
        val constraint = constraint(
            allowedDifference = 1,
            weight = 5.0
        )

        val schedule = schedule(
            "resource-1" to 5,
            "resource-2" to 3,
            "resource-3" to 2
        )

        val result = constraint.evaluate(schedule)

        // max = 5
        // min = 2
        // difference = 3
        // allowed = 1
        // excess = 2

        assertEquals(1, result.violations)
        assertEquals(2.0, result.rawPenalty)
        assertEquals(10.0, result.weightedPenalty)
    }

    @Test
    fun `resource with zero assignments participates in workload balance`() {
        val constraint = constraint(
            allowedDifference = 1,
            weight = 5.0
        )

        val schedule = schedule(
            "resource-1" to 3,
            "resource-2" to 2
        )

        val result = constraint.evaluate(schedule)

        // resource-3 has zero assignments.
        //
        // resource-1 = 3
        // resource-2 = 2
        // resource-3 = 0
        //
        // difference = 3
        // allowed = 1
        // excess = 2

        assertEquals(1, result.violations)
        assertEquals(2.0, result.rawPenalty)
        assertEquals(10.0, result.weightedPenalty)

        assertTrue(
            result.violationDetails.first()
                .relatedEntityIds
                .contains("resource-3")
        )
    }

    @Test
    fun `weight changes workload penalty impact`() {
        val schedule = schedule(
            "resource-1" to 5,
            "resource-2" to 3,
            "resource-3" to 2
        )

        val lowWeightConstraint = constraint(
            allowedDifference = 1,
            weight = 2.0
        )

        val highWeightConstraint = constraint(
            allowedDifference = 1,
            weight = 10.0
        )

        val lowWeightResult =
            lowWeightConstraint.evaluate(schedule)

        val highWeightResult =
            highWeightConstraint.evaluate(schedule)

        assertEquals(
            lowWeightResult.violations,
            highWeightResult.violations
        )

        assertEquals(
            lowWeightResult.rawPenalty,
            highWeightResult.rawPenalty
        )

        assertEquals(2.0, lowWeightResult.rawPenalty)
        assertEquals(4.0, lowWeightResult.weightedPenalty)
        assertEquals(20.0, highWeightResult.weightedPenalty)
    }

    @Test
    fun `single resource produces no workload balance violation`() {
        val constraint = BalancedWorkloadConstraint(
            id = "balanced-workload",
            name = "Balanced workload",
            weight = 5.0,
            resourceIds = setOf("resource-1"),
            allowedDifference = 0
        )

        val schedule = schedule(
            "resource-1" to 5
        )

        val result = constraint.evaluate(schedule)

        assertEquals(0, result.violations)
        assertEquals(0.0, result.rawPenalty)
        assertEquals(0.0, result.weightedPenalty)
    }

    private fun constraint(
        allowedDifference: Int,
        weight: Double
    ) = BalancedWorkloadConstraint(
        id = "balanced-workload",
        name = "Balanced workload",
        weight = weight,
        resourceIds = setOf(
            "resource-1",
            "resource-2",
            "resource-3"
        ),
        allowedDifference = allowedDifference
    )

    private fun schedule(
        vararg workloads: Pair<String, Int>
    ): Schedule {
        var activityIndex = 0

        val assignments = workloads.flatMap { (resourceId, count) ->
            (1..count).map {
                activityIndex++

                Assignment(
                    activityId = "activity-$activityIndex",
                    timeSlotId = "slot-$activityIndex",
                    resourceAssignments = mapOf(
                        "resource-requirement" to listOf(resourceId)
                    )
                )
            }
        }

        return Schedule(
            planningProblemId = "problem-1",
            assignments = assignments
        )
    }
}