package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.listener

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Component
import org.springframework.transaction.TransactionException
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.RejectionReason
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3.ReturnMessage
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3.ReturnStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.EventService
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.RejectOrderAndNotify
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.Up3SimulationService
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
data class ReturnsDomainEvent(
  val data: ReturnMessage,
  val simulationOrderId: UUID? = null,
  val simulationVersionId: UUID? = null,
  val simulationEventId: UUID? = null,
)

@Component
@ConditionalOnExpression("\${settings.return-listener-enabled:false}")
class ReturnsEventTranslator(
  private val rejectOrderAndNotify: RejectOrderAndNotify,
  private val objectMapper: ObjectMapper,
  private val eventService: EventService,
  private val simulation: Up3SimulationService,
) {
  @SqsListener("returnseventqueue", factory = "hmppsQueueContainerFactoryProxy")
  fun processEvent(rawMessage: String) {
    var parsedEvent: ReturnsDomainEvent? = null
    try {
      val event: ReturnsDomainEvent = objectMapper.readValue(rawMessage)
      parsedEvent = event
      val message: ReturnMessage = event.data
      val dateTime = ZonedDateTime.ofInstant(Instant.parse(message.datetimeOfStatusChange), ZoneId.of("Europe/London"))

      when (message.status) {
        ReturnStatus.REJECTED -> {
          val applied = simulation.processReturn(event) {
            rejectOrderAndNotify.execute(
              message.caseId,
              dateTime,
              message.reasons.map { RejectionReason(section = it.section, details = it.details) },
            )
          }
          eventService.recordEvent(
            if (applied) "Returns_Status_Update" else "Returns_Simulation_Event_No_Change",
            mapOf("caseId" to message.caseId, "status" to message.status.toString()),
          )
        }
      }
    } catch (e: Exception) {
      try {
        parsedEvent?.let { simulation.recordReturnFailure(it) }
      } catch (recordingFailure: DataAccessException) {
        log.error("Failed to record simulated return failure: {}", recordingFailure.javaClass.simpleName)
      } catch (recordingFailure: TransactionException) {
        log.error("Failed to record simulated return failure: {}", recordingFailure.javaClass.simpleName)
      }
      log.error("Failed to process returns event: {}", e.javaClass.simpleName)
      eventService.recordEvent(
        "Returns_Event_Processing_Failed",
        mapOf("errorType" to e.javaClass.simpleName),
      )
      throw e
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
