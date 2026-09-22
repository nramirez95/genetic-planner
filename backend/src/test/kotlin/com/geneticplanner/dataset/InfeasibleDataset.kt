package com.geneticplanner.dataset

import com.geneticplanner.domain.Activity
import com.geneticplanner.domain.PlanningHorizon
import com.geneticplanner.domain.PlanningProblem
import com.geneticplanner.domain.Resource
import com.geneticplanner.domain.ResourceRequirement
import com.geneticplanner.domain.ResourceType
import com.geneticplanner.domain.TimeSlot
import com.geneticplanner.domain.constraint.hard.NoOverlapConstraint
import java.time.LocalDateTime

object InfeasibleDataset {

    fun create(): PlanningProblem {
        val slot =
            TimeSlot(
                id = "slot-1",
                start = dateTime(8),
                end = dateTime(9)
            )

        return PlanningProblem(
            id = "dataset-infeasible",
            name = "Infeasible dataset",
            planningHorizon = PlanningHorizon(
                start = dateTime(8),
                end = dateTime(10)
            ),
            resourceTypes = listOf(
                ResourceType(
                    id = "worker",
                    name = "Worker"
                )
            ),
            resources = listOf(
                Resource(
                    id = "worker-1",
                    name = "Worker 1",
                    typeId = "worker"
                )
            ),
            activities = listOf(
                activity(
                    id = "activity-1",
                    requirementId = "requirement-1"
                ),
                activity(
                    id = "activity-2",
                    requirementId = "requirement-2"
                )
            ),
            timeSlots = listOf(slot),
            constraints = listOf(
                NoOverlapConstraint(
                    id = "no-overlap",
                    name = "No resource overlap",
                    weight = 1000.0,
                    timeSlots = listOf(slot)
                )
            )
        )
    }

    private fun activity(
        id: String,
        requirementId: String
    ) =
        Activity(
            id = id,
            name = id,
            resourceRequirements = listOf(
                ResourceRequirement(
                    id = requirementId,
                    resourceTypeId = "worker",
                    quantity = 1,
                    candidateResourceIds =
                        setOf("worker-1")
                )
            ),
            allowedTimeSlotIds =
                setOf("slot-1")
        )

    private fun dateTime(hour: Int) =
        LocalDateTime.of(
            2026,
            1,
            1,
            hour,
            0
        )
}