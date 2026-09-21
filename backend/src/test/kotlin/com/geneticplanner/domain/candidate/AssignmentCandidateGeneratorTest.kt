package com.geneticplanner.domain.candidate

import com.geneticplanner.domain.Activity
import com.geneticplanner.domain.Location
import com.geneticplanner.domain.PlanningHorizon
import com.geneticplanner.domain.PlanningProblem
import com.geneticplanner.domain.Resource
import com.geneticplanner.domain.ResourceRequirement
import com.geneticplanner.domain.ResourceType
import com.geneticplanner.domain.TimeSlot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class AssignmentCandidateGeneratorTest {

    private val generator = AssignmentCandidateGenerator()

    @Test
    fun `generates all structurally valid assignment options`() {
        val activity = activity(
            allowedTimeSlotIds = setOf("slot-1", "slot-2"),
            allowedLocationIds = setOf("location-1", "location-2"),
            candidateResourceIds = setOf("worker-1", "worker-2")
        )

        val problem = problem(
            activities = listOf(activity)
        )

        val result = generator.generate(problem)

        val options = result.candidatesByActivity
            .single()
            .options

        // 2 time slots
        // x 2 resources
        // x 2 locations
        // = 8 options

        assertEquals(8, options.size)
        assertTrue(result.activitiesWithoutOptions.isEmpty())
        assertTrue(!result.hasMissingOptions)

        assertTrue(
            options.all {
                it.activityId == "activity-1"
            }
        )

        assertEquals(
            setOf("slot-1", "slot-2"),
            options.map { it.timeSlotId }.toSet()
        )

        assertEquals(
            setOf("location-1", "location-2"),
            options.mapNotNull { it.locationId }.toSet()
        )

        assertEquals(
            setOf("worker-1", "worker-2"),
            options
                .flatMap {
                    it.resourceAssignments
                        .getValue("worker-requirement")
                }
                .toSet()
        )
    }

    @Test
    fun `only allowed time slots are used`() {
        val activity = activity(
            allowedTimeSlotIds = setOf("slot-2"),
            allowedLocationIds = setOf("location-1"),
            candidateResourceIds = setOf("worker-1")
        )

        val result = generator.generate(
            problem(
                activities = listOf(activity)
            )
        )

        val options = result.candidatesByActivity
            .single()
            .options

        assertEquals(1, options.size)

        assertTrue(
            options.all {
                it.timeSlotId == "slot-2"
            }
        )
    }

    @Test
    fun `all time slots are available when allowed time slots are null`() {
        val activity = activity(
            allowedTimeSlotIds = null,
            allowedLocationIds = setOf("location-1"),
            candidateResourceIds = setOf("worker-1")
        )

        val result = generator.generate(
            problem(
                activities = listOf(activity)
            )
        )

        val options = result.candidatesByActivity
            .single()
            .options

        assertEquals(3, options.size)

        assertEquals(
            setOf("slot-1", "slot-2", "slot-3"),
            options.map { it.timeSlotId }.toSet()
        )
    }

    @Test
    fun `empty allowed time slots produce no options`() {
        val activity = activity(
            allowedTimeSlotIds = emptySet(),
            allowedLocationIds = setOf("location-1"),
            candidateResourceIds = setOf("worker-1")
        )

        val result = generator.generate(
            problem(
                activities = listOf(activity)
            )
        )

        val options = result.candidatesByActivity
            .single()
            .options

        assertTrue(options.isEmpty())
        assertEquals(
            listOf("activity-1"),
            result.activitiesWithoutOptions
        )
        assertTrue(result.hasMissingOptions)
    }

    @Test
    fun `only allowed locations are used`() {
        val activity = activity(
            allowedTimeSlotIds = setOf("slot-1"),
            allowedLocationIds = setOf("location-2"),
            candidateResourceIds = setOf("worker-1")
        )

        val result = generator.generate(
            problem(
                activities = listOf(activity)
            )
        )

        val options = result.candidatesByActivity
            .single()
            .options

        assertEquals(1, options.size)
        assertEquals(
            "location-2",
            options.single().locationId
        )
    }

    @Test
    fun `all locations are available when allowed locations are null`() {
        val activity = activity(
            allowedTimeSlotIds = setOf("slot-1"),
            allowedLocationIds = null,
            candidateResourceIds = setOf("worker-1")
        )

        val result = generator.generate(
            problem(
                activities = listOf(activity)
            )
        )

        val options = result.candidatesByActivity
            .single()
            .options

        assertEquals(2, options.size)

        assertEquals(
            setOf("location-1", "location-2"),
            options.mapNotNull { it.locationId }.toSet()
        )
    }

    @Test
    fun `problem without locations generates option with null location`() {
        val activity = activity(
            allowedTimeSlotIds = setOf("slot-1"),
            allowedLocationIds = null,
            candidateResourceIds = setOf("worker-1")
        )

        val problem = problem(
            activities = listOf(activity),
            locations = emptyList()
        )

        val result = generator.generate(problem)

        val options = result.candidatesByActivity
            .single()
            .options

        assertEquals(1, options.size)
        assertEquals(null, options.single().locationId)
    }

    @Test
    fun `empty allowed locations produce no options`() {
        val activity = activity(
            allowedTimeSlotIds = setOf("slot-1"),
            allowedLocationIds = emptySet(),
            candidateResourceIds = setOf("worker-1")
        )

        val result = generator.generate(
            problem(
                activities = listOf(activity)
            )
        )

        assertTrue(
            result.candidatesByActivity
                .single()
                .options
                .isEmpty()
        )

        assertEquals(
            listOf("activity-1"),
            result.activitiesWithoutOptions
        )
    }

    @Test
    fun `only candidate resources are used`() {
        val activity = activity(
            allowedTimeSlotIds = setOf("slot-1"),
            allowedLocationIds = setOf("location-1"),
            candidateResourceIds = setOf("worker-2")
        )

        val result = generator.generate(
            problem(
                activities = listOf(activity)
            )
        )

        val options = result.candidatesByActivity
            .single()
            .options

        assertEquals(1, options.size)

        assertEquals(
            listOf("worker-2"),
            options.single()
                .resourceAssignments
                .getValue("worker-requirement")
        )
    }

    @Test
    fun `all resources of required type are candidates when candidate set is null`() {
        val activity = activity(
            allowedTimeSlotIds = setOf("slot-1"),
            allowedLocationIds = setOf("location-1"),
            candidateResourceIds = null
        )

        val result = generator.generate(
            problem(
                activities = listOf(activity)
            )
        )

        val options = result.candidatesByActivity
            .single()
            .options

        assertEquals(2, options.size)

        assertEquals(
            setOf("worker-1", "worker-2"),
            options
                .flatMap {
                    it.resourceAssignments
                        .getValue("worker-requirement")
                }
                .toSet()
        )
    }

    @Test
    fun `resources with wrong type are never used`() {
        val activity = activity(
            allowedTimeSlotIds = setOf("slot-1"),
            allowedLocationIds = setOf("location-1"),
            candidateResourceIds = null
        )

        val customResources = listOf(
            Resource(
                id = "worker-1",
                name = "Worker 1",
                typeId = "worker"
            ),
            Resource(
                id = "worker-2",
                name = "Worker 2",
                typeId = "worker"
            ),
            Resource(
                id = "machine-1",
                name = "Machine 1",
                typeId = "machine"
            )
        )

        val result = generator.generate(
            problem(
                activities = listOf(activity),
                resources = customResources
            )
        )

        val usedResourceIds = result.candidatesByActivity
            .single()
            .options
            .flatMap {
                it.resourceAssignments
                    .getValue("worker-requirement")
            }
            .toSet()

        assertEquals(
            setOf("worker-1", "worker-2"),
            usedResourceIds
        )

        assertTrue(
            "machine-1" !in usedResourceIds
        )
    }

    @Test
    fun `quantity greater than one generates distinct resource combinations`() {
        val requirement = ResourceRequirement(
            id = "worker-requirement",
            resourceTypeId = "worker",
            quantity = 2,
            candidateResourceIds = setOf(
                "worker-1",
                "worker-2",
                "worker-3"
            )
        )

        val activity = Activity(
            id = "activity-1",
            name = "Activity 1",
            resourceRequirements = listOf(requirement),
            allowedTimeSlotIds = setOf("slot-1"),
            allowedLocationIds = setOf("location-1")
        )

        val resources = listOf(
            worker("worker-1"),
            worker("worker-2"),
            worker("worker-3")
        )

        val result = generator.generate(
            problem(
                activities = listOf(activity),
                resources = resources
            )
        )

        val options = result.candidatesByActivity
            .single()
            .options

        // C(3, 2) = 3

        assertEquals(3, options.size)

        val combinations = options
            .map {
                it.resourceAssignments
                    .getValue("worker-requirement")
                    .toSet()
            }
            .toSet()

        assertEquals(
            setOf(
                setOf("worker-1", "worker-2"),
                setOf("worker-1", "worker-3"),
                setOf("worker-2", "worker-3")
            ),
            combinations
        )

        assertTrue(
            options.all {
                val assigned =
                    it.resourceAssignments
                        .getValue("worker-requirement")

                assigned.size == assigned.distinct().size
            }
        )
    }

    @Test
    fun `multiple resource requirements generate cartesian product`() {
        val workerRequirement = ResourceRequirement(
            id = "worker-requirement",
            resourceTypeId = "worker",
            quantity = 1,
            candidateResourceIds = setOf(
                "worker-1",
                "worker-2"
            )
        )

        val machineRequirement = ResourceRequirement(
            id = "machine-requirement",
            resourceTypeId = "machine",
            quantity = 1,
            candidateResourceIds = setOf(
                "machine-1",
                "machine-2"
            )
        )

        val activity = Activity(
            id = "activity-1",
            name = "Activity 1",
            resourceRequirements = listOf(
                workerRequirement,
                machineRequirement
            ),
            allowedTimeSlotIds = setOf("slot-1"),
            allowedLocationIds = setOf("location-1")
        )

        val resources = listOf(
            worker("worker-1"),
            worker("worker-2"),
            Resource(
                id = "machine-1",
                name = "Machine 1",
                typeId = "machine"
            ),
            Resource(
                id = "machine-2",
                name = "Machine 2",
                typeId = "machine"
            )
        )

        val result = generator.generate(
            problem(
                activities = listOf(activity),
                resources = resources
            )
        )

        val options = result.candidatesByActivity
            .single()
            .options

        // 2 worker choices x 2 machine choices = 4

        assertEquals(4, options.size)

        assertTrue(
            options.all {
                it.resourceAssignments.keys ==
                        setOf(
                            "worker-requirement",
                            "machine-requirement"
                        )
            }
        )
    }

    @Test
    fun `activity without resource requirements can still generate options`() {
        val activity = Activity(
            id = "activity-1",
            name = "Activity 1",
            resourceRequirements = emptyList(),
            allowedTimeSlotIds = setOf("slot-1"),
            allowedLocationIds = setOf("location-1")
        )

        val result = generator.generate(
            problem(
                activities = listOf(activity)
            )
        )

        val options = result.candidatesByActivity
            .single()
            .options

        assertEquals(1, options.size)
        assertTrue(
            options.single()
                .resourceAssignments
                .isEmpty()
        )
    }

    @Test
    fun `different activities can have different number of options`() {
        val activityA = activity(
            id = "activity-a",
            allowedTimeSlotIds = setOf("slot-1"),
            allowedLocationIds = setOf("location-1"),
            candidateResourceIds = setOf("worker-1")
        )

        val activityB = activity(
            id = "activity-b",
            allowedTimeSlotIds = setOf(
                "slot-1",
                "slot-2"
            ),
            allowedLocationIds = setOf(
                "location-1",
                "location-2"
            ),
            candidateResourceIds = setOf(
                "worker-1",
                "worker-2"
            )
        )

        val result = generator.generate(
            problem(
                activities = listOf(
                    activityA,
                    activityB
                )
            )
        )

        val activityAOptions =
            result.candidatesByActivity
                .first { it.activityId == "activity-a" }
                .options

        val activityBOptions =
            result.candidatesByActivity
                .first { it.activityId == "activity-b" }
                .options

        assertEquals(1, activityAOptions.size)

        // 2 slots x 2 locations x 2 resources
        assertEquals(8, activityBOptions.size)
    }

    @Test
    fun `activity without enough resources produces no options`() {
        val requirement = ResourceRequirement(
            id = "worker-requirement",
            resourceTypeId = "worker",
            quantity = 3,
            candidateResourceIds = setOf(
                "worker-1",
                "worker-2"
            )
        )

        val activity = Activity(
            id = "activity-1",
            name = "Activity 1",
            resourceRequirements = listOf(requirement),
            allowedTimeSlotIds = setOf("slot-1"),
            allowedLocationIds = setOf("location-1")
        )

        val result = generator.generate(
            problem(
                activities = listOf(activity)
            )
        )

        assertTrue(
            result.candidatesByActivity
                .single()
                .options
                .isEmpty()
        )

        assertEquals(
            listOf("activity-1"),
            result.activitiesWithoutOptions
        )

        assertTrue(result.hasMissingOptions)
    }

    @Test
    fun `invalid references are never included in generated options`() {
        val activity = activity(
            allowedTimeSlotIds = setOf(
                "slot-1",
                "unknown-slot"
            ),
            allowedLocationIds = setOf(
                "location-1",
                "unknown-location"
            ),
            candidateResourceIds = setOf(
                "worker-1",
                "unknown-resource"
            )
        )

        val result = generator.generate(
            problem(
                activities = listOf(activity)
            )
        )

        val options = result.candidatesByActivity
            .single()
            .options

        assertEquals(1, options.size)

        val option = options.single()

        assertEquals("slot-1", option.timeSlotId)
        assertEquals("location-1", option.locationId)

        assertEquals(
            listOf("worker-1"),
            option.resourceAssignments
                .getValue("worker-requirement")
        )
    }

    private fun activity(
        id: String = "activity-1",
        allowedTimeSlotIds: Set<String>?,
        allowedLocationIds: Set<String>?,
        candidateResourceIds: Set<String>?
    ) = Activity(
        id = id,
        name = "Activity $id",
        resourceRequirements = listOf(
            ResourceRequirement(
                id = "worker-requirement",
                resourceTypeId = "worker",
                quantity = 1,
                candidateResourceIds = candidateResourceIds
            )
        ),
        allowedTimeSlotIds = allowedTimeSlotIds,
        allowedLocationIds = allowedLocationIds
    )

    private fun problem(
        activities: List<Activity>,
        resources: List<Resource> = listOf(
            worker("worker-1"),
            worker("worker-2")
        ),
        locations: List<Location> = standardLocations()
    ) = PlanningProblem(
        id = "problem-1",
        name = "Test problem",
        planningHorizon = PlanningHorizon(
            start = dateTime(8),
            end = dateTime(18)
        ),
        resourceTypes = listOf(
            ResourceType(
                id = "worker",
                name = "Worker"
            ),
            ResourceType(
                id = "machine",
                name = "Machine"
            )
        ),
        resources = resources,
        activities = activities,
        timeSlots = standardTimeSlots(),
        locations = locations,
        constraints = emptyList()
    )

    private fun worker(
        id: String
    ) = Resource(
        id = id,
        name = "Worker $id",
        typeId = "worker"
    )

    private fun standardTimeSlots() = listOf(
        TimeSlot(
            id = "slot-1",
            start = dateTime(8),
            end = dateTime(9)
        ),
        TimeSlot(
            id = "slot-2",
            start = dateTime(9),
            end = dateTime(10)
        ),
        TimeSlot(
            id = "slot-3",
            start = dateTime(10),
            end = dateTime(11)
        )
    )

    private fun standardLocations() = listOf(
        Location(
            id = "location-1",
            name = "Location 1"
        ),
        Location(
            id = "location-2",
            name = "Location 2"
        )
    )

    private fun dateTime(
        hour: Int
    ): LocalDateTime =
        LocalDateTime.of(
            2026,
            9,
            21,
            hour,
            0
        )
}