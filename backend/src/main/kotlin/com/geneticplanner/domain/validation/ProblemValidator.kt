package com.geneticplanner.domain.validation

import com.geneticplanner.domain.PlanningProblem

class ProblemValidator {

    fun validate(problem: PlanningProblem): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        validateUniqueIds(problem, errors)
        validatePlanningHorizon(problem, errors)
        validateTimeSlots(problem, errors)
        validateResourceTypeReferences(problem, errors)
        validateRequirements(problem, errors)
        validateActivityTimeSlots(problem, errors)
        validateActivityLocations(problem, errors)

        return ValidationResult(errors)
    }

    private fun validateUniqueIds(
        problem: PlanningProblem,
        errors: MutableList<ValidationError>
    ) {
        validateUniqueIds(
            entityType = "ResourceType",
            ids = problem.resourceTypes.map { it.id },
            errors = errors
        )

        validateUniqueIds(
            entityType = "Resource",
            ids = problem.resources.map { it.id },
            errors = errors
        )

        validateUniqueIds(
            entityType = "Activity",
            ids = problem.activities.map { it.id },
            errors = errors
        )

        validateUniqueIds(
            entityType = "TimeSlot",
            ids = problem.timeSlots.map { it.id },
            errors = errors
        )

        validateUniqueIds(
            entityType = "Location",
            ids = problem.locations.map { it.id },
            errors = errors
        )

        problem.activities.forEach { activity ->
            validateUniqueIds(
                entityType = "ResourceRequirement",
                ids = activity.resourceRequirements.map { it.id },
                errors = errors
            )
        }
    }

    private fun validateUniqueIds(
        entityType: String,
        ids: List<String>,
        errors: MutableList<ValidationError>
    ) {
        ids.groupingBy { it }
            .eachCount()
            .filterValues { it > 1 }
            .keys
            .forEach { duplicateId ->
                errors += ValidationError(
                    code = ValidationErrorCode.DUPLICATE_ID,
                    message = "$entityType ID '$duplicateId' is duplicated.",
                    entityType = entityType,
                    entityId = duplicateId,
                    field = "id"
                )
            }
    }

    private fun validatePlanningHorizon(
        problem: PlanningProblem,
        errors: MutableList<ValidationError>
    ) {
        if (!problem.planningHorizon.start.isBefore(problem.planningHorizon.end)) {
            errors += ValidationError(
                code = ValidationErrorCode.INVALID_PLANNING_HORIZON,
                message = "Planning horizon start must be before its end.",
                entityType = "PlanningProblem",
                entityId = problem.id,
                field = "planningHorizon"
            )
        }
    }

    private fun validateTimeSlots(
        problem: PlanningProblem,
        errors: MutableList<ValidationError>
    ) {
        val horizon = problem.planningHorizon

        problem.timeSlots.forEach { slot ->
            if (!slot.start.isBefore(slot.end)) {
                errors += ValidationError(
                    code = ValidationErrorCode.INVALID_TIME_SLOT,
                    message = "Time slot '${slot.id}' must start before it ends.",
                    entityType = "TimeSlot",
                    entityId = slot.id,
                    field = "start/end"
                )
            }

            if (slot.start.isBefore(horizon.start) || slot.end.isAfter(horizon.end)) {
                errors += ValidationError(
                    code = ValidationErrorCode.INVALID_TIME_SLOT,
                    message = "Time slot '${slot.id}' is outside the planning horizon.",
                    entityType = "TimeSlot",
                    entityId = slot.id,
                    field = "start/end"
                )
            }
        }
    }

    private fun validateResourceTypeReferences(
        problem: PlanningProblem,
        errors: MutableList<ValidationError>
    ) {
        val resourceTypeIds = problem.resourceTypes.map { it.id }.toSet()

        problem.resources.forEach { resource ->
            if (resource.typeId !in resourceTypeIds) {
                errors += ValidationError(
                    code = ValidationErrorCode.UNKNOWN_RESOURCE_TYPE,
                    message = "Resource '${resource.id}' references unknown resource type '${resource.typeId}'.",
                    entityType = "Resource",
                    entityId = resource.id,
                    field = "typeId"
                )
            }
        }
    }

    private fun validateRequirements(
        problem: PlanningProblem,
        errors: MutableList<ValidationError>
    ) {
        val resourceTypesById = problem.resourceTypes.associateBy { it.id }
        val resourcesById = problem.resources.associateBy { it.id }

        problem.activities.forEach { activity ->
            activity.resourceRequirements.forEach { requirement ->

                if (requirement.quantity < 1) {
                    errors += ValidationError(
                        code = ValidationErrorCode.INVALID_REQUIREMENT_QUANTITY,
                        message = "Resource requirement '${requirement.id}' must have quantity >= 1.",
                        entityType = "ResourceRequirement",
                        entityId = requirement.id,
                        field = "quantity"
                    )
                }

                if (
                    requirement.candidateResourceIds != null &&
                    requirement.candidateResourceIds.size < requirement.quantity
                ) {
                    errors += ValidationError(
                        code = ValidationErrorCode.INSUFFICIENT_CANDIDATE_RESOURCES,
                        message =
                            "Resource requirement '${requirement.id}' requires " +
                                    "${requirement.quantity} distinct resource(s), but only " +
                                    "${requirement.candidateResourceIds.size} candidate(s) are available.",
                        entityType = "ResourceRequirement",
                        entityId = requirement.id,
                        field = "candidateResourceIds"
                    )
                }

                if (requirement.resourceTypeId !in resourceTypesById) {
                    errors += ValidationError(
                        code = ValidationErrorCode.UNKNOWN_RESOURCE_TYPE,
                        message = "Resource requirement '${requirement.id}' references unknown resource type '${requirement.resourceTypeId}'.",
                        entityType = "ResourceRequirement",
                        entityId = requirement.id,
                        field = "resourceTypeId"
                    )
                }

                requirement.candidateResourceIds?.forEach { candidateId ->
                    val resource = resourcesById[candidateId]

                    if (resource == null) {
                        errors += ValidationError(
                            code = ValidationErrorCode.UNKNOWN_CANDIDATE_RESOURCE,
                            message = "Resource requirement '${requirement.id}' references unknown candidate resource '$candidateId'.",
                            entityType = "ResourceRequirement",
                            entityId = requirement.id,
                            field = "candidateResourceIds"
                        )
                    } else if (resource.typeId != requirement.resourceTypeId) {
                        errors += ValidationError(
                            code = ValidationErrorCode.CANDIDATE_RESOURCE_TYPE_MISMATCH,
                            message = "Candidate resource '$candidateId' does not match expected resource type '${requirement.resourceTypeId}'.",
                            entityType = "ResourceRequirement",
                            entityId = requirement.id,
                            field = "candidateResourceIds"
                        )
                    }
                }
            }
        }
    }

    private fun validateActivityTimeSlots(
        problem: PlanningProblem,
        errors: MutableList<ValidationError>
    ) {
        val timeSlotIds = problem.timeSlots.map { it.id }.toSet()

        problem.activities.forEach { activity ->
            activity.allowedTimeSlotIds?.forEach { timeSlotId ->
                if (timeSlotId !in timeSlotIds) {
                    errors += ValidationError(
                        code = ValidationErrorCode.UNKNOWN_TIME_SLOT,
                        message = "Activity '${activity.id}' references unknown time slot '$timeSlotId'.",
                        entityType = "Activity",
                        entityId = activity.id,
                        field = "allowedTimeSlotIds"
                    )
                }
            }

            val possibleTimeSlots = activity.allowedTimeSlotIds
                ?.filter { it in timeSlotIds }
                ?: timeSlotIds

            if (possibleTimeSlots.isEmpty()) {
                errors += ValidationError(
                    code = ValidationErrorCode.NO_POSSIBLE_TIME_SLOT,
                    message = "Activity '${activity.id}' has no possible time slot.",
                    entityType = "Activity",
                    entityId = activity.id,
                    field = "allowedTimeSlotIds"
                )
            }
        }
    }

    private fun validateActivityLocations(
        problem: PlanningProblem,
        errors: MutableList<ValidationError>
    ) {
        val locationIds = problem.locations.map { it.id }.toSet()

        problem.activities.forEach { activity ->
            activity.allowedLocationIds?.forEach { locationId ->
                if (locationId !in locationIds) {
                    errors += ValidationError(
                        code = ValidationErrorCode.UNKNOWN_LOCATION,
                        message = "Activity '${activity.id}' references unknown location '$locationId'.",
                        entityType = "Activity",
                        entityId = activity.id,
                        field = "allowedLocationIds"
                    )
                }
            }
        }
    }
}
