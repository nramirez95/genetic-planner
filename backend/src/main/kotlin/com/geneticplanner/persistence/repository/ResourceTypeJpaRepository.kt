package com.geneticplanner.persistence.repository

import com.geneticplanner.persistence.entity.ResourceTypeEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ResourceTypeJpaRepository :
    JpaRepository<ResourceTypeEntity, String> {

    fun findAllByPlanningProblemId(
        planningProblemId: String
    ): List<ResourceTypeEntity>

    fun deleteAllByPlanningProblemId(planningProblemId: String)
}