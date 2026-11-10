package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import software.amazon.awssdk.core.exception.SdkException
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.SimulatedReturnStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.SimulationFailureStage
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Up3SimulationReturnEvent
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.Up3SimulationReturnEventRepository
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.MissingQueueException
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@Service
@ConditionalOnProperty(name = ["settings.up3-simulation-enabled"], havingValue = "true")
class Up3SimulationReturnPublisher(
  private val events: Up3SimulationReturnEventRepository,
  private val queues: HmppsQueueService,
  private val telemetry: EventService,
) {
  @Transactional
  fun publish(eventId: UUID) {
    val event = events.lockById(eventId) ?: return
    val nextAttempt = event.nextPublicationAttemptAt ?: return
    if (nextAttempt.isAfter(Instant.now()) ||
      event.status !in setOf(SimulatedReturnStatus.PENDING_PUBLICATION, SimulatedReturnStatus.FAILED)
    ) {
      return
    }
    event.publicationAttempts++
    try {
      val queue = queues.findByQueueId("returnseventqueue")
        ?: throw MissingQueueException("Returns queue is not configured")
      queue.sqsClient.sendMessage(
        SendMessageRequest.builder()
          .queueUrl(queue.queueUrl)
          .messageBody(event.payload)
          .messageGroupId("case:${Up3SimulationPayloads.hash(event.caseId)}")
          .messageDeduplicationId("simulation:${event.eventId}")
          .build(),
      ).get(10, TimeUnit.SECONDS)
      event.status = SimulatedReturnStatus.QUEUED
      event.queuedAt = Instant.now()
      event.nextPublicationAttemptAt = null
      event.failureStage = null
      event.errorCode = null
      telemetry.recordEvent(
        "UP3_Simulation_Return_Published",
        mapOf("orderId" to event.orderId.toString(), "eventId" to event.eventId.toString()),
      )
    } catch (e: ExecutionException) {
      failed(event, e)
    } catch (e: TimeoutException) {
      failed(event, e)
    } catch (e: CancellationException) {
      failed(event, e)
    } catch (e: SdkException) {
      failed(event, e)
    } catch (e: MissingQueueException) {
      failed(event, e)
    } catch (e: InterruptedException) {
      Thread.currentThread().interrupt()
      failed(event, e)
    }
    events.save(event)
  }

  private fun failed(event: Up3SimulationReturnEvent, exception: Exception) {
    event.status = SimulatedReturnStatus.FAILED
    event.failureStage = SimulationFailureStage.PUBLICATION
    event.errorCode = "UP3_SIMULATION_PUBLICATION_FAILED"
    event.nextPublicationAttemptAt = if (event.publicationAttempts < 5) Instant.now().plusSeconds(5) else null
    log.warn("Simulation return publication failed: event={}, cause={}", event.eventId, exception.javaClass.simpleName)
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
