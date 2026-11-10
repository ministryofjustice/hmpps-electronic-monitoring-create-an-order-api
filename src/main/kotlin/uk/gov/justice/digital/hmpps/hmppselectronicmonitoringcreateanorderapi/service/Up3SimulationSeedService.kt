package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.FeatureFlags
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.Up3SimulationSettings
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.CreateSimulatedOrderDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.SimulatedOrderState
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.CaseState
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import java.util.UUID

@Service
@ConditionalOnProperty(name = ["settings.up3-simulation-enabled"], havingValue = "true")
class Up3SimulationSeedService(
  private val settings: Up3SimulationSettings,
  private val simulation: Up3SimulationService,
  private val orderService: OrderService,
  private val featureFlags: FeatureFlags,
) : OrderSectionServiceBase() {
  @Transactional
  fun create(request: CreateSimulatedOrderDto, token: JwtAuthenticationToken, fullName: String): Order {
    settings.requireEnabled()
    val source = request.order
    val orderId = UUID.randomUUID()
    val versionId = UUID.randomUUID()
    val version = OrderVersion(
      id = versionId,
      orderId = orderId,
      username = token.name,
      status = OrderStatus.IN_PROGRESS,
      type = source.type,
      dataDictionaryVersion = source.dataDictionaryVersion ?: featureFlags.dataDictionaryVersion,
      isSentencingAct = source.isSentencingAct,
      deviceWearer = source.deviceWearer?.copy(id = UUID.randomUUID(), versionId = versionId, version = null),
      deviceWearerResponsibleAdult = source.deviceWearerResponsibleAdult?.copy(
        id = UUID.randomUUID(),
        versionId = versionId,
        version = null,
      ),
      addresses = source.addresses.map { it.copy(id = UUID.randomUUID(), versionId = versionId, version = null) }
        .toMutableList(),
      additionalDocuments = source.additionalDocuments.map {
        it.copy(id = UUID.randomUUID(), versionId = versionId, version = null)
      }.toMutableList(),
      contactDetails = source.contactDetails?.copy(id = UUID.randomUUID(), versionId = versionId, version = null),
      interestedParties = source.interestedParties?.copy(id = UUID.randomUUID(), versionId = versionId, version = null),
      probationDeliveryUnit = source.probationDeliveryUnit?.copy(
        id = UUID.randomUUID(),
        versionId = versionId,
        version = null,
      ),
      monitoringConditions = source.monitoringConditions?.copy(
        id = UUID.randomUUID(),
        versionId = versionId,
        version = null,
      ),
      monitoringConditionsTrail = source.monitoringConditionsTrail?.copy(
        id = UUID.randomUUID(),
        versionId = versionId,
        version = null,
      ),
      monitoringConditionsAlcohol = source.monitoringConditionsAlcohol?.copy(
        id = UUID.randomUUID(),
        versionId = versionId,
        version = null,
      ),
      curfewConditions = source.curfewConditions?.copy(id = UUID.randomUUID(), versionId = versionId, version = null),
      curfewReleaseDateConditions = source.curfewReleaseDateConditions?.copy(
        id = UUID.randomUUID(),
        versionId = versionId,
        version = null,
      ),
      curfewTimeTable = source.curfewTimeTable.map {
        it.copy(id = UUID.randomUUID(), versionId = versionId, version = null)
      }.toMutableList(),
      enforcementZoneConditions = source.enforcementZoneConditions.orEmpty().map {
        it.copy(id = UUID.randomUUID(), versionId = versionId, version = null)
      }.toMutableList(),
      mandatoryAttendanceConditions = source.mandatoryAttendanceConditions.map {
        it.copy(id = UUID.randomUUID(), versionId = versionId, version = null)
      }.toMutableList(),
      installationAndRisk = source.installationAndRisk?.copy(
        id = UUID.randomUUID(),
        versionId = versionId,
        version = null,
      ),
      variationDetails = source.variationDetails?.copy(id = UUID.randomUUID(), versionId = versionId, version = null),
      installationLocation = source.installationLocation?.copy(
        id = UUID.randomUUID(),
        versionId = versionId,
        version = null,
      ),
      installationAppointment = source.installationAppointment?.copy(
        id = UUID.randomUUID(),
        versionId = versionId,
        version = null,
      ),
      orderParameters = source.orderParameters?.copy(id = UUID.randomUUID(), versionId = versionId, version = null),
      dapoClauses = source.dapoClauses.map {
        it.copy(id = UUID.randomUUID(), versionId = versionId, version = null)
      }.toMutableList(),
      offences = source.offences.map {
        it.copy(id = UUID.randomUUID(), versionId = versionId, version = null)
      }.toMutableList(),
      offenceAdditionalDetails = source.offenceAdditionalDetails?.copy(
        id = UUID.randomUUID(),
        versionId = versionId,
        version = null,
      ),
      mappa = source.mappa?.copy(id = UUID.randomUUID(), versionId = versionId, version = null),
      detailsOfInstallation = source.detailsOfInstallation?.copy(
        id = UUID.randomUUID(),
        versionId = versionId,
        version = null,
      ),
    )
    val order = updateLastUpdatedByAndSaveOrder(
      Order(id = orderId, versions = mutableListOf(version)),
      version.interestedParties,
    )
    simulation.enrolNewDraft(order, token.name)
    if (request.state != SimulatedOrderState.DRAFT) {
      orderService.submitOrder(order.id, token, fullName)
      if (request.state == SimulatedOrderState.ACCEPTED) {
        simulation.setState(order, CaseState.AWAITING_VALIDATION)
      }
    }
    return orderService.getOrder(order.id, token)
  }
}
