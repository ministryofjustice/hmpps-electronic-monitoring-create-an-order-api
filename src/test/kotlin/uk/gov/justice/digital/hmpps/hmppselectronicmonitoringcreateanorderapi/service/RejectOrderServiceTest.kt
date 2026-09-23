package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ProcessingStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.utilities.TestUtilities
import java.time.Instant

class RejectOrderServiceTest {

  @Test
  fun `adds a status update with its reasons to the order matching the given case id`() {
    val gateway = FakeOrderByCaseIdGateway()
    val repo = mock<OrderRepository>()
    val service = RejectOrderService(gateway, repo)

    val order = TestUtilities.createReadyToSubmitOrder()
    gateway.addOrder("CASE123", order)

    service.execute(
      "CASE123",
      ProcessingStatus.REJECTED,
      "2024-01-01T10:15:30Z",
      listOf(
        StatusUpdateReasonRequest(section = "Section A", details = "A details"),
        StatusUpdateReasonRequest(section = "Section B", details = "B details"),
      ),
    )

    assertThat(order.status).isEqualTo(OrderStatus.REJECTED)

    val statusUpdate = order.statusUpdates.single()
    assertThat(statusUpdate.versionId).isEqualTo(order.versionId)
    assertThat(statusUpdate.status).isEqualTo(ProcessingStatus.REJECTED)
    assertThat(statusUpdate.datetimeOfStatusChange.toInstant())
      .isEqualTo(Instant.parse("2024-01-01T10:15:30Z"))

    assertThat(statusUpdate.statusUpdateReasons).hasSize(2)
    assertThat(statusUpdate.statusUpdateReasons.map { it.statusUpdateId }).containsOnly(statusUpdate.id)
    assertThat(statusUpdate.statusUpdateReasons[0].section).isEqualTo("Section A")
    assertThat(statusUpdate.statusUpdateReasons[0].details).isEqualTo("A details")
    assertThat(statusUpdate.statusUpdateReasons[1].section).isEqualTo("Section B")
    assertThat(statusUpdate.statusUpdateReasons[1].details).isEqualTo("B details")

    verify(repo).save(order)
  }

  @Test
  fun `adds a status update with no reasons when none are given`() {
    val gateway = FakeOrderByCaseIdGateway()
    val repo = mock<OrderRepository>()
    val service = RejectOrderService(gateway, repo)

    val order = TestUtilities.createReadyToSubmitOrder()
    gateway.addOrder("CASE123", order)

    service.execute("CASE123", ProcessingStatus.REJECTED, "2024-01-01T10:15:30Z", emptyList())

    assertThat(order.statusUpdates.single().statusUpdateReasons).isEmpty()
  }

  @Test
  fun `throws when no order matches the given case id`() {
    val gateway = FakeOrderByCaseIdGateway()
    val repo = mock<OrderRepository>()
    val service = RejectOrderService(gateway, repo)

    assertThatThrownBy {
      service.execute("UNKNOWN_CASE_ID", ProcessingStatus.REJECTED, "2024-01-01T10:15:30Z", emptyList())
    }.isInstanceOf(EntityNotFoundException::class.java)
  }
}
