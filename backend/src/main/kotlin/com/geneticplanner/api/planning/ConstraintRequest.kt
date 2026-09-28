package com.geneticplanner.api.planning

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class ConstraintRequest(
    @field:NotBlank
    val id: String,

    @field:NotBlank
    val name: String,

    val key: ConstraintKeyRequest,

    @field:Positive
    val weight: Double,

    val availableTimeSlotIdsByResourceId: Map<String, Set<String>>? = null,

    val preferredTimeSlotIdsByActivityId: Map<String, Set<String>>? = null,

    @field:Min(1)
    val maxConsecutive: Int? = null,

    val resourceIds: Set<String>? = null,

    @field:Min(0)
    val allowedDifference: Int? = null
)

enum class ConstraintKeyRequest {
    NO_OVERLAP,
    AVAILABILITY,
    REQUIRED_RESOURCE,
    LOCATION_CAPACITY,
    PREFERRED_TIME_SLOT,
    MAX_CONSECUTIVE,
    BALANCED_WORKLOAD
}