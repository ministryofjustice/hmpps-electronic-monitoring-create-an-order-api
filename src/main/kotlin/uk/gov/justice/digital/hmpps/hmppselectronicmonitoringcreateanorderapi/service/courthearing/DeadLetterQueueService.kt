package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service.courthearing

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import java.util.UUID

private const val GROUP_ID = "COURT_HEARING_EVENT"

@Service
@ConditionalOnExpression("\${toggle.common-platform.processing.enabled:false}")
class DeadLetterQueueService(
  private val hmppsQueueService: HmppsQueueService,
  private val objectMapper: ObjectMapper,
) {

  private val dlQueue by lazy { hmppsQueueService.findByQueueId("courthearingeventqueue") as HmppsQueue }
  private val dlClient by lazy { dlQueue.sqsDlqClient!! }
  private val dlQueueUrl by lazy { dlQueue.dlqUrl }

  fun sentEvent(payload: Any, errorMessage: String?, retryAttempts: Int? = 0) {
    val messageBody = if (payload is String) payload else objectMapper.writeValueAsString(payload)
    dlClient.sendMessage(
      SendMessageRequest.builder()
        .queueUrl(dlQueueUrl)
        .messageBody(messageBody)
        .messageAttributes(
          mapOf(
            "Error" to MessageAttributeValue.builder().dataType("String").stringValue(errorMessage).build(),
            "RetryAttempts" to MessageAttributeValue
              .builder()
              .dataType("String")
              .stringValue(retryAttempts.toString()).build(),
          ),
        ).messageGroupId(GROUP_ID)
        .messageDeduplicationId(UUID.randomUUID().toString())
        .build(),
    )
  }
}
