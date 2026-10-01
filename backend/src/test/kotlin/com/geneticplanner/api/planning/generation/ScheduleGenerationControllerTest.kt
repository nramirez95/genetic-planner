package com.geneticplanner.api.planning.generation

import com.geneticplanner.application.port.out.PlanningProblemPersistencePort
import com.geneticplanner.dataset.TrivialDataset
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class ScheduleGenerationControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var planningProblemPersistencePort:
            PlanningProblemPersistencePort

    @BeforeEach
    fun setUp() {
        if (
            planningProblemPersistencePort.existsById(
                PLANNING_ID
            )
        ) {
            planningProblemPersistencePort.deleteById(
                PLANNING_ID
            )
        }
    }

    @Test
    fun `should generate schedule`() {
        saveValidPlanning()

        mockMvc.perform(
            post(
                "/api/plannings/$PLANNING_ID/generate"
            )
                .contentType(
                    MediaType.APPLICATION_JSON
                )
        )
            .andExpect(status().isOk)
            .andExpect(
                jsonPath("$.planningId")
                    .value(PLANNING_ID)
            )
            .andExpect(
                jsonPath("$.feasible")
                    .value(true)
            )
            .andExpect(
                jsonPath("$.fitness")
                    .isNumber
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
                    .value(
                        greaterThanOrEqualTo(1)
                    )
            )
            .andExpect(
                jsonPath("$.executionTimeMillis")
                    .value(
                        greaterThanOrEqualTo(0)
                    )
            )
            .andExpect(
                jsonPath("$.randomSeed")
                    .isNumber
            )
            .andExpect(
                jsonPath("$.assignments")
                    .isArray
            )
            .andExpect(
                jsonPath("$.assignments.length()")
                    .value(2)
            )
            .andExpect(
                jsonPath(
                    "$.assignments[0].activityId"
                )
                    .isString
            )
            .andExpect(
                jsonPath(
                    "$.assignments[0].timeSlotId"
                )
                    .isString
            )
            .andExpect(
                jsonPath("$.constraintResults")
                    .isArray
            )
    }

    @Test
    fun `should return 404 when planning does not exist`() {
        mockMvc.perform(
            post(
                "/api/plannings/unknown-planning/generate"
            )
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
                        "Planning 'unknown-planning' was not found."
                    )
            )
    }

    @Test
    fun `should reject invalid planning`() {
        val invalidProblem =
            TrivialDataset.create()
                .copy(
                    id = PLANNING_ID,
                    timeSlots = emptyList()
                )

        planningProblemPersistencePort.save(
            invalidProblem
        )

        mockMvc.perform(
            post(
                "/api/plannings/$PLANNING_ID/generate"
            )
        )
            .andExpect(status().isBadRequest)
            .andExpect(
                jsonPath("$.status")
                    .value(400)
            )
            .andExpect(
                jsonPath("$.error")
                    .value("Bad Request")
            )
            .andExpect(
                jsonPath("$.message")
                    .value(
                        "Planning '$PLANNING_ID' is not valid for generation."
                    )
            )
            .andExpect(
                jsonPath("$.errors")
                    .isArray
            )
            .andExpect(
                jsonPath("$.errors")
                    .isNotEmpty
            )
            .andExpect(
                jsonPath("$.errors[0].code")
                    .exists()
            )
            .andExpect(
                jsonPath("$.errors[0].message")
                    .exists()
            )
    }

    private fun saveValidPlanning() {
        val problem =
            TrivialDataset.create()
                .copy(
                    id = PLANNING_ID
                )

        planningProblemPersistencePort.save(
            problem
        )
    }

    companion object {

        private const val PLANNING_ID =
            "generation-api-test"
    }
}