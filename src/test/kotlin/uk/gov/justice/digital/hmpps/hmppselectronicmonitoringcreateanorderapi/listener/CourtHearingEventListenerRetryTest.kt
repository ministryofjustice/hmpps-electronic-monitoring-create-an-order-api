package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.listener

import io.awspring.cloud.sqs.listener.SqsHeaders.SQS_SOURCE_DATA_HEADER
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.messaging.support.GenericMessage
import software.amazon.awssdk.services.sqs.model.Message
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue
import tools.jackson.module.kotlin.jacksonObjectMapper
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.EventService
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.S3Service
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.courthearing.DeadLetterQueueService
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.courthearing.HearingEventHandler

class CourtHearingEventListenerRetryTest {
  private val eventHandler = mock(HearingEventHandler::class.java)
  private val deadLetterQueueService = mock(DeadLetterQueueService::class.java)
  private val eventService = mock(EventService::class.java)
  private val s3Service = mock(S3Service::class.java)
  private lateinit var listener: CourtHearingEventListener

  @BeforeEach
  fun setUp() {
    listener = CourtHearingEventListener(
      eventHandler,
      deadLetterQueueService,
      jacksonObjectMapper(),
      eventService,
      s3Service,
    )
  }

  @Test
  fun `preserves retry attempts when a replayed message fails again`() {
    val sourceMessage = Message.builder()
      .messageAttributes(
        mapOf(
          "RetryAttempts" to MessageAttributeValue.builder()
            .dataType("String")
            .stringValue("2")
            .build(),
        ),
      )
      .build()

    listener.onDomainEvent(GenericMessage("BAD JSON", mapOf(SQS_SOURCE_DATA_HEADER to sourceMessage)))

    verify(deadLetterQueueService).sentEvent(
      eq("BAD JSON"),
      eq("Malformed event received. Could not parse JSON"),
      eq(2),
    )
  }

  @Test
  fun `reads retry attempts from a mapped SQS message header`() {
    listener.onDomainEvent(GenericMessage("BAD JSON", mapOf("RetryAttempts" to "2")))

    verify(deadLetterQueueService).sentEvent(
      eq("BAD JSON"),
      eq("Malformed event received. Could not parse JSON"),
      eq(2),
    )
  }

  @Test
  fun `uses zero retry attempts for an initial message`() {
    listener.onDomainEvent("BAD JSON")

    verify(deadLetterQueueService).sentEvent(
      eq("BAD JSON"),
      eq("Malformed event received. Could not parse JSON"),
      eq(0),
    )
  }
}
