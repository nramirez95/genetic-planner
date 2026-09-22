package com.geneticplanner.persistence.repository

import com.geneticplanner.persistence.entity.ActivityEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ActivityJpaRepository :
    JpaRepository<ActivityEntity, String> {

    fun findAllByPlanningProblemId(
        planningProblemId: String
    ): List<ActivityEntity>

    fun deleteAllByPlanningProblemId(planningProblemId: String)
}