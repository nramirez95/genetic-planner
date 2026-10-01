package com.geneticplanner.api.planning.result

data class ResultAssignmentResponse(
    val activity: ResultActivityResponse,
    val timeSlot: ResultTimeSlotResponse,
    val resources: List<ResultResourceResponse>,
    val location: ResultLocationResponse?
)

data class ResultActivityResponse(
    val id: String,
    val name: String,
    val type: String?
)

data class ResultTimeSlotResponse(
    val id: String,
    val start: String,
    val end: String,
    val label: String?
)

data class ResultResourceResponse(
    val id: String,
    val name: String,
    val typeId: String
)

data class ResultLocationResponse(
    val id: String,
    val name: String,
    val type: String?
)