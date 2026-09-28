package com.geneticplanner.api

import com.geneticplanner.application.PlanningAlreadyExistsException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(
        exception: IllegalArgumentException
    ): ResponseEntity<ApiErrorResponse> =
        error(
            status = HttpStatus.BAD_REQUEST,
            message = exception.message ?: "Invalid request."
        )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(
        exception: MethodArgumentNotValidException
    ): ResponseEntity<ApiErrorResponse> {
        val message =
            exception.bindingResult
                .fieldErrors
                .joinToString("; ") { fieldError ->
                    "${fieldError.field}: " +
                            (fieldError.defaultMessage ?: "invalid value")
                }

        return error(
            status = HttpStatus.BAD_REQUEST,
            message = message.ifBlank {
                "Request validation failed."
            }
        )
    }

    @ExceptionHandler(PlanningAlreadyExistsException::class)
    fun handleAlreadyExists(
        exception: PlanningAlreadyExistsException
    ): ResponseEntity<ApiErrorResponse> =
        error(
            status = HttpStatus.CONFLICT,
            message = exception.message
                ?: "Planning already exists."
        )

    private fun error(
        status: HttpStatus,
        message: String
    ): ResponseEntity<ApiErrorResponse> =
        ResponseEntity
            .status(status)
            .body(
                ApiErrorResponse(
                    status = status.value(),
                    error = status.reasonPhrase,
                    message = message
                )
            )
}