package com.geneticplanner.domain.candidate

data class ActivityCandidateOptions(
    val activityId: String,
    val options: List<AssignmentOption>
)