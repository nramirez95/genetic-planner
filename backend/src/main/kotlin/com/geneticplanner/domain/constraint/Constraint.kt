package com.geneticplanner.domain.constraint

import com.geneticplanner.domain.Schedule

interface Constraint {

    val id: String

    val name: String

    val type: ConstraintType

    val weight: Double

    fun evaluate(schedule: Schedule): ConstraintResult
}