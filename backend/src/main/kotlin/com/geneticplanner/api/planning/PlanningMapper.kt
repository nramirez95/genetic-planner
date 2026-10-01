package com.geneticplanner.api.planning

import com.geneticplanner.application.model.PlanningConfiguration
import com.geneticplanner.domain.Activity
import com.geneticplanner.domain.Location
import com.geneticplanner.domain.PlanningHorizon
import com.geneticplanner.domain.PlanningProblem
import com.geneticplanner.domain.Resource
import com.geneticplanner.domain.ResourceRequirement
import com.geneticplanner.domain.ResourceType
import com.geneticplanner.domain.TimeSlot
import com.geneticplanner.genetic.config.GeneticAlgorithmConfig
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class PlanningApiMapper(
    private val constraintApiMapper: ConstraintApiMapper
) {

    fun toDomain(request: PlanningRequest): PlanningProblem {
        val horizonStart = requireNotNull(request.horizonStart) {
            "Planning horizon start is required."
        }

        val horizonEnd = requireNotNull(request.horizonEnd) {
            "Planning horizon end is required."
        }

        require(horizonStart < horizonEnd) {
            "Planning horizon start must be before end."
        }

        validateTimeSlots(
            request = request,
            horizonStart = horizonStart,
            horizonEnd = horizonEnd
        )

        validateReferences(request)

        val resourceTypes =
            request.resourceTypes.orEmpty().map {
                ResourceType(
                    id = it.id,
                    name = it.name
                )
            }

        val resources =
            request.resources.orEmpty().map {
                Resource(
                    id = it.id,
                    name = it.name,
                    typeId = it.typeId,
                    attributes = it.attributes.orEmpty()
                )
            }

        val activities =
            request.activities.orEmpty().map { activity ->
                Activity(
                    id = activity.id,
                    name = activity.name,
                    type = activity.type,
                    resourceRequirements =
                        activity.resourceRequirements.orEmpty().map { requirement ->
                            ResourceRequirement(
                                id = requirement.id,
                                resourceTypeId = requirement.resourceTypeId,
                                quantity = requirement.quantity,
                                candidateResourceIds =
                                    requirement.candidateResourceIds
                            )
                        },
                    allowedTimeSlotIds = activity.allowedTimeSlotIds,
                    allowedLocationIds = activity.allowedLocationIds,
                    requiredLocationCapacity =
                        activity.requiredLocationCapacity,
                    attributes = activity.attributes.orEmpty()
                )
            }

        val timeSlots =
            request.timeSlots.orEmpty().map { timeSlot ->
                TimeSlot(
                    id = timeSlot.id,
                    start = requireNotNull(timeSlot.start),
                    end = requireNotNull(timeSlot.end),
                    label = timeSlot.label
                )
            }

        val locations =
            request.locations.orEmpty().map {
                Location(
                    id = it.id,
                    name = it.name,
                    type = it.type,
                    capacity = it.capacity,
                    attributes = it.attributes.orEmpty()
                )
            }

        val constraints =
            request.constraints.orEmpty().map { constraint ->
                constraintApiMapper.toDomain(
                    request = constraint,
                    activities = activities,
                    resources = resources,
                    timeSlots = timeSlots,
                    locations = locations
                )
            }

        return PlanningProblem(
            id = request.id,
            name = request.name,
            templateType = request.templateType,
            planningHorizon = PlanningHorizon(
                start = horizonStart,
                end = horizonEnd
            ),
            resourceTypes = resourceTypes,
            resources = resources,
            activities = activities,
            timeSlots = timeSlots,
            locations = locations,
            constraints = constraints
        )
    }

    /*
     * Compatibility mapper for code that still works directly
     * with PlanningProblem.
     */
    fun toResponse(
        problem: PlanningProblem
    ): PlanningResponse =
        toResponse(
            PlanningConfiguration(
                problem = problem,
                optimizationConfiguration = null
            )
        )

    /*
     * Complete API response including the optional
     * optimization configuration.
     */
    fun toResponse(
        configuration: PlanningConfiguration
    ): PlanningResponse {

        val problem = configuration.problem

        return PlanningResponse(
            id = problem.id,
            name = problem.name,
            templateType = problem.templateType,
            horizonStart = problem.planningHorizon.start,
            horizonEnd = problem.planningHorizon.end,

            resourceTypes = problem.resourceTypes.map {
                ResourceTypeResponse(
                    id = it.id,
                    name = it.name
                )
            },

            resources = problem.resources.map {
                ResourceResponse(
                    id = it.id,
                    name = it.name,
                    typeId = it.typeId,
                    attributes = it.attributes
                )
            },

            activities = problem.activities.map { activity ->
                ActivityResponse(
                    id = activity.id,
                    name = activity.name,
                    type = activity.type,
                    resourceRequirements =
                        activity.resourceRequirements.map { requirement ->
                            ResourceRequirementResponse(
                                id = requirement.id,
                                resourceTypeId =
                                    requirement.resourceTypeId,
                                quantity = requirement.quantity,
                                candidateResourceIds =
                                    requirement.candidateResourceIds
                            )
                        },
                    allowedTimeSlotIds =
                        activity.allowedTimeSlotIds,
                    allowedLocationIds =
                        activity.allowedLocationIds,
                    requiredLocationCapacity =
                        activity.requiredLocationCapacity,
                    attributes = activity.attributes
                )
            },

            timeSlots = problem.timeSlots.map {
                TimeSlotResponse(
                    id = it.id,
                    start = it.start,
                    end = it.end,
                    label = it.label
                )
            },

            locations = problem.locations.map {
                LocationResponse(
                    id = it.id,
                    name = it.name,
                    type = it.type,
                    capacity = it.capacity,
                    attributes = it.attributes
                )
            },

            constraints =
                problem.constraints.map(
                    constraintApiMapper::toResponse
                ),

            optimizationConfiguration =
                configuration.optimizationConfiguration?.let {
                    OptimizationConfigurationResponse(
                        populationSize = it.populationSize,
                        generationLimit = it.generationLimit,
                        mutationProbability = it.mutationProbability,
                        crossoverProbability = it.crossoverProbability,
                        eliteCount = it.eliteCount,
                        randomSeed = it.randomSeed
                    )
                }
        )
    }

    /*
     * Converts the complete REST request into the application
     * model used to manage planning configuration.
     */
    fun toConfiguration(
        request: PlanningRequest
    ): PlanningConfiguration =
        PlanningConfiguration(
            problem = toDomain(request),
            optimizationConfiguration =
                request.optimizationConfiguration?.let {
                    GeneticAlgorithmConfig(
                        populationSize = it.populationSize,
                        generationLimit = it.generationLimit,
                        mutationProbability = it.mutationProbability,
                        crossoverProbability = it.crossoverProbability,
                        eliteCount = it.eliteCount,
                        randomSeed = it.randomSeed
                    )
                }
        )

    private fun validateTimeSlots(
        request: PlanningRequest,
        horizonStart: LocalDateTime,
        horizonEnd: LocalDateTime
    ) {
        request.timeSlots.orEmpty().forEach { timeSlot ->
            val start = requireNotNull(timeSlot.start) {
                "Time slot '${timeSlot.id}' start is required."
            }

            val end = requireNotNull(timeSlot.end) {
                "Time slot '${timeSlot.id}' end is required."
            }

            require(start < end) {
                "Time slot '${timeSlot.id}' start must be before end."
            }

            require(
                !start.isBefore(horizonStart) &&
                        !end.isAfter(horizonEnd)
            ) {
                "Time slot '${timeSlot.id}' must be inside " +
                        "the planning horizon."
            }
        }
    }

    private fun validateReferences(
        request: PlanningRequest
    ) {
        val resourceTypeIds =
            request.resourceTypes.orEmpty()
                .map { it.id }
                .toSet()

        val resourcesById =
            request.resources.orEmpty()
                .associateBy { it.id }

        val timeSlotIds =
            request.timeSlots.orEmpty()
                .map { it.id }
                .toSet()

        val locationIds =
            request.locations.orEmpty()
                .map { it.id }
                .toSet()

        request.resources.orEmpty().forEach { resource ->
            require(resource.typeId in resourceTypeIds) {
                "Resource '${resource.id}' references unknown " +
                        "resource type '${resource.typeId}'."
            }
        }

        request.activities.orEmpty().forEach { activity ->

            activity.resourceRequirements.orEmpty()
                .forEach { requirement ->

                    require(
                        requirement.resourceTypeId in resourceTypeIds
                    ) {
                        "Requirement '${requirement.id}' references " +
                                "unknown resource type " +
                                "'${requirement.resourceTypeId}'."
                    }

                    requirement.candidateResourceIds
                        ?.forEach { resourceId ->

                            require(resourceId in resourcesById) {
                                "Requirement '${requirement.id}' " +
                                        "references unknown resource " +
                                        "'$resourceId'."
                            }

                            val candidate =
                                resourcesById.getValue(resourceId)

                            require(
                                candidate.typeId ==
                                        requirement.resourceTypeId
                            ) {
                                "Resource '$resourceId' does not " +
                                        "match resource type " +
                                        "'${requirement.resourceTypeId}' " +
                                        "required by " +
                                        "'${requirement.id}'."
                            }
                        }
                }

            activity.allowedTimeSlotIds
                ?.forEach { timeSlotId ->
                    require(timeSlotId in timeSlotIds) {
                        "Activity '${activity.id}' references " +
                                "unknown time slot '$timeSlotId'."
                    }
                }

            activity.allowedLocationIds
                ?.forEach { locationId ->
                    require(locationId in locationIds) {
                        "Activity '${activity.id}' references " +
                                "unknown location '$locationId'."
                    }
                }
        }
    }
}