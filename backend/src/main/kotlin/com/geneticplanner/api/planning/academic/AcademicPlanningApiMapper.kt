package com.geneticplanner.api.planning.academic

import com.geneticplanner.api.planning.PlanningApiMapper
import com.geneticplanner.application.model.PlanningConfiguration
import com.geneticplanner.template.academic.AcademicClassroom
import com.geneticplanner.template.academic.AcademicLesson
import com.geneticplanner.template.academic.AcademicSchedulingConfig
import com.geneticplanner.template.academic.AcademicSchedulingTemplate
import com.geneticplanner.template.academic.AcademicStudentGroup
import com.geneticplanner.template.academic.AcademicTeacher
import com.geneticplanner.template.academic.AcademicTeachingPeriod
import org.springframework.stereotype.Component

@Component
class AcademicPlanningApiMapper(
    private val academicSchedulingTemplate: AcademicSchedulingTemplate,
    private val planningApiMapper: PlanningApiMapper
) {

    fun toConfiguration(
        request: AcademicPlanningRequest
    ): PlanningConfiguration {

        val academicConfig = AcademicSchedulingConfig(
            id = request.id,
            name = request.name,

            horizonStart = requireNotNull(request.horizonStart) {
                "Planning horizon start is required."
            },

            horizonEnd = requireNotNull(request.horizonEnd) {
                "Planning horizon end is required."
            },

            teachers = request.teachers.map {
                AcademicTeacher(
                    id = it.id,
                    name = it.name
                )
            },

            studentGroups = request.studentGroups.map {
                AcademicStudentGroup(
                    id = it.id,
                    name = it.name,
                    size = it.size
                )
            },

            lessons = request.lessons.map {
                AcademicLesson(
                    id = it.id,
                    name = it.name,
                    teacherIds = it.teacherIds,
                    studentGroupIds = it.studentGroupIds,
                    allowedTeachingPeriodIds =
                        it.allowedTeachingPeriodIds,
                    allowedClassroomIds =
                        it.allowedClassroomIds,
                    requiredCapacity =
                        it.requiredCapacity
                )
            },

            classrooms = request.classrooms.map {
                AcademicClassroom(
                    id = it.id,
                    name = it.name,
                    capacity = it.capacity
                )
            },

            teachingPeriods = request.teachingPeriods.map {
                AcademicTeachingPeriod(
                    id = it.id,
                    start = requireNotNull(it.start) {
                        "Teaching period '${it.id}' start is required."
                    },
                    end = requireNotNull(it.end) {
                        "Teaching period '${it.id}' end is required."
                    },
                    label = it.label
                )
            }
        )

        validateAcademicDates(academicConfig)

        return PlanningConfiguration(
            problem = academicSchedulingTemplate
                .createPlanningProblem(academicConfig),

            optimizationConfiguration = planningApiMapper
                .toGeneticConfig(
                    request.optimizationConfiguration
                )
        )
    }

    private fun validateAcademicDates(
        config: AcademicSchedulingConfig
    ) {
        require(config.horizonStart < config.horizonEnd) {
            "Planning horizon start must be before end."
        }

        config.teachingPeriods.forEach { period ->
            require(period.start < period.end) {
                "Teaching period '${period.id}' start must be before end."
            }

            require(
                !period.start.isBefore(config.horizonStart) &&
                        !period.end.isAfter(config.horizonEnd)
            ) {
                "Teaching period '${period.id}' must be inside " +
                        "the planning horizon."
            }
        }
    }
}