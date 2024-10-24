package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource

import jakarta.validation.Valid
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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MonitoringConditionType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderTypeDescription
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.AtLeastOneSelected
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.MonitoringConditionsService
import java.util.UUID

@RestController
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/")
class MonitoringConditionsController(
  @Autowired val monitoringConditionsService: MonitoringConditionsService,
) {
  @PutMapping("/orders/{orderId}/monitoring-conditions")
  fun updateMonitoringConditions(
    @PathVariable orderId: UUID,
    @RequestBody @Valid monitoringConditionsUpdateRecord: UpdateMonitoringConditionsDto,
    authentication: Authentication,
  ): ResponseEntity<MonitoringConditions> {
    val username = authentication.name
    val monitoringConditions = monitoringConditionsService.updateMonitoringConditions(
      orderId,
      username,
      monitoringConditionsUpdateRecord,
    )

    return ResponseEntity(monitoringConditions, HttpStatus.OK)
  }
}

@AtLeastOneSelected(
  fieldNames = ["curfew", "exclusionZone", "trail", "mandatoryAttendance", "alcohol"],
  message = "Select at least one monitoring type.",
)
data class UpdateMonitoringConditionsDto(
  @field:NotNull(message = "Order type is required")
  val orderType: String? = null,

  val devicesRequired: Array<String>? = null,

  var acquisitiveCrime: Boolean? = null,

  var dapol: Boolean? = null,

  var curfew: Boolean? = null,

  var exclusionZone: Boolean? = null,

  var trail: Boolean? = null,

  var mandatoryAttendance: Boolean? = null,

  var alcohol: Boolean? = null,

  @field:NotNull(message = "Condition type is required")
  var conditionType: MonitoringConditionType? = null,

  @field:NotNull(message = "Order type description type is required")
  val orderTypeDescription: OrderTypeDescription? = null,
)
