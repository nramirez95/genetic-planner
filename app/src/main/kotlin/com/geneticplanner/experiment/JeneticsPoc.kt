package com.geneticplanner.experiment

import io.jenetics.Genotype
import io.jenetics.IntegerChromosome
import io.jenetics.IntegerGene
import io.jenetics.engine.Engine
import io.jenetics.engine.EvolutionResult
import io.jenetics.util.Factory

fun main() {

    val genotypeFactory: Factory<Genotype<IntegerGene>> =
        Genotype.of(
            IntegerChromosome.of(0, 3, 1), // Activity A → 0..2
            IntegerChromosome.of(0, 9, 1), // Activity B → 0..8
            IntegerChromosome.of(0, 5, 1)  // Activity C → 0..4
        )

    val engine = Engine.builder(
        ::fitness,
        genotypeFactory
    )
        .populationSize(50)
        .build()

    val best = engine.stream()
        .limit(100)
        .collect(EvolutionResult.toBestPhenotype())

    println("Best genotype: ${best.genotype()}")
    println("Best fitness: ${best.fitness()}")

    println(
        "Decoded: ${
            best.genotype()
                .map { chromosome ->
                    chromosome.gene().allele()
                }
        }"
    )
}

fun fitness(
    genotype: Genotype<IntegerGene>
): Int =
    genotype
        .sumOf { chromosome ->
            chromosome.gene().allele()
        }