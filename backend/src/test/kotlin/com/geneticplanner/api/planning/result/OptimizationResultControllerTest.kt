package com.geneticplanner.api.planning.result

import com.geneticplanner.application.model.StoredAssignment
import com.geneticplanner.application.model.StoredConstraintResult
import com.geneticplanner.application.model.StoredConstraintViolation
import com.geneticplanner.application.model.StoredOptimizationResult
import com.geneticplanner.application.port.out.OptimizationResultPersistencePort
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OptimizationResultControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var planningPersistencePort:
            PlanningProblemPersistencePort

    @Autowired
    private lateinit var resultPersistencePort:
            OptimizationResultPersistencePort

    @BeforeEach
    fun cleanDatabase() {
        resultPersistencePort.deleteByPlanningProblemId(
            PLANNING_ID
        )

        if (planningPersistencePort.existsById(PLANNING_ID)) {
            planningPersistencePort.deleteById(
                PLANNING_ID
            )
        }
    }

    @AfterEach
    fun cleanUp() {
        resultPersistencePort.deleteByPlanningProblemId(
            PLANNING_ID
        )

        if (planningPersistencePort.existsById(PLANNING_ID)) {
            planningPersistencePort.deleteById(
                PLANNING_ID
            )
        }
    }

    @Test
    fun `should retrieve optimization result`() {
        createPlanning()
        persistResult()

        mockMvc.perform(
            get("/api/plannings/$PLANNING_ID/result")
        )
            .andExpect(status().isOk)

            // Summary
            .andExpect(
                jsonPath("$.planningId")
                    .value(PLANNING_ID)
            )
            .andExpect(
                jsonPath("$.fitness")
                    .value(15.0)
            )
            .andExpect(
                jsonPath("$.feasible")
                    .value(false)
            )
            .andExpect(
                jsonPath("$.hardPenalty")
                    .value(10.0)
            )
            .andExpect(
                jsonPath("$.softPenalty")
                    .value(5.0)
            )
            .andExpect(
                jsonPath("$.generationsExecuted")
                    .value(125)
            )
            .andExpect(
                jsonPath("$.executionTimeMillis")
                    .value(1500)
            )
            .andExpect(
                jsonPath("$.randomSeed")
                    .value(12345)
            )

            // Enriched assignment
            .andExpect(
                jsonPath("$.assignments.length()")
                    .value(1)
            )
            .andExpect(
                jsonPath("$.assignments[0].activity.id")
                    .value("lesson-1")
            )
            .andExpect(
                jsonPath("$.assignments[0].activity.name")
                    .value("Algorithms")
            )
            .andExpect(
                jsonPath("$.assignments[0].activity.type")
                    .value("LESSON")
            )
            .andExpect(
                jsonPath("$.assignments[0].timeSlot.id")
                    .value("slot-1")
            )
            .andExpect(
                jsonPath("$.assignments[0].timeSlot.start")
                    .value("2026-10-05T09:00")
            )
            .andExpect(
                jsonPath("$.assignments[0].timeSlot.end")
                    .value("2026-10-05T10:00")
            )
            .andExpect(
                jsonPath("$.assignments[0].timeSlot.label")
                    .value("Monday 09:00")
            )
            .andExpect(
                jsonPath("$.assignments[0].resources[0].id")
                    .value("teacher-1")
            )
            .andExpect(
                jsonPath("$.assignments[0].resources[0].name")
                    .value("Professor Smith")
            )
            .andExpect(
                jsonPath("$.assignments[0].resources[0].typeId")
                    .value("TEACHER")
            )
            .andExpect(
                jsonPath("$.assignments[0].location.id")
                    .value("room-1")
            )
            .andExpect(
                jsonPath("$.assignments[0].location.name")
                    .value("Room 101")
            )
            .andExpect(
                jsonPath("$.assignments[0].location.type")
                    .value("CLASSROOM")
            )

            // Constraint breakdown
            .andExpect(
                jsonPath("$.constraintResults.length()")
                    .value(1)
            )
            .andExpect(
                jsonPath("$.constraintResults[0].constraintId")
                    .value("preferred-slot")
            )
            .andExpect(
                jsonPath("$.constraintResults[0].type")
                    .value("SOFT")
            )
            .andExpect(
                jsonPath("$.constraintResults[0].violations")
                    .value(1)
            )
            .andExpect(
                jsonPath("$.constraintResults[0].rawPenalty")
                    .value(1.0)
            )
            .andExpect(
                jsonPath("$.constraintResults[0].weight")
                    .value(5.0)
            )
            .andExpect(
                jsonPath("$.constraintResults[0].weightedPenalty")
                    .value(5.0)
            )
            .andExpect(
                jsonPath(
                    "$.constraintResults[0]" +
                            ".violationDetails[0].message"
                ).value("Preferred time slot was not used.")
            )
            .andExpect(
                jsonPath(
                    "$.constraintResults[0]" +
                            ".violationDetails[0].penalty"
                ).value(1.0)
            )
            .andExpect(
                jsonPath(
                    "$.constraintResults[0]" +
                            ".violationDetails[0]" +
                            ".relatedEntityIds[0]"
                ).value("lesson-1")
            )
    }

    @Test
    fun `should return 404 when planning does not exist`() {
        mockMvc.perform(
            get("/api/plannings/unknown-planning/result")
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should return structured 404 when result does not exist`() {
        createPlanning()

        mockMvc.perform(
            get("/api/plannings/$PLANNING_ID/result")
        )
            .andExpect(status().isNotFound)
            .andExpect(
                jsonPath("$.status")
                    .value(404)
            )
            .andExpect(
                jsonPath("$.error")
                    .value("Not Found")
            )
            .andExpect(
                jsonPath("$.message")
                    .value(
                        "No optimization result was found " +
                                "for planning '$PLANNING_ID'."
                    )
            )
    }

    @Test
    fun `should retrieve result after schedule generation`() {
        createPlanning()

        mockMvc.perform(
            post("/api/plannings/$PLANNING_ID/generate")
        )
            .andExpect(status().isOk)
            .andExpect(
                jsonPath("$.planningId")
                    .value(PLANNING_ID)
            )

        /*
         * The result returned here must come from persistence,
         * not directly from the genetic engine response.
         */
        mockMvc.perform(
            get("/api/plannings/$PLANNING_ID/result")
        )
            .andExpect(status().isOk)
            .andExpect(
                jsonPath("$.planningId")
                    .value(PLANNING_ID)
            )
            .andExpect(
                jsonPath("$.fitness")
                    .isNumber
            )
            .andExpect(
                jsonPath("$.feasible")
                    .isBoolean
            )
            .andExpect(
                jsonPath("$.hardPenalty")
                    .isNumber
            )
            .andExpect(
                jsonPath("$.softPenalty")
                    .isNumber
            )
            .andExpect(
                jsonPath("$.generationsExecuted")
                    .isNumber
            )
            .andExpect(
                jsonPath("$.executionTimeMillis")
                    .isNumber
            )
            .andExpect(
                jsonPath("$.assignments")
                    .isArray
            )
            .andExpect(
                jsonPath("$.constraintResults")
                    .isArray
            )
    }

    private fun persistResult() {
        resultPersistencePort.save(
            StoredOptimizationResult(
                planningProblemId =
                    PLANNING_ID,
                fitness =
                    15.0,
                feasible =
                    false,
                hardPenalty =
                    10.0,
                softPenalty =
                    5.0,
                generationsExecuted =
                    125,
                executionTimeMillis =
                    1500,
                randomSeed =
                    12345,
                assignments =
                    listOf(
                        StoredAssignment(
                            activityId =
                                "lesson-1",
                            timeSlotId =
                                "slot-1",
                            resourceAssignments =
                                mapOf(
                                    "requirement-1" to
                                            listOf("teacher-1")
                                ),
                            locationId =
                                "room-1"
                        )
                    ),
                constraintResults =
                    listOf(
                        StoredConstraintResult(
                            constraintId =
                                "preferred-slot",
                            type =
                                "SOFT",
                            violations =
                                1,
                            rawPenalty =
                                1.0,
                            weight =
                                5.0,
                            weightedPenalty =
                                5.0,
                            violationDetails =
                                listOf(
                                    StoredConstraintViolation(
                                        message =
                                            "Preferred time slot was not used.",
                                        penalty =
                                            1.0,
                                        relatedEntityIds =
                                            setOf("lesson-1")
                                    )
                                )
                        )
                    )
            )
        )
    }

    private fun createPlanning() {
        mockMvc.perform(
            post("/api/plannings")
                .contentType(
                    MediaType.APPLICATION_JSON
                )
                .content(validPlanningJson())
        )
            .andExpect(status().isCreated)
    }

    private fun validPlanningJson(): String =
        """
        {
          "id": "$PLANNING_ID",
          "name": "Academic planning",
          "templateType": "ACADEMIC",
          "horizonStart": "2026-10-05T08:00:00",
          "horizonEnd": "2026-10-09T20:00:00",
          "resourceTypes": [
            {
              "id": "TEACHER",
              "name": "Teacher"
            }
          ],
          "resources": [
            {
              "id": "teacher-1",
              "name": "Professor Smith",
              "typeId": "TEACHER"
            }
          ],
          "activities": [
            {
              "id": "lesson-1",
              "name": "Algorithms",
              "type": "LESSON",
              "resourceRequirements": [
                {
                  "id": "requirement-1",
                  "resourceTypeId": "TEACHER",
                  "quantity": 1,
                  "candidateResourceIds": ["teacher-1"]
                }
              ],
              "allowedTimeSlotIds": ["slot-1"],
              "allowedLocationIds": ["room-1"],
              "requiredLocationCapacity": 20
            }
          ],
          "timeSlots": [
            {
              "id": "slot-1",
              "start": "2026-10-05T09:00:00",
              "end": "2026-10-05T10:00:00",
              "label": "Monday 09:00"
            }
          ],
          "locations": [
            {
              "id": "room-1",
              "name": "Room 101",
              "type": "CLASSROOM",
              "capacity": 30
            }
          ]
        }
        """.trimIndent()

    companion object {
        private const val PLANNING_ID =
            "optimization-result-api-test"
    }
}