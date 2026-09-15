package com.geneticplanner.domain

data class Activity (
    val id: String,
    val name: String,
    val type: String? = null,
    val requiredLocationCapacity: Int? = null,
    val resourceRequirements: List<ResourceRequirement>,
    val allowedTimeSlotIds: Set<String>? = null,
    val allowedLocationIds: Set<String>? = null,
    val attributes: Map<String, String> = emptyMap()
)