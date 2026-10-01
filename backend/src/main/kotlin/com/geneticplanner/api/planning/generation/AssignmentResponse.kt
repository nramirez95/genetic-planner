package com.geneticplanner.api.planning.generation

data class AssignmentResponse(
    val activityId: String,
    val timeSlotId: String,
    val resourceAssignments: Map<String, List<String>>,
    val locationId: String?
)