package com.geneticplanner.api.planning.academic

import com.geneticplanner.application.port.out.PlanningProblemPersistencePort
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AcademicPlanningControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var planningPersistencePort:
            PlanningProblemPersistencePort

    @BeforeEach
    fun cleanDatabase() {
        deleteTestPlanning()
    }

    @AfterEach
    fun cleanUp() {
        deleteTestPlanning()
    }

    @Test
    fun `should create academic planning from template`() {
        mockMvc.perform(
            post("/api/plannings/from-template/academic")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validAcademicPlanningJson())
        )
            .andExpect(status().isCreated)
            .andExpect(
                header().string(
                    "Location",
                    "/api/plannings/$PLANNING_ID"
                )
            )
            .andExpect(
                jsonPath("$.id").value(PLANNING_ID)
            )
            .andExpect(
                jsonPath("$.templateType").value("ACADEMIC")
            )
            .andExpect(
                jsonPath("$.resources.length()").value(2)
            )
            .andExpect(
                jsonPath("$.activities.length()").value(1)
            )
            .andExpect(
                jsonPath("$.constraints.length()").value(3)
            )
            .andExpect(
                jsonPath("$.optimizationConfiguration.populationSize")
                    .value(20)
            )

        // Verify that the planning was persisted.
        mockMvc.perform(
            get("/api/plannings/$PLANNING_ID")
        )
            .andExpect(status().isOk)
            .andExpect(
                jsonPath("$.name").value("Academic test planning")
            )
            .andExpect(
                jsonPath("$.resources[0].typeId").value("TEACHER")
            )
            .andExpect(
                jsonPath("$.resources[1].typeId").value("STUDENT_GROUP")
            )
            .andExpect(
                jsonPath("$.activities[0].resourceRequirements.length()")
                    .value(2)
            )
            .andExpect(
                jsonPath("$.activities[0].allowedTimeSlotIds[0]")
                    .value("period-1")
            )
            .andExpect(
                jsonPath("$.activities[0].allowedLocationIds[0]")
                    .value("room-1")
            )
            .andExpect(
                jsonPath("$.optimizationConfiguration.generationLimit")
                    .value(30)
            )
    }

    @Test
    fun `should reject academic planning with unknown teacher`() {
        val invalidJson = validAcademicPlanningJson()
            .replace(
                "\"teacherIds\": [\"teacher-1\"]",
                "\"teacherIds\": [\"unknown-teacher\"]"
            )

        mockMvc.perform(
            post("/api/plannings/from-template/academic")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson)
        )
            .andExpect(status().isBadRequest)
            .andExpect(
                jsonPath("$.status").value(400)
            )
    }

    @Test
    fun `should reject teaching period outside planning horizon`() {
        val invalidJson = validAcademicPlanningJson()
            .replace(
                "\"start\": \"2026-11-02T09:00:00\"",
                "\"start\": \"2026-11-07T09:00:00\""
            )
            .replace(
                "\"end\": \"2026-11-02T10:00:00\"",
                "\"end\": \"2026-11-07T10:00:00\""
            )

        mockMvc.perform(
            post("/api/plannings/from-template/academic")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson)
        )
            .andExpect(status().isBadRequest)
            .andExpect(
                jsonPath("$.status").value(400)
            )
    }

    @Test
    fun `should reject academic planning with duplicate teacher IDs`() {
        val invalidJson = validAcademicPlanningJson()
            .replace(
                "\"name\": \"Professor Smith\"",
                """
            "name": "Professor Smith"
            },
            {
              "id": "teacher-1",
              "name": "Professor Johnson"
            """.trimIndent()
            )

        mockMvc.perform(
            post("/api/plannings/from-template/academic")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson)
        )
            .andExpect(status().isBadRequest)
            .andExpect(
                jsonPath("$.status").value(400)
            )

        // Verify that the invalid planning was not persisted.
        mockMvc.perform(
            get("/api/plannings/$PLANNING_ID")
        )
            .andExpect(status().isNotFound)
    }

    private fun deleteTestPlanning() {
        if (planningPersistencePort.existsById(PLANNING_ID)) {
            planningPersistencePort.deleteById(PLANNING_ID)
        }
    }

    private fun validAcademicPlanningJson(): String =
        """
        {
          "id": "$PLANNING_ID",
          "name": "Academic test planning",
          "horizonStart": "2026-11-02T08:00:00",
          "horizonEnd": "2026-11-06T18:00:00",
          "teachers": [
            {
              "id": "teacher-1",
              "name": "Professor Smith"
            }
          ],
          "studentGroups": [
            {
              "id": "group-1",
              "name": "Group A",
              "size": 25
            }
          ],
          "lessons": [
            {
              "id": "lesson-1",
              "name": "Algorithms",
              "teacherIds": ["teacher-1"],
              "studentGroupIds": ["group-1"],
              "allowedTeachingPeriodIds": ["period-1"],
              "allowedClassroomIds": ["room-1"],
              "requiredCapacity": 25
            }
          ],
          "classrooms": [
            {
              "id": "room-1",
              "name": "Room 101",
              "capacity": 30
            }
          ],
          "teachingPeriods": [
            {
              "id": "period-1",
              "start": "2026-11-02T09:00:00",
              "end": "2026-11-02T10:00:00",
              "label": "Monday 09:00"
            }
          ],
          "optimizationConfiguration": {
            "populationSize": 20,
            "generationLimit": 30,
            "mutationProbability": 0.15,
            "crossoverProbability": 0.80,
            "eliteCount": 2,
            "randomSeed": 12345
          }
        }
        """.trimIndent()

    companion object {
        private const val PLANNING_ID =
            "academic-template-api-test"
    }
}