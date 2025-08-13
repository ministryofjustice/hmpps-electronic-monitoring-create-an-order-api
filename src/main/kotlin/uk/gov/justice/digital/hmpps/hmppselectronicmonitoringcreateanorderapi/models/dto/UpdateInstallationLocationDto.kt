package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.InstallationLocationType

data class UpdateInstallationLocationDto(
  @field:NotNull(message = ValidationErrors.InstallationLocation.INSTALLATION_LOCATION_REQUIRED)
  val location: InstallationLocationType? = null,
)
