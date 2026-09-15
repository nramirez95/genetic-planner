package com.geneticplanner.domain

data class PlanningProblem (
    val id: String,
    val name: String,
    val templateType: String? = null,
    val planningHorizon: PlanningHorizon,
    val resourceTypes: List<ResourceType> = emptyList(),
    val resources: List<Resource> = emptyList(),
    val activities: List<Activity> = emptyList(),
    val timeSlots: List<TimeSlot> = emptyList(),
    val locations: List<Location> = emptyList()
)