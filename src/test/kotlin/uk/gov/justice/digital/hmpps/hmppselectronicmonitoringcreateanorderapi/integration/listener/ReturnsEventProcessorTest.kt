package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.listener

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import tools.jackson.databind.ObjectMapper
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.utilities.SqsTestQueueFactory
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.listener.ReturnsEventProcessor
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3.Reason
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3.ReturnMessage
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.external.up3.ReturnStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsDeviceWearerSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionStrategyKind
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.FmsSubmissionResultRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository

class ReturnsEventProcessorTest : IntegrationTestBase() {

  @MockitoSpyBean
  lateinit var orderRepo: OrderRepository

  @MockitoSpyBean
  lateinit var fmsSubmissionResultRepository: FmsSubmissionResultRepository

  @Autowired
  lateinit var sqsTestQueueFactory: SqsTestQueueFactory

  @Autowired
  lateinit var returnsEventProcessor: ReturnsEventProcessor

  @Autowired
  lateinit var objectMapper: ObjectMapper

  private val queue by lazy { sqsTestQueueFactory.create("returnseventqueue") }

  @BeforeEach
  fun setup() {
    queue.purge()
    queue.purgeDlq()
  }

  @Test
  fun `update order version to failed`() {
    val caseId = "CASE123"
    val submittedOrder = arrangeSubmittedOrder(caseId)

    queue.sendMessage(createReturnEventMessage(caseId, ReturnStatus.REJECTED))

    await().until { queue.isEmpty() }
    assertThat(queue.dlqIsEmpty()).isEqualTo(true)

    val order = orderRepo.findById(submittedOrder.id).get()

    assertThat(order.status).isEqualTo(OrderStatus.REJECTED)
  }

  private fun arrangeSubmittedOrder(caseId: String): Order {
    val submittedOrder = createSubmittedOrder(RequestType.REQUEST, DataDictionaryVersion.DDV7)
    fmsSubmissionResultRepository.save(
      FmsSubmissionResult(
        orderId = submittedOrder.id,
        strategy = FmsSubmissionStrategyKind.ORDER,
        orderSource = FmsOrderSource.CEMO,
        deviceWearerResult = FmsDeviceWearerSubmissionResult(deviceWearerId = caseId),
      ),
    )
    return submittedOrder
  }

  fun createReturnEventMessage(
    caseId: String,
    status: ReturnStatus = ReturnStatus.REJECTED,
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
