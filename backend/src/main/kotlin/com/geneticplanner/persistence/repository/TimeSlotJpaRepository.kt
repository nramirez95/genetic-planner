package com.geneticplanner.persistence.repository

import com.geneticplanner.persistence.entity.TimeSlotEntity
import org.springframework.data.jpa.repository.JpaRepository

interface TimeSlotJpaRepository :
    JpaRepository<TimeSlotEntity, String> {

    fun findAllByPlanningProblemId(
        planningProblemId: String
    ): List<TimeSlotEntity>

    fun deleteAllByPlanningProblemId(planningProblemId: String)
}