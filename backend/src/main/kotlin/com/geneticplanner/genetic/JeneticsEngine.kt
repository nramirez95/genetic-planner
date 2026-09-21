package com.geneticplanner.genetic

import com.geneticplanner.domain.PlanningProblem
import com.geneticplanner.domain.candidate.AssignmentCandidateGenerator
import com.geneticplanner.domain.evaluation.ConstraintEvaluator
import com.geneticplanner.genetic.codec.ScheduleGenotypeCodec
import com.geneticplanner.genetic.config.GeneticAlgorithmConfig
import io.jenetics.EliteSelector
import io.jenetics.IntegerGene
import io.jenetics.Mutator
import io.jenetics.Optimize
import io.jenetics.SinglePointCrossover
import io.jenetics.TournamentSelector
import io.jenetics.engine.Engine
import io.jenetics.engine.Limits
import java.time.Duration

class JeneticsEngine(
    private val candidateGenerator: AssignmentCandidateGenerator =
        AssignmentCandidateGenerator()
) : GeneticEngine {

    override fun optimize(
        problem: PlanningProblem,
        config: GeneticAlgorithmConfig
    ): OptimizationResult {

        val startTime =
            System.nanoTime()

        /*
         * 1. Generate all structurally valid assignment options.
         */
        val candidateResult =
            candidateGenerator.generate(problem)

        require(!candidateResult.hasMissingOptions) {
            "Cannot optimize planning problem because one or more " +
                    "activities have no assignment options: " +
                    candidateResult.activitiesWithoutOptions.joinToString()
        }

        /*
         * 2. Create the adapter between the planning domain
         * and the Jenetics genotype.
         */
        val codec =
            ScheduleGenotypeCodec(
                planningProblemId = problem.id,
                candidateGenerationResult = candidateResult
            )

        /*
         * 3. Create the constraint evaluator used as fitness function.
         */
        val constraintEvaluator =
            ConstraintEvaluator(
                constraints = problem.constraints
            )

        /*
         * 4. Build the genetic engine.
         */
        val engine =
            Engine.builder(
                { genotype ->
                    val schedule =
                        codec.decode(genotype)

                    constraintEvaluator
                        .evaluate(schedule)
                        .fitness
                },
                codec.createGenotype()
            )
                .optimize(Optimize.MINIMUM)
                .populationSize(config.populationSize)
                .survivorsSize(config.eliteCount)
                .survivorsSelector(
                    EliteSelector()
                )
                .offspringSelector(
                    TournamentSelector(TOURNAMENT_SIZE)
                )
                .alterers(
                    SinglePointCrossover<IntegerGene, Double>(
                        config.crossoverProbability
                    ),
                    Mutator<IntegerGene, Double>(
                        config.mutationProbability
                    )
                )
                .build()

        /*
         * 5. Execute all configured generations.
         */
        val evolutionResults =
            engine.stream()
                .limit(
                    Limits.byFixedGeneration(
                        config.generationLimit.toLong()
                    )
                )
                .toList()

        /*
         * 6. Select the best phenotype found during the complete run.
         */
        val bestPhenotype =
            evolutionResults
                .map { it.bestPhenotype() }
                .minBy { it.fitness() }

        val generationsExecuted =
            evolutionResults.size.toLong()

        /*
         * 7. Decode and evaluate the best schedule.
         */
        val bestSchedule =
            codec.decode(
                bestPhenotype.genotype()
            )

        val bestEvaluation =
            constraintEvaluator.evaluate(
                bestSchedule
            )

        /*
         * 8. Measure complete optimization execution time.
         */
        val executionTime =
            Duration.ofNanos(
                System.nanoTime() - startTime
            )

        /*
         * 9. Return a domain-oriented result.
         */
        return OptimizationResult(
            schedule = bestSchedule,
            evaluation = bestEvaluation,
            generationsExecuted = generationsExecuted,
            executionTime = executionTime
        )
    }

    private companion object {
        const val TOURNAMENT_SIZE = 3
    }
}