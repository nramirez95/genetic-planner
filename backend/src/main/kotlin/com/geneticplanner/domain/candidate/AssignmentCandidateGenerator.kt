package com.geneticplanner.domain.candidate

import com.geneticplanner.domain.Activity
import com.geneticplanner.domain.PlanningProblem
import com.geneticplanner.domain.ResourceRequirement
import com.geneticplanner.domain.TimeSlot

class AssignmentCandidateGenerator {

    fun generate(
        problem: PlanningProblem
    ): AssignmentCandidateGenerationResult {
        val candidates = problem.activities.map { activity ->
            ActivityCandidateOptions(
                activityId = activity.id,
                options = generateOptions(
                    activity = activity,
                    problem = problem
                )
            )
        }

        return AssignmentCandidateGenerationResult(
            candidatesByActivity = candidates
        )
    }

    private fun generateOptions(
        activity: Activity,
        problem: PlanningProblem
    ): List<AssignmentOption> {
        val timeSlots =
            validTimeSlots(activity, problem)

        val locations =
            validLocationIds(activity, problem)

        val resourceAssignments =
            generateResourceAssignments(
                activity = activity,
                problem = problem
            )

        if (
            timeSlots.isEmpty() ||
            locations.isEmpty() ||
            resourceAssignments.isEmpty()
        ) {
            return emptyList()
        }

        return timeSlots.flatMap { timeSlot ->
            resourceAssignments.flatMap { resources ->
                locations.map { locationId ->
                    AssignmentOption(
                        activityId = activity.id,
                        timeSlotId = timeSlot.id,
                        resourceAssignments = resources,
                        locationId = locationId
                    )
                }
            }
        }
    }

    private fun validTimeSlots(
        activity: Activity,
        problem: PlanningProblem
    ): List<TimeSlot> =
        if (activity.allowedTimeSlotIds == null) {
            problem.timeSlots
        } else {
            problem.timeSlots.filter {
                it.id in activity.allowedTimeSlotIds
            }
        }

    private fun validLocationIds(
        activity: Activity,
        problem: PlanningProblem
    ): List<String?> {
        val locations =
            if (activity.allowedLocationIds == null) {
                problem.locations
            } else {
                problem.locations.filter {
                    it.id in activity.allowedLocationIds
                }
            }

        if (locations.isNotEmpty()) {
            return locations.map { it.id }
        }

        return if (
            activity.allowedLocationIds == null &&
            problem.locations.isEmpty()
        ) {
            listOf(null)
        } else {
            emptyList()
        }
    }

    private fun generateResourceAssignments(
        activity: Activity,
        problem: PlanningProblem
    ): List<Map<String, List<String>>> {
        if (activity.resourceRequirements.isEmpty()) {
            return listOf(emptyMap())
        }

        val optionsByRequirement =
            activity.resourceRequirements.map { requirement ->
                generateRequirementOptions(
                    requirement = requirement,
                    problem = problem
                )
            }

        if (optionsByRequirement.any { it.isEmpty() }) {
            return emptyList()
        }

        return combineRequirementOptions(
            optionsByRequirement
        )
    }

    private fun generateRequirementOptions(
        requirement: ResourceRequirement,
        problem: PlanningProblem
    ): List<Map<String, List<String>>> {
        val resources = problem.resources
            .filter { resource ->
                resource.typeId == requirement.resourceTypeId &&
                        (
                                requirement.candidateResourceIds == null ||
                                        resource.id in requirement.candidateResourceIds
                                )
            }

        return combinations(
            items = resources.map { it.id },
            size = requirement.quantity
        ).map { resourceIds ->
            mapOf(
                requirement.id to resourceIds
            )
        }
    }

    private fun combineRequirementOptions(
        optionsByRequirement:
        List<List<Map<String, List<String>>>>
    ): List<Map<String, List<String>>> {
        var combinations =
            listOf<Map<String, List<String>>>(emptyMap())

        optionsByRequirement.forEach { requirementOptions ->
            combinations = combinations.flatMap { current ->
                requirementOptions.map { option ->
                    current + option
                }
            }
        }

        return combinations
    }

    private fun <T> combinations(
        items: List<T>,
        size: Int
    ): List<List<T>> {
        if (size == 0) {
            return listOf(emptyList())
        }

        if (size > items.size) {
            return emptyList()
        }

        if (size == 1) {
            return items.map { listOf(it) }
        }

        return items.flatMapIndexed { index, item ->
            combinations(
                items = items.drop(index + 1),
                size = size - 1
            ).map { rest ->
                listOf(item) + rest
            }
        }
    }
}