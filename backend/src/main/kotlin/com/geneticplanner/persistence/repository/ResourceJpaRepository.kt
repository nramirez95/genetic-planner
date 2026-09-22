package com.geneticplanner.persistence.repository

import com.geneticplanner.persistence.entity.ResourceEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ResourceJpaRepository :
    JpaRepository<ResourceEntity, String> {

    fun findAllByPlanningProblemId(
        planningProblemId: String
    ): List<ResourceEntity>

    fun deleteAllByPlanningProblemId(planningProblemId: String)
}