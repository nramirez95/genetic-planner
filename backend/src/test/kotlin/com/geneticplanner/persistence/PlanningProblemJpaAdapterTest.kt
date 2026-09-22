package com.geneticplanner.persistence

import com.geneticplanner.application.port.out.PlanningProblemPersistencePort
import com.geneticplanner.domain.*
import com.geneticplanner.domain.constraint.hard.AvailabilityConstraint
import com.geneticplanner.domain.constraint.hard.LocationCapacityConstraint
import com.geneticplanner.domain.constraint.hard.NoOverlapConstraint
import com.geneticplanner.domain.constraint.hard.RequiredResourceConstraint
import com.geneticplanner.domain.constraint.soft.BalancedWorkloadConstraint
import com.geneticplanner.domain.constraint.soft.MaxConsecutiveConstraint
import com.geneticplanner.domain.constraint.soft.PreferredTimeSlotConstraint
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@SpringBootTest
@ActiveProfiles("test")
class PlanningProblemJpaAdapterTest {

    @Autowired
    private lateinit var persistencePort: PlanningProblemPersistencePort

    @AfterEach
    fun cleanUp() {
        persistencePort.deleteById("problem-1")
    }

    @Test
    fun `should save and reconstruct planning problem`() {
        val problem = createPlanningProblem()

        persistencePort.save(problem)

        val loaded = persistencePort.findById(problem.id)

        assertNotNull(loaded)
        loaded!!

        assertEquals(problem.id, loaded.id)
        assertEquals(problem.name, loaded.name)
        assertEquals(problem.templateType, loaded.templateType)
        assertEquals(problem.planningHorizon, loaded.planningHorizon)

        assertEquals(problem.resourceTypes.toSet(), loaded.resourceTypes.toSet())
        assertEquals(problem.resources.toSet(), loaded.resources.toSet())
        assertEquals(problem.timeSlots.toSet(), loaded.timeSlots.toSet())
        assertEquals(problem.locations.toSet(), loaded.locations.toSet())
        assertEquals(problem.activities.toSet(), loaded.activities.toSet())
    }

    @Test
    fun `should update existing planning problem`() {
        val original = createPlanningProblem()

        persistencePort.save(original)

        val updated = original.copy(
            name = "Updated academic planning",
            templateType = "UPDATED_ACADEMIC"
        )

        persistencePort.save(updated)

        val loaded = persistencePort.findById(original.id)

        assertNotNull(loaded)
        loaded!!

        assertEquals(original.id, loaded.id)
        assertEquals("Updated academic planning", loaded.name)
        assertEquals("UPDATED_ACADEMIC", loaded.templateType)

        // The update must replace the aggregate, not duplicate it.
        assertEquals(1, loaded.resourceTypes.size)
        assertEquals(1, loaded.resources.size)
        assertEquals(1, loaded.activities.size)
        assertEquals(1, loaded.timeSlots.size)
        assertEquals(1, loaded.locations.size)
    }

    @Test
    fun `should delete planning problem`() {
        val problem = createPlanningProblem()

        persistencePort.save(problem)

        assertEquals(true, persistencePort.existsById(problem.id))

        persistencePort.deleteById(problem.id)

        assertEquals(false, persistencePort.existsById(problem.id))
        assertEquals(null, persistencePort.findById(problem.id))
    }

    @Test
    fun `should preserve null and empty restriction semantics`() {
        val base = createPlanningProblem()

        val unrestrictedRequirement = ResourceRequirement(
            id = "requirement-unrestricted",
            resourceTypeId = "teacher",
            quantity = 1,
            candidateResourceIds = null
        )

        val restrictedEmptyRequirement = ResourceRequirement(
            id = "requirement-empty",
            resourceTypeId = "teacher",
            quantity = 1,
            candidateResourceIds = emptySet()
        )

        val unrestrictedActivity = Activity(
            id = "activity-unrestricted",
            name = "Unrestricted activity",
            resourceRequirements = listOf(unrestrictedRequirement),
            allowedTimeSlotIds = null,
            allowedLocationIds = null
        )

        val restrictedEmptyActivity = Activity(
            id = "activity-empty",
            name = "Explicitly restricted activity",
            resourceRequirements = listOf(restrictedEmptyRequirement),
            allowedTimeSlotIds = emptySet(),
            allowedLocationIds = emptySet()
        )

        val problem = base.copy(
            activities = listOf(
                unrestrictedActivity,
                restrictedEmptyActivity
            )
        )

        persistencePort.save(problem)

        val loaded = persistencePort.findById(problem.id)

        assertNotNull(loaded)
        loaded!!

        val activitiesById = loaded.activities.associateBy { it.id }

        val loadedUnrestricted =
            activitiesById.getValue("activity-unrestricted")

        assertEquals(null, loadedUnrestricted.allowedTimeSlotIds)
        assertEquals(null, loadedUnrestricted.allowedLocationIds)
        assertEquals(
            null,
            loadedUnrestricted.resourceRequirements
                .single()
                .candidateResourceIds
        )

        val loadedRestrictedEmpty =
            activitiesById.getValue("activity-empty")

        assertEquals(
            emptySet<String>(),
            loadedRestrictedEmpty.allowedTimeSlotIds
        )
        assertEquals(
            emptySet<String>(),
            loadedRestrictedEmpty.allowedLocationIds
        )
        assertEquals(
            emptySet<String>(),
            loadedRestrictedEmpty.resourceRequirements
                .single()
                .candidateResourceIds
        )
    }

    @Test
    fun `should persist and reconstruct all constraint types`() {
        val base = createPlanningProblem()

        val problem = base.copy(
            constraints = listOf(
                NoOverlapConstraint(
                    id = "constraint-no-overlap",
                    name = "No overlap",
                    weight = 1000.0,
                    timeSlots = base.timeSlots
                ),
                AvailabilityConstraint(
                    id = "constraint-availability",
                    name = "Resource availability",
                    weight = 1000.0,
                    availableTimeSlotIdsByResourceId = mapOf(
                        "teacher-1" to setOf("slot-1")
                    )
                ),
                RequiredResourceConstraint(
                    id = "constraint-required-resource",
                    name = "Required resources",
                    weight = 1000.0,
                    activities = base.activities,
                    resources = base.resources
                ),
                LocationCapacityConstraint(
                    id = "constraint-location-capacity",
                    name = "Location capacity",
                    weight = 1000.0,
                    activities = base.activities,
                    locations = base.locations
                ),
                PreferredTimeSlotConstraint(
                    id = "constraint-preferred-slot",
                    name = "Preferred time slot",
                    weight = 10.0,
                    preferredTimeSlotIdsByActivityId = mapOf(
                        "activity-1" to setOf("slot-1")
                    )
                ),
                MaxConsecutiveConstraint(
                    id = "constraint-max-consecutive",
                    name = "Maximum consecutive assignments",
                    weight = 5.0,
                    maxConsecutive = 3,
                    timeSlots = base.timeSlots
                ),
                BalancedWorkloadConstraint(
                    id = "constraint-balanced-workload",
                    name = "Balanced workload",
                    weight = 5.0,
                    resourceIds = setOf("teacher-1"),
                    allowedDifference = 1
                )
            )
        )

        persistencePort.save(problem)

        val loaded = persistencePort.findById(problem.id)

        assertNotNull(loaded)
        loaded!!

        assertEquals(7, loaded.constraints.size)

        val constraintsById = loaded.constraints.associateBy { it.id }

        val noOverlap =
            constraintsById.getValue("constraint-no-overlap")

        assertEquals("No overlap", noOverlap.name)
        assertEquals(1000.0, noOverlap.weight)

        val preferredConstraint =
            constraintsById.getValue("constraint-preferred-slot")

        assertEquals("Preferred time slot", preferredConstraint.name)
        assertEquals(10.0, preferredConstraint.weight)

        assertEquals(
            NoOverlapConstraint::class,
            constraintsById.getValue("constraint-no-overlap")::class
        )

        assertEquals(
            RequiredResourceConstraint::class,
            constraintsById.getValue("constraint-required-resource")::class
        )

        assertEquals(
            LocationCapacityConstraint::class,
            constraintsById.getValue("constraint-location-capacity")::class
        )

        val availability =
            constraintsById.getValue("constraint-availability")
                    as AvailabilityConstraint

        assertEquals(
            mapOf("teacher-1" to setOf("slot-1")),
            availability.availableTimeSlotIdsByResourceId
        )

        val preferred =
            constraintsById.getValue("constraint-preferred-slot")
                    as PreferredTimeSlotConstraint

        assertEquals(
            mapOf("activity-1" to setOf("slot-1")),
            preferred.preferredTimeSlotIdsByActivityId
        )

        val maxConsecutive =
            constraintsById.getValue("constraint-max-consecutive")
                    as MaxConsecutiveConstraint

        assertEquals(3, maxConsecutive.maxConsecutive)

        val balanced =
            constraintsById.getValue("constraint-balanced-workload")
                    as BalancedWorkloadConstraint

        assertEquals(
            setOf("teacher-1"),
            balanced.resourceIds
        )
        assertEquals(1, balanced.allowedDifference)
    }

    private fun createPlanningProblem(): PlanningProblem {
        val resourceType = ResourceType(
            id = "teacher",
            name = "Teacher"
        )

        val resource = Resource(
            id = "teacher-1",
            name = "Teacher 1",
            typeId = resourceType.id,
            attributes = mapOf(
                "department" to "Computer Science"
            )
        )

        val timeSlot = TimeSlot(
            id = "slot-1",
            start = LocalDateTime.of(2026, 10, 5, 9, 0),
            end = LocalDateTime.of(2026, 10, 5, 10, 0),
            label = "Monday 09:00"
        )

        val location = Location(
            id = "room-1",
            name = "Room 1",
            type = "CLASSROOM",
            capacity = 30,
            attributes = mapOf(
                "building" to "A"
            )
        )

        val requirement = ResourceRequirement(
            id = "requirement-1",
            resourceTypeId = resourceType.id,
            quantity = 1,
            candidateResourceIds = setOf(resource.id)
        )

        val activity = Activity(
            id = "activity-1",
            name = "Algorithms",
            type = "CLASS",
            resourceRequirements = listOf(requirement),
            allowedTimeSlotIds = setOf(timeSlot.id),
            allowedLocationIds = setOf(location.id),
            requiredLocationCapacity = 20,
            attributes = mapOf(
                "course" to "Computer Engineering"
            )
        )

        return PlanningProblem(
            id = "problem-1",
            name = "Academic planning",
            templateType = "ACADEMIC",
            planningHorizon = PlanningHorizon(
                start = LocalDateTime.of(2026, 10, 5, 8, 0),
                end = LocalDateTime.of(2026, 10, 9, 20, 0)
            ),
            resourceTypes = listOf(resourceType),
            resources = listOf(resource),
            activities = listOf(activity),
            timeSlots = listOf(timeSlot),
            locations = listOf(location),
            constraints = emptyList()
        )
    }
}