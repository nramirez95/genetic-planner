package com.geneticplanner.persistence.repository

import com.geneticplanner.persistence.entity.ResourceRequirementEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ResourceRequirementJpaRepository :
    JpaRepository<ResourceRequirementEntity, String> {

    fun findAllByActivityPlanningProblemId(
        planningProblemId: String
    ): List<ResourceRequirementEntity>

    fun deleteAllByActivityPlanningProblemId(planningProblemId: String)
}