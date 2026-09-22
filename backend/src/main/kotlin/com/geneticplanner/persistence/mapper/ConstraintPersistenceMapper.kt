package com.geneticplanner.persistence.mapper

import com.geneticplanner.domain.Activity
import com.geneticplanner.domain.Location
import com.geneticplanner.domain.Resource
import com.geneticplanner.domain.TimeSlot
import com.geneticplanner.domain.constraint.Constraint
import com.geneticplanner.domain.constraint.ConstraintType
import com.geneticplanner.domain.constraint.hard.AvailabilityConstraint
import com.geneticplanner.domain.constraint.hard.LocationCapacityConstraint
import com.geneticplanner.domain.constraint.hard.NoOverlapConstraint
import com.geneticplanner.domain.constraint.hard.RequiredResourceConstraint
import com.geneticplanner.domain.constraint.soft.BalancedWorkloadConstraint
import com.geneticplanner.domain.constraint.soft.MaxConsecutiveConstraint
import com.geneticplanner.domain.constraint.soft.PreferredTimeSlotConstraint
import com.geneticplanner.persistence.entity.PersistenceConstraintType
import com.geneticplanner.persistence.entity.PlanningConstraintEntity
import com.geneticplanner.persistence.entity.PlanningProblemEntity
import org.springframework.stereotype.Component

@Component
class ConstraintPersistenceMapper {

    fun toPersistence(
        constraint: Constraint,
        planningProblem: PlanningProblemEntity
    ): PlanningConstraintEntity =
        PlanningConstraintEntity(
            id = constraint.id,
            planningProblem = planningProblem,
            constraintKey = constraintKey(constraint),
            constraintType = constraint.type.toPersistenceType(),
            name = constraint.name,
            weight = constraint.weight,
            configuration = configuration(constraint)
        )

    fun toDomain(
        entity: PlanningConstraintEntity,
        activities: List<Activity>,
        resources: List<Resource>,
        timeSlots: List<TimeSlot>,
        locations: List<Location>
    ): Constraint =
        when (entity.constraintKey) {
            NO_OVERLAP ->
                NoOverlapConstraint(
                    id = entity.id,
                    name = entity.name,
                    weight = entity.weight,
                    timeSlots = timeSlots
                )

            AVAILABILITY ->
                AvailabilityConstraint(
                    id = entity.id,
                    name = entity.name,
                    weight = entity.weight,
                    availableTimeSlotIdsByResourceId =
                        entity.configuration.stringSetMap(
                            "availableTimeSlotIdsByResourceId"
                        )
                )

            REQUIRED_RESOURCE ->
                RequiredResourceConstraint(
                    id = entity.id,
                    name = entity.name,
                    weight = entity.weight,
                    activities = activities,
                    resources = resources
                )

            LOCATION_CAPACITY ->
                LocationCapacityConstraint(
                    id = entity.id,
                    name = entity.name,
                    weight = entity.weight,
                    activities = activities,
                    locations = locations
                )

            PREFERRED_TIME_SLOT ->
                PreferredTimeSlotConstraint(
                    id = entity.id,
                    name = entity.name,
                    weight = entity.weight,
                    preferredTimeSlotIdsByActivityId =
                        entity.configuration.stringSetMap(
                            "preferredTimeSlotIdsByActivityId"
                        )
                )

            MAX_CONSECUTIVE ->
                MaxConsecutiveConstraint(
                    id = entity.id,
                    name = entity.name,
                    weight = entity.weight,
                    maxConsecutive =
                        entity.configuration.intValue("maxConsecutive"),
                    timeSlots = timeSlots
                )

            BALANCED_WORKLOAD ->
                BalancedWorkloadConstraint(
                    id = entity.id,
                    name = entity.name,
                    weight = entity.weight,
                    resourceIds =
                        entity.configuration.stringSet("resourceIds"),
                    allowedDifference =
                        entity.configuration.intValue("allowedDifference")
                )

            else ->
                error(
                    "Unsupported constraint key '${entity.constraintKey}'"
                )
        }

    private fun constraintKey(constraint: Constraint): String =
        when (constraint) {
            is NoOverlapConstraint -> NO_OVERLAP
            is AvailabilityConstraint -> AVAILABILITY
            is RequiredResourceConstraint -> REQUIRED_RESOURCE
            is LocationCapacityConstraint -> LOCATION_CAPACITY
            is PreferredTimeSlotConstraint -> PREFERRED_TIME_SLOT
            is MaxConsecutiveConstraint -> MAX_CONSECUTIVE
            is BalancedWorkloadConstraint -> BALANCED_WORKLOAD
            else -> error(
                "Unsupported constraint type '${constraint::class.qualifiedName}'"
            )
        }

    private fun configuration(
        constraint: Constraint
    ): Map<String, Any?> =
        when (constraint) {
            is AvailabilityConstraint ->
                mapOf(
                    "availableTimeSlotIdsByResourceId" to
                            constraint.availableTimeSlotIdsByResourceId
                )

            is PreferredTimeSlotConstraint ->
                mapOf(
                    "preferredTimeSlotIdsByActivityId" to
                            constraint.preferredTimeSlotIdsByActivityId
                )

            is MaxConsecutiveConstraint ->
                mapOf(
                    "maxConsecutive" to constraint.maxConsecutive
                )

            is BalancedWorkloadConstraint ->
                mapOf(
                    "resourceIds" to constraint.resourceIds,
                    "allowedDifference" to constraint.allowedDifference
                )

            is NoOverlapConstraint,
            is RequiredResourceConstraint,
            is LocationCapacityConstraint ->
                emptyMap()

            else -> error(
                "Unsupported constraint type '${constraint::class.qualifiedName}'"
            )
        }

    private fun ConstraintType.toPersistenceType(): PersistenceConstraintType =
        when (this) {
            ConstraintType.HARD -> PersistenceConstraintType.HARD
            ConstraintType.SOFT -> PersistenceConstraintType.SOFT
        }

    private fun Map<String, Any?>.intValue(key: String): Int =
        (this[key] as? Number)?.toInt()
            ?: error("Constraint configuration '$key' must be a number")

    private fun Map<String, Any?>.stringSet(key: String): Set<String> {
        val value = this[key]
            ?: error("Constraint configuration '$key' is required")

        return when (value) {
            is Collection<*> ->
                value.map {
                    it as? String
                        ?: error(
                            "Constraint configuration '$key' must contain strings"
                        )
                }.toSet()

            else ->
                error(
                    "Constraint configuration '$key' must be a collection"
                )
        }
    }

    private fun Map<String, Any?>.stringSetMap(
        key: String
    ): Map<String, Set<String>> {
        val value = this[key]
            ?: error("Constraint configuration '$key' is required")

        val map = value as? Map<*, *>
            ?: error(
                "Constraint configuration '$key' must be an object"
            )

        return map.entries.associate { (mapKey, mapValue) ->
            val stringKey = mapKey as? String
                ?: error(
                    "Constraint configuration '$key' must use string keys"
                )

            val values = mapValue as? Collection<*>
                ?: error(
                    "Constraint configuration '$key.$stringKey' " +
                            "must be a collection"
                )

            stringKey to values.map {
                it as? String
                    ?: error(
                        "Constraint configuration '$key.$stringKey' " +
                                "must contain strings"
                    )
            }.toSet()
        }
    }

    companion object {
        const val NO_OVERLAP = "NO_OVERLAP"
        const val AVAILABILITY = "AVAILABILITY"
        const val REQUIRED_RESOURCE = "REQUIRED_RESOURCE"
        const val LOCATION_CAPACITY = "LOCATION_CAPACITY"
        const val PREFERRED_TIME_SLOT = "PREFERRED_TIME_SLOT"
        const val MAX_CONSECUTIVE = "MAX_CONSECUTIVE"
        const val BALANCED_WORKLOAD = "BALANCED_WORKLOAD"
    }
}