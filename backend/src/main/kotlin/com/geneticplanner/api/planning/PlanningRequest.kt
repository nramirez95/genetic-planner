package com.geneticplanner.api.planning

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class PlanningRequest(
    @field:NotBlank
    val id: String,

    @field:NotBlank
    val name: String,

    val templateType: String? = null,

    @field:NotNull
    val horizonStart: LocalDateTime?,

    @field:NotNull
    val horizonEnd: LocalDateTime?,

    @field:Valid
    val resourceTypes: List<ResourceTypeRequest>? = null,

    @field:Valid
    val resources: List<ResourceRequest>? = null,

    @field:Valid
    val activities: List<ActivityRequest>? = null,

    @field:Valid
    val timeSlots: List<TimeSlotRequest>? = null,

    @field:Valid
    val locations: List<LocationRequest>? = null,

    @field:Valid
    val constraints: List<ConstraintRequest>? = null,

    @field:Valid
    val optimizationConfiguration: OptimizationConfigurationRequest? = null
)

data class ResourceTypeRequest(
    @field:NotBlank
    val id: String,

    @field:NotBlank
    val name: String
)

data class ResourceRequest(
    @field:NotBlank
    val id: String,

    @field:NotBlank
    val name: String,

    @field:NotBlank
    val typeId: String,

    val attributes: Map<String, String>? = null
)

data class ActivityRequest(
    @field:NotBlank
    val id: String,

    @field:NotBlank
    val name: String,

    val type: String? = null,

    @field:Valid
    val resourceRequirements: List<ResourceRequirementRequest>? = null,

    val allowedTimeSlotIds: Set<String>? = null,

    val allowedLocationIds: Set<String>? = null,

    @field:Min(0)
    val requiredLocationCapacity: Int? = null,

    val attributes: Map<String, String>? = null
)

data class ResourceRequirementRequest(
    @field:NotBlank
    val id: String,

    @field:NotBlank
    val resourceTypeId: String,

    @field:Min(1)
    val quantity: Int = 1,

    val candidateResourceIds: Set<String>? = null
)

data class TimeSlotRequest(
    @field:NotBlank
    val id: String,

    @field:NotNull
    val start: LocalDateTime?,

    @field:NotNull
    val end: LocalDateTime?,

    val label: String? = null
)

data class LocationRequest(
    @field:NotBlank
    val id: String,

    @field:NotBlank
    val name: String,

    val type: String? = null,

    @field:Min(0)
    val capacity: Int? = null,

    val attributes: Map<String, String>? = null
)