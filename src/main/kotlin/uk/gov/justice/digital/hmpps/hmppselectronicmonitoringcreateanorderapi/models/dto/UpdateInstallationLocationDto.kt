package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.InstallationLocationType
import java.time.ZonedDateTime

data class UpdateInstallationLocationDto(
  @field:NotNull(message = ValidationErrors.InstallationLocation.INSTALLATION_LOCATION_REQUIRED)
  val location: InstallationLocationType? = null,
)

data class UpdateInstallationAppointmentDto(
  @field:NotNull(message = ValidationErrors.InstallationAppointment.PLACE_NAME_REQUIRED)
  val placeName: String? = null,

  @field:NotNull(message = ValidationErrors.InstallationAppointment.APPOINT_DATE_REQUIRED)
  val appointmentDate: ZonedDateTime? = null,
)
