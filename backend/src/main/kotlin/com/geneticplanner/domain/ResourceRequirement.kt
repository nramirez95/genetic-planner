package com.geneticplanner.domain

data class ResourceRequirement (
    val id: String,
    val resourceTypeId: String,
    val quantity: Int = 1,
    val candidateResourceIds: Set<String>? = null
)