package com.geneticplanner.domain.constraint.hard

import com.geneticplanner.domain.Activity
import com.geneticplanner.domain.Assignment
import com.geneticplanner.domain.Resource
import com.geneticplanner.domain.ResourceRequirement
import com.geneticplanner.domain.Schedule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RequiredResourceConstraintTest {

    @Test
    fun `assignment satisfying resource requirement produces no violation`() {
        val activity = activity(
            quantity = 2,
            candidateResourceIds = setOf(
                "worker-1",
                "worker-2"
            )
        )

        val constraint = RequiredResourceConstraint(
            id = "required-resource",
            name = "Required resources",
            weight = 1000.0,
            activities = listOf(activity),
            resources = workers()
        )

        val schedule = schedule(
            resourceIds = listOf(
                "worker-1",
                "worker-2"
            )
        )

        val result = constraint.evaluate(schedule)

        assertEquals(0, result.violations)
        assertEquals(0.0, result.rawPenalty)
        assertEquals(0.0, result.weightedPenalty)
        assertTrue(result.violationDetails.isEmpty())
    }

    @Test
    fun `insufficient number of resources produces violation`() {
        val activity = activity(
            quantity = 2,
            candidateResourceIds = setOf(
                "worker-1",
                "worker-2"
            )
        )

        val constraint = RequiredResourceConstraint(
            id = "required-resource",
            name = "Required resources",
            weight = 1000.0,
            activities = listOf(activity),
            resources = workers()
        )

        val schedule = schedule(
            resourceIds = listOf("worker-1")
        )

        val result = constraint.evaluate(schedule)

        assertEquals(1, result.violations)
        assertEquals(1.0, result.rawPenalty)
        assertEquals(1000.0, result.weightedPenalty)

        assertTrue(
            result.violationDetails.first()
                .relatedEntityIds
                .containsAll(
                    listOf(
                        "activity-1",
                        "worker-requirement",
                        "worker-1"
                    )
                )
        )
    }

    @Test
    fun `too many resources do not satisfy quantity`() {
        val activity = activity(
            quantity = 1,
            candidateResourceIds = null
        )

        val constraint = RequiredResourceConstraint(
            id = "required-resource",
            name = "Required resources",
            weight = 1000.0,
            activities = listOf(activity),
            resources = workers()
        )

        val schedule = schedule(
            resourceIds = listOf(
                "worker-1",
                "worker-2"
            )
        )

        val result = constraint.evaluate(schedule)

        assertEquals(1, result.violations)
        assertEquals(1.0, result.rawPenalty)
        assertEquals(1000.0, result.weightedPenalty)
    }

    @Test
    fun `same resource assigned multiple times does not satisfy quantity`() {
        val activity = activity(
            quantity = 2,
            candidateResourceIds = setOf(
                "worker-1",
                "worker-2"
            )
        )

        val constraint = RequiredResourceConstraint(
            id = "required-resource",
            name = "Required resources",
            weight = 1000.0,
            activities = listOf(activity),
            resources = workers()
        )

        val schedule = schedule(
            resourceIds = listOf(
                "worker-1",
                "worker-1"
            )
        )

        val result = constraint.evaluate(schedule)

        assertEquals(1, result.violations)
        assertEquals(1.0, result.rawPenalty)
        assertEquals(1000.0, result.weightedPenalty)

        assertTrue(
            result.violationDetails.first()
                .relatedEntityIds
                .containsAll(
                    listOf(
                        "activity-1",
                        "worker-requirement",
                        "worker-1"
                    )
                )
        )
    }

    @Test
    fun `resource with wrong type produces violation`() {
        val activity = activity(
            quantity = 1,
            candidateResourceIds = null
        )

        val wrongResource = Resource(
            id = "machine-1",
            name = "Machine 1",
            typeId = "machine"
        )

        val constraint = RequiredResourceConstraint(
            id = "required-resource",
            name = "Required resources",
            weight = 1000.0,
            activities = listOf(activity),
            resources = workers() + wrongResource
        )

        val schedule = schedule(
            resourceIds = listOf("machine-1")
        )

        val result = constraint.evaluate(schedule)

        assertEquals(1, result.violations)
        assertEquals(1.0, result.rawPenalty)
        assertEquals(1000.0, result.weightedPenalty)

        assertTrue(
            result.violationDetails.first()
                .relatedEntityIds
                .containsAll(
                    listOf(
                        "activity-1",
                        "worker-requirement",
                        "machine-1"
                    )
                )
        )
    }

    @Test
    fun `resource outside candidate set produces violation`() {
        val activity = activity(
            quantity = 1,
            candidateResourceIds = setOf("worker-1")
        )

        val constraint = RequiredResourceConstraint(
            id = "required-resource",
            name = "Required resources",
            weight = 1000.0,
            activities = listOf(activity),
            resources = workers()
        )

        val schedule = schedule(
            resourceIds = listOf("worker-2")
        )

        val result = constraint.evaluate(schedule)

        assertEquals(1, result.violations)
        assertEquals(1.0, result.rawPenalty)
        assertEquals(1000.0, result.weightedPenalty)

        assertTrue(
            result.violationDetails.first()
                .relatedEntityIds
                .containsAll(
                    listOf(
                        "activity-1",
                        "worker-requirement",
                        "worker-2"
                    )
                )
        )
    }

    @Test
    fun `candidate resources are unrestricted when candidate set is null`() {
        val activity = activity(
            quantity = 1,
            candidateResourceIds = null
        )

        val constraint = RequiredResourceConstraint(
            id = "required-resource",
            name = "Required resources",
            weight = 1000.0,
            activities = listOf(activity),
            resources = workers()
        )

        val schedule = schedule(
            resourceIds = listOf("worker-2")
        )

        val result = constraint.evaluate(schedule)

        assertEquals(0, result.violations)
        assertEquals(0.0, result.rawPenalty)
        assertEquals(0.0, result.weightedPenalty)
    }

    @Test
    fun `missing resource assignment produces violation`() {
        val activity = activity(
            quantity = 1,
            candidateResourceIds = null
        )

        val constraint = RequiredResourceConstraint(
            id = "required-resource",
            name = "Required resources",
            weight = 1000.0,
            activities = listOf(activity),
            resources = workers()
        )

        val schedule = Schedule(
            planningProblemId = "problem-1",
            assignments = listOf(
                Assignment(
                    activityId = "activity-1",
                    timeSlotId = "slot-1",
                    resourceAssignments = emptyMap()
                )
            )
        )

        val result = constraint.evaluate(schedule)

        assertEquals(1, result.violations)
        assertEquals(1.0, result.rawPenalty)
        assertEquals(1000.0, result.weightedPenalty)

        assertTrue(
            result.violationDetails.first()
                .relatedEntityIds
                .containsAll(
                    listOf(
                        "activity-1",
                        "worker-requirement"
                    )
                )
        )
    }

    private fun activity(
        quantity: Int,
        candidateResourceIds: Set<String>?
    ) = Activity(
        id = "activity-1",
        name = "Activity 1",
        resourceRequirements = listOf(
            ResourceRequirement(
                id = "worker-requirement",
                resourceTypeId = "worker",
                quantity = quantity,
                candidateResourceIds = candidateResourceIds
            )
        )
    )

    private fun workers() = listOf(
        Resource(
            id = "worker-1",
            name = "Worker 1",
            typeId = "worker"
        ),
        Resource(
            id = "worker-2",
            name = "Worker 2",
            typeId = "worker"
        )
    )

    private fun schedule(
        resourceIds: List<String>
    ) = Schedule(
        planningProblemId = "problem-1",
        assignments = listOf(
            Assignment(
                activityId = "activity-1",
                timeSlotId = "slot-1",
                resourceAssignments = mapOf(
                    "worker-requirement" to resourceIds
                )
            )
        )
    )
}