package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ConstraintViolationException
import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.transaction.TransactionSystemException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.servlet.resource.NoResourceFoundException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.CreateSercoEntityException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.DocumentApiBadRequestException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.SercoConnectionException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.SubmitOrderException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ListItemValidationError
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidationError
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.util.stream.Collectors

@RestControllerAdvice
class HmppsElectronicMonitoringCreateAnOrderApiExceptionHandler {

  @ExceptionHandler(ValidationException::class)
  fun handleValidationException(e: ValidationException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(BAD_REQUEST)
    .body(
      ErrorResponse(
        status = BAD_REQUEST,
        userMessage = "Validation failure: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.info("Validation exception: {}", e.message) }

  @ExceptionHandler(NoResourceFoundException::class)
  fun handleNoResourceFoundException(e: NoResourceFoundException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(NOT_FOUND)
    .body(
      ErrorResponse(
        status = NOT_FOUND,
        userMessage = "No resource found failure: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.info("No resource found exception: {}", e.message) }

  @ExceptionHandler(AccessDeniedException::class)
  fun handleAccessDeniedException(e: AccessDeniedException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(FORBIDDEN)
    .body(
      ErrorResponse(
        status = FORBIDDEN,
        userMessage = "Forbidden: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.debug("Forbidden (403) returned: {}", e.message) }

  @ExceptionHandler(EntityNotFoundException::class)
  fun handleEntityNotFoundException(e: EntityNotFoundException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(NOT_FOUND)
    .body(
      ErrorResponse(
        status = NOT_FOUND,
        userMessage = "Not Found",
        developerMessage = e.message,
      ),
    ).also { log.info("Entity not found exception: {}", e.message) }

  @ExceptionHandler(TransactionSystemException::class)
  fun handleConstrainException(e: TransactionSystemException): ResponseEntity<List<ValidationError>> {
    if (e.rootCause is ConstraintViolationException) {
      val constraintViolationException = e.rootCause as ConstraintViolationException
      val details: List<ValidationError> = constraintViolationException.constraintViolations
        .stream().map { violation ->
          ValidationError(violation.propertyPath.toString(), violation.message)
        }
        .toList()
      return ResponseEntity.status(BAD_REQUEST).body(details)
    }
    throw e
  }

  @ExceptionHandler(HandlerMethodValidationException::class)
  fun handleHandlerMethodValidationException(e: HandlerMethodValidationException): ResponseEntity<List<ListItemValidationError>> {
    if (e.reason == "Validation failure") {
      val validationResult = e.allValidationResults
      val details: List<ListItemValidationError> = validationResult.stream()
        .map {
          val errors = it.resolvableErrors.stream().map {
              violation ->
            val error = violation as FieldError
            ValidationError(error.field.toString(), error.defaultMessage!!)
          }.collect(Collectors.toList())
          ListItemValidationError(errors, it.containerIndex!!)
        }
        .toList()

      return ResponseEntity.status(BAD_REQUEST).body(details)
    }
    throw e
  }

  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<List<ValidationError>> {
    val fieldErrors = e.bindingResult.fieldErrors.map {
      ValidationError(it.field, it.defaultMessage ?: "")
    }

    val globalErrors = e.bindingResult.globalErrors.map {
      ValidationError(it.objectName, it.defaultMessage ?: "")
    }

    return ResponseEntity.status(BAD_REQUEST).body(
      fieldErrors + globalErrors,
    )
  }

  @ExceptionHandler(DocumentApiBadRequestException::class)
  fun handleDocumentApiBadRequestException(e: DocumentApiBadRequestException): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(BAD_REQUEST)
    .body(
      e.error,
    ).also { log.error("Unexpected exception", e) }

  @ExceptionHandler(SubmitOrderException::class)
  fun handleSubmitOrderException(e: Exception): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(BAD_REQUEST)
    .body(
      ErrorResponse(
        status = BAD_REQUEST,
        userMessage = "Error submitting order: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.error("Unexpected exception", e) }

  @ExceptionHandler(SercoConnectionException::class)
  fun handleSercoAuthenticationException(e: Exception): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(INTERNAL_SERVER_ERROR)
    .body(
      ErrorResponse(
        status = INTERNAL_SERVER_ERROR,
        userMessage = "Error with Serco service Now: ${e.message}",
        developerMessage = e.message,
      ),
    ).also { log.error("Unexpected exception", e) }

  @ExceptionHandler(CreateSercoEntityException::class)
  fun handleCreateSercoDeviceWearerException(e: Exception): ResponseEntity<ErrorResponse> = ResponseEntity
    .status(INTERNAL_SERVER_ERROR)
    .body(
      ErrorResponse(
        status = INTERNAL_SERVER_ERROR,
        userMessage = e.message,
        developerMessage = e.message,
      ),
    ).also { log.error("Unexpected exception", e) }

  @ExceptionHandler(Exception::class)
  fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
    if (e is MaxUploadSizeExceededException) {
      return ResponseEntity
        .status(BAD_REQUEST)
        .body(
          ErrorResponse(
            status = BAD_REQUEST,
            userMessage = "File uploaded exceed max file size limit of 10MB",
            developerMessage = e.message,
          ),
        ).also { log.error("Unexpected exception", e) }
    }

    return ResponseEntity
      .status(INTERNAL_SERVER_ERROR)
      .body(
        ErrorResponse(
          status = INTERNAL_SERVER_ERROR,
          userMessage = "Unexpected error: ${e.message}",
          developerMessage = e.message,
        ),
      ).also { log.error("Unexpected exception", e) }
  }

  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
