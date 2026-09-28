package com.geneticplanner.api.planning

import com.geneticplanner.domain.constraint.ConstraintType

data class ConstraintResponse(
    val id: String,
    val name: String,
    val key: ConstraintKeyResponse,
    val type: ConstraintType,
    val weight: Double,
    val availableTimeSlotIdsByResourceId: Map<String, Set<String>>? = null,
    val preferredTimeSlotIdsByActivityId: Map<String, Set<String>>? = null,
    val maxConsecutive: Int? = null,
    val resourceIds: Set<String>? = null,
    val allowedDifference: Int? = null
)

enum class ConstraintKeyResponse {
    NO_OVERLAP,
    AVAILABILITY,
    REQUIRED_RESOURCE,
    LOCATION_CAPACITY,
    PREFERRED_TIME_SLOT,
    MAX_CONSECUTIVE,
    BALANCED_WORKLOAD
}