package com.geneticplanner.template.academic

import java.time.LocalDateTime

data class AcademicSchedulingConfig(
    val id: String,
    val name: String,
    val horizonStart: LocalDateTime,
    val horizonEnd: LocalDateTime,
    val teachers: List<AcademicTeacher> = emptyList(),
    val studentGroups: List<AcademicStudentGroup> = emptyList(),
    val lessons: List<AcademicLesson> = emptyList(),
    val classrooms: List<AcademicClassroom> = emptyList(),
    val teachingPeriods: List<AcademicTeachingPeriod> = emptyList()
)

data class AcademicTeacher(
    val id: String,
    val name: String
)

data class AcademicStudentGroup(
    val id: String,
    val name: String,
    val size: Int? = null
)

data class AcademicLesson(
    val id: String,
    val name: String,
    val teacherIds: Set<String> = emptySet(),
    val studentGroupIds: Set<String> = emptySet(),
    val allowedTeachingPeriodIds: Set<String>? = null,
    val allowedClassroomIds: Set<String>? = null,
    val requiredCapacity: Int? = null
)

data class AcademicClassroom(
    val id: String,
    val name: String,
    val capacity: Int? = null
)

data class AcademicTeachingPeriod(
    val id: String,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val label: String? = null
)