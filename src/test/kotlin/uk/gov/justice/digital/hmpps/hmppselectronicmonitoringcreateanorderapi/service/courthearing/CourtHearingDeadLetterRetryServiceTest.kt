package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.courthearing

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.ChangeMessageVisibilityRequest
import software.amazon.awssdk.services.sqs.model.ChangeMessageVisibilityResponse
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest
import software.amazon.awssdk.services.sqs.model.DeleteMessageResponse
import software.amazon.awssdk.services.sqs.model.Message
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue
import software.amazon.awssdk.services.sqs.model.MessageSystemAttributeName
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import software.amazon.awssdk.services.sqs.model.SendMessageResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.CourtHearingDeadLetterRetryProperties
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.EventService
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import java.util.concurrent.CompletableFuture

class CourtHearingDeadLetterRetryServiceTest {
  private val queueService = mock(HmppsQueueService::class.java)
  private val queue = mock(HmppsQueue::class.java)
  private val mainQueueClient = mock(SqsAsyncClient::class.java)
  private val deadLetterQueueClient = mock(SqsAsyncClient::class.java)
  private val eventService = mock(EventService::class.java)
  private val properties = CourtHearingDeadLetterRetryProperties().apply {
    maxRetries = 3
    batchSize = 10
  }
  private lateinit var service: CourtHearingDeadLetterRetryService

  @BeforeEach
  fun setUp() {
    whenever(queueService.findByQueueId("courthearingeventqueue")).thenReturn(queue)
    whenever(queue.sqsClient).thenReturn(mainQueueClient)
    whenever(queue.queueUrl).thenReturn("main-queue-url")
    whenever(queue.sqsDlqClient).thenReturn(deadLetterQueueClient)
    whenever(queue.dlqUrl).thenReturn("dead-letter-queue-url")
    whenever(mainQueueClient.sendMessage(any<SendMessageRequest>())).thenReturn(
      CompletableFuture.completedFuture(SendMessageResponse.builder().build()),
    )
    whenever(deadLetterQueueClient.sendMessage(any<SendMessageRequest>())).thenReturn(
      CompletableFuture.completedFuture(SendMessageResponse.builder().build()),
    )
    whenever(deadLetterQueueClient.deleteMessage(any<DeleteMessageRequest>()))
      .thenReturn(CompletableFuture.completedFuture(DeleteMessageResponse.builder().build()))
    whenever(deadLetterQueueClient.changeMessageVisibility(any<ChangeMessageVisibilityRequest>()))
      .thenReturn(CompletableFuture.completedFuture(ChangeMessageVisibilityResponse.builder().build()))
    service = CourtHearingDeadLetterRetryService(queueService, properties, eventService)
  }

  @Test
  fun `replays a DLQ message to the existing main queue and increments retry attempts`() {
    receive(message(retryAttempts = 1))

    service.retryDeadLetterMessages()

    val request = argumentCaptor<SendMessageRequest>()
    verify(mainQueueClient).sendMessage(request.capture())
    assertThat(request.firstValue.queueUrl()).isEqualTo("main-queue-url")
    assertThat(request.firstValue.messageBody()).isEqualTo("CP payload")
    assertThat(request.firstValue.messageAttributes()["RetryAttempts"]?.stringValue()).isEqualTo("2")
    assertThat(request.firstValue.messageAttributes()["Error"]?.stringValue()).isEqualTo("mapping failed")
    assertThat(request.firstValue.messageGroupId()).isEqualTo("COURT_HEARING_EVENT")
    verify(deadLetterQueueClient).deleteMessage(any<DeleteMessageRequest>())
    verifyNoInteractions(eventService)
  }

  @Test
  fun `does not delete the DLQ message when replay to the main queue fails`() {
    receive(message(retryAttempts = 1))
    whenever(mainQueueClient.sendMessage(any<SendMessageRequest>())).thenReturn(
      CompletableFuture.failedFuture(IllegalStateException("SQS unavailable")),
    )

    service.retryDeadLetterMessages()

    verify(deadLetterQueueClient, never()).deleteMessage(any<DeleteMessageRequest>())
    verifyNoInteractions(eventService)
  }

  @Test
  fun `retains an exhausted message in the DLQ and emits one payload-free alert event`() {
    receive(message(retryAttempts = 3))

    service.retryDeadLetterMessages()

    verify(mainQueueClient, never()).sendMessage(any<SendMessageRequest>())
    val request = argumentCaptor<SendMessageRequest>()
    verify(deadLetterQueueClient).sendMessage(request.capture())
    assertThat(request.firstValue.queueUrl()).isEqualTo("dead-letter-queue-url")
    assertThat(request.firstValue.messageAttributes()["AlertSent"]?.stringValue()).isEqualTo("true")
    assertThat(request.firstValue.messageAttributes()["RetryAttempts"]?.stringValue()).isEqualTo("3")
    assertThat(request.firstValue.messageGroupId()).isEqualTo("COURT_HEARING_EVENT_EXHAUSTED_message-id")
    verify(eventService).recordEvent(
      eq("Common_Platform_DLQ_Retry_Exhausted"),
      eq(
        mapOf(
          "SQS Message ID" to "message-id",
          "Retry Attempts" to "3",
          "Queue ID" to "courthearingeventqueue",
        ),
      ),
      eq(0),
    )
  }

  @Test
  fun `defers an exhausted message already marked as alerted`() {
    receive(message(retryAttempts = 3, alertSent = true))

    service.retryDeadLetterMessages()

    verify(mainQueueClient, never()).sendMessage(any<SendMessageRequest>())
    verify(deadLetterQueueClient, never()).sendMessage(any<SendMessageRequest>())
    verify(deadLetterQueueClient, never()).deleteMessage(any<DeleteMessageRequest>())
    val request = argumentCaptor<ChangeMessageVisibilityRequest>()
    verify(deadLetterQueueClient).changeMessageVisibility(request.capture())
    assertThat(request.firstValue.visibilityTimeout()).isEqualTo(43_200)
    verifyNoInteractions(eventService)
  }

  private fun receive(message: Message) {
    whenever(deadLetterQueueClient.receiveMessage(any<ReceiveMessageRequest>()))
      .thenReturn(CompletableFuture.completedFuture(ReceiveMessageResponse.builder().messages(message).build()))
  }

  private fun message(retryAttempts: Int, alertSent: Boolean = false): Message {
    val attributes = mutableMapOf(
      "RetryAttempts" to stringAttribute(retryAttempts.toString()),
      "Error" to stringAttribute("mapping failed"),
    )
    if (alertSent) attributes["AlertSent"] = stringAttribute(true.toString())

    return Message.builder()
      .messageId("message-id")
      .receiptHandle("receipt-handle")
      .body("CP payload")
      .messageAttributes(attributes)
      .attributes(mapOf(MessageSystemAttributeName.MESSAGE_GROUP_ID to "COURT_HEARING_EVENT"))
      .build()
  }

  private fun stringAttribute(value: String): MessageAttributeValue = MessageAttributeValue.builder()
    .dataType("String")
    .stringValue(value)
    .build()
}
