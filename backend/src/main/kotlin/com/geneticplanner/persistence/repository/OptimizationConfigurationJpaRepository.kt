package com.geneticplanner.persistence.repository

import com.geneticplanner.persistence.entity.OptimizationConfigurationEntity
import org.springframework.data.jpa.repository.JpaRepository

interface OptimizationConfigurationJpaRepository :
    JpaRepository<OptimizationConfigurationEntity, String>