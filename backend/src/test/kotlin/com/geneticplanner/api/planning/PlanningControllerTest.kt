package com.geneticplanner.api.planning

import com.geneticplanner.application.port.out.PlanningProblemPersistencePort
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlanningControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var persistencePort: PlanningProblemPersistencePort

    @AfterEach
    fun cleanUp() {
        persistencePort.deleteById(PLANNING_ID)
    }

    @BeforeEach
    fun cleanDatabase() {
        if (persistencePort.existsById(PLANNING_ID)) {
            persistencePort.deleteById(PLANNING_ID)
        }

        if (
            persistencePort.existsById(
                "planning-with-constraints"
            )
        ) {
            persistencePort.deleteById(
                "planning-with-constraints"
            )
        }
    }

    @Test
    fun `should create planning`() {
        mockMvc.perform(
            post("/api/plannings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPlanningJson())
        )
            .andExpect(status().isCreated)
            .andExpect(
                header().string(
                    "Location",
                    "/api/plannings/$PLANNING_ID"
                )
            )
            .andExpect(jsonPath("$.id").value(PLANNING_ID))
            .andExpect(
                jsonPath("$.name")
                    .value("Academic planning")
            )
            .andExpect(
                jsonPath("$.templateType")
                    .value("ACADEMIC")
            )

        // Explicitly verify that POST reached PostgreSQL.
        check(persistencePort.existsById(PLANNING_ID))
    }

    @Test
    fun `should retrieve planning by id`() {
        createPlanning()

        mockMvc.perform(
            get("/api/plannings/$PLANNING_ID")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(PLANNING_ID))
            .andExpect(
                jsonPath("$.resources[0].id")
                    .value("teacher-1")
            )
            .andExpect(
                jsonPath("$.activities[0].id")
                    .value("lesson-1")
            )
            .andExpect(
                jsonPath("$.locations[0].id")
                    .value("room-1")
            )
            .andExpect(
                jsonPath("$.timeSlots[0].id")
                    .value("slot-1")
            )
    }

    @Test
    fun `should retrieve all plannings`() {
        createPlanning()

        mockMvc.perform(
            get("/api/plannings")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(PLANNING_ID))
    }

    @Test
    fun `should update planning`() {
        createPlanning()

        mockMvc.perform(
            put("/api/plannings/$PLANNING_ID")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    validPlanningJson(
                        name = "Updated planning"
                    )
                )
        )
            .andExpect(status().isOk)
            .andExpect(
                jsonPath("$.name")
                    .value("Updated planning")
            )

        mockMvc.perform(
            get("/api/plannings/$PLANNING_ID")
        )
            .andExpect(status().isOk)
            .andExpect(
                jsonPath("$.name")
                    .value("Updated planning")
            )
    }

    @Test
    fun `should delete planning`() {
        createPlanning()

        mockMvc.perform(
            delete("/api/plannings/$PLANNING_ID")
        )
            .andExpect(status().isNoContent)

        mockMvc.perform(
            get("/api/plannings/$PLANNING_ID")
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should return 404 when planning does not exist`() {
        mockMvc.perform(
            get("/api/plannings/unknown-planning")
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should return 404 when updating unknown planning`() {
        mockMvc.perform(
            put("/api/plannings/unknown-planning")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    validPlanningJson(
                        id = "unknown-planning"
                    )
                )
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should return 404 when deleting unknown planning`() {
        mockMvc.perform(
            delete("/api/plannings/unknown-planning")
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should return 409 when planning already exists`() {
        createPlanning()

        mockMvc.perform(
            post("/api/plannings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPlanningJson())
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(
                jsonPath("$.message")
                    .value("Planning '$PLANNING_ID' already exists.")
            )
    }

    @Test
    fun `should return 400 when path and body ids do not match`() {
        createPlanning()

        mockMvc.perform(
            put("/api/plannings/$PLANNING_ID")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    validPlanningJson(
                        id = "another-planning"
                    )
                )
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
    }

    @Test
    fun `should return 400 when planning name is blank`() {
        val invalidJson =
            validPlanningJson()
                .replace(
                    "\"name\": \"Academic planning\"",
                    "\"name\": \"\""
                )

        mockMvc.perform(
            post("/api/plannings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
    }

    @Test
    fun `should return 400 for unknown candidate resource`() {
        val invalidJson =
            validPlanningJson()
                .replace(
                    "\"candidateResourceIds\": [\"teacher-1\"]",
                    "\"candidateResourceIds\": [\"unknown-teacher\"]"
                )

        mockMvc.perform(
            post("/api/plannings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
    }

    @Test
    fun `should return 400 when time slot is outside planning horizon`() {
        val invalidJson =
            validPlanningJson()
                .replace(
                    "\"start\": \"2026-10-05T09:00:00\"",
                    "\"start\": \"2026-10-10T09:00:00\""
                )
                .replace(
                    "\"end\": \"2026-10-05T10:00:00\"",
                    "\"end\": \"2026-10-10T10:00:00\""
                )

        mockMvc.perform(
            post("/api/plannings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
    }

    @Test
    fun `should accept omitted optional collections`() {
        val minimalJson =
            """
        {
          "id": "$PLANNING_ID",
          "name": "Empty planning",
          "horizonStart": "2026-10-05T08:00:00",
          "horizonEnd": "2026-10-09T20:00:00"
        }
        """.trimIndent()

        mockMvc.perform(
            post("/api/plannings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(minimalJson)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.resourceTypes").isEmpty)
            .andExpect(jsonPath("$.resources").isEmpty)
            .andExpect(jsonPath("$.activities").isEmpty)
            .andExpect(jsonPath("$.timeSlots").isEmpty)
            .andExpect(jsonPath("$.locations").isEmpty)
    }

    @Test
    fun `should persist and retrieve planning constraints`() {
        val json =
            """
        {
          "id": "planning-with-constraints",
          "name": "Planning with constraints",
          "horizonStart": "2026-10-05T08:00:00",
          "horizonEnd": "2026-10-05T18:00:00",
          "resourceTypes": [
            {
              "id": "TEACHER",
              "name": "Teacher"
            }
          ],
          "resources": [
            {
              "id": "teacher-1",
              "name": "Teacher 1",
              "typeId": "TEACHER"
            },
            {
              "id": "teacher-2",
              "name": "Teacher 2",
              "typeId": "TEACHER"
            }
          ],
          "timeSlots": [
            {
              "id": "slot-1",
              "start": "2026-10-05T08:00:00",
              "end": "2026-10-05T09:00:00"
            },
            {
              "id": "slot-2",
              "start": "2026-10-05T09:00:00",
              "end": "2026-10-05T10:00:00"
            }
          ],
          "locations": [
            {
              "id": "room-1",
              "name": "Room 1",
              "capacity": 30
            }
          ],
          "activities": [
            {
              "id": "lesson-1",
              "name": "Lesson 1",
              "resourceRequirements": [
                {
                  "id": "teacher-requirement",
                  "resourceTypeId": "TEACHER",
                  "quantity": 1,
                  "candidateResourceIds": [
                    "teacher-1",
                    "teacher-2"
                  ]
                }
              ],
              "allowedTimeSlotIds": [
                "slot-1",
                "slot-2"
              ],
              "allowedLocationIds": [
                "room-1"
              ],
              "requiredLocationCapacity": 20
            }
          ],
          "constraints": [
            {
              "id": "no-overlap",
              "name": "No overlap",
              "key": "NO_OVERLAP",
              "weight": 1000.0
            },
            {
              "id": "availability",
              "name": "Resource availability",
              "key": "AVAILABILITY",
              "weight": 1000.0,
              "availableTimeSlotIdsByResourceId": {
                "teacher-1": ["slot-1"],
                "teacher-2": ["slot-2"]
              }
            },
            {
              "id": "required-resource",
              "name": "Required resource",
              "key": "REQUIRED_RESOURCE",
              "weight": 1000.0
            },
            {
              "id": "location-capacity",
              "name": "Location capacity",
              "key": "LOCATION_CAPACITY",
              "weight": 1000.0
            },
            {
              "id": "preferred-slot",
              "name": "Preferred time slot",
              "key": "PREFERRED_TIME_SLOT",
              "weight": 10.0,
              "preferredTimeSlotIdsByActivityId": {
                "lesson-1": ["slot-1"]
              }
            },
            {
              "id": "max-consecutive",
              "name": "Maximum consecutive assignments",
              "key": "MAX_CONSECUTIVE",
              "weight": 5.0,
              "maxConsecutive": 2
            },
            {
              "id": "balanced-workload",
              "name": "Balanced workload",
              "key": "BALANCED_WORKLOAD",
              "weight": 5.0,
              "resourceIds": [
                "teacher-1",
                "teacher-2"
              ],
              "allowedDifference": 1
            }
          ]
        }
        """.trimIndent()

        // Create planning and persist its constraints.
        mockMvc.perform(
            post("/api/plannings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
            .andExpect(status().isCreated)
            .andExpect(
                jsonPath("$.id")
                    .value("planning-with-constraints")
            )
            .andExpect(
                jsonPath("$.constraints.length()")
                    .value(7)
            )

        // Retrieve it again from PostgreSQL.
        mockMvc.perform(
            get("/api/plannings/planning-with-constraints")
        )
            .andExpect(status().isOk)
            .andExpect(
                jsonPath("$.id")
                    .value("planning-with-constraints")
            )
            .andExpect(
                jsonPath("$.constraints.length()")
                    .value(7)
            )

            // Do not rely on persistence ordering.
            .andExpect(
                jsonPath("$.constraints[*].key")
                    .value(
                        containsInAnyOrder(
                            "NO_OVERLAP",
                            "AVAILABILITY",
                            "REQUIRED_RESOURCE",
                            "LOCATION_CAPACITY",
                            "PREFERRED_TIME_SLOT",
                            "MAX_CONSECUTIVE",
                            "BALANCED_WORKLOAD"
                        )
                    )
            )

            // NO_OVERLAP
            .andExpect(
                jsonPath(
                    "$.constraints[?(@.key == 'NO_OVERLAP')].type"
                ).value("HARD")
            )

            // AVAILABILITY
            .andExpect(
                jsonPath(
                    "$.constraints[?(@.key == 'AVAILABILITY')].type"
                ).value("HARD")
            )
            .andExpect(
                jsonPath(
                    "$.constraints[?(@.key == 'AVAILABILITY')]" +
                            ".availableTimeSlotIdsByResourceId.teacher-1[0]"
                ).value("slot-1")
            )
            .andExpect(
                jsonPath(
                    "$.constraints[?(@.key == 'AVAILABILITY')]" +
                            ".availableTimeSlotIdsByResourceId.teacher-2[0]"
                ).value("slot-2")
            )

            // REQUIRED_RESOURCE
            .andExpect(
                jsonPath(
                    "$.constraints[?(@.key == 'REQUIRED_RESOURCE')].type"
                ).value("HARD")
            )

            // LOCATION_CAPACITY
            .andExpect(
                jsonPath(
                    "$.constraints[?(@.key == 'LOCATION_CAPACITY')].type"
                ).value("HARD")
            )

            // PREFERRED_TIME_SLOT
            .andExpect(
                jsonPath(
                    "$.constraints[?(@.key == 'PREFERRED_TIME_SLOT')].type"
                ).value("SOFT")
            )
            .andExpect(
                jsonPath(
                    "$.constraints[?(@.key == 'PREFERRED_TIME_SLOT')]" +
                            ".preferredTimeSlotIdsByActivityId.lesson-1[0]"
                ).value("slot-1")
            )

            // MAX_CONSECUTIVE
            .andExpect(
                jsonPath(
                    "$.constraints[?(@.key == 'MAX_CONSECUTIVE')].type"
                ).value("SOFT")
            )
            .andExpect(
                jsonPath(
                    "$.constraints[?(@.key == 'MAX_CONSECUTIVE')]" +
                            ".maxConsecutive"
                ).value(2)
            )

            // BALANCED_WORKLOAD
            .andExpect(
                jsonPath(
                    "$.constraints[?(@.key == 'BALANCED_WORKLOAD')].type"
                ).value("SOFT")
            )
            // BALANCED_WORKLOAD
            .andExpect(
                jsonPath(
                    "$.constraints[?(@.key == 'BALANCED_WORKLOAD')].type"
                ).value("SOFT")
            )
            .andExpect(
                jsonPath(
                    "$.constraints[?(@.key == 'BALANCED_WORKLOAD')]" +
                            ".allowedDifference"
                ).value(1)
            )
            .andExpect(
                jsonPath(
                    "$.constraints[?(@.key == 'BALANCED_WORKLOAD')]" +
                            ".resourceIds.length()"
                ).value(2)
            )
    }

    @Test
    fun `should return 400 when availability constraint references unknown resource`() {
        val json =
            """
        {
          "id": "invalid-constraint-planning",
          "name": "Invalid constraint planning",
          "horizonStart": "2026-10-05T08:00:00",
          "horizonEnd": "2026-10-05T18:00:00",
          "timeSlots": [
            {
              "id": "slot-1",
              "start": "2026-10-05T08:00:00",
              "end": "2026-10-05T09:00:00"
            }
          ],
          "constraints": [
            {
              "id": "availability",
              "name": "Resource availability",
              "key": "AVAILABILITY",
              "weight": 1000.0,
              "availableTimeSlotIdsByResourceId": {
                "unknown-resource": ["slot-1"]
              }
            }
          ]
        }
        """.trimIndent()

        mockMvc.perform(
            post("/api/plannings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(
                jsonPath("$.message")
                    .value(
                        "Constraint 'availability' references " +
                                "unknown resource 'unknown-resource'."
                    )
            )
    }

    @Test
    fun `should return 400 when constraint configuration is missing`() {
        val json =
            """
        {
          "id": "invalid-constraint-planning",
          "name": "Invalid constraint planning",
          "horizonStart": "2026-10-05T08:00:00",
          "horizonEnd": "2026-10-05T18:00:00",
          "constraints": [
            {
              "id": "max-consecutive",
              "name": "Maximum consecutive assignments",
              "key": "MAX_CONSECUTIVE",
              "weight": 5.0
            }
          ]
        }
        """.trimIndent()

        mockMvc.perform(
            post("/api/plannings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(
                jsonPath("$.message")
                    .value(
                        "Constraint 'max-consecutive' requires " +
                                "'maxConsecutive'."
                    )
            )
    }

    @Test
    fun `should persist and retrieve optimization configuration`() {
        val json =
            validPlanningJson()
                .replace(
                    "\"locations\": [",
                    """
                "optimizationConfiguration": {
                  "populationSize": 100,
                  "generationLimit": 250,
                  "mutationProbability": 0.15,
                  "crossoverProbability": 0.80,
                  "eliteCount": 5,
                  "randomSeed": 12345
                },
                "locations": [
                """.trimIndent()
                )

        mockMvc.perform(
            post("/api/plannings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
            .andExpect(status().isCreated)
            .andExpect(
                jsonPath("$.optimizationConfiguration.populationSize")
                    .value(100)
            )
            .andExpect(
                jsonPath("$.optimizationConfiguration.generationLimit")
                    .value(250)
            )
            .andExpect(
                jsonPath("$.optimizationConfiguration.mutationProbability")
                    .value(0.15)
            )
            .andExpect(
                jsonPath("$.optimizationConfiguration.crossoverProbability")
                    .value(0.80)
            )
            .andExpect(
                jsonPath("$.optimizationConfiguration.eliteCount")
                    .value(5)
            )
            .andExpect(
                jsonPath("$.optimizationConfiguration.randomSeed")
                    .value(12345)
            )

        /*
         * GET is deliberately performed after POST.
         *
         * This verifies that the optimization configuration is not only
         * returned from the POST request but can also be reconstructed
         * from persistence.
         */
        mockMvc.perform(
            get("/api/plannings/$PLANNING_ID")
        )
            .andExpect(status().isOk)
            .andExpect(
                jsonPath("$.optimizationConfiguration.populationSize")
                    .value(100)
            )
            .andExpect(
                jsonPath("$.optimizationConfiguration.generationLimit")
                    .value(250)
            )
            .andExpect(
                jsonPath("$.optimizationConfiguration.mutationProbability")
                    .value(0.15)
            )
            .andExpect(
                jsonPath("$.optimizationConfiguration.crossoverProbability")
                    .value(0.80)
            )
            .andExpect(
                jsonPath("$.optimizationConfiguration.eliteCount")
                    .value(5)
            )
            .andExpect(
                jsonPath("$.optimizationConfiguration.randomSeed")
                    .value(12345)
            )
    }

    @Test
    fun `should update optimization configuration`() {
        val initialJson =
            validPlanningJson()
                .replace(
                    "\"locations\": [",
                    """
                "optimizationConfiguration": {
                  "populationSize": 100,
                  "generationLimit": 250,
                  "mutationProbability": 0.15,
                  "crossoverProbability": 0.80,
                  "eliteCount": 5,
                  "randomSeed": 12345
                },
                "locations": [
                """.trimIndent()
                )

        mockMvc.perform(
            post("/api/plannings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(initialJson)
        )
            .andExpect(status().isCreated)

        val updatedJson =
            initialJson
                .replace(
                    "\"populationSize\": 100",
                    "\"populationSize\": 200"
                )
                .replace(
                    "\"generationLimit\": 250",
                    "\"generationLimit\": 500"
                )
                .replace(
                    "\"mutationProbability\": 0.15",
                    "\"mutationProbability\": 0.20"
                )
                .replace(
                    "\"eliteCount\": 5",
                    "\"eliteCount\": 10"
                )

        mockMvc.perform(
            put("/api/plannings/$PLANNING_ID")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedJson)
        )
            .andExpect(status().isOk)
            .andExpect(
                jsonPath("$.optimizationConfiguration.populationSize")
                    .value(200)
            )
            .andExpect(
                jsonPath("$.optimizationConfiguration.generationLimit")
                    .value(500)
            )
            .andExpect(
                jsonPath("$.optimizationConfiguration.mutationProbability")
                    .value(0.20)
            )
            .andExpect(
                jsonPath("$.optimizationConfiguration.eliteCount")
                    .value(10)
            )

        mockMvc.perform(
            get("/api/plannings/$PLANNING_ID")
        )
            .andExpect(status().isOk)
            .andExpect(
                jsonPath("$.optimizationConfiguration.populationSize")
                    .value(200)
            )
            .andExpect(
                jsonPath("$.optimizationConfiguration.generationLimit")
                    .value(500)
            )
            .andExpect(
                jsonPath("$.optimizationConfiguration.mutationProbability")
                    .value(0.20)
            )
            .andExpect(
                jsonPath("$.optimizationConfiguration.eliteCount")
                    .value(10)
            )
    }

    @Test
    fun `should remove optimization configuration when omitted on update`() {
        val jsonWithConfiguration =
            validPlanningJson()
                .replace(
                    "\"locations\": [",
                    """
                "optimizationConfiguration": {
                  "populationSize": 100,
                  "generationLimit": 250,
                  "mutationProbability": 0.15,
                  "crossoverProbability": 0.80,
                  "eliteCount": 5,
                  "randomSeed": 12345
                },
                "locations": [
                """.trimIndent()
                )

        mockMvc.perform(
            post("/api/plannings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonWithConfiguration)
        )
            .andExpect(status().isCreated)

        /*
         * validPlanningJson() does not contain optimizationConfiguration,
         * therefore PUT must remove the existing configuration.
         */
        mockMvc.perform(
            put("/api/plannings/$PLANNING_ID")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPlanningJson())
        )
            .andExpect(status().isOk)
            .andExpect(
                jsonPath("$.optimizationConfiguration")
                    .doesNotExist()
            )

        mockMvc.perform(
            get("/api/plannings/$PLANNING_ID")
        )
            .andExpect(status().isOk)
            .andExpect(
                jsonPath("$.optimizationConfiguration")
                    .doesNotExist()
            )
    }

    @Test
    fun `should return 400 for invalid optimization configuration`() {
        val invalidJson =
            validPlanningJson()
                .replace(
                    "\"locations\": [",
                    """
                "optimizationConfiguration": {
                  "populationSize": 100,
                  "generationLimit": 250,
                  "mutationProbability": 1.5,
                  "crossoverProbability": 0.80,
                  "eliteCount": 5,
                  "randomSeed": 12345
                },
                "locations": [
                """.trimIndent()
                )

        mockMvc.perform(
            post("/api/plannings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson)
        )
            .andExpect(status().isBadRequest)
            .andExpect(
                jsonPath("$.status")
                    .value(400)
            )
    }

    private fun createPlanning() {
        mockMvc.perform(
            post("/api/plannings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPlanningJson())
        )
            .andExpect(status().isCreated)
    }

    private fun validPlanningJson(
        id: String = PLANNING_ID,
        name: String = "Academic planning"
    ): String =
        """
        {
          "id": "$id",
          "name": "$name",
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
              "typeId": "TEACHER",
              "attributes": {}
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
              "requiredLocationCapacity": 20,
              "attributes": {}
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
              "capacity": 30,
              "attributes": {}
            }
          ]
        }
        """.trimIndent()

    companion object {
        private const val PLANNING_ID = "api-planning-1"
    }
}