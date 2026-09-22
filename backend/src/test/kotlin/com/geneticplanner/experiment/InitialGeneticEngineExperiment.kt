package com.geneticplanner.experiment

import com.geneticplanner.dataset.SmallFeasibleDataset
import com.geneticplanner.domain.constraint.ConstraintType
import com.geneticplanner.genetic.JeneticsEngine
import com.geneticplanner.genetic.config.GeneticAlgorithmPreset

fun main() {

    val engine =
        JeneticsEngine()

    val problem =
        SmallFeasibleDataset.create()

    val seed =
        42L

    println(
        "preset,fitness,hardViolations," +
                "softPenalty,executionTimeMs,generations,seed"
    )

    GeneticAlgorithmPreset.entries.forEach { preset ->

        val config =
            preset
                .toConfig()
                .copy(
                    randomSeed = seed
                )

        val result =
            engine.optimize(
                problem = problem,
                config = config
            )

        val hardViolations =
            result.constraintResults
                .filter {
                    it.type == ConstraintType.HARD
                }
                .sumOf {
                    it.violations
                }

        println(
            listOf(
                preset.name,
                result.fitness,
                hardViolations,
                result.softPenalty,
                result.executionTime.toMillis(),
                result.generationsExecuted,
                result.randomSeed
            ).joinToString(",")
        )
    }
}