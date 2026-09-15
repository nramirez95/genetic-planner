package com.geneticplanner.domain

import com.geneticplanner.domain.constraint.Constraint

data class PlanningProblem (
    val id: String,
    val name: String,
    val templateType: String? = null,
    val planningHorizon: PlanningHorizon,
    val activities: List<Activity> = emptyList(),
    val constraints: List<Constraint> = emptyList(),
    val locations: List<Location> = emptyList(),
    val resources: List<Resource> = emptyList(),
    val resourceTypes: List<ResourceType> = emptyList(),
    val timeSlots: List<TimeSlot> = emptyList(),

)