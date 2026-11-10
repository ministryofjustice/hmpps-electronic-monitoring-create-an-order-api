package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.Up3SimulationControlService
import java.util.UUID

@RestController
@ConditionalOnProperty(name = ["settings.up3-simulation-enabled"], havingValue = "true")
@PreAuthorize("hasRole('ROLE_EM_CEMO__CREATE_ORDER')")
class Up3SimulationCreationController(private val simulation: Up3SimulationControlService) {
  @GetMapping("/api/dev/up3-simulation/creations/{creationRequestId}")
  fun getCreation(@PathVariable creationRequestId: UUID, authentication: JwtAuthenticationToken) =
    simulation.getCreation(creationRequestId, authentication)
}
