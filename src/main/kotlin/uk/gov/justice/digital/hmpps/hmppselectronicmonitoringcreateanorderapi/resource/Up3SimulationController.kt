package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource

import jakarta.validation.Valid
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.AuthAwareAuthenticationToken
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.CreateSimulatedOrderDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.CreatedSimulatedOrderDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.SimulateReturnDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateSimulatedCaseStateDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.Up3SimulationControlService
import java.util.UUID

@RestController
@ConditionalOnProperty(name = ["settings.up3-simulation-enabled"], havingValue = "true")
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
@RequestMapping("/api/dev/up3-simulation/orders")
class Up3SimulationController(private val simulation: Up3SimulationControlService) {
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  fun create(
    @Valid @RequestBody request: CreateSimulatedOrderDto,
    token: AuthAwareAuthenticationToken,
  ): CreatedSimulatedOrderDto = simulation.create(request, token, token.getUserFullName())

  @GetMapping("/{orderId}")
  fun get(@PathVariable orderId: UUID, authentication: JwtAuthenticationToken) = simulation.get(orderId, authentication)

  @PutMapping("/{orderId}/state")
  fun setState(
    @PathVariable orderId: UUID,
    authentication: JwtAuthenticationToken,
    @Valid @RequestBody request: UpdateSimulatedCaseStateDto,
  ) = simulation.setState(orderId, authentication, request)

  @PostMapping("/{orderId}/return")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun returnOrder(
    @PathVariable orderId: UUID,
    authentication: JwtAuthenticationToken,
    @Valid @RequestBody request: SimulateReturnDto,
  ) = simulation.publishReturn(orderId, authentication, request)

  @GetMapping("/{orderId}/returns/{eventId}")
  fun getReturnEvent(
    @PathVariable orderId: UUID,
    @PathVariable eventId: UUID,
    authentication: JwtAuthenticationToken,
  ) = simulation.getReturnEvent(orderId, eventId, authentication)
}
