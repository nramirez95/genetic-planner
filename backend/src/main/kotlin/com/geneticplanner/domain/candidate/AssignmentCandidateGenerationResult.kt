package com.geneticplanner.domain.candidate

data class AssignmentCandidateGenerationResult(
    val candidatesByActivity: List<ActivityCandidateOptions>
) {
    val activitiesWithoutOptions: List<String>
        get() = candidatesByActivity
            .filter { it.options.isEmpty() }
            .map { it.activityId }

    val hasMissingOptions: Boolean
        get() = activitiesWithoutOptions.isNotEmpty()
}