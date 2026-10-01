package com.geneticplanner.application

class ScheduleGenerationException(
    val planningId: String,
    cause: Throwable
) : RuntimeException(
    "Schedule generation failed for planning '$planningId'.",
    cause
)