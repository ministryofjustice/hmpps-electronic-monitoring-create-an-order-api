package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource

import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AlcoholMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringInstallationLocationType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.MonitoringConditionsAlcoholService
import java.time.ZonedDateTime
import java.util.UUID

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class MonitoringConditionsAlcoholController(
  @Autowired val monitoringConditionsAlcoholService: MonitoringConditionsAlcoholService,
) {
  @PutMapping("/orders/{orderId}/monitoring-conditions-alcohol")
  fun updateAlcoholMonitoringConditions(
    @PathVariable orderId: UUID,
    @RequestBody @Valid alcoholMonitoringConditionsUpdateRecord: UpdateAlcoholMonitoringConditionsDto,
    authentication: Authentication,
  ): ResponseEntity<AlcoholMonitoringConditions> {
    val username = authentication.name
    val alcoholMonitoringConditions = monitoringConditionsAlcoholService.createOrUpdateAlcoholMonitoringConditions(orderId, username, alcoholMonitoringConditionsUpdateRecord)

    return ResponseEntity(alcoholMonitoringConditions, HttpStatus.OK)
  }
}

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
    return !((installationLocation == AlcoholMonitoringInstallationLocationType.PRISON) && prisonName.isNullOrBlank())
  }

  @AssertTrue(message = "You must provide a probation office name if the installation location is a probation office")
  fun isProbationOfficeName(): Boolean {
    return !((installationLocation == AlcoholMonitoringInstallationLocationType.PROBATION_OFFICE) && probationOfficeName.isNullOrBlank())
  }
}
