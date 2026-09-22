package com.geneticplanner.persistence.mapper

import com.geneticplanner.domain.*
import com.geneticplanner.persistence.entity.*
import org.springframework.stereotype.Component

@Component
class PlanningProblemPersistenceMapper (private val constraintMapper: ConstraintPersistenceMapper) {

    fun toPersistence(problem: PlanningProblem): PlanningProblemPersistenceData {
        val problemEntity = PlanningProblemEntity(
            id = problem.id,
            name = problem.name,
            templateType = problem.templateType,
            planningHorizonStart = problem.planningHorizon.start,
            planningHorizonEnd = problem.planningHorizon.end
        )

        val resourceTypeEntities = problem.resourceTypes.map {
            ResourceTypeEntity(
                id = it.id,
                name = it.name,
                planningProblem = problemEntity
            )
        }

        val resourceTypesById = resourceTypeEntities.associateBy { it.id }

        val resourceEntities = problem.resources.map {
            ResourceEntity(
                id = it.id,
                name = it.name,
                planningProblem = problemEntity,
                type = requireNotNull(resourceTypesById[it.typeId]) {
                    "Resource '${it.id}' references unknown resource type '${it.typeId}'"
                },
                attributes = it.attributes
            )
        }

        val resourcesById = resourceEntities.associateBy { it.id }

        val timeSlotEntities = problem.timeSlots.map {
            TimeSlotEntity(
                id = it.id,
                planningProblem = problemEntity,
                start = it.start,
                end = it.end,
                label = it.label
            )
        }

        val timeSlotsById = timeSlotEntities.associateBy { it.id }

        val locationEntities = problem.locations.map {
            LocationEntity(
                id = it.id,
                planningProblem = problemEntity,
                name = it.name,
                type = it.type,
                capacity = it.capacity,
                attributes = it.attributes
            )
        }

        val locationsById = locationEntities.associateBy { it.id }

        val activityEntities = problem.activities.map {
            ActivityEntity(
                id = it.id,
                planningProblem = problemEntity,
                name = it.name,
                type = it.type,

                timeSlotsRestricted = it.allowedTimeSlotIds != null,
                locationsRestricted = it.allowedLocationIds != null,

                allowedTimeSlots = it.allowedTimeSlotIds
                    ?.map { id ->
                        requireNotNull(timeSlotsById[id]) {
                            "Activity '${it.id}' references unknown time slot '$id'"
                        }
                    }
                    ?.toMutableSet()
                    ?: mutableSetOf(),

                allowedLocations = it.allowedLocationIds
                    ?.map { id ->
                        requireNotNull(locationsById[id]) {
                            "Activity '${it.id}' references unknown location '$id'"
                        }
                    }
                    ?.toMutableSet()
                    ?: mutableSetOf(),

                requiredLocationCapacity = it.requiredLocationCapacity,
                attributes = it.attributes
            )
        }

        val activitiesById = activityEntities.associateBy { it.id }

        val requirementEntities = problem.activities.flatMap { activity ->
            activity.resourceRequirements.map { requirement ->

                val activityEntity = requireNotNull(activitiesById[activity.id])

                ResourceRequirementEntity(
                    id = requirement.id,
                    activity = activityEntity,

                    resourceType = requireNotNull(
                        resourceTypesById[requirement.resourceTypeId]
                    ) {
                        "Requirement '${requirement.id}' references unknown resource type '${requirement.resourceTypeId}'"
                    },

                    quantity = requirement.quantity,

                    candidateResourcesRestricted =
                        requirement.candidateResourceIds != null,

                    candidateResources = requirement.candidateResourceIds
                        ?.map { resourceId ->
                            requireNotNull(resourcesById[resourceId]) {
                                "Requirement '${requirement.id}' references unknown resource '$resourceId'"
                            }
                        }
                        ?.toMutableSet()
                        ?: mutableSetOf()
                )
            }
        }

        val constraintEntities = problem.constraints.map { constraint ->
            constraintMapper.toPersistence(
                constraint = constraint,
                planningProblem = problemEntity
            )
        }

        return PlanningProblemPersistenceData(
            problem = problemEntity,
            resourceTypes = resourceTypeEntities,
            resources = resourceEntities,
            timeSlots = timeSlotEntities,
            locations = locationEntities,
            activities = activityEntities,
            requirements = requirementEntities,
            constraints = constraintEntities
        )
    }

    fun toDomain(
        problem: PlanningProblemEntity,
        resourceTypes: List<ResourceTypeEntity>,
        resources: List<ResourceEntity>,
        timeSlots: List<TimeSlotEntity>,
        locations: List<LocationEntity>,
        activities: List<ActivityEntity>,
        requirements: List<ResourceRequirementEntity>,
        constraints: List<PlanningConstraintEntity>
    ): PlanningProblem {

        val requirementsByActivityId =
            requirements.groupBy { it.activity.id }

        val domainResourceTypes = resourceTypes.map {
            ResourceType(
                id = it.id,
                name = it.name
            )
        }

        val domainResources = resources.map {
            Resource(
                id = it.id,
                name = it.name,
                typeId = it.type.id,
                attributes = it.attributes
            )
        }

        val domainTimeSlots = timeSlots.map {
            TimeSlot(
                id = it.id,
                start = it.start,
                end = it.end,
                label = it.label
            )
        }

        val domainLocations = locations.map {
            Location(
                id = it.id,
                name = it.name,
                type = it.type,
                capacity = it.capacity,
                attributes = it.attributes
            )
        }

        val domainActivities = activities.map { activity ->
            Activity(
                id = activity.id,
                name = activity.name,
                type = activity.type,

                resourceRequirements =
                    requirementsByActivityId[activity.id]
                        .orEmpty()
                        .map { requirement ->
                            ResourceRequirement(
                                id = requirement.id,
                                resourceTypeId = requirement.resourceType.id,
                                quantity = requirement.quantity,

                                candidateResourceIds =
                                    if (requirement.candidateResourcesRestricted) {
                                        requirement.candidateResources
                                            .map { it.id }
                                            .toSet()
                                    } else {
                                        null
                                    }
                            )
                        },

                allowedTimeSlotIds =
                    if (activity.timeSlotsRestricted) {
                        activity.allowedTimeSlots
                            .map { it.id }
                            .toSet()
                    } else {
                        null
                    },

                allowedLocationIds =
                    if (activity.locationsRestricted) {
                        activity.allowedLocations
                            .map { it.id }
                            .toSet()
                    } else {
                        null
                    },

                requiredLocationCapacity =
                    activity.requiredLocationCapacity,

                attributes = activity.attributes
            )
        }

        val domainConstraints = constraints.map { constraint ->
            constraintMapper.toDomain(
                entity = constraint,
                activities = domainActivities,
                resources = domainResources,
                timeSlots = domainTimeSlots,
                locations = domainLocations
            )
        }

        return PlanningProblem(
            id = problem.id,
            name = problem.name,
            templateType = problem.templateType,

            planningHorizon = PlanningHorizon(
                start = problem.planningHorizonStart,
                end = problem.planningHorizonEnd
            ),

            resourceTypes = domainResourceTypes,
            resources = domainResources,
            activities = domainActivities,
            timeSlots = domainTimeSlots,
            locations = domainLocations,
            constraints = domainConstraints
        )
    }
}