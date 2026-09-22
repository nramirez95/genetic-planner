package com.geneticplanner.dataset

import com.geneticplanner.domain.Activity
import com.geneticplanner.domain.PlanningHorizon
import com.geneticplanner.domain.PlanningProblem
import com.geneticplanner.domain.Resource
import com.geneticplanner.domain.ResourceRequirement
import com.geneticplanner.domain.ResourceType
import com.geneticplanner.domain.TimeSlot
import com.geneticplanner.domain.constraint.hard.NoOverlapConstraint
import com.geneticplanner.domain.constraint.soft.PreferredTimeSlotConstraint
import java.time.LocalDateTime

object SmallFeasibleDataset {

    const val OPTIMAL_FITNESS = 0.0

    fun create(): PlanningProblem {
        val slots = listOf(
            timeSlot("slot-1", 8, 9),
            timeSlot("slot-2", 9, 10),
            timeSlot("slot-3", 10, 11),
            timeSlot("slot-4", 11, 12)
        )

        return PlanningProblem(
            id = "dataset-small-feasible",
            name = "Small feasible constrained dataset",
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
                worker("worker-1"),
                worker("worker-2"),
                worker("worker-3")
            ),
            activities = listOf(
                activity(
                    id = "activity-1",
                    requirementId = "requirement-1"
                ),
                activity(
                    id = "activity-2",
                    requirementId = "requirement-2"
                ),
                activity(
                    id = "activity-3",
                    requirementId = "requirement-3"
                ),
                activity(
                    id = "activity-4",
                    requirementId = "requirement-4"
                ),
                activity(
                    id = "activity-5",
                    requirementId = "requirement-5"
                ),
                activity(
                    id = "activity-6",
                    requirementId = "requirement-6"
                )
            ),
            timeSlots = slots,
            constraints = listOf(
                NoOverlapConstraint(
                    id = "no-overlap",
                    name = "No resource overlap",
                    weight = 1000.0,
                    timeSlots = slots
                ),
                PreferredTimeSlotConstraint(
                    id = "preferred-time",
                    name = "Preferred time slots",
                    weight = 10.0,
                    preferredTimeSlotIdsByActivityId =
                        mapOf(
                            "activity-1" to setOf("slot-1"),
                            "activity-2" to setOf("slot-1"),
                            "activity-3" to setOf("slot-2"),
                            "activity-4" to setOf("slot-2"),
                            "activity-5" to setOf("slot-3"),
                            "activity-6" to setOf("slot-3")
                        )
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
                    quantity = 1
                )
            )
        )

    private fun worker(id: String) =
        Resource(
            id = id,
            name = id,
            typeId = "worker"
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