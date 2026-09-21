package com.geneticplanner.domain.candidate

data class AssignmentOption(
    val activityId: String,
    val timeSlotId: String,
    val resourceAssignments: Map<String, List<String>>,
    val locationId: String? = null
)