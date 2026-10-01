package com.geneticplanner.api

import com.geneticplanner.application.InvalidPlanningProblemException
import com.geneticplanner.application.OptimizationResultNotFoundException
import com.geneticplanner.application.PlanningAlreadyExistsException
import com.geneticplanner.application.PlanningNotFoundException
import com.geneticplanner.application.ScheduleGenerationException
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

    @ExceptionHandler(PlanningNotFoundException::class)
    fun handlePlanningNotFound(
        exception: PlanningNotFoundException
    ): ResponseEntity<ApiErrorResponse> =
        error(
            status = HttpStatus.NOT_FOUND,
            message = exception.message
                ?: "Planning was not found."
        )

    @ExceptionHandler(InvalidPlanningProblemException::class)
    fun handleInvalidPlanningProblem(
        exception: InvalidPlanningProblemException
    ): ResponseEntity<PlanningValidationErrorResponse> {

        val status =
            HttpStatus.BAD_REQUEST

        val errors =
            exception.validationResult.errors.map { validationError ->
                PlanningValidationIssueResponse(
                    code =
                        validationError.code.name,
                    message =
                        validationError.message,
                    entityType =
                        validationError.entityType,
                    entityId =
                        validationError.entityId,
                    field =
                        validationError.field
                )
            }

        return ResponseEntity
            .status(status)
            .body(
                PlanningValidationErrorResponse(
                    status = status.value(),
                    error = status.reasonPhrase,
                    message =
                        exception.message
                            ?: "Planning is not valid for generation.",
                    errors = errors
                )
            )
    }

    @ExceptionHandler(ScheduleGenerationException::class)
    fun handleScheduleGeneration(
        exception: ScheduleGenerationException
    ): ResponseEntity<ApiErrorResponse> =
        error(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            message = exception.message
                ?: "Schedule generation failed."
        )

    @ExceptionHandler(
        OptimizationResultNotFoundException::class
    )
    fun handleOptimizationResultNotFound(
        exception: OptimizationResultNotFoundException
    ): ResponseEntity<ApiErrorResponse> =
        error(
            status = HttpStatus.NOT_FOUND,
            message =
                exception.message
                    ?: "Optimization result was not found."
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