package com.geneticplanner.application

import com.geneticplanner.application.port.out.PlanningProblemPersistencePort
import com.geneticplanner.domain.PlanningProblem
import org.springframework.stereotype.Service

@Service
class PlanningService(
    private val persistencePort: PlanningProblemPersistencePort
) {

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
}