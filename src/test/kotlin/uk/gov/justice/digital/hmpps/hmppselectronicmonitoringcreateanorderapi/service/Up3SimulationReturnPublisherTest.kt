package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import software.amazon.awssdk.services.sqs.model.SendMessageResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.SimulatedReturnStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.SimulationFailureStage
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Up3SimulationReturnEvent
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.Up3SimulationReturnEventRepository
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CompletableFuture

class Up3SimulationReturnPublisherTest {
  private val events = mock<Up3SimulationReturnEventRepository>()
  private val queues = mock<HmppsQueueService>()
  private val telemetry = mock<EventService>()
  private val sqs = mock<SqsAsyncClient>()
  private val publisher = Up3SimulationReturnPublisher(events, queues, telemetry)
  private val event = Up3SimulationReturnEvent(
    UUID.randomUUID(),
    UUID.randomUUID(),
    UUID.randomUUID(),
    UUID.randomUUID(),
    "test-case",
    "hash",
    """{"test":true}""",
    createdAt = Instant.now().minusSeconds(1),
  )

  @Test
  fun `publication uses the persisted original payload and only sends an intent once`() {
    configureQueue()
    whenever(sqs.sendMessage(any<SendMessageRequest>())).thenReturn(
      CompletableFuture.completedFuture(SendMessageResponse.builder().messageId("message-1").build()),
    )
    publisher.publish(event.eventId)
    publisher.publish(event.eventId)
    assertThat(event.status).isEqualTo(SimulatedReturnStatus.QUEUED)
    assertThat(event.queuedAt).isNotNull()
    assertThat(event.publicationAttempts).isEqualTo(1)
    assertThat(event.nextPublicationAttemptAt).isNull()
    verify(sqs).sendMessage(
      org.mockito.kotlin.check<SendMessageRequest> {
        assertThat(it.messageBody()).isEqualTo(event.payload)
        assertThat(it.messageDeduplicationId()).isEqualTo("simulation:${event.eventId}")
      },
    )
  }

  @Test
  fun `publication failure is visible and schedules a bounded retry`() {
    configureQueue()
    whenever(sqs.sendMessage(any<SendMessageRequest>())).thenReturn(
      CompletableFuture.failedFuture(IllegalStateException("queue unavailable")),
    )
    publisher.publish(event.eventId)
    assertThat(event.status).isEqualTo(SimulatedReturnStatus.FAILED)
    assertThat(event.failureStage).isEqualTo(SimulationFailureStage.PUBLICATION)
    assertThat(event.errorCode).isEqualTo("UP3_SIMULATION_PUBLICATION_FAILED")
    assertThat(event.nextPublicationAttemptAt).isAfter(Instant.now())
    publisher.publish(event.eventId)
    assertThat(event.publicationAttempts).isEqualTo(1)
  }

  @Test
  fun `fifth publication failure stops automatic retries`() {
    configureQueue()
    event.publicationAttempts = 4
    whenever(sqs.sendMessage(any<SendMessageRequest>())).thenReturn(
      CompletableFuture.failedFuture(IllegalStateException("queue unavailable")),
    )
    publisher.publish(event.eventId)
    assertThat(event.status).isEqualTo(SimulatedReturnStatus.FAILED)
    assertThat(event.publicationAttempts).isEqualTo(5)
    assertThat(event.nextPublicationAttemptAt).isNull()
    publisher.publish(event.eventId)
    assertThat(event.publicationAttempts).isEqualTo(5)
  }

  @Test
  fun `a delivery completed before publication acknowledgement is never republished`() {
    whenever(events.lockById(event.eventId)).thenReturn(event)
    event.complete(SimulatedReturnStatus.PROCESSED)
    publisher.publish(event.eventId)
    verifyNoInteractions(queues, sqs, telemetry)
  }

  @Test
  fun `missing queue is an explicit publication failure`() {
    whenever(events.lockById(event.eventId)).thenReturn(event)
    publisher.publish(event.eventId)
    assertThat(event.status).isEqualTo(SimulatedReturnStatus.FAILED)
    assertThat(event.failureStage).isEqualTo(SimulationFailureStage.PUBLICATION)
    verifyNoInteractions(sqs)
  }

  private fun configureQueue() {
    val queue = mock<HmppsQueue>()
    whenever(events.lockById(event.eventId)).thenReturn(event)
    whenever(queues.findByQueueId("returnseventqueue")).thenReturn(queue)
    whenever(queue.queueUrl).thenReturn("http://localhost/test.fifo")
    whenever(queue.sqsClient).thenReturn(sqs)
  }
}
