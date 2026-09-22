package com.geneticplanner.dataset

import com.geneticplanner.domain.Activity
import com.geneticplanner.domain.PlanningHorizon
import com.geneticplanner.domain.PlanningProblem
import com.geneticplanner.domain.Resource
import com.geneticplanner.domain.ResourceRequirement
import com.geneticplanner.domain.ResourceType
import com.geneticplanner.domain.TimeSlot
import java.time.LocalDateTime

object TrivialDataset {

    const val OPTIMAL_FITNESS = 0.0

    fun create(): PlanningProblem =
        PlanningProblem(
            id = "dataset-trivial",
            name = "Trivial dataset",
            planningHorizon = PlanningHorizon(
                start = dateTime(8),
                end = dateTime(12)
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
                ),
                Resource(
                    id = "worker-2",
                    name = "Worker 2",
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
            timeSlots = listOf(
                timeSlot("slot-1", 8, 9),
                timeSlot("slot-2", 9, 10),
                timeSlot("slot-3", 10, 11)
            )
        )

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
                    quantity = 1
                )
            )
        )

    private fun timeSlot(
        id: String,
        startHour: Int,
        endHour: Int
    ) =
        TimeSlot(
            id = id,
            start = dateTime(startHour),
            end = dateTime(endHour)
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