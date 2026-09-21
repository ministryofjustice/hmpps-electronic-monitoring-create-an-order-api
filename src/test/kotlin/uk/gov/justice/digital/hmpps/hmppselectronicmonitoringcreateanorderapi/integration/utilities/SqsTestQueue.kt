package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.utilities

import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.MissingQueueException
import uk.gov.justice.hmpps.sqs.countAllMessagesOnQueue
import java.util.UUID

class SqsTestQueue(hmppsQueueService: HmppsQueueService, queueId: String) {

  private val config: HmppsQueue = hmppsQueueService.findByQueueId(queueId)
    ?: throw MissingQueueException("HmppsQueue $queueId not found")
  private val queueUrl: String = this.config.queueUrl
  private val queueClient: SqsAsyncClient = this.config.sqsClient
  private val dlqUrl: String = this.config.dlqUrl as String
  private val dlqClient: SqsAsyncClient = this.config.sqsDlqClient as SqsAsyncClient

  fun sendMessage(raw: String) {
    queueClient.sendMessage(
      SendMessageRequest.builder().queueUrl(
        queueUrl,
      ).messageBody(raw).messageGroupId("RETURNS_EVENT").messageDeduplicationId(
        UUID.randomUUID().toString(),
      ).build(),
    )
  }

  fun purge() {
    queueClient.purgeQueue(
      PurgeQueueRequest.builder().queueUrl(queueUrl)
        .build(),
    ).get()
  }

  fun purgeDlq() {
    dlqClient.purgeQueue(
      PurgeQueueRequest.builder().queueUrl(dlqUrl)
        .build(),
    ).get()
  }

  fun isEmpty(): Boolean = this.countMessages() == 0

  fun dlqIsEmpty(): Boolean = this.countMessagesDlq() == 0

  private fun countMessages(): Int = queueClient.countAllMessagesOnQueue(queueUrl).get()

  private fun countMessagesDlq(): Int = dlqClient.countAllMessagesOnQueue(dlqUrl).get()
}

@Component
class SqsTestQueueFactory(private val hmppsQueueService: HmppsQueueService) {
  fun create(queueId: String): SqsTestQueue = SqsTestQueue(this.hmppsQueueService, queueId)
}
