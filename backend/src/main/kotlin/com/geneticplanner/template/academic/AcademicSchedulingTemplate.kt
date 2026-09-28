package com.geneticplanner.template.academic

import com.geneticplanner.domain.Activity
import com.geneticplanner.domain.Location
import com.geneticplanner.domain.PlanningHorizon
import com.geneticplanner.domain.PlanningProblem
import com.geneticplanner.domain.Resource
import com.geneticplanner.domain.ResourceRequirement
import com.geneticplanner.domain.ResourceType
import com.geneticplanner.domain.TimeSlot
import com.geneticplanner.domain.constraint.Constraint
import com.geneticplanner.domain.constraint.hard.LocationCapacityConstraint
import com.geneticplanner.domain.constraint.hard.NoOverlapConstraint
import com.geneticplanner.domain.constraint.hard.RequiredResourceConstraint
import org.springframework.stereotype.Component

@Component
class AcademicSchedulingTemplate {

    fun createPlanningProblem(
        config: AcademicSchedulingConfig
    ): PlanningProblem {
        validateConfig(config)

        val resourceTypes = createResourceTypes()
        val resources = createResources(config)
        val timeSlots = createTimeSlots(config)
        val locations = createLocations(config)
        val activities = createActivities(config)

        val constraints = createDefaultConstraints(
            activities = activities,
            resources = resources,
            timeSlots = timeSlots,
            locations = locations
        )

        return PlanningProblem(
            id = config.id,
            name = config.name,
            templateType = TEMPLATE_TYPE,
            planningHorizon = PlanningHorizon(
                start = config.horizonStart,
                end = config.horizonEnd
            ),
            resourceTypes = resourceTypes,
            resources = resources,
            activities = activities,
            timeSlots = timeSlots,
            locations = locations,
            constraints = constraints
        )
    }

    private fun validateConfig(
        config: AcademicSchedulingConfig
    ) {
        requireUniqueIds(
            values = config.teachers.map { it.id },
            type = "teacher"
        )

        requireUniqueIds(
            values = config.studentGroups.map { it.id },
            type = "student group"
        )

        requireUniqueIds(
            values = config.lessons.map { it.id },
            type = "lesson"
        )

        requireUniqueIds(
            values = config.classrooms.map { it.id },
            type = "classroom"
        )

        requireUniqueIds(
            values = config.teachingPeriods.map { it.id },
            type = "teaching period"
        )

        val resourceIds =
            config.teachers.map { it.id } +
                    config.studentGroups.map { it.id }

        require(resourceIds.size == resourceIds.toSet().size) {
            "Teacher and student group IDs must be unique across academic resources."
        }

        val teacherIds =
            config.teachers.map { it.id }.toSet()

        val studentGroupIds =
            config.studentGroups.map { it.id }.toSet()

        val classroomIds =
            config.classrooms.map { it.id }.toSet()

        val teachingPeriodIds =
            config.teachingPeriods.map { it.id }.toSet()

        config.lessons.forEach { lesson ->
            require(teacherIds.containsAll(lesson.teacherIds)) {
                "Lesson '${lesson.id}' references unknown teachers."
            }

            require(
                studentGroupIds.containsAll(
                    lesson.studentGroupIds
                )
            ) {
                "Lesson '${lesson.id}' references unknown student groups."
            }

            lesson.allowedClassroomIds?.let { allowedIds ->
                require(classroomIds.containsAll(allowedIds)) {
                    "Lesson '${lesson.id}' references unknown classrooms."
                }
            }

            lesson.allowedTeachingPeriodIds?.let { allowedIds ->
                require(teachingPeriodIds.containsAll(allowedIds)) {
                    "Lesson '${lesson.id}' references unknown teaching periods."
                }
            }
        }
    }

    private fun requireUniqueIds(
        values: List<String>,
        type: String
    ) {
        require(values.size == values.toSet().size) {
            "Academic configuration contains duplicate $type IDs."
        }
    }

    private fun createResourceTypes(): List<ResourceType> =
        listOf(
            ResourceType(
                id = TEACHER_RESOURCE_TYPE,
                name = "Teacher"
            ),
            ResourceType(
                id = STUDENT_GROUP_RESOURCE_TYPE,
                name = "Student Group"
            )
        )

    private fun createResources(
        config: AcademicSchedulingConfig
    ): List<Resource> {
        val teachers = config.teachers.map { teacher ->
            Resource(
                id = teacher.id,
                name = teacher.name,
                typeId = TEACHER_RESOURCE_TYPE
            )
        }

        val studentGroups = config.studentGroups.map { group ->
            Resource(
                id = group.id,
                name = group.name,
                typeId = STUDENT_GROUP_RESOURCE_TYPE,
                attributes = buildMap {
                    group.size?.let {
                        put("size", it.toString())
                    }
                }
            )
        }

        return teachers + studentGroups
    }

    private fun createTimeSlots(
        config: AcademicSchedulingConfig
    ): List<TimeSlot> =
        config.teachingPeriods.map { period ->
            TimeSlot(
                id = period.id,
                start = period.start,
                end = period.end,
                label = period.label
            )
        }

    private fun createLocations(
        config: AcademicSchedulingConfig
    ): List<Location> =
        config.classrooms.map { classroom ->
            Location(
                id = classroom.id,
                name = classroom.name,
                type = CLASSROOM_LOCATION_TYPE,
                capacity = classroom.capacity
            )
        }

    private fun createActivities(
        config: AcademicSchedulingConfig
    ): List<Activity> =
        config.lessons.map { lesson ->
            Activity(
                id = lesson.id,
                name = lesson.name,
                type = LESSON_ACTIVITY_TYPE,
                resourceRequirements =
                    createRequirements(lesson),
                allowedTimeSlotIds =
                    lesson.allowedTeachingPeriodIds,
                allowedLocationIds =
                    lesson.allowedClassroomIds,
                requiredLocationCapacity =
                    lesson.requiredCapacity
            )
        }

    private fun createRequirements(
        lesson: AcademicLesson
    ): List<ResourceRequirement> {
        val teacherRequirements =
            lesson.teacherIds.map { teacherId ->
                ResourceRequirement(
                    id = "${lesson.id}-teacher-$teacherId",
                    resourceTypeId = TEACHER_RESOURCE_TYPE,
                    quantity = 1,
                    candidateResourceIds = setOf(teacherId)
                )
            }

        val studentGroupRequirements =
            lesson.studentGroupIds.map { groupId ->
                ResourceRequirement(
                    id = "${lesson.id}-student-group-$groupId",
                    resourceTypeId = STUDENT_GROUP_RESOURCE_TYPE,
                    quantity = 1,
                    candidateResourceIds = setOf(groupId)
                )
            }

        return teacherRequirements + studentGroupRequirements
    }

    private fun createDefaultConstraints(
        activities: List<Activity>,
        resources: List<Resource>,
        timeSlots: List<TimeSlot>,
        locations: List<Location>
    ): List<Constraint> =
        listOf(
            NoOverlapConstraint(
                id = "academic-no-overlap",
                name = "No overlapping assignments",
                weight = HARD_CONSTRAINT_WEIGHT,
                timeSlots = timeSlots
            ),
            RequiredResourceConstraint(
                id = "academic-required-resource",
                name = "Required academic resources",
                weight = HARD_CONSTRAINT_WEIGHT,
                activities = activities,
                resources = resources
            ),
            LocationCapacityConstraint(
                id = "academic-location-capacity",
                name = "Classroom capacity",
                weight = HARD_CONSTRAINT_WEIGHT,
                activities = activities,
                locations = locations
            )
        )

    companion object {
        const val TEMPLATE_TYPE = "ACADEMIC"

        const val TEACHER_RESOURCE_TYPE = "TEACHER"
        const val STUDENT_GROUP_RESOURCE_TYPE = "STUDENT_GROUP"

        const val LESSON_ACTIVITY_TYPE = "LESSON"
        const val CLASSROOM_LOCATION_TYPE = "CLASSROOM"

        private const val HARD_CONSTRAINT_WEIGHT = 1000.0
    }
}