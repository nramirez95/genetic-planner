package com.geneticplanner.application

import com.geneticplanner.application.port.out.OptimizationConfigurationPersistencePort
import com.geneticplanner.application.port.out.PlanningProblemPersistencePort
import com.geneticplanner.domain.validation.ProblemValidator
import com.geneticplanner.genetic.GeneticEngine
import com.geneticplanner.genetic.OptimizationResult
import com.geneticplanner.genetic.config.GeneticAlgorithmPreset
import org.springframework.stereotype.Service

@Service
class ScheduleGenerationService(
    private val planningProblemPersistencePort:
    PlanningProblemPersistencePort,
    private val optimizationConfigurationPersistencePort:
    OptimizationConfigurationPersistencePort,
    private val geneticEngine: GeneticEngine,
    private val problemValidator: ProblemValidator
) {

    fun generate(
        planningId: String
    ): OptimizationResult {

        /*
         * 1. Load the planning aggregate.
         */
        val problem =
            planningProblemPersistencePort.findById(planningId)
                ?: throw PlanningNotFoundException(planningId)

        /*
         * 2. Validate the problem before executing the genetic engine.
         */
        val validationResult =
            problemValidator.validate(problem)

        if (!validationResult.isValid) {
            throw InvalidPlanningProblemException(
                planningId = planningId,
                validationResult = validationResult
            )
        }

        /*
         * 3. Use the persisted optimization configuration when
         * available. Otherwise fall back to the default M2 preset.
         */
        val config =
            optimizationConfigurationPersistencePort
                .findByPlanningProblemId(planningId)
                ?: GeneticAlgorithmPreset.DEFAULT.toConfig()

        /*
         * 4. Execute the M2 genetic engine.
         *
         * The application layer depends only on GeneticEngine.
         * No Jenetics implementation type leaks outside the engine.
         */
        return try {
            geneticEngine.optimize(
                problem = problem,
                config = config
            )
        } catch (exception: Exception) {
            throw ScheduleGenerationException(
                planningId = planningId,
                cause = exception
            )
        }
    }
}