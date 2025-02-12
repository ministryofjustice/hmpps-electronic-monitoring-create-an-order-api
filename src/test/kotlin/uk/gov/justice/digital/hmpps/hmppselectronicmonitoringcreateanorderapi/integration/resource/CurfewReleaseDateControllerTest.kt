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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewReleaseDateConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.UpdateCurfewReleaseDateConditionsDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidationError
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class CurfewReleaseDateControllerTest : IntegrationTestBase() {
  @Autowired
  lateinit var objectMapper: ObjectMapper

  val mockReleaseDate: ZonedDateTime = ZonedDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS)

  @BeforeEach
  fun setup() {
    repo.deleteAll()
  }

  @Test
  fun `Curfew release date for an order created by a different user are not update-able`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-release-date")
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
  fun `Curfew release date for an order already submitted are not update-able`() {
    val order = createSubmittedOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-release-date")
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
  fun `Should return errors when curfew release date is invalid`() {
    val order = createSubmittedOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-release-date")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockRequestBody(null, null, null, null),
        ),
      )
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBodyList(ValidationError::class.java)
      .returnResult()
    val error = result.responseBody!!
    Assertions.assertThat(result.responseBody).hasSize(4)
    Assertions.assertThat(
      error,
    ).contains(ValidationError("curfewAddress", "Curfew address is required"))
    Assertions.assertThat(error).contains(ValidationError("startTime", "Enter start time"))
    Assertions.assertThat(error).contains(ValidationError("endTime", "Enter end time"))
    Assertions.assertThat(
      error,
    ).contains(ValidationError("releaseDate", "Enter curfew release date"))
  }

  @Test
  fun `Should not return errors when release date is in the past`() {
    val order = createOrder()
    webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-release-date")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockRequestBody(
            ZonedDateTime.now().minusDays(1),
            "19:00:00",
            "23:00:00",
            AddressType.PRIMARY,
          ),
        ),
      )
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(CurfewReleaseDateConditions::class.java)
  }

  @Test
  fun `Should save order with updated release date `() {
    val order = createOrder()
    webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-release-date")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockValidRequestBody(),
        ),
      )
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isOk

    // Get updated order
    val updatedOrder = getOrder(order.id)

    Assertions.assertThat(updatedOrder.curfewReleaseDateConditions).isNotNull()
    Assertions.assertThat(updatedOrder.curfewReleaseDateConditions?.releaseDate).isEqualTo(mockReleaseDate)
    Assertions.assertThat(updatedOrder.curfewReleaseDateConditions?.startTime).isEqualTo("19:00:00")
    Assertions.assertThat(updatedOrder.curfewReleaseDateConditions?.endTime).isEqualTo("23:59:00")
    Assertions.assertThat(updatedOrder.curfewReleaseDateConditions?.curfewAddress).isEqualTo(AddressType.PRIMARY)
  }

  fun mockValidRequestBody(
    releaseDate: ZonedDateTime? = mockReleaseDate,
    startTime: String? = "19:00:00",
    endTime: String? = "23:59:00",
    curfewAddress: AddressType? = AddressType.PRIMARY,
  ): String {
    return mockRequestBody(releaseDate, startTime, endTime, curfewAddress)
  }

  fun mockRequestBody(
    releaseDate: ZonedDateTime?,
    startTime: String?,
    endTime: String?,
    curfewAddress: AddressType?,
  ): String {
    val dto = UpdateCurfewReleaseDateConditionsDto(
      releaseDate = releaseDate,
      startTime = startTime,
      endTime = endTime,
      curfewAddress = curfewAddress,
    )
    return objectMapper.writeValueAsString(dto)
  }
}
