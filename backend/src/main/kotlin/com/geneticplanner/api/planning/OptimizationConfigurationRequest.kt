package com.geneticplanner.api.planning

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min

data class OptimizationConfigurationRequest(

    @field:Min(1)
    val populationSize: Int,

    @field:Min(1)
    val generationLimit: Int,

    @field:DecimalMin("0.0")
    @field:DecimalMax("1.0")
    val mutationProbability: Double,

    @field:DecimalMin("0.0")
    @field:DecimalMax("1.0")
    val crossoverProbability: Double,

    @field:Min(0)
    val eliteCount: Int,

    val randomSeed: Long? = null
)