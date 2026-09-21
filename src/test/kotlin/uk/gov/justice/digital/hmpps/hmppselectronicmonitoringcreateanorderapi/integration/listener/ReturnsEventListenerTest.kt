package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.listener

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import tools.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.listener.ReturnsEventListener
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3.Reason
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3.ReturnMessage
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsDeviceWearerSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionStrategyKind
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.FmsSubmissionResultRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.MissingQueueException
import uk.gov.justice.hmpps.sqs.countAllMessagesOnQueue
import java.util.UUID

class ReturnsEventListenerTest : IntegrationTestBase() {

  @MockitoSpyBean
  lateinit var orderRepo: OrderRepository

  @MockitoSpyBean
  lateinit var fmsSubmissionResultRepository: FmsSubmissionResultRepository

  @Autowired
  lateinit var hmppsQueueService: HmppsQueueService

  @Autowired
  lateinit var returnsEventListener: ReturnsEventListener

  @Autowired
  lateinit var objectMapper: ObjectMapper

  val returnsEventQueueConfig by lazy {
    hmppsQueueService.findByQueueId("returnseventqueue")
      ?: throw MissingQueueException("HmppsQueue returnseventqueue not found")
  }
  val queueUrl by lazy { returnsEventQueueConfig.queueUrl }
  val queueClient by lazy { returnsEventQueueConfig.sqsClient }
  val dlqClient by lazy { returnsEventQueueConfig.sqsDlqClient as SqsAsyncClient }
  val dlqUrl by lazy { returnsEventQueueConfig.dlqUrl as String }

  @BeforeEach
  fun setup() {
    queueClient.purgeQueue(
      PurgeQueueRequest.builder().queueUrl(queueUrl)
        .build(),
    ).get()

    dlqClient.purgeQueue(
      PurgeQueueRequest.builder().queueUrl(dlqUrl)
        .build(),
    ).get()
  }

  @Test
  fun `update order version to failed`() {
    val caseId = "CASE123"

    val submittedOrder = createSubmittedOrder(RequestType.REQUEST, DataDictionaryVersion.DDV7)
    fmsSubmissionResultRepository.save(
      FmsSubmissionResult(
        orderId = submittedOrder.id,
        strategy = FmsSubmissionStrategyKind.ORDER,
        orderSource = FmsOrderSource.CEMO,
        deviceWearerResult = FmsDeviceWearerSubmissionResult(deviceWearerId = caseId),
      ),
    )

    val message = createReturnEventMessage(caseId, "Form returned")
    queueClient.sendMessage(
      SendMessageRequest.builder().queueUrl(
        queueUrl,
      ).messageBody(message).messageGroupId("RETURNS_EVENT").messageDeduplicationId(
        UUID.randomUUID().toString(),
      ).build(),
    )

    await().until { queueClient.countAllMessagesOnQueue(queueUrl).get() == 0 }

    assertThat(dlqClient.countAllMessagesOnQueue(dlqUrl).get()).isEqualTo(0)

    val order = orderRepo.findById(submittedOrder.id).get()

    assertThat(order).isNotNull()
    assertThat(order.status).isEqualTo(OrderStatus.REJECTED)
  }

  fun createReturnEventMessage(
    caseId: String,
    status: String = "approved",
    section: String = "",
    details: String = "",
    dateTime: String = "",
  ): String {
    val message = ReturnMessage(
      caseId = caseId,
      status = status,
      reasons = listOf(Reason(section = section, details = details)),
      datetimeOfStatusChange = dateTime,
    )

    return objectMapper.writeValueAsString(message)
  }
}
