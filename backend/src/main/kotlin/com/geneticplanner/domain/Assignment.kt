package com.geneticplanner.domain

data class Assignment (
    val activityId: String,
    val timeSlotId: String,
    val resourceAssignments: Map<String, List<String>>,
    val locationId: String? = null,
)