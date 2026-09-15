package com.geneticplanner.domain.validation

import com.geneticplanner.domain.Activity
import com.geneticplanner.domain.Location
import com.geneticplanner.domain.PlanningHorizon
import com.geneticplanner.domain.PlanningProblem
import com.geneticplanner.domain.Resource
import com.geneticplanner.domain.ResourceRequirement
import com.geneticplanner.domain.ResourceType
import com.geneticplanner.domain.TimeSlot
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class ProblemValidatorTest {

    private val validator = ProblemValidator()

    @Test
    fun `valid problem returns no validation errors`() {
        val result = validator.validate(validProblem())

        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `duplicate resource IDs are detected`() {
        val problem = validProblem().copy(
            resources = listOf(
                Resource(
                    id = "teacher-1",
                    name = "Teacher 1",
                    typeId = "teacher"
                ),
                Resource(
                    id = "teacher-1",
                    name = "Teacher 2",
                    typeId = "teacher"
                )
            )
        )

        val result = validator.validate(problem)

        assertFalse(result.isValid)
        assertTrue(
            result.errors.any {
                it.code == ValidationErrorCode.DUPLICATE_ID &&
                        it.entityType == "Resource" &&
                        it.entityId == "teacher-1"
            }
        )
    }

    @Test
    fun `invalid planning horizon is detected`() {
        val problem = validProblem().copy(
            planningHorizon = PlanningHorizon(
                start = LocalDateTime.of(2026, 9, 25, 18, 0),
                end = LocalDateTime.of(2026, 9, 21, 8, 0)
            )
        )

        val result = validator.validate(problem)

        assertFalse(result.isValid)
        assertTrue(
            result.errors.any {
                it.code == ValidationErrorCode.INVALID_PLANNING_HORIZON
            }
        )
    }

    @Test
    fun `time slot outside planning horizon is detected`() {
        val original = validProblem()

        val problem = original.copy(
            timeSlots = listOf(
                TimeSlot(
                    id = "slot-1",
                    start = LocalDateTime.of(2026, 9, 26, 9, 0),
                    end = LocalDateTime.of(2026, 9, 26, 10, 0)
                )
            )
        )

        val result = validator.validate(problem)

        assertFalse(result.isValid)
        assertTrue(
            result.errors.any {
                it.code == ValidationErrorCode.INVALID_TIME_SLOT &&
                        it.entityId == "slot-1"
            }
        )
    }

    @Test
    fun `resource with unknown resource type is detected`() {
        val original = validProblem()

        val problem = original.copy(
            resources = listOf(
                original.resources.first().copy(
                    typeId = "unknown-type"
                )
            )
        )

        val result = validator.validate(problem)

        assertFalse(result.isValid)
        assertTrue(
            result.errors.any {
                it.code == ValidationErrorCode.UNKNOWN_RESOURCE_TYPE &&
                        it.entityType == "Resource" &&
                        it.entityId == "teacher-1"
            }
        )
    }

    @Test
    fun `resource requirement with unknown resource type is detected`() {
        val original = validProblem()
        val activity = original.activities.first()

        val requirement = activity.resourceRequirements.first().copy(
            resourceTypeId = "unknown-type",
            candidateResourceIds = null
        )

        val problem = original.copy(
            activities = listOf(
                activity.copy(
                    resourceRequirements = listOf(requirement)
                )
            )
        )

        val result = validator.validate(problem)

        assertFalse(result.isValid)
        assertTrue(
            result.errors.any {
                it.code == ValidationErrorCode.UNKNOWN_RESOURCE_TYPE &&
                        it.entityType == "ResourceRequirement" &&
                        it.entityId == "teacher-requirement"
            }
        )
    }

    @Test
    fun `unknown candidate resource is detected`() {
        val original = validProblem()
        val activity = original.activities.first()

        val requirement = activity.resourceRequirements.first().copy(
            candidateResourceIds = setOf("unknown-teacher")
        )

        val problem = original.copy(
            activities = listOf(
                activity.copy(
                    resourceRequirements = listOf(requirement)
                )
            )
        )

        val result = validator.validate(problem)

        assertFalse(result.isValid)
        assertTrue(
            result.errors.any {
                it.code == ValidationErrorCode.UNKNOWN_CANDIDATE_RESOURCE &&
                        it.entityId == "teacher-requirement"
            }
        )
    }

    @Test
    fun `candidate resource with wrong type is detected`() {
        val original = validProblem()

        val roomType = ResourceType(
            id = "room-resource",
            name = "Room Resource"
        )

        val roomResource = Resource(
            id = "room-resource-1",
            name = "Room Resource 1",
            typeId = roomType.id
        )

        val activity = original.activities.first()

        val requirement = activity.resourceRequirements.first().copy(
            candidateResourceIds = setOf(roomResource.id)
        )

        val problem = original.copy(
            resourceTypes = original.resourceTypes + roomType,
            resources = original.resources + roomResource,
            activities = listOf(
                activity.copy(
                    resourceRequirements = listOf(requirement)
                )
            )
        )

        val result = validator.validate(problem)

        assertFalse(result.isValid)
        assertTrue(
            result.errors.any {
                it.code ==
                        ValidationErrorCode.CANDIDATE_RESOURCE_TYPE_MISMATCH &&
                        it.entityId == "teacher-requirement"
            }
        )
    }

    @Test
    fun `resource requirement quantity below one is detected`() {
        val original = validProblem()
        val activity = original.activities.first()

        val requirement = activity.resourceRequirements.first().copy(
            quantity = 0
        )

        val problem = original.copy(
            activities = listOf(
                activity.copy(
                    resourceRequirements = listOf(requirement)
                )
            )
        )

        val result = validator.validate(problem)

        assertFalse(result.isValid)
        assertTrue(
            result.errors.any {
                it.code ==
                        ValidationErrorCode.INVALID_REQUIREMENT_QUANTITY &&
                        it.entityId == "teacher-requirement"
            }
        )
    }

    @Test
    fun `unknown allowed time slot is detected`() {
        val original = validProblem()

        val problem = original.copy(
            activities = listOf(
                original.activities.first().copy(
                    allowedTimeSlotIds = setOf("unknown-slot")
                )
            )
        )

        val result = validator.validate(problem)

        assertFalse(result.isValid)
        assertTrue(
            result.errors.any {
                it.code == ValidationErrorCode.UNKNOWN_TIME_SLOT &&
                        it.entityId == "math-session-1"
            }
        )
    }

    @Test
    fun `activity without possible time slot is detected`() {
        val original = validProblem()

        val problem = original.copy(
            activities = listOf(
                original.activities.first().copy(
                    allowedTimeSlotIds = emptySet()
                )
            )
        )

        val result = validator.validate(problem)

        assertFalse(result.isValid)
        assertTrue(
            result.errors.any {
                it.code == ValidationErrorCode.NO_POSSIBLE_TIME_SLOT &&
                        it.entityId == "math-session-1"
            }
        )
    }

    @Test
    fun `activity has no possible time slot when problem contains no time slots`() {
        val original = validProblem()

        val problem = original.copy(
            timeSlots = emptyList(),
            activities = listOf(
                original.activities.first().copy(
                    allowedTimeSlotIds = null
                )
            )
        )

        val result = validator.validate(problem)

        assertFalse(result.isValid)
        assertTrue(
            result.errors.any {
                it.code == ValidationErrorCode.NO_POSSIBLE_TIME_SLOT &&
                        it.entityId == "math-session-1"
            }
        )
    }

    @Test
    fun `unknown allowed location is detected`() {
        val original = validProblem()

        val problem = original.copy(
            activities = listOf(
                original.activities.first().copy(
                    allowedLocationIds = setOf("unknown-location")
                )
            )
        )

        val result = validator.validate(problem)

        assertFalse(result.isValid)
        assertTrue(
            result.errors.any {
                it.code == ValidationErrorCode.UNKNOWN_LOCATION &&
                        it.entityId == "math-session-1"
            }
        )
    }

    private fun validProblem(): PlanningProblem {
        val teacherType = ResourceType(
            id = "teacher",
            name = "Teacher"
        )

        val teacher = Resource(
            id = "teacher-1",
            name = "Teacher 1",
            typeId = teacherType.id
        )

        val timeSlot = TimeSlot(
            id = "slot-1",
            start = LocalDateTime.of(2026, 9, 21, 9, 0),
            end = LocalDateTime.of(2026, 9, 21, 10, 0),
            label = "Monday 09:00-10:00"
        )

        val location = Location(
            id = "room-1",
            name = "Room 1",
            type = "classroom",
            capacity = 30
        )

        val requirement = ResourceRequirement(
            id = "teacher-requirement",
            resourceTypeId = teacherType.id,
            quantity = 1,
            candidateResourceIds = setOf(teacher.id)
        )

        val activity = Activity(
            id = "math-session-1",
            name = "Mathematics 1A - Session 1",
            resourceRequirements = listOf(requirement),
            allowedTimeSlotIds = setOf(timeSlot.id),
            allowedLocationIds = setOf(location.id)
        )

        return PlanningProblem(
            id = "problem-1",
            name = "Valid Planning Problem",
            templateType = "academic",
            planningHorizon = PlanningHorizon(
                start = LocalDateTime.of(2026, 9, 21, 8, 0),
                end = LocalDateTime.of(2026, 9, 25, 18, 0)
            ),
            resourceTypes = listOf(teacherType),
            resources = listOf(teacher),
            activities = listOf(activity),
            timeSlots = listOf(timeSlot),
            locations = listOf(location)
        )
    }
}