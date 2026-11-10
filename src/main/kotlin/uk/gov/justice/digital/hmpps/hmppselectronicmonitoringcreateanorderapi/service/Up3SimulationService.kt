package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.Up3SimulationSettings
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.OrderChangeException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.exception.Up3SimulationException
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.listener.ReturnsDomainEvent
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.SimulatedReturnStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.SimulationFailureStage
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Up3Simulation
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Up3SimulationReturnEvent
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.SimulatedReturnReasonDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.CaseState
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ProcessingStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.emails.Email
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.emails.EmailType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsAttachmentResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsAttachmentResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsRetrieveDWandMO
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.MonitoringOrder
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.FmsSubmissionResultRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.Up3SimulationRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.Up3SimulationReturnEventRepository
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.UUID

@Service
class Up3SimulationService(
  private val settings: Up3SimulationSettings,
  private val simulationRepository: Up3SimulationRepository?,
  private val submissions: FmsSubmissionResultRepository,
  private val orders: OrderRepository,
  private val objectMapper: ObjectMapper,
  private val eventRepository: Up3SimulationReturnEventRepository?,
  private val payloads: Up3SimulationPayloads,
) {
  private val simulations: Up3SimulationRepository
    get() = checkNotNull(simulationRepository) { "UP3 simulation persistence is not configured" }
  private val events: Up3SimulationReturnEventRepository
    get() = checkNotNull(eventRepository) { "UP3 simulation persistence is not configured" }

  fun find(orderId: UUID): Up3Simulation? =
    if (settings.storageAvailable) simulations.findById(orderId).orElse(null) else null

  fun requireSimulation(orderId: UUID): Up3Simulation {
    settings.requireEnabled()
    return find(orderId) ?: throw Up3SimulationException(
      NOT_FOUND,
      "UP3_SIMULATION_NOT_ENROLLED",
      "Order is not enrolled in UP3 simulation",
    )
  }

  @Transactional
  fun enrolNewDraft(order: Order, username: String): Up3Simulation {
    settings.requireEnabled()
    check(order.status == OrderStatus.IN_PROGRESS && order.versions.size == 1 && order.fmsResultId == null) {
      "Only a new draft can be enrolled before its first submission"
    }
    check(find(order.id) == null) { "Order is already enrolled" }
    return simulations.save(
      Up3Simulation(order.id, order.versionId, "sim-${order.versionId}", enrolledBy = username),
    )
  }

  @Transactional
  fun setState(order: Order, state: CaseState): Up3Simulation {
    settings.requireEnabled()
    val simulation = simulations.lockByOrderId(order.id)
      ?: throw Up3SimulationException(
        NOT_FOUND,
        "UP3_SIMULATION_NOT_ENROLLED",
        "Order is not enrolled in UP3 simulation",
      )
    requireCurrentSubmission(order, simulation)
    simulation.caseState = state
    if (state == CaseState.AWAITING_VALIDATION) {
      order.recordProcessingOutcome(ProcessingStatus.ACCEPTED)
      orders.save(order)
    }
    return simulations.save(simulation)
  }

  @Transactional(noRollbackFor = [OrderChangeException::class])
  fun deviceWearerResponse(orderId: UUID): FmsResponse? {
    find(orderId) ?: return null
    settings.requireEnabled()
    orders.lockById(orderId)
    val simulation = requireNotNull(simulations.lockByOrderId(orderId))
    val order = orders.findById(orderId).orElseThrow()
    simulation.versionId = order.versionId
    simulation.caseId = "sim-${order.versionId}"
    simulation.caseState = CaseState.NEW
    simulations.save(simulation)
    return response(simulation.caseId)
  }

  fun monitoringOrderResponse(orderId: UUID): FmsResponse? {
    find(orderId) ?: return null
    settings.requireEnabled()
    val order = orders.findById(orderId).orElseThrow()
    return response("sim-mo-${order.versionId}")
  }

  fun stateResponse(caseId: String): CaseState? {
    val simulation = findByCaseId(caseId) ?: return null
    settings.requireEnabled()
    if (simulation.caseId == caseId) return simulation.caseState
    val submission = submissions.findByCaseId(caseId) ?: return CaseState.UNKNOWN
    val version = orders.findById(simulation.orderId).orElseThrow().versions
      .firstOrNull { it.fmsResultId == submission.id } ?: return CaseState.UNKNOWN
    return when {
      version.status == OrderStatus.REJECTED ||
        version.statusUpdates.any { it.status == ProcessingStatus.REJECTED } -> CaseState.CANCELLED
      version.statusUpdates.any { it.status == ProcessingStatus.ACCEPTED } -> CaseState.AWAITING_VALIDATION
      else -> CaseState.UNKNOWN
    }
  }

  fun attachmentResponse(caseId: String, fileName: String): FmsAttachmentResponse? {
    findByCaseId(caseId) ?: return null
    settings.requireEnabled()
    return FmsAttachmentResponse(
      FmsAttachmentResult(sysId = UUID.randomUUID().toString(), fileName = fileName, tableSysId = caseId),
    )
  }

  fun detailsResponse(caseId: String): FmsRetrieveDWandMO? {
    val simulation = findByCaseId(caseId) ?: return null
    settings.requireEnabled()
    val order = orders.findById(simulation.orderId).orElseThrow()
    val source = order.versions.filter { it.status == OrderStatus.SUBMITTED && it.fmsResultId != null }
      .maxByOrNull { it.versionId }
      ?: throw OrderChangeException("UP3_SIMULATION_NO_DETAILS", "No submitted version is available")
    val result = submissions.findById(requireNotNull(source.fmsResultId)).orElseThrow()
    return FmsRetrieveDWandMO(
      caseId = requireNotNull(result.caseId),
      deviceWearer = objectMapper.readValue(result.deviceWearerResult.payload, DeviceWearer::class.java),
      monitoringOrder = objectMapper.readValue(result.monitoringOrderResult.payload, MonitoringOrder::class.java),
    )
  }

  @Transactional
  fun captureNotifications(order: Order, emails: List<Email>): Boolean {
    if (!settings.storageAvailable) return false
    val simulation = simulations.lockByOrderId(order.id) ?: return false
    // Enrolment remains a safety boundary even when the feature has subsequently been disabled.
    simulation.capturedUserEmails += emails.count { it.type == EmailType.USER }
    simulation.capturedNoEmails += emails.count { it.type == EmailType.NO }
    simulations.save(simulation)
    return true
  }

  @Transactional
  fun processReturn(event: ReturnsDomainEvent, reject: () -> Unit): Boolean {
    val markers = listOf(event.simulationOrderId, event.simulationVersionId, event.simulationEventId)
    if (markers.any { it != null } && markers.any { it == null }) {
      throw OrderChangeException("UP3_SIMULATION_INVALID_EVENT", "Simulation event markers must all be supplied")
    }
    val simulationOrderId = event.simulationOrderId
    if (simulationOrderId == null) {
      if (findByCaseId(event.data.caseId) != null) {
        throw OrderChangeException(
          "UP3_SIMULATION_REAL_EVENT",
          "Real returns cannot modify an enrolled simulation order",
        )
      }
      reject()
      return true
    }
    settings.requireEnabled()
    // Every workflow takes the order lock before simulation/event locks.
    orders.lockById(simulationOrderId)
    val simulation = simulations.lockByOrderId(simulationOrderId)
      ?: throw OrderChangeException("UP3_SIMULATION_NOT_ENROLLED", "Simulated event has no enrolled order")
    val receipt = events.lockById(requireNotNull(event.simulationEventId))
      ?: throw OrderChangeException("UP3_SIMULATION_INVALID_EVENT", "Simulated return has no durable receipt")
    if (!matches(receipt, event)) {
      throw OrderChangeException("UP3_SIMULATION_INVALID_EVENT", "Simulated return does not match its durable receipt")
    }
    if (receipt.status in setOf(SimulatedReturnStatus.PROCESSED, SimulatedReturnStatus.STALE_IGNORED)) return false
    val order = orders.findById(simulation.orderId).orElseThrow()
    if (order.status != OrderStatus.SUBMITTED ||
      order.versionId != receipt.targetOrderVersionId ||
      simulation.versionId != receipt.simulatedSubmissionVersionId ||
      simulation.caseId != receipt.caseId
    ) {
      receipt.complete(SimulatedReturnStatus.STALE_IGNORED, "UP3_SIMULATION_STALE_EVENT")
      events.save(receipt)
      return false
    }
    reject()
    simulation.caseState = CaseState.CANCELLED
    simulation.lastProcessedEventId = event.simulationEventId
    simulations.save(simulation)
    receipt.complete(SimulatedReturnStatus.PROCESSED)
    events.save(receipt)
    return true
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun recordReturnFailure(event: ReturnsDomainEvent) {
    val id = event.simulationEventId ?: return
    if (!settings.storageAvailable) {
      log.warn("Cannot record simulation return failure outside local/dev: event={}", id)
      return
    }
    val receipt = events.lockById(id) ?: return
    val timestamp = try {
      Instant.parse(event.data.datetimeOfStatusChange)
    } catch (e: DateTimeParseException) {
      log.warn("Cannot attach invalid timestamp failure to simulation receipt: event={}", id)
      return
    }
    if (!matches(receipt, event, timestamp) ||
      receipt.status in setOf(SimulatedReturnStatus.PROCESSED, SimulatedReturnStatus.STALE_IGNORED)
    ) {
      return
    }
    receipt.status = SimulatedReturnStatus.FAILED
    receipt.failureStage = SimulationFailureStage.PROCESSING
    receipt.errorCode = "UP3_SIMULATION_PROCESSING_FAILED"
    receipt.processingFailures++
    receipt.nextPublicationAttemptAt = null
    events.save(receipt)
  }

  private fun matches(
    receipt: Up3SimulationReturnEvent,
    event: ReturnsDomainEvent,
    timestamp: Instant = Instant.parse(event.data.datetimeOfStatusChange),
  ): Boolean = receipt.orderId == event.simulationOrderId &&
    receipt.simulatedSubmissionVersionId == event.simulationVersionId &&
    receipt.caseId == event.data.caseId &&
    receipt.payloadHash == payloads.returnHash(
      receipt.orderId,
      receipt.targetOrderVersionId,
      receipt.simulatedSubmissionVersionId,
      timestamp,
      event.data.reasons.map { SimulatedReturnReasonDto(it.section, it.details) },
    )

  fun requireCurrentSubmission(order: Order, simulation: Up3Simulation) {
    if (order.status != OrderStatus.SUBMITTED || order.versionId != simulation.versionId) {
      throw OrderChangeException(
        "UP3_SIMULATION_NOT_SUBMITTED",
        "The simulation requires the current submitted version",
      )
    }
  }

  private fun findByCaseId(caseId: String): Up3Simulation? {
    val simulation = if (settings.storageAvailable) {
      simulations.findByCaseId(caseId) ?: submissions.findByCaseId(caseId)?.let { find(it.orderId) }
    } else {
      null
    }
    return simulation ?: if (caseId.startsWith("sim-")) {
      throw OrderChangeException("UP3_SIMULATION_UNKNOWN_CASE", "Synthetic cases cannot use real UP3")
    } else {
      null
    }
  }

  private fun response(id: String) = FmsResponse(result = listOf(FmsResult(id = id)))

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
