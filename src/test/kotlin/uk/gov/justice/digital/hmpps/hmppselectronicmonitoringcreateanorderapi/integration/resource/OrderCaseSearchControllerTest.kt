package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.OrderCaseSearchResultDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsDeviceWearerSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionStrategyKind
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.FmsSubmissionResultRepository

class OrderCaseSearchControllerTest : IntegrationTestBase() {

  @Autowired
  lateinit var fmsSubmissionResultRepository: FmsSubmissionResultRepository

  private fun givenAnOrderSubmittedWithCaseId(caseId: String) = createStoredOrder().also { order ->
    fmsSubmissionResultRepository.save(
      FmsSubmissionResult(
        orderId = order.id,
        strategy = FmsSubmissionStrategyKind.ORDER,
        orderSource = FmsOrderSource.CEMO,
        deviceWearerResult = FmsDeviceWearerSubmissionResult(deviceWearerId = caseId),
      ),
    )
  }

  @Nested
  @DisplayName("GET /api/orders/search/by-case-id/{caseId}")
  inner class FindByCaseId {
    @Test
    fun `returns the order with its full version history when the case id matches`() {
      val order = givenAnOrderSubmittedWithCaseId("CASE123")

      webTestClient.get()
        .uri("/api/orders/search/by-case-id/CASE123")
        .headers(setAuthorisation(roles = listOf("ROLE_EM_CEMO__EXTERNAL_SEARCH")))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody<OrderCaseSearchResultDto>()
        .consumeWith {
          val result = it.responseBody!!
          assertThat(result.id).isEqualTo(order.id)
          assertThat(result.versions).hasSize(order.versions.size)
        }
    }

    @Test
    fun `returns not found when no order matches the case id`() {
      webTestClient.get()
        .uri("/api/orders/search/by-case-id/UNKNOWN_CASE_ID")
        .headers(setAuthorisation(roles = listOf("ROLE_EM_CEMO__EXTERNAL_SEARCH")))
        .exchange()
        .expectStatus()
        .isNotFound()
    }

    @Test
    fun `returns forbidden when the caller lacks the external search role`() {
      givenAnOrderSubmittedWithCaseId("CASE456")

      webTestClient.get()
        .uri("/api/orders/search/by-case-id/CASE456")
        .headers(setAuthorisation(roles = listOf("ROLE_EM_CEMO__CREATE_ORDER")))
        .exchange()
        .expectStatus()
        .isForbidden()
    }
  }
}
