package com.geneticplanner.persistence.repository

import com.geneticplanner.persistence.entity.PlanningProblemEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PlanningProblemJpaRepository :
    JpaRepository<PlanningProblemEntity, String>