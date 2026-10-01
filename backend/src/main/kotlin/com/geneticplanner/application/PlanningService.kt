package com.geneticplanner.application

import com.geneticplanner.application.model.PlanningConfiguration
import com.geneticplanner.application.port.out.OptimizationConfigurationPersistencePort
import com.geneticplanner.application.port.out.PlanningProblemPersistencePort
import com.geneticplanner.domain.PlanningProblem
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PlanningService(
    private val persistencePort: PlanningProblemPersistencePort,
    private val optimizationConfigurationPersistencePort:
    OptimizationConfigurationPersistencePort
) {

    /*
     * Existing PlanningProblem API.
     *
     * Kept for compatibility with existing application code and tests.
     */
    fun create(problem: PlanningProblem): PlanningProblem {
        require(!persistencePort.existsById(problem.id)) {
            throw PlanningAlreadyExistsException(problem.id)
        }

        return persistencePort.save(problem)
    }

    fun findAll(): List<PlanningProblem> =
        persistencePort.findAll()

    fun findById(id: String): PlanningProblem? =
        persistencePort.findById(id)

    fun update(
        id: String,
        problem: PlanningProblem
    ): PlanningProblem? {
        if (!persistencePort.existsById(id)) {
            return null
        }

        require(id == problem.id) {
            "Planning ID in path and body must match."
        }

        return persistencePort.save(problem)
    }

    fun delete(id: String): Boolean {
        if (!persistencePort.existsById(id)) {
            return false
        }

        persistencePort.deleteById(id)
        return true
    }

    /*
     * Complete planning configuration API.
     *
     * This includes both the PlanningProblem and the optional
     * genetic algorithm configuration.
     */

    @Transactional
    fun createConfiguration(
        configuration: PlanningConfiguration
    ): PlanningConfiguration {

        val problem = configuration.problem

        require(!persistencePort.existsById(problem.id)) {
            throw PlanningAlreadyExistsException(problem.id)
        }

        val savedProblem =
            persistencePort.save(problem)

        configuration.optimizationConfiguration?.let {
            optimizationConfigurationPersistencePort.save(
                planningProblemId = problem.id,
                configuration = it
            )
        }

        return PlanningConfiguration(
            problem = savedProblem,
            optimizationConfiguration =
                configuration.optimizationConfiguration
        )
    }

    @Transactional(readOnly = true)
    fun findConfigurationById(
        id: String
    ): PlanningConfiguration? {

        val problem =
            persistencePort.findById(id)
                ?: return null

        val optimizationConfiguration =
            optimizationConfigurationPersistencePort
                .findByPlanningProblemId(id)

        return PlanningConfiguration(
            problem = problem,
            optimizationConfiguration = optimizationConfiguration
        )
    }

    @Transactional(readOnly = true)
    fun findAllConfigurations(): List<PlanningConfiguration> =
        persistencePort.findAll()
            .map { problem ->
                PlanningConfiguration(
                    problem = problem,
                    optimizationConfiguration =
                        optimizationConfigurationPersistencePort
                            .findByPlanningProblemId(problem.id)
                )
            }

    @Transactional
    fun updateConfiguration(
        id: String,
        configuration: PlanningConfiguration
    ): PlanningConfiguration? {

        if (!persistencePort.existsById(id)) {
            return null
        }

        require(id == configuration.problem.id) {
            "Planning ID in path and body must match."
        }

        val savedProblem =
            persistencePort.save(configuration.problem)

        val optimizationConfiguration =
            configuration.optimizationConfiguration

        if (optimizationConfiguration != null) {
            optimizationConfigurationPersistencePort.save(
                planningProblemId = id,
                configuration = optimizationConfiguration
            )
        } else {
            optimizationConfigurationPersistencePort
                .deleteByPlanningProblemId(id)
        }

        return PlanningConfiguration(
            problem = savedProblem,
            optimizationConfiguration = optimizationConfiguration
        )
    }

    @Transactional
    fun deleteConfiguration(id: String): Boolean {

        if (!persistencePort.existsById(id)) {
            return false
        }

        /*
         * Explicit deletion makes the application semantics clear.
         * The database FK ON DELETE CASCADE remains an additional
         * safeguard.
         */
        optimizationConfigurationPersistencePort
            .deleteByPlanningProblemId(id)

        persistencePort.deleteById(id)

        return true
    }
}