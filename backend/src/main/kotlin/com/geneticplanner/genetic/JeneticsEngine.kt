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
import io.jenetics.engine.EvolutionResult
import io.jenetics.engine.Limits
import io.jenetics.util.RandomRegistry
import java.time.Duration
import java.util.Random
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.Executor

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
         * Use the provided seed when available.
         *
         * When no seed is provided, generate one and record it so
         * that the execution can be reproduced afterwards.
         */
        val usedRandomSeed =
            config.randomSeed
                ?: ThreadLocalRandom
                    .current()
                    .nextLong()

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
         * 3. Create the constraint evaluator used by the
         * fitness function.
         */
        val constraintEvaluator =
            ConstraintEvaluator(
                constraints = problem.constraints
            )

        /*
         * 4. Execute the complete genetic process with a scoped
         * deterministic random generator.
         *
         * RandomRegistry.with(...) returns a Runner in Jenetics 9.1.
         * Runner.call(...) executes a value-returning operation
         * inside that random scope.
         */
        lateinit var evolutionResults:
                List<EvolutionResult<IntegerGene, Double>>

        RandomRegistry
            .with(Random(usedRandomSeed))
            .run {
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
                        .populationSize(
                            config.populationSize
                        )
                        .survivorsSize(
                            config.eliteCount
                        )
                        .survivorsSelector(
                            EliteSelector()
                        )
                        .offspringSelector(
                            TournamentSelector(
                                TOURNAMENT_SIZE
                            )
                        )
                        .alterers(
                            SinglePointCrossover<IntegerGene, Double>(
                                config.crossoverProbability
                            ),
                            Mutator<IntegerGene, Double>(
                                config.mutationProbability
                            )
                        )
                        .executor(DIRECT_EXECUTOR)
                        .build()

                evolutionResults =
                    engine.stream()
                        .limit(
                            Limits.byFixedGeneration(
                                config.generationLimit.toLong()
                            )
                        )
                        .toList()
            }

        /*
         * 5. Select the best phenotype found during all
         * executed generations.
         */
        val bestPhenotype =
            evolutionResults
                .map { evolutionResult ->
                    evolutionResult.bestPhenotype()
                }
                .minBy { phenotype ->
                    phenotype.fitness()
                }

        /*
         * 6. Record the actual number of generations executed.
         */
        val generationsExecuted =
            evolutionResults.size.toLong()

        /*
         * 7. Decode the best genotype into the planning domain.
         */
        val bestSchedule =
            codec.decode(
                bestPhenotype.genotype()
            )

        /*
         * 8. Evaluate the final schedule to expose the complete
         * constraint breakdown.
         */
        val bestEvaluation =
            constraintEvaluator.evaluate(
                bestSchedule
            )

        /*
         * 9. Measure the complete optimization execution time.
         */
        val executionTime =
            Duration.ofNanos(
                System.nanoTime() - startTime
            )

        /*
         * 10. Return a domain-oriented optimization result.
         *
         * No Jenetics-specific types escape this class.
         */
        return OptimizationResult(
            schedule = bestSchedule,
            evaluation = bestEvaluation,
            generationsExecuted = generationsExecuted,
            executionTime = executionTime,
            randomSeed = usedRandomSeed
        )
    }

    private companion object {

        const val TOURNAMENT_SIZE = 3

        val DIRECT_EXECUTOR =
            Executor { command ->
                command.run()
            }
    }
}