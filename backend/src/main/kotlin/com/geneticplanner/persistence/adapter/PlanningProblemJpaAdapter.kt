package com.geneticplanner.persistence.adapter

import com.geneticplanner.application.port.out.PlanningProblemPersistencePort
import com.geneticplanner.domain.PlanningProblem
import com.geneticplanner.persistence.mapper.PlanningProblemPersistenceMapper
import com.geneticplanner.persistence.repository.ActivityJpaRepository
import com.geneticplanner.persistence.repository.LocationJpaRepository
import com.geneticplanner.persistence.repository.PlanningConstraintJpaRepository
import com.geneticplanner.persistence.repository.PlanningProblemJpaRepository
import com.geneticplanner.persistence.repository.ResourceJpaRepository
import com.geneticplanner.persistence.repository.ResourceRequirementJpaRepository
import com.geneticplanner.persistence.repository.ResourceTypeJpaRepository
import com.geneticplanner.persistence.repository.TimeSlotJpaRepository
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class PlanningProblemJpaAdapter(
    private val planningProblemRepository: PlanningProblemJpaRepository,
    private val resourceTypeRepository: ResourceTypeJpaRepository,
    private val resourceRepository: ResourceJpaRepository,
    private val timeSlotRepository: TimeSlotJpaRepository,
    private val locationRepository: LocationJpaRepository,
    private val activityRepository: ActivityJpaRepository,
    private val resourceRequirementRepository: ResourceRequirementJpaRepository,
    private val planningConstraintRepository: PlanningConstraintJpaRepository,
    private val mapper: PlanningProblemPersistenceMapper,
    private val entityManager: EntityManager
) : PlanningProblemPersistencePort {

    @Transactional
    override fun save(problem: PlanningProblem): PlanningProblem {
        val exists = planningProblemRepository.existsById(problem.id)

        if (exists) {
            deleteChildren(problem.id)
            entityManager.flush()
        }

        val data = mapper.toPersistence(problem)

        if (exists) {
            val existing = entityManager.find(
                com.geneticplanner.persistence.entity.PlanningProblemEntity::class.java,
                problem.id
            )

            existing.name = data.problem.name
            existing.templateType = data.problem.templateType
            existing.planningHorizonStart = data.problem.planningHorizonStart
            existing.planningHorizonEnd = data.problem.planningHorizonEnd

            // Children must reference the managed root.
            data.resourceTypes.forEach { it.planningProblem = existing }
            data.resources.forEach { it.planningProblem = existing }
            data.timeSlots.forEach { it.planningProblem = existing }
            data.locations.forEach { it.planningProblem = existing }
            data.activities.forEach { it.planningProblem = existing }
            data.constraints.forEach { it.planningProblem = existing }
        } else {
            entityManager.persist(data.problem)
        }

        data.resourceTypes.forEach(entityManager::persist)
        data.resources.forEach(entityManager::persist)

        data.timeSlots.forEach(entityManager::persist)
        data.locations.forEach(entityManager::persist)

        data.activities.forEach(entityManager::persist)
        data.requirements.forEach(entityManager::persist)

        data.constraints.forEach(entityManager::persist)

        entityManager.flush()

        return problem
    }

    @Transactional(readOnly = true)
    override fun findById(id: String): PlanningProblem? {
        val problem = planningProblemRepository.findById(id)
            .orElse(null)
            ?: return null

        return loadAggregate(problem)
    }

    @Transactional(readOnly = true)
    override fun existsById(id: String): Boolean =
        planningProblemRepository.existsById(id)

    @Transactional(readOnly = true)
    override fun findAll(): List<PlanningProblem> =
        planningProblemRepository.findAll()
            .map { loadAggregate(it) }

    @Transactional
    override fun deleteById(id: String) {
        if (!planningProblemRepository.existsById(id)) {
            return
        }

        deleteChildren(id)
        entityManager.flush()

        planningProblemRepository.deleteById(id)
    }

    private fun loadAggregate(
        problem: com.geneticplanner.persistence.entity.PlanningProblemEntity
    ): PlanningProblem {
        val problemId = problem.id

        return mapper.toDomain(
            problem = problem,
            resourceTypes =
                resourceTypeRepository.findAllByPlanningProblemId(problemId),
            resources =
                resourceRepository.findAllByPlanningProblemId(problemId),
            timeSlots =
                timeSlotRepository.findAllByPlanningProblemId(problemId),
            locations =
                locationRepository.findAllByPlanningProblemId(problemId),
            activities =
                activityRepository.findAllByPlanningProblemId(problemId),
            requirements =
                resourceRequirementRepository
                    .findAllByActivityPlanningProblemId(problemId),
            constraints =
                planningConstraintRepository
                    .findAllByPlanningProblemId(problemId)
        )
    }

    private fun deleteChildren(problemId: String) {
        resourceRequirementRepository
            .deleteAllByActivityPlanningProblemId(problemId)

        entityManager.flush()

        activityRepository
            .deleteAllByPlanningProblemId(problemId)

        planningConstraintRepository
            .deleteAllByPlanningProblemId(problemId)

        resourceRepository
            .deleteAllByPlanningProblemId(problemId)

        entityManager.flush()

        resourceTypeRepository
            .deleteAllByPlanningProblemId(problemId)

        locationRepository
            .deleteAllByPlanningProblemId(problemId)

        timeSlotRepository
            .deleteAllByPlanningProblemId(problemId)

        entityManager.flush()
    }
}