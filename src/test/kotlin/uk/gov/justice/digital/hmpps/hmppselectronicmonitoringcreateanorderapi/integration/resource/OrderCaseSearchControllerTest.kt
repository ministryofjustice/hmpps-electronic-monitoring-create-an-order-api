package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.OrderCaseSearchResultDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FmsOrderSource
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsDeviceWearerSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsSubmissionStrategyKind
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.FmsSubmissionResultRepository

class OrderCaseSearchControllerTest : IntegrationTestBase() {

  @Autowired
  lateinit var fmsSubmissionResultRepository: FmsSubmissionResultRepository

  private fun givenAnOrderSubmittedWithCaseId(caseId: String) = createStoredOrder().also { order ->
    order.versions.add(
      OrderVersion(
        orderId = order.id,
        versionId = 1,
        username = testUser,
        status = OrderStatus.SUBMITTED,
        type = RequestType.VARIATION,
        dataDictionaryVersion = DataDictionaryVersion.DDV4,
      ),
    )
    repo.save(order)
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
        .headers(setAuthorisation(roles = listOf("ROLE_EM_CEMO__GET_ORDER__RO")))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody<OrderCaseSearchResultDto>()
        .consumeWith {
          val result = it.responseBody!!
          assertThat(result.id).isEqualTo(order.id)
          assertThat(result.versions.map { version -> version.versionId })
            .containsExactlyInAnyOrder(0, 1)
        }
    }

    @Test
    fun `returns not found when no order matches the case id`() {
      webTestClient.get()
        .uri("/api/orders/search/by-case-id/UNKNOWN_CASE_ID")
        .headers(setAuthorisation(roles = listOf("ROLE_EM_CEMO__GET_ORDER__RO")))
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
