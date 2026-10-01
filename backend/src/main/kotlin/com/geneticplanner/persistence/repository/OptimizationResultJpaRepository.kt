package com.geneticplanner.persistence.repository

import com.geneticplanner.persistence.entity.OptimizationResultEntity
import org.springframework.data.jpa.repository.JpaRepository

interface OptimizationResultJpaRepository :
    JpaRepository<OptimizationResultEntity, String>