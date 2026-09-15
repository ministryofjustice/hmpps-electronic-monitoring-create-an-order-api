package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.OrderCaseSearchResultDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.utilities.TestUtilities

class FakeOrderByCaseIdGateway : OrderByCaseIdGateway {
  private val ordersByCaseId = mutableMapOf<String, Order>()

  fun addOrder(caseId: String, order: Order) {
    ordersByCaseId[caseId] = order
  }

  override fun findOrderByCaseId(caseId: String): Order? = ordersByCaseId[caseId]
}

class FindOrderByCaseIdServiceTest {

  @Test
  fun `returns the order with full version history when the case id matches a submitted order`() {
    val gateway = FakeOrderByCaseIdGateway()
    val service = FindOrderByCaseIdService(gateway)

    val order = TestUtilities.createReadyToSubmitOrder()
    gateway.addOrder("CASE123", order)

    val result: OrderCaseSearchResultDto? = service.execute("CASE123")

    assertThat(result?.id).isEqualTo(order.id)
    assertThat(result?.versions).hasSize(order.versions.size)
    assertThat(result?.versions?.map { it.versionId }).isEqualTo(order.versions.map { it.versionId })
    assertThat(result?.versions?.first()?.status).isEqualTo(order.versions.first().status)
    assertThat(result?.versions?.first()?.type).isEqualTo(order.versions.first().type)
    assertThat(result?.versions?.first()?.deviceWearer).isEqualTo(order.versions.first().deviceWearer)
  }

  @Test
  fun `returns null when no order matches the given case id`() {
    val gateway = FakeOrderByCaseIdGateway()
    val service = FindOrderByCaseIdService(gateway)

    val result = service.execute("UNKNOWN_CASE_ID")

    assertThat(result).isNull()
  }
}
