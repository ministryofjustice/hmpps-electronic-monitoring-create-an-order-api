package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.utilities.TestUtilities

class UpdateOrderStatusServiceTest {

  @Test
  fun `updates the status of the order matching the given case id`() {
    val gateway = FakeOrderByCaseIdGateway()
    val repo = mock<OrderRepository>()
    val service = UpdateOrderStatusService(gateway, repo)

    val order = TestUtilities.createReadyToSubmitOrder()
    gateway.addOrder("CASE123", order)

    service.execute("CASE123", OrderStatus.REJECTED)

    assertThat(order.status).isEqualTo(OrderStatus.REJECTED)
    verify(repo).save(order)
  }

  @Test
  fun `throws when no order matches the given case id`() {
    val gateway = FakeOrderByCaseIdGateway()
    val repo = mock<OrderRepository>()
    val service = UpdateOrderStatusService(gateway, repo)

    assertThatThrownBy { service.execute("UNKNOWN_CASE_ID", OrderStatus.REJECTED) }
      .isInstanceOf(EntityNotFoundException::class.java)
  }
}
