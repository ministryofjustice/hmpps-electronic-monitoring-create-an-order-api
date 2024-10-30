package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringInstallationLocationType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import java.time.ZonedDateTime

data class UpdateAlcoholMonitoringConditionsDto(
  @field:NotNull(message = "Monitoring type is required")
  val monitoringType: AlcoholMonitoringType? = null,

  @field:NotNull(message = "Start date is required")
  @field:Future(message = "Start date must be in the future")
  val startDate: ZonedDateTime? = null,

  @field:Future(message = "End date must be in the future")
  val endDate: ZonedDateTime? = null,

  @field:NotNull(message = "Installation location is required")
  val installationLocation: AlcoholMonitoringInstallationLocationType? = null,

  val prisonName: String? = null,
  val probationOfficeName: String? = null,
) {
  @AssertTrue(message = "You must provide a prison name if the installation location is a prison")
  fun isPrisonName(): Boolean {
    return !(
      installationLocation == AlcoholMonitoringInstallationLocationType.PRISON && prisonName.isNullOrBlank()
      )
  }

  @AssertTrue(
    message = "You must provide a probation office name if the installation location is a probation office",
  )
  fun isProbationOfficeName(): Boolean {
    return !(
      installationLocation == AlcoholMonitoringInstallationLocationType.PROBATION_OFFICE &&
        probationOfficeName.isNullOrBlank()
      )
  }
}
