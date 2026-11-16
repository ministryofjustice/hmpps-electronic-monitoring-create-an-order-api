package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.service

import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.StatusUpdate
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.StatusUpdateReason
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ProcessingStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.StatusUpdateRepository
import java.time.ZonedDateTime
import java.util.UUID

class StatusUpdateServiceTest {
  private val orderRepo = mock<OrderRepository>()
  private val statusUpdateRepo = mock<StatusUpdateRepository>()
  private val service = StatusUpdateService(orderRepo, statusUpdateRepo)

  @Test
  fun `returns status updates for the order, newest first, with reasons`() {
    val orderId = UUID.randomUUID()
    val now = ZonedDateTime.now()
    val older = StatusUpdate(
      orderId = orderId,
      orderVersionId = UUID.randomUUID(),
      orderVersionType = RequestType.REQUEST,
      status = ProcessingStatus.CREATED,
      datetimeOfStatusChange = now.minusDays(1),
    )
    val newerId = UUID.randomUUID()
    val newer = StatusUpdate(
      id = newerId,
      orderId = orderId,
      orderVersionId = UUID.randomUUID(),
      orderVersionType = RequestType.REQUEST,
      status = ProcessingStatus.REJECTED,
      datetimeOfStatusChange = now,
      statusUpdateReasons = mutableListOf(
        StatusUpdateReason(statusUpdateId = newerId, section = "Section A", details = "details"),
      ),
    )
    whenever(orderRepo.existsById(orderId)).thenReturn(true)
    whenever(statusUpdateRepo.findAllByOrderId(orderId)).thenReturn(listOf(older, newer))

    val result = service.getStatusUpdates(orderId)

    assertThat(result.map { it.status }).containsExactly(ProcessingStatus.REJECTED, ProcessingStatus.CREATED)
    assertThat(result[0].reasons.map { it.section }).containsExactly("Section A")
  }

  @Test
  fun `throws when the order does not exist`() {
    val orderId = UUID.randomUUID()
    whenever(orderRepo.existsById(orderId)).thenReturn(false)

    assertThatThrownBy { service.getStatusUpdates(orderId) }.isInstanceOf(EntityNotFoundException::class.java)
    verifyNoInteractions(statusUpdateRepo)
  }
}
