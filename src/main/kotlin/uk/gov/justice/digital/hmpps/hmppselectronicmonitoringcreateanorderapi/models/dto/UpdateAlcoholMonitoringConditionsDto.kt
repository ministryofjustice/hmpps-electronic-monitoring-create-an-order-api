package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringInstallationLocationType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import java.time.ZonedDateTime

data class UpdateAlcoholMonitoringConditionsDto(
  @field:NotNull(message = ValidationErrors.AlcoholMonitoring.MONITORING_TYPE_REQUIRED)
  val monitoringType: AlcoholMonitoringType? = null,

  @field:NotNull(message = ValidationErrors.AlcoholMonitoring.START_DATE_REQUIRED)
  val startDate: ZonedDateTime? = null,

  @field:Future(message = ValidationErrors.AlcoholMonitoring.END_DATE_MUST_BE_IN_FUTURE)
  val endDate: ZonedDateTime? = null,

  @field:NotNull(message = ValidationErrors.AlcoholMonitoring.INSTALLATION_LOCATION_REQUIRED)
  val installationLocation: AlcoholMonitoringInstallationLocationType? = null,

  val prisonName: String? = null,
  val probationOfficeName: String? = null,
) {
  @AssertTrue(message = ValidationErrors.AlcoholMonitoring.PRISON_NAME_REQUIRED_LOCATION_IS_PRISON)
  fun isPrisonName(): Boolean = !(
    installationLocation == AlcoholMonitoringInstallationLocationType.PRISON && prisonName.isNullOrBlank()
    )

  @AssertTrue(
    message = ValidationErrors.AlcoholMonitoring.OFFICE_NAME_REQUIRED_LOCATION_IS_PROBATION_OFFICE,
  )
  fun isProbationOfficeName(): Boolean = !(
    installationLocation == AlcoholMonitoringInstallationLocationType.PROBATION_OFFICE &&
      probationOfficeName.isNullOrBlank()
    )
}
