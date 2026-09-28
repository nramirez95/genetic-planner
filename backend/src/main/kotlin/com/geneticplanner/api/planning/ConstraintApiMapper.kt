package com.geneticplanner.api.planning

import com.geneticplanner.domain.Activity
import com.geneticplanner.domain.Location
import com.geneticplanner.domain.Resource
import com.geneticplanner.domain.TimeSlot
import com.geneticplanner.domain.constraint.Constraint
import com.geneticplanner.domain.constraint.hard.AvailabilityConstraint
import com.geneticplanner.domain.constraint.hard.LocationCapacityConstraint
import com.geneticplanner.domain.constraint.hard.NoOverlapConstraint
import com.geneticplanner.domain.constraint.hard.RequiredResourceConstraint
import com.geneticplanner.domain.constraint.soft.BalancedWorkloadConstraint
import com.geneticplanner.domain.constraint.soft.MaxConsecutiveConstraint
import com.geneticplanner.domain.constraint.soft.PreferredTimeSlotConstraint
import org.springframework.stereotype.Component

@Component
class ConstraintApiMapper {

    fun toDomain(
        request: ConstraintRequest,
        activities: List<Activity>,
        resources: List<Resource>,
        timeSlots: List<TimeSlot>,
        locations: List<Location>
    ): Constraint {
        validateReferences(
            request = request,
            activities = activities,
            resources = resources,
            timeSlots = timeSlots
        )

        return when (request.key) {
            ConstraintKeyRequest.NO_OVERLAP ->
                NoOverlapConstraint(
                    id = request.id,
                    name = request.name,
                    weight = request.weight,
                    timeSlots = timeSlots
                )

            ConstraintKeyRequest.AVAILABILITY ->
                AvailabilityConstraint(
                    id = request.id,
                    name = request.name,
                    weight = request.weight,
                    availableTimeSlotIdsByResourceId =
                        requireNotNull(
                            request.availableTimeSlotIdsByResourceId
                        ) {
                            "Constraint '${request.id}' requires " +
                                    "'availableTimeSlotIdsByResourceId'."
                        }
                )

            ConstraintKeyRequest.REQUIRED_RESOURCE ->
                RequiredResourceConstraint(
                    id = request.id,
                    name = request.name,
                    weight = request.weight,
                    activities = activities,
                    resources = resources
                )

            ConstraintKeyRequest.LOCATION_CAPACITY ->
                LocationCapacityConstraint(
                    id = request.id,
                    name = request.name,
                    weight = request.weight,
                    activities = activities,
                    locations = locations
                )

            ConstraintKeyRequest.PREFERRED_TIME_SLOT ->
                PreferredTimeSlotConstraint(
                    id = request.id,
                    name = request.name,
                    weight = request.weight,
                    preferredTimeSlotIdsByActivityId =
                        requireNotNull(
                            request.preferredTimeSlotIdsByActivityId
                        ) {
                            "Constraint '${request.id}' requires " +
                                    "'preferredTimeSlotIdsByActivityId'."
                        }
                )

            ConstraintKeyRequest.MAX_CONSECUTIVE ->
                MaxConsecutiveConstraint(
                    id = request.id,
                    name = request.name,
                    weight = request.weight,
                    maxConsecutive =
                        requireNotNull(request.maxConsecutive) {
                            "Constraint '${request.id}' requires " +
                                    "'maxConsecutive'."
                        },
                    timeSlots = timeSlots
                )

            ConstraintKeyRequest.BALANCED_WORKLOAD ->
                BalancedWorkloadConstraint(
                    id = request.id,
                    name = request.name,
                    weight = request.weight,
                    resourceIds =
                        requireNotNull(request.resourceIds) {
                            "Constraint '${request.id}' requires " +
                                    "'resourceIds'."
                        },
                    allowedDifference =
                        requireNotNull(request.allowedDifference) {
                            "Constraint '${request.id}' requires " +
                                    "'allowedDifference'."
                        }
                )
        }
    }

    fun toResponse(constraint: Constraint): ConstraintResponse =
        when (constraint) {
            is NoOverlapConstraint ->
                baseResponse(
                    constraint = constraint,
                    key = ConstraintKeyResponse.NO_OVERLAP
                )

            is AvailabilityConstraint ->
                baseResponse(
                    constraint = constraint,
                    key = ConstraintKeyResponse.AVAILABILITY,
                    availableTimeSlotIdsByResourceId =
                        constraint.availableTimeSlotIdsByResourceId
                )

            is RequiredResourceConstraint ->
                baseResponse(
                    constraint = constraint,
                    key = ConstraintKeyResponse.REQUIRED_RESOURCE
                )

            is LocationCapacityConstraint ->
                baseResponse(
                    constraint = constraint,
                    key = ConstraintKeyResponse.LOCATION_CAPACITY
                )

            is PreferredTimeSlotConstraint ->
                baseResponse(
                    constraint = constraint,
                    key = ConstraintKeyResponse.PREFERRED_TIME_SLOT,
                    preferredTimeSlotIdsByActivityId =
                        constraint.preferredTimeSlotIdsByActivityId
                )

            is MaxConsecutiveConstraint ->
                baseResponse(
                    constraint = constraint,
                    key = ConstraintKeyResponse.MAX_CONSECUTIVE,
                    maxConsecutive = constraint.maxConsecutive
                )

            is BalancedWorkloadConstraint ->
                baseResponse(
                    constraint = constraint,
                    key = ConstraintKeyResponse.BALANCED_WORKLOAD,
                    resourceIds = constraint.resourceIds,
                    allowedDifference = constraint.allowedDifference
                )

            else ->
                error(
                    "Unsupported constraint type " +
                            "'${constraint::class.qualifiedName}'."
                )
        }

    private fun validateReferences(
        request: ConstraintRequest,
        activities: List<Activity>,
        resources: List<Resource>,
        timeSlots: List<TimeSlot>
    ) {
        val activityIds = activities.map { it.id }.toSet()
        val resourceIds = resources.map { it.id }.toSet()
        val timeSlotIds = timeSlots.map { it.id }.toSet()

        when (request.key) {
            ConstraintKeyRequest.AVAILABILITY -> {
                request.availableTimeSlotIdsByResourceId
                    ?.forEach { (resourceId, availableTimeSlotIds) ->
                        require(resourceId in resourceIds) {
                            "Constraint '${request.id}' references " +
                                    "unknown resource '$resourceId'."
                        }

                        availableTimeSlotIds.forEach { timeSlotId ->
                            require(timeSlotId in timeSlotIds) {
                                "Constraint '${request.id}' references " +
                                        "unknown time slot '$timeSlotId'."
                            }
                        }
                    }
            }

            ConstraintKeyRequest.PREFERRED_TIME_SLOT -> {
                request.preferredTimeSlotIdsByActivityId
                    ?.forEach { (activityId, preferredTimeSlotIds) ->
                        require(activityId in activityIds) {
                            "Constraint '${request.id}' references " +
                                    "unknown activity '$activityId'."
                        }

                        preferredTimeSlotIds.forEach { timeSlotId ->
                            require(timeSlotId in timeSlotIds) {
                                "Constraint '${request.id}' references " +
                                        "unknown time slot '$timeSlotId'."
                            }
                        }
                    }
            }

            ConstraintKeyRequest.BALANCED_WORKLOAD -> {
                request.resourceIds?.forEach { resourceId ->
                    require(resourceId in resourceIds) {
                        "Constraint '${request.id}' references " +
                                "unknown resource '$resourceId'."
                    }
                }
            }

            ConstraintKeyRequest.NO_OVERLAP,
            ConstraintKeyRequest.REQUIRED_RESOURCE,
            ConstraintKeyRequest.LOCATION_CAPACITY,
            ConstraintKeyRequest.MAX_CONSECUTIVE -> {
                // No explicit entity IDs are configured by these constraints.
            }
        }
    }

    private fun baseResponse(
        constraint: Constraint,
        key: ConstraintKeyResponse,
        availableTimeSlotIdsByResourceId:
        Map<String, Set<String>>? = null,
        preferredTimeSlotIdsByActivityId:
        Map<String, Set<String>>? = null,
        maxConsecutive: Int? = null,
        resourceIds: Set<String>? = null,
        allowedDifference: Int? = null
    ): ConstraintResponse =
        ConstraintResponse(
            id = constraint.id,
            name = constraint.name,
            key = key,
            type = constraint.type,
            weight = constraint.weight,
            availableTimeSlotIdsByResourceId =
                availableTimeSlotIdsByResourceId,
            preferredTimeSlotIdsByActivityId =
                preferredTimeSlotIdsByActivityId,
            maxConsecutive = maxConsecutive,
            resourceIds = resourceIds,
            allowedDifference = allowedDifference
        )
}