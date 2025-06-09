package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewConditions
import java.time.ZonedDateTime
import java.util.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import java.time.temporal.ChronoUnit

class CurfewAdditionalDetailsControllerTest : IntegrationTestBase() {
  @Autowired
  lateinit var objectMapper: ObjectMapper

  val mockStartDate: ZonedDateTime = ZonedDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS)
  val mockEndDate: ZonedDateTime = ZonedDateTime.now().plusDays(3).truncatedTo(ChronoUnit.SECONDS)

  @BeforeEach
  fun setup() {
    repo.deleteAll()
  }

  @Test
  fun `Curfew additional details can be updated with null`() {

    val order = createOrder()

    webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-conditions")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockValidRequestBody(order.id),
        ),
      )
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isOk

    webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-details")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "curfewAdditionalDetails": null
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isOk
  }

  fun mockValidRequestBody(
    orderId: UUID,
    startDate: ZonedDateTime? = mockStartDate,
    endDate: ZonedDateTime? = mockEndDate,
    curfewAddress: String? = "PRIMARY,SECONDARY",
  ): String = mockRequestBody(orderId, startDate, endDate, curfewAddress)

  fun mockRequestBody(
    orderId: UUID,
    startDate: ZonedDateTime?,
    endDate: ZonedDateTime?,
    curfewAddress: String?,
  ): String {
    val condition = CurfewConditions(
      versionId = orderId,
      startDate = startDate,
      endDate = endDate,
      curfewAddress = curfewAddress,
    )
    return objectMapper.writeValueAsString(condition)
  }
}