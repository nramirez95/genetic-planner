package com.geneticplanner.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "optimization_configuration")
class OptimizationConfigurationEntity(

    @Id
    @Column(name = "planning_problem_id", nullable = false, length = 255)
    var planningProblemId: String,

    @Column(name = "population_size", nullable = false)
    var populationSize: Int,

    @Column(name = "generation_limit", nullable = false)
    var generationLimit: Int,

    @Column(name = "mutation_probability", nullable = false)
    var mutationProbability: Double,

    @Column(name = "crossover_probability", nullable = false)
    var crossoverProbability: Double,

    @Column(name = "elite_count", nullable = false)
    var eliteCount: Int,

    @Column(name = "random_seed")
    var randomSeed: Long? = null
)