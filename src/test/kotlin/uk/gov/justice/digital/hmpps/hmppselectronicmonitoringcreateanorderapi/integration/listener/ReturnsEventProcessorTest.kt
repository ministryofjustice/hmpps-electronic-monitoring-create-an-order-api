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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ProcessingStatus
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

  @Test
  fun `store return type and reasons against the order version`() {
    val caseId = "CASE789"
    val submittedOrder = arrangeSubmittedOrder(caseId)

    queue.sendMessage(
      createReturnEventMessage(
        caseId = caseId,
        status = ReturnStatus.REJECTED,
        section = "Section 5",
        details = "Missing signature on the licence",
        dateTime = "2026-09-23T10:15:00Z",
      ),
    )

    await().until { queue.isEmpty() }
    assertThat(queue.dlqIsEmpty()).isEqualTo(true)

    val statusUpdates = repo.findById(submittedOrder.id).get().statusUpdates

    assertThat(statusUpdates).hasSize(1)

    val statusUpdate = statusUpdates.first()
    assertThat(statusUpdate.status).isEqualTo(ProcessingStatus.REJECTED)
    assertThat(statusUpdate.datetimeOfStatusChange).isEqualTo("2026-09-23T10:15:00Z")
    assertThat(statusUpdate.statusUpdateReasons).hasSize(1)
    assertThat(statusUpdate.statusUpdateReasons.first().section).isEqualTo("Section 5")
    assertThat(statusUpdate.statusUpdateReasons.first().details).isEqualTo("Missing signature on the licence")
  }

  @Test
  fun `dead letters a malformed returns event`() {
    queue.sendMessage("BAD JSON")

    await().until { !queue.dlqIsEmpty() }
    assertThat(queue.dlqIsEmpty()).isEqualTo(false)
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
    dateTime: String = "2026-09-23T10:15:00Z",
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
