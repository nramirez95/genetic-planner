package com.geneticplanner.domain.constraint.hard

import com.geneticplanner.domain.Activity
import com.geneticplanner.domain.Location
import com.geneticplanner.domain.Schedule
import com.geneticplanner.domain.constraint.Constraint
import com.geneticplanner.domain.constraint.ConstraintResult
import com.geneticplanner.domain.constraint.ConstraintType
import com.geneticplanner.domain.constraint.ConstraintViolation

class LocationCapacityConstraint(
    override val id: String,
    override val name: String,
    override val weight: Double,
    activities: List<Activity>,
    locations: List<Location>
) : Constraint {

    override val type = ConstraintType.HARD

    private val activitiesById = activities.associateBy { it.id }
    private val locationsById = locations.associateBy { it.id }

    override fun evaluate(schedule: Schedule): ConstraintResult {
        val violations = mutableListOf<ConstraintViolation>()

        schedule.assignments.forEach { assignment ->
            val activity =
                activitiesById[assignment.activityId] ?: return@forEach

            val requiredCapacity =
                activity.requiredLocationCapacity ?: return@forEach

            val locationId =
                assignment.locationId ?: return@forEach

            val location =
                locationsById[locationId] ?: return@forEach

            val availableCapacity =
                location.capacity ?: return@forEach

            if (availableCapacity < requiredCapacity) {
                val shortage = requiredCapacity - availableCapacity

                violations += ConstraintViolation(
                    message =
                        "Location '$locationId' does not have enough capacity " +
                                "for activity '${activity.id}': required " +
                                "$requiredCapacity, available $availableCapacity.",
                    penalty = shortage.toDouble(),
                    relatedEntityIds = setOf(
                        activity.id,
                        locationId
                    )
                )
            }
        }

        return ConstraintResult(
            constraintId = id,
            type = type,
            violationDetails = violations,
            weight = weight
        )
    }
}