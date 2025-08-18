package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto

import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.data.ValidationErrors
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import java.time.ZonedDateTime

data class UpdateAlcoholMonitoringConditionsDto(
  @field:NotNull(message = ValidationErrors.AlcoholMonitoring.MONITORING_TYPE_REQUIRED)
  val monitoringType: AlcoholMonitoringType? = null,

  @field:NotNull(message = ValidationErrors.AlcoholMonitoring.START_DATE_REQUIRED)
  val startDate: ZonedDateTime? = null,

  @field:Future(message = ValidationErrors.AlcoholMonitoring.END_DATE_MUST_BE_IN_FUTURE)
  val endDate: ZonedDateTime? = null,
)
