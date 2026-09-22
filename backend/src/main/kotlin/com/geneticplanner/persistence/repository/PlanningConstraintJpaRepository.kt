package com.geneticplanner.persistence.repository

import com.geneticplanner.persistence.entity.PlanningConstraintEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PlanningConstraintJpaRepository :
    JpaRepository<PlanningConstraintEntity, String> {

    fun findAllByPlanningProblemId(
        planningProblemId: String
    ): List<PlanningConstraintEntity>

    fun deleteAllByPlanningProblemId(planningProblemId: String)
}