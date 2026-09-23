package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.RejectionReason
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.utilities.TestUtilities
import java.time.ZonedDateTime

class RejectOrderServiceTest {

  @Test
  fun `rejects and saves the order matching the given case id`() {
    val gateway = FakeOrderByCaseIdGateway()
    val repo = mock<OrderRepository>()
    val service = RejectOrderService(gateway, repo)

    val order = TestUtilities.createReadyToSubmitOrder()
    gateway.addOrder("CASE123", order)

    service.execute(
      "CASE123",
      ZonedDateTime.now(),
      listOf(RejectionReason(section = "Section A", details = "A details")),
    )

    assertThat(order.status).isEqualTo(OrderStatus.REJECTED)
    assertThat(order.statusUpdates).hasSize(1)
    verify(repo).save(order)
  }

  @Test
  fun `throws and saves nothing when no order matches the given case id`() {
    val gateway = FakeOrderByCaseIdGateway()
    val repo = mock<OrderRepository>()
    val service = RejectOrderService(gateway, repo)

    assertThatThrownBy {
      service.execute("UNKNOWN_CASE_ID", ZonedDateTime.now(), emptyList())
    }.isInstanceOf(EntityNotFoundException::class.java)

    verifyNoInteractions(repo)
  }
}
