package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateInstallationAndRiskDto
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.util.*

class InstallationAndRiskControllerTest : IntegrationTestBase() {
  @Autowired
  lateinit var objectMapper: ObjectMapper

  @BeforeEach
  fun setup() {
    repo.deleteAll()
  }

  @Test
  fun `Installation and Risk for an order created by a different user are not update-able`() {
    val order = createOrder()
    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/installation-and-risk")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockValidRequestBody(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM_2"))
      .exchange()
      .expectStatus()
      .isNotFound
      .expectBodyList(ErrorResponse::class.java)
      .returnResult()
    val error = result.responseBody!!.first()
    Assertions.assertThat(
      error.developerMessage,
    ).isEqualTo("An editable order with ${order.id} does not exist")
  }

  @Test
  fun `Installation and Risk for an order already submitted are not update-able`() {
    val order = createSubmittedOrder()
    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/installation-and-risk")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockValidRequestBody(),
        ),
      )
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isNotFound
      .expectBodyList(ErrorResponse::class.java)
      .returnResult()
    val error = result.responseBody!!.first()
    Assertions.assertThat(
      error.developerMessage,
    ).isEqualTo("An editable order with ${order.id} does not exist")
  }

  @Test
  fun `Should save order with updated installation and risk`() {
    val order = createOrder()
    val mockRisk = mockValidRequestBody(
      offence = "MockOffence",
      riskCategory = arrayOf("MockCategory"),
      riskDetails = "mockRisk",
      mappaLevel = "mockMappaLevel",
      mappaCaseType = "mockMappaType",
    )
    webTestClient.put()
      .uri("/api/orders/${order.id}/installation-and-risk")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockRisk,
        ),
      )
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isOk

    // Get updated order
    val updatedOrder = getOrder(order.id)

    Assertions.assertThat(updatedOrder.installationAndRisk?.offence).isEqualTo("MockOffence")
    Assertions.assertThat(updatedOrder.installationAndRisk?.riskCategory?.first()).isEqualTo("MockCategory")
    Assertions.assertThat(updatedOrder.installationAndRisk?.riskDetails).isEqualTo("mockRisk")
    Assertions.assertThat(updatedOrder.installationAndRisk?.mappaLevel).isEqualTo("mockMappaLevel")
    Assertions.assertThat(updatedOrder.installationAndRisk?.mappaCaseType).isEqualTo("mockMappaType")
  }

  @Test
  fun `Should save order with updated installation and risk will all default value`() {
    val order = createOrder()
    val mockRisk = mockValidRequestBody()

    webTestClient.put()
      .uri("/api/orders/${order.id}/installation-and-risk")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockRisk,
        ),
      )
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isOk

    // Get updated order
    val updatedOrder = getOrder(order.id)

    Assertions.assertThat(updatedOrder.installationAndRisk?.offence).isNull()
    Assertions.assertThat(updatedOrder.installationAndRisk?.riskCategory).isNull()
    Assertions.assertThat(updatedOrder.installationAndRisk?.riskDetails).isNull()
    Assertions.assertThat(updatedOrder.installationAndRisk?.mappaLevel).isNull()
    Assertions.assertThat(updatedOrder.installationAndRisk?.mappaCaseType).isNull()
  }

  fun mockValidRequestBody(
    offence: String? = null,
    riskCategory: Array<String>? = null,
    riskDetails: String? = null,
    mappaLevel: String? = null,
    mappaCaseType: String? = null,
  ): String {
    val condition = UpdateInstallationAndRiskDto(
      offence = offence,
      riskCategory = riskCategory,
      riskDetails = riskDetails,
      mappaLevel = mappaLevel,
      mappaCaseType = mappaCaseType,
    )
    return objectMapper.writeValueAsString(condition)
  }
}
