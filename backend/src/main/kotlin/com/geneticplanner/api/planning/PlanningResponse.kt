package com.geneticplanner.api.planning

import java.time.LocalDateTime

data class PlanningResponse(
    val id: String,
    val name: String,
    val templateType: String?,
    val horizonStart: LocalDateTime,
    val horizonEnd: LocalDateTime,
    val resourceTypes: List<ResourceTypeResponse>,
    val resources: List<ResourceResponse>,
    val activities: List<ActivityResponse>,
    val timeSlots: List<TimeSlotResponse>,
    val locations: List<LocationResponse>,
    val constraints: List<ConstraintResponse>,
    val optimizationConfiguration: OptimizationConfigurationResponse?
)

data class ResourceTypeResponse(
    val id: String,
    val name: String
)

data class ResourceResponse(
    val id: String,
    val name: String,
    val typeId: String,
    val attributes: Map<String, String>
)

data class ActivityResponse(
    val id: String,
    val name: String,
    val type: String?,
    val resourceRequirements: List<ResourceRequirementResponse>,
    val allowedTimeSlotIds: Set<String>?,
    val allowedLocationIds: Set<String>?,
    val requiredLocationCapacity: Int?,
    val attributes: Map<String, String>
)

data class ResourceRequirementResponse(
    val id: String,
    val resourceTypeId: String,
    val quantity: Int,
    val candidateResourceIds: Set<String>?
)

data class TimeSlotResponse(
    val id: String,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val label: String?
)

data class LocationResponse(
    val id: String,
    val name: String,
    val type: String?,
    val capacity: Int?,
    val attributes: Map<String, String>
)