package com.geneticplanner.genetic

import com.geneticplanner.domain.PlanningProblem
import com.geneticplanner.domain.candidate.AssignmentCandidateGenerator
import com.geneticplanner.domain.evaluation.ConstraintEvaluator
import com.geneticplanner.genetic.codec.ScheduleGenotypeCodec
import com.geneticplanner.genetic.config.GeneticAlgorithmConfig
import io.jenetics.*
import io.jenetics.engine.Engine
import io.jenetics.engine.Limits

class JeneticsEngine(
    private val candidateGenerator: AssignmentCandidateGenerator =
        AssignmentCandidateGenerator()
) : GeneticEngine {

    override fun optimize(
        problem: PlanningProblem,
        config: GeneticAlgorithmConfig
    ): OptimizationResult {

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
        val codec = ScheduleGenotypeCodec(
            planningProblemId = problem.id,
            candidateGenerationResult = candidateResult
        )

        /*
         * 3. Create the evaluator used by the fitness function.
         */
        val constraintEvaluator =
            ConstraintEvaluator(
                constraints = problem.constraints
            )

        /*
         * 4. Build the Jenetics engine.
         *
         * Fitness represents the total constraint penalty,
         * therefore lower values represent better schedules.
         *
         * Exactly eliteCount individuals survive unchanged.
         * The remaining population is generated as offspring.
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
         * 5. Execute the evolutionary process.
         */
        val evolutionResults =
            engine.stream()
                .limit(
                    Limits.byFixedGeneration(
                        config.generationLimit.toLong()
                    )
                )
                .toList()

        val bestPhenotype =
            evolutionResults
                .map { it.bestPhenotype() }
                .minBy { it.fitness() }

        val generationsExecuted =
            evolutionResults.size.toLong()

        /*
         * 6. Decode the best genotype into our domain model.
         */
        val bestSchedule =
            codec.decode(
                bestPhenotype.genotype()
            )

        /*
         * 7. Evaluate the best schedule again so the caller receives
         * the complete penalty and constraint breakdown.
         */
        val bestEvaluation =
            constraintEvaluator.evaluate(
                bestSchedule
            )

        /*
         * 8. Return the result without performing persistence.
         */

        return OptimizationResult(
            schedule = bestSchedule,
            evaluation = bestEvaluation,
            generationsExecuted = generationsExecuted
        )
    }

    private companion object {
        const val TOURNAMENT_SIZE = 3
    }
}