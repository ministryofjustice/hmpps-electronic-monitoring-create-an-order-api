package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.validation.ValidationException
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import tools.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.Up3SimulationException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Up3Simulation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Up3SimulationCreation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Up3SimulationReturnEvent
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.CreateSimulatedOrderDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.CreatedSimulatedOrderDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.SimulateReturnDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.SimulatedCreationReceiptDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.SimulatedOrderState
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.SimulatedReturnEventDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.SimulatedReturnReasonDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.Up3SimulationDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateSimulatedCaseStateDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.Up3SimulationCreationRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.Up3SimulationReturnEventRepository
import java.time.Instant
import java.util.UUID

@Service
@ConditionalOnProperty(name = ["settings.up3-simulation-enabled"], havingValue = "true")
class Up3SimulationControlService(
  private val orderService: OrderService,
  private val simulation: Up3SimulationService,
  private val objectMapper: ObjectMapper,
  private val eventService: EventService,
  private val seeds: Up3SimulationSeedService,
  private val orders: OrderRepository,
  private val events: Up3SimulationReturnEventRepository,
  private val creations: Up3SimulationCreationRepository,
  private val payloads: Up3SimulationPayloads,
  transactionManager: PlatformTransactionManager,
) {
  private val transactions = TransactionTemplate(transactionManager).apply {
    propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
  }

  fun create(
    request: CreateSimulatedOrderDto,
    token: JwtAuthenticationToken,
    fullName: String,
  ): CreatedSimulatedOrderDto {
    if ((request.state == SimulatedOrderState.RETURNED) != request.reasons.isNotEmpty()) {
      throw ValidationException("Return reasons must be supplied only when creating a RETURNED order")
    }
    validateReturnReasons(request.reasons)
    val hash = payloads.creationHash(request)
    // Reserve before creation, then serialise all attempts on this durable identity.
    try {
      transactions.executeWithoutResult {
        if (!creations.existsById(request.creationRequestId)) {
          creations.saveAndFlush(
            Up3SimulationCreation(request.creationRequestId, hash, token.name, request.state),
          )
        }
      }
    } catch (e: DataIntegrityViolationException) {
      if (!creations.existsById(request.creationRequestId)) throw e
    }
    return requireNotNull(
      transactions.execute {
        val receipt = requireNotNull(creations.lockById(request.creationRequestId))
        requireCreator(receipt, token)
        if (receipt.payloadHash != hash) {
          throw Up3SimulationException(
            CONFLICT,
            "UP3_SIMULATION_CREATION_ID_CONFLICT",
            "Creation identity was used with different form data",
          )
        }
        val orderId = receipt.orderId
        if (orderId != null) {
          requireCreatedOrderExists(orderId)
          return@execute createdDto(receipt, ownedOrder(orderId, token), token)
        }
        val order = seeds.create(request, token, fullName)
        if (request.state == SimulatedOrderState.RETURNED) {
          val event = registerReturn(
            order,
            SimulateReturnDto(
              UUID.randomUUID(),
              Instant.now(),
              request.reasons,
              order.versionId,
              order.versionId,
            ),
          )
          receipt.returnEventId = event.eventId
        }
        receipt.orderId = order.id
        receipt.createdOrderVersionId = order.versionId
        creations.save(receipt)
        eventService.recordEvent(
          "UP3_Simulation_Created",
          mapOf("orderId" to order.id.toString(), "user" to token.name, "state" to request.state.name),
        )
        createdDto(receipt, order, token)
      },
    )
  }

  @Transactional
  fun getCreation(requestId: UUID, token: JwtAuthenticationToken): SimulatedCreationReceiptDto {
    val receipt = creations.findById(requestId).orElseThrow {
      Up3SimulationException(NOT_FOUND, "UP3_SIMULATION_CREATION_NOT_FOUND", "Creation identity has not been recorded")
    }
    requireCreator(receipt, token)
    receipt.orderId?.let {
      requireCreatedOrderExists(it)
      ownedOrder(it, token)
    }
    return receipt.toDto()
  }

  @Transactional
  fun get(orderId: UUID, token: JwtAuthenticationToken): Up3SimulationDto =
    toDto(ownedOrder(orderId, token), simulation.requireSimulation(orderId), token)

  @Transactional
  fun setState(orderId: UUID, token: JwtAuthenticationToken, request: UpdateSimulatedCaseStateDto): Up3SimulationDto {
    val order = ownedOrder(orderId, token)
    val state = simulation.requireSimulation(orderId)
    requireVersions(order, state, request.expectedCurrentOrderVersionId, request.expectedSimulatedSubmissionVersionId)
    return toDto(order, simulation.setState(order, request.state.caseState), token)
  }

  @Transactional
  fun publishReturn(
    orderId: UUID,
    token: JwtAuthenticationToken,
    request: SimulateReturnDto,
  ): SimulatedReturnEventDto = registerReturn(ownedOrder(orderId, token), request).toDto()

  @Transactional
  fun getReturnEvent(orderId: UUID, eventId: UUID, token: JwtAuthenticationToken): SimulatedReturnEventDto {
    ownedOrder(orderId, token)
    val event = events.findById(eventId).orElse(null)
    if (event == null || event.orderId != orderId) {
      throw Up3SimulationException(NOT_FOUND, "UP3_SIMULATION_EVENT_NOT_FOUND", "Return event does not exist")
    }
    return event.toDto()
  }

  private fun registerReturn(order: Order, request: SimulateReturnDto): Up3SimulationReturnEvent {
    val hash = payloads.returnHash(order.id, request)
    events.findById(request.eventId).orElse(null)?.let {
      if (it.orderId != order.id || it.payloadHash != hash) {
        throw Up3SimulationException(
          CONFLICT,
          "UP3_SIMULATION_EVENT_ID_CONFLICT",
          "Return identity was used with a different payload",
        )
      }
      return it
    }
    val state = simulation.requireSimulation(order.id)
    requireVersions(order, state, request.expectedCurrentOrderVersionId, request.expectedSimulatedSubmissionVersionId)
    simulation.requireCurrentSubmission(order, state)
    validateReturnReasons(request.reasons)
    if (request.datetimeOfStatusChange.isAfter(Instant.now()) ||
      order.fmsResultDate?.toInstant()?.isAfter(request.datetimeOfStatusChange) == true
    ) {
      throw ValidationException("Return timestamp must be between submission and now")
    }
    val event = mapOf(
      "eventId" to "simulation:${request.eventId}",
      "publishedAt" to Instant.now(),
      "data" to mapOf(
        "caseId" to state.caseId,
        "status" to "rejected",
        "reasons" to request.reasons,
        "datetimeOfStatusChange" to request.datetimeOfStatusChange,
      ),
      "version" to 1,
      "source" to "hmpps-external-api",
      "eventType" to "electronic-monitoring.case-status-returned",
      "simulationOrderId" to order.id,
      "simulationVersionId" to state.versionId,
      "simulationEventId" to request.eventId,
    )
    return events.save(
      Up3SimulationReturnEvent(
        request.eventId,
        order.id,
        order.versionId,
        state.versionId,
        state.caseId,
        hash,
        objectMapper.writeValueAsString(event),
      ),
    )
  }

  private fun ownedOrder(orderId: UUID, token: JwtAuthenticationToken): Order {
    orders.lockById(orderId)
    return orderService.getOrder(orderId, token).also {
      if (it.username != token.name) {
        throw Up3SimulationException(
          FORBIDDEN,
          "UP3_SIMULATION_PERMISSION_DENIED",
          "Only the order owner can control its simulation",
        )
      }
    }
  }

  private fun requireVersions(
    order: Order,
    state: Up3Simulation?,
    expectedCurrent: UUID,
    expectedSubmission: UUID? = null,
  ) {
    val actualSubmission = state?.versionId?.takeIf { order.getSpecificVersion(it)?.fmsResultId != null }
    if (order.versionId != expectedCurrent || (expectedSubmission != null && actualSubmission != expectedSubmission)) {
      throw Up3SimulationException(
        CONFLICT,
        "UP3_SIMULATION_STALE_VERSION",
        "Refresh the order before issuing another command",
        expectedCurrent,
        order.versionId,
        expectedSubmission,
        actualSubmission,
      )
    }
  }

  private fun requireCreator(receipt: Up3SimulationCreation, token: JwtAuthenticationToken) {
    if (receipt.createdBy != token.name) {
      throw Up3SimulationException(
        FORBIDDEN,
        "UP3_SIMULATION_PERMISSION_DENIED",
        "Creation receipt belongs to a different caller",
      )
    }
  }

  private fun requireCreatedOrderExists(orderId: UUID) {
    if (!orders.existsById(orderId)) {
      throw Up3SimulationException(
        NOT_FOUND,
        "UP3_SIMULATION_CREATED_ORDER_REMOVED",
        "The created test order was deleted; it will not be recreated",
      )
    }
  }

  private fun validateReturnReasons(reasons: List<SimulatedReturnReasonDto>) {
    if (reasons.any { it.section == "other" && it.details.isNullOrBlank() }) {
      throw ValidationException("details must be supplied when reason section is other")
    }
  }

  private fun createdDto(receipt: Up3SimulationCreation, order: Order, token: JwtAuthenticationToken) =
    CreatedSimulatedOrderDto(
      receipt.creationRequestId,
      requireNotNull(receipt.createdOrderVersionId),
      toDto(order, simulation.requireSimulation(order.id), token),
      receipt.requestedState,
      receipt.returnEventId,
    )

  private fun toDto(order: Order, state: Up3Simulation, token: JwtAuthenticationToken): Up3SimulationDto {
    val submission = order.getSpecificVersion(state.versionId)?.takeIf { it.fmsResultId != null }
    return Up3SimulationDto(
      orderId = order.id,
      currentOrderVersionId = order.versionId,
      currentOrderVersionNumber = order.getCurrentVersion().versionId,
      simulatedSubmissionVersionId = submission?.id,
      simulatedSubmissionVersionNumber = submission?.versionId,
      caseId = state.caseId.takeIf { submission != null },
      caseState = state.caseState.takeIf { submission != null },
      orderStatus = order.status,
      isOwner = order.username == token.name,
      canAmend = orderService.canAmend(order, state.caseState),
      canControlSubmission = submission?.id == order.versionId && order.status == OrderStatus.SUBMITTED,
      lastProcessedEventId = state.lastProcessedEventId,
      capturedUserEmails = state.capturedUserEmails,
      capturedNoEmails = state.capturedNoEmails,
    )
  }
}
