package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import java.time.ZonedDateTime

data class UpdateInstallationAppointmentDto(
  @field:NotNull(message = ValidationErrors.InstallationAppointment.PLACE_NAME_REQUIRED)
  val placeName: String? = null,

  @field:NotNull(message = ValidationErrors.InstallationAppointment.APPOINTMENT_DATE_REQUIRED)
  @field:Future(message = ValidationErrors.InstallationAppointment.APPOINTMENT_DATE_MUST_BE_IN_FUTURE)
  val appointmentDate: ZonedDateTime? = null,
)
