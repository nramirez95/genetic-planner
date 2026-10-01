package com.geneticplanner.api

import com.geneticplanner.application.ScheduleGenerationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class ApiExceptionHandlerTest {

    private val handler =
        ApiExceptionHandler()

    @Test
    fun `should return structured 500 for schedule generation failure`() {
        val cause =
            IllegalStateException(
                "Internal genetic engine failure."
            )

        val exception =
            ScheduleGenerationException(
                planningId = PLANNING_ID,
                cause = cause
            )

        val response =
            handler.handleScheduleGeneration(
                exception
            )

        assertEquals(
            HttpStatus.INTERNAL_SERVER_ERROR,
            response.statusCode
        )

        val body =
            response.body

        assertNotNull(body)

        assertEquals(
            500,
            body!!.status
        )

        assertEquals(
            "Internal Server Error",
            body.error
        )

        assertEquals(
            exception.message,
            body.message
        )
    }

    companion object {
        private const val PLANNING_ID =
            "generation-api-test"
    }
}