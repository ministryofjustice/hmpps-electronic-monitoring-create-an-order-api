package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.courthearing

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sqs.model.ChangeMessageVisibilityRequest
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest
import software.amazon.awssdk.services.sqs.model.Message
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue
import software.amazon.awssdk.services.sqs.model.MessageSystemAttributeName
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.config.CourtHearingDeadLetterRetryProperties
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.EventService
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import java.util.UUID

@Service
@ConditionalOnExpression(
  "\${common-platform.dead-letter-retry.enabled:false} && \${toggle.common-platform.processing.enabled:false}",
)
class CourtHearingDeadLetterRetryService(
  hmppsQueueService: HmppsQueueService,
  private val properties: CourtHearingDeadLetterRetryProperties,
  private val eventService: EventService,
) {
  private val queue by lazy { requireNotNull(hmppsQueueService.findByQueueId(QUEUE_ID)) }
  private val dlqClient by lazy { requireNotNull(queue.sqsDlqClient) }
  private val dlqUrl by lazy { requireNotNull(queue.dlqUrl) }

  @Scheduled(fixedDelayString = "\${common-platform.dead-letter-retry.poll-interval:60000}")
  fun retryDeadLetterMessages() {
    val messages = dlqClient.receiveMessage(
      ReceiveMessageRequest.builder()
        .queueUrl(dlqUrl)
        .maxNumberOfMessages(properties.batchSize)
        .messageAttributeNames("All")
        .messageSystemAttributeNames(MessageSystemAttributeName.ALL)
        .build(),
    ).get().messages()

    messages.forEach { message ->
      try {
        processMessage(message)
      } catch (exception: Exception) {
        log.error("Failed to process court-hearing DLQ message {}", message.messageId(), exception)
      }
    }
  }

  private fun processMessage(message: Message) {
    val retryAttempts = message.messageAttributes()[RETRY_ATTEMPTS]
      ?.stringValue()
      ?.toIntOrNull()
      ?: 0

    if (retryAttempts >= properties.maxRetries) {
      markExhaustedAndAlert(message, retryAttempts)
    } else {
      replayToMainQueue(message, retryAttempts + 1)
    }
  }

  private fun replayToMainQueue(message: Message, retryAttempts: Int) {
    queue.sqsClient.sendMessage(
      SendMessageRequest.builder()
        .queueUrl(queue.queueUrl)
        .messageBody(message.body())
        .messageAttributes(
          message.messageAttributes() +
            (RETRY_ATTEMPTS to stringAttribute(retryAttempts.toString())),
        )
        .messageGroupId(messageGroupId(message))
        .messageDeduplicationId(UUID.randomUUID().toString())
        .build(),
    ).get()

    deleteFromDeadLetterQueue(message)
  }

  private fun markExhaustedAndAlert(message: Message, retryAttempts: Int) {
    if (message.messageAttributes()[ALERT_SENT]?.stringValue().toBoolean()) {
      deferExhaustedMessage(message)
      return
    }

    dlqClient.sendMessage(
      SendMessageRequest.builder()
        .queueUrl(dlqUrl)
        .messageBody(message.body())
        .messageAttributes(
          message.messageAttributes() +
            (ALERT_SENT to stringAttribute(true.toString())),
        )
        .messageGroupId("$EXHAUSTED_GROUP_PREFIX${message.messageId()}")
        .messageDeduplicationId(UUID.randomUUID().toString())
        .build(),
    ).get()
    deleteFromDeadLetterQueue(message)

    eventService.recordEvent(
      "Common_Platform_DLQ_Retry_Exhausted",
      mapOf(
        "SQS Message ID" to message.messageId(),
        "Retry Attempts" to retryAttempts.toString(),
        "Queue ID" to QUEUE_ID,
      ),
    )
  }

  private fun deferExhaustedMessage(message: Message) {
    dlqClient.changeMessageVisibility(
      ChangeMessageVisibilityRequest.builder()
        .queueUrl(dlqUrl)
        .receiptHandle(message.receiptHandle())
        .visibilityTimeout(properties.exhaustedVisibilityTimeoutSeconds)
        .build(),
    ).get()
  }

  private fun deleteFromDeadLetterQueue(message: Message) {
    dlqClient.deleteMessage(
      DeleteMessageRequest.builder()
        .queueUrl(dlqUrl)
        .receiptHandle(message.receiptHandle())
        .build(),
    ).get()
  }

  private fun messageGroupId(message: Message): String =
    message.attributes()[MessageSystemAttributeName.MESSAGE_GROUP_ID] ?: GROUP_ID

  private fun stringAttribute(value: String): MessageAttributeValue = MessageAttributeValue.builder()
    .dataType("String")
    .stringValue(value)
    .build()

  private companion object {
    val log = LoggerFactory.getLogger(CourtHearingDeadLetterRetryService::class.java)
    const val QUEUE_ID = "courthearingeventqueue"
    const val GROUP_ID = "COURT_HEARING_EVENT"
    const val EXHAUSTED_GROUP_PREFIX = "COURT_HEARING_EVENT_EXHAUSTED_"
    const val RETRY_ATTEMPTS = "RetryAttempts"
    const val ALERT_SENT = "AlertSent"
  }
}
