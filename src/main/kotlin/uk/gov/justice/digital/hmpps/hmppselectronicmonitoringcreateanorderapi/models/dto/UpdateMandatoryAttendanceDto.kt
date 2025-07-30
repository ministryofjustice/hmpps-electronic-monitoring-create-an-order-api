package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import java.time.ZonedDateTime
import java.util.*

data class UpdateMandatoryAttendanceDto(
  val id: UUID? = null,

  @field:NotNull(
    message = ValidationErrors.MandatoryAttendance.START_DATE_REQUIRED,
  )
  val startDate: ZonedDateTime? = null,

  @field:Future(message = ValidationErrors.MandatoryAttendance.END_DATE_MUST_BE_IN_FUTURE)
  var endDate: ZonedDateTime? = null,

  @field:NotEmpty(message = ValidationErrors.MandatoryAttendance.PURPOSE_REQUIRED)
  @field:Size(max = 200, message = ValidationErrors.MandatoryAttendance.PURPOSE_MAX_LENGTH)
  var purpose: String? = null,

  @field:NotEmpty(
    message = ValidationErrors.MandatoryAttendance
      .APPOINTMENT_DAY_REQUIRED,
  )
  @field:Size(max = 200, message = ValidationErrors.MandatoryAttendance.APPOINTMENT_DAY_MAX_LENGTH)
  var appointmentDay: String? = null,

  @field:NotNull(message = ValidationErrors.MandatoryAttendance.START_TIME_REQUIRED)
  var startTime: String? = null,

  @field:NotNull(message = ValidationErrors.MandatoryAttendance.END_TIME_REQUIRED)
  var endTime: String? = null,

  @field:NotEmpty(message = ValidationErrors.Address.ADDRESS_1_REQUIRED)
  @field:Size(max = 200, message = ValidationErrors.Address.ADDRESS_1_MAX_LENGTH)
  var addressLine1: String? = null,

  @field:Size(max = 200, message = ValidationErrors.Address.ADDRESS_2_MAX_LENGTH)
  @field:Size(message = ValidationErrors.Address.ADDRESS_2_MAX_LENGTH)
  var addressLine2: String? = null,

  @field:NotEmpty(message = ValidationErrors.Address.ADDRESS_3_REQUIRED)
  @field:Size(max = 200, message = ValidationErrors.Address.ADDRESS_3_MAX_LENGTH)
  var addressLine3: String? = null,

  @field:Size(max = 200, message = ValidationErrors.Address.ADDRESS_4_MAX_LENGTH)
  var addressLine4: String? = null,

  @field:NotEmpty(message = ValidationErrors.Address.POSTCODE_REQUIRED)
  @field:Size(max = 200, message = ValidationErrors.Address.POSTCODE_MAX_LENGTH)
  var postcode: String? = null,
) {
  @AssertTrue(message = ValidationErrors.MandatoryAttendance.END_DATE_MUST_BE_AFTER_START_DATE)
  fun isEndDate(): Boolean {
    if (this.endDate != null && this.startDate != null) {
      return this.endDate!! > this.startDate
    }
    return true
  }
}
