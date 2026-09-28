package com.geneticplanner.template.academic

import com.geneticplanner.domain.constraint.hard.LocationCapacityConstraint
import com.geneticplanner.domain.constraint.hard.NoOverlapConstraint
import com.geneticplanner.domain.constraint.hard.RequiredResourceConstraint
import com.geneticplanner.genetic.GeneticEngine
import com.geneticplanner.genetic.JeneticsEngine
import com.geneticplanner.genetic.config.GeneticAlgorithmConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class AcademicSchedulingTemplateTest {

    private val template = AcademicSchedulingTemplate()

    @Test
    fun `should transform academic configuration into planning problem`() {
        val config = createAcademicConfig()

        val problem = template.createPlanningProblem(config)

        assertEquals("academic-1", problem.id)
        assertEquals("Computer Science Schedule", problem.name)
        assertEquals("ACADEMIC", problem.templateType)

        assertEquals(
            LocalDateTime.of(2026, 10, 5, 8, 0),
            problem.planningHorizon.start
        )
        assertEquals(
            LocalDateTime.of(2026, 10, 9, 20, 0),
            problem.planningHorizon.end
        )
    }

    @Test
    fun `should provide academic resource types`() {
        val problem = template.createPlanningProblem(createAcademicConfig())

        val resourceTypes = problem.resourceTypes.associateBy { it.id }

        assertEquals(2, resourceTypes.size)

        assertEquals(
            "Teacher",
            resourceTypes.getValue("TEACHER").name
        )
        assertEquals(
            "Student Group",
            resourceTypes.getValue("STUDENT_GROUP").name
        )
    }

    @Test
    fun `should map teachers and student groups to resources`() {
        val problem = template.createPlanningProblem(createAcademicConfig())

        val resources = problem.resources.associateBy { it.id }

        val teacher = resources.getValue("teacher-1")
        assertEquals("Professor Smith", teacher.name)
        assertEquals("TEACHER", teacher.typeId)

        val group = resources.getValue("group-a")
        assertEquals("Group A", group.name)
        assertEquals("STUDENT_GROUP", group.typeId)
        assertEquals("25", group.attributes["size"])
    }

    @Test
    fun `should map teaching periods and classrooms`() {
        val problem = template.createPlanningProblem(createAcademicConfig())

        val timeSlot = problem.timeSlots.single()

        assertEquals("period-1", timeSlot.id)
        assertEquals("Monday 09:00", timeSlot.label)
        assertEquals(
            LocalDateTime.of(2026, 10, 5, 9, 0),
            timeSlot.start
        )
        assertEquals(
            LocalDateTime.of(2026, 10, 5, 10, 0),
            timeSlot.end
        )

        val classroom = problem.locations.single()

        assertEquals("room-1", classroom.id)
        assertEquals("Room 101", classroom.name)
        assertEquals("CLASSROOM", classroom.type)
        assertEquals(30, classroom.capacity)
    }

    @Test
    fun `should map lessons to activities and resource requirements`() {
        val problem = template.createPlanningProblem(createAcademicConfig())

        val activity = problem.activities.single()

        assertEquals("lesson-1", activity.id)
        assertEquals("Algorithms", activity.name)
        assertEquals("LESSON", activity.type)

        assertEquals(
            setOf("period-1"),
            activity.allowedTimeSlotIds
        )
        assertEquals(
            setOf("room-1"),
            activity.allowedLocationIds
        )
        assertEquals(25, activity.requiredLocationCapacity)

        assertEquals(2, activity.resourceRequirements.size)

        val requirements =
            activity.resourceRequirements.associateBy { it.resourceTypeId }

        val teacherRequirement =
            requirements.getValue("TEACHER")

        assertEquals(1, teacherRequirement.quantity)
        assertEquals(
            setOf("teacher-1"),
            teacherRequirement.candidateResourceIds
        )

        val groupRequirement =
            requirements.getValue("STUDENT_GROUP")

        assertEquals(1, groupRequirement.quantity)
        assertEquals(
            setOf("group-a"),
            groupRequirement.candidateResourceIds
        )
    }

    @Test
    fun `should provide academic default constraints`() {
        val problem = template.createPlanningProblem(createAcademicConfig())

        assertEquals(3, problem.constraints.size)

        assertTrue(
            problem.constraints.any {
                it is NoOverlapConstraint
            }
        )

        assertTrue(
            problem.constraints.any {
                it is RequiredResourceConstraint
            }
        )

        assertTrue(
            problem.constraints.any {
                it is LocationCapacityConstraint
            }
        )

        problem.constraints.forEach {
            assertNotNull(it.id)
            assertEquals(1000.0, it.weight)
        }
    }

    @Test
    fun `should optimize academic planning using generic genetic engine`() {
        val problem =
            template.createPlanningProblem(createAcademicConfig())

        val engine: GeneticEngine = JeneticsEngine()

        val config = GeneticAlgorithmConfig(
            populationSize = 20,
            generationLimit = 10,
            mutationProbability = 0.1,
            crossoverProbability = 0.8,
            eliteCount = 2,
            randomSeed = 42L
        )

        val result = engine.optimize(
            problem = problem,
            config = config
        )

        assertEquals(
            problem.id,
            result.schedule.planningProblemId
        )

        assertEquals(
            problem.activities.size,
            result.schedule.assignments.size
        )

        assertEquals(
            "lesson-1",
            result.schedule.assignments.single().activityId
        )
    }

    @Test
    fun `should reject lesson referencing unknown teacher`() {
        val config = createAcademicConfig()

        val invalidLesson =
            config.lessons.single().copy(
                teacherIds = setOf("unknown-teacher")
            )

        val invalidConfig =
            config.copy(
                lessons = listOf(invalidLesson)
            )

        assertThrows(IllegalArgumentException::class.java) {
            template.createPlanningProblem(invalidConfig)
        }
    }

    @Test
    fun `should reject lesson referencing unknown classroom`() {
        val config = createAcademicConfig()

        val invalidLesson =
            config.lessons.single().copy(
                allowedClassroomIds = setOf("unknown-room")
            )

        val invalidConfig =
            config.copy(
                lessons = listOf(invalidLesson)
            )

        assertThrows(IllegalArgumentException::class.java) {
            template.createPlanningProblem(invalidConfig)
        }
    }

    @Test
    fun `should reject duplicate academic IDs`() {
        val config = createAcademicConfig()

        val duplicateTeacher =
            AcademicTeacher(
                id = "teacher-1",
                name = "Another Professor"
            )

        val invalidConfig =
            config.copy(
                teachers = config.teachers + duplicateTeacher
            )

        assertThrows(IllegalArgumentException::class.java) {
            template.createPlanningProblem(invalidConfig)
        }
    }

    private fun createAcademicConfig(): AcademicSchedulingConfig =
        AcademicSchedulingConfig(
            id = "academic-1",
            name = "Computer Science Schedule",
            horizonStart =
                LocalDateTime.of(2026, 10, 5, 8, 0),
            horizonEnd =
                LocalDateTime.of(2026, 10, 9, 20, 0),
            teachers = listOf(
                AcademicTeacher(
                    id = "teacher-1",
                    name = "Professor Smith"
                )
            ),
            studentGroups = listOf(
                AcademicStudentGroup(
                    id = "group-a",
                    name = "Group A",
                    size = 25
                )
            ),
            lessons = listOf(
                AcademicLesson(
                    id = "lesson-1",
                    name = "Algorithms",
                    teacherIds = setOf("teacher-1"),
                    studentGroupIds = setOf("group-a"),
                    allowedTeachingPeriodIds =
                        setOf("period-1"),
                    allowedClassroomIds =
                        setOf("room-1"),
                    requiredCapacity = 25
                )
            ),
            classrooms = listOf(
                AcademicClassroom(
                    id = "room-1",
                    name = "Room 101",
                    capacity = 30
                )
            ),
            teachingPeriods = listOf(
                AcademicTeachingPeriod(
                    id = "period-1",
                    start =
                        LocalDateTime.of(2026, 10, 5, 9, 0),
                    end =
                        LocalDateTime.of(2026, 10, 5, 10, 0),
                    label = "Monday 09:00"
                )
            )
        )
}