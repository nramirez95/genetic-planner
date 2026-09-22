package com.geneticplanner.persistence.repository

import com.geneticplanner.persistence.entity.LocationEntity
import org.springframework.data.jpa.repository.JpaRepository

interface LocationJpaRepository :
    JpaRepository<LocationEntity, String> {

    fun findAllByPlanningProblemId(
        planningProblemId: String
    ): List<LocationEntity>

    fun deleteAllByPlanningProblemId(planningProblemId: String)
}