package com.geneticplanner.api.planning.academic

import com.geneticplanner.api.planning.OptimizationConfigurationRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class AcademicPlanningRequest(
    @field:NotBlank
    val id: String,

    @field:NotBlank
    val name: String,

    @field:NotNull
    val horizonStart: LocalDateTime?,

    @field:NotNull
    val horizonEnd: LocalDateTime?,

    val teachers: List<@Valid AcademicTeacherRequest> = emptyList(),

    val studentGroups: List<@Valid AcademicStudentGroupRequest> = emptyList(),

    val lessons: List<@Valid AcademicLessonRequest> = emptyList(),

    val classrooms: List<@Valid AcademicClassroomRequest> = emptyList(),

    val teachingPeriods: List<@Valid AcademicTeachingPeriodRequest> = emptyList(),

    @field:Valid
    val optimizationConfiguration:
    OptimizationConfigurationRequest? = null
)

data class AcademicTeacherRequest(
    @field:NotBlank
    val id: String,

    @field:NotBlank
    val name: String
)

data class AcademicStudentGroupRequest(
    @field:NotBlank
    val id: String,

    @field:NotBlank
    val name: String,

    @field:Min(0)
    val size: Int? = null
)

data class AcademicLessonRequest(
    @field:NotBlank
    val id: String,

    @field:NotBlank
    val name: String,

    val teacherIds: Set<String> = emptySet(),

    val studentGroupIds: Set<String> = emptySet(),

    val allowedTeachingPeriodIds: Set<String>? = null,

    val allowedClassroomIds: Set<String>? = null,

    @field:Min(0)
    val requiredCapacity: Int? = null
)

data class AcademicClassroomRequest(
    @field:NotBlank
    val id: String,

    @field:NotBlank
    val name: String,

    @field:Min(0)
    val capacity: Int? = null
)

data class AcademicTeachingPeriodRequest(
    @field:NotBlank
    val id: String,

    @field:NotNull
    val start: LocalDateTime?,

    @field:NotNull
    val end: LocalDateTime?,

    val label: String? = null
)