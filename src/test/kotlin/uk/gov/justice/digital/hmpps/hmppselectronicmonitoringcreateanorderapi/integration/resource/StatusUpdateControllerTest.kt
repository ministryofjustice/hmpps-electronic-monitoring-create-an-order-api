package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.expectBodyList
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.StatusUpdate
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.StatusUpdateDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.ProcessingStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.StatusUpdateRepository
import java.time.ZonedDateTime
import java.util.UUID

class StatusUpdateControllerTest : IntegrationTestBase() {
  @Autowired
  lateinit var statusUpdateRepo: StatusUpdateRepository

  @Test
  fun `Should return status updates for the order only, newest first`() {
    val order = createOrder()
    val otherOrder = createOrder()
    statusUpdateRepo.save(statusUpdate(order.id, ProcessingStatus.SUBMITTED, ZonedDateTime.now().plusMinutes(1)))
    statusUpdateRepo.save(statusUpdate(otherOrder.id, ProcessingStatus.REJECTED, ZonedDateTime.now()))

    val result = webTestClient.get()
      .uri("/api/orders/${order.id}/status-updates")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isOk
      .expectBodyList<StatusUpdateDto>()
      .returnResult().responseBody!!

    assertThat(result).allMatch { it.orderId == order.id }
    assertThat(result.map { it.status }).doesNotContain(ProcessingStatus.REJECTED)
    assertThat(result.first().status).isEqualTo(ProcessingStatus.SUBMITTED)
  }

  @Test
  fun `Should return 404 when the order does not exist`() {
    webTestClient.get()
      .uri("/api/orders/${UUID.randomUUID()}/status-updates")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `Should return 403 when the user lacks the create order role`() {
    webTestClient.get()
      .uri("/api/orders/${UUID.randomUUID()}/status-updates")
      .headers(setAuthorisation(roles = listOf("ROLE_OTHER")))
      .exchange()
      .expectStatus().isForbidden
  }

  private fun statusUpdate(orderId: UUID, status: ProcessingStatus, at: ZonedDateTime) = StatusUpdate(
    orderId = orderId,
    orderVersionId = UUID.randomUUID(),
    orderVersionType = RequestType.REQUEST,
    status = status,
    datetimeOfStatusChange = at,
  )
}
