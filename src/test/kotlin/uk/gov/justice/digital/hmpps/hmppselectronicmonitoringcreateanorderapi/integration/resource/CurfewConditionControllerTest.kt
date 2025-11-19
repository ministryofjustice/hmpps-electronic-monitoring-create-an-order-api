package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidationError
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*

class CurfewConditionControllerTest : IntegrationTestBase() {

  @Autowired
  lateinit var objectMapper: ObjectMapper

  val mockStartDate: ZonedDateTime = ZonedDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS)
  val mockEndDate: ZonedDateTime = ZonedDateTime.now().plusDays(3).truncatedTo(ChronoUnit.SECONDS)
  private val mockPastStartDate = ZonedDateTime.of(
    LocalDate.of(1970, 2, 1),
    LocalTime.NOON,
    ZoneId.of("UTC"),
  )
  private val mockPastEndDate = mockPastStartDate.plusDays(1)

  private object ErrorMessages {
    const val START_DATE_REQUIRED: String = "Enter date curfew starts"
    const val END_DATE_REQUIRED: String = "Enter date curfew ends"
    const val END_DATE_MUST_BE_IN_FUTURE: String = "Date curfew ends must be in the future"
    const val END_DATE_MUST_BE_AFTER_START_DATE: String = "Date curfew ends must be after the date curfew starts"
    const val ADDRESS_REQUIRED: String = "Select where the device wearer will be during curfew hours"
  }

  @BeforeEach
  fun setup() {
    repo.deleteAll()
  }

  @Test
  fun `Curfew release date for an order created by a different user are not update-able`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-conditions")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockValidRequestBody(order.id),
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
      .isNotFound
      .expectBodyList(ErrorResponse::class.java)
      .returnResult()
    val error = result.responseBody!!.first()
    Assertions.assertThat(
      error.developerMessage,
    ).isEqualTo("An editable order with ${order.id} does not exist")
  }

  @Test
  fun `Should return errors when curfew condition is invalid`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-conditions")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockRequestBody(order.id, null, null, null),
        ),
      )
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBodyList(ValidationError::class.java)
      .returnResult()
    val error = result.responseBody!!
    Assertions.assertThat(result.responseBody).hasSize(2)
    Assertions.assertThat(error).contains(ValidationError("startDate", ErrorMessages.START_DATE_REQUIRED))
    Assertions.assertThat(error).contains(ValidationError("endDate", ErrorMessages.END_DATE_REQUIRED))
  }

  @Test
  fun `Should return errors when end date is in the past`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-conditions")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockRequestBody(
            order.id,
            startDate = mockPastStartDate,
            endDate = mockPastEndDate,
            "PRIMARY",
          ),
        ),
      )
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBodyList(ValidationError::class.java)
      .returnResult()
    val error = result.responseBody!!

    Assertions.assertThat(result.responseBody).hasSize(1)
    Assertions.assertThat(
      error,
    ).contains(ValidationError("endDate", ErrorMessages.END_DATE_MUST_BE_IN_FUTURE))
  }

  @Test
  fun `Should not return error when curfew conditions start date is in the past`() {
    val order = createOrder()

    webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-conditions")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockRequestBody(
            order.id,
            startDate = mockPastStartDate,
            endDate = mockEndDate,
            "PRIMARY",
          ),
        ),
      )
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isOk()
      .expectBody(CurfewConditions::class.java)
  }

  @Test
  fun `Should return errors when end date is before start date`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-conditions")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockRequestBody(
            order.id,
            ZonedDateTime.now().plusDays(3),
            ZonedDateTime.now().plusDays(2),
            "PRIMARY",
          ),
        ),
      )
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBodyList(ValidationError::class.java)
      .returnResult()
    val error = result.responseBody!!
    Assertions.assertThat(result.responseBody).hasSize(1)

    Assertions.assertThat(
      error,
    ).contains(ValidationError("endDate", ErrorMessages.END_DATE_MUST_BE_AFTER_START_DATE))
  }

  @Test
  fun `Should save order with updated conditions`() {
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

    // Get updated order
    val updatedOrder = getOrder(order.id)

    Assertions.assertThat(updatedOrder.curfewConditions?.startDate).isEqualTo(mockStartDate)
    Assertions.assertThat(updatedOrder.curfewConditions?.endDate).isEqualTo(mockEndDate)
    Assertions.assertThat(updatedOrder.curfewConditions?.curfewAddress).isEqualTo("PRIMARY,SECONDARY")
  }

  @Test
  fun `Curfew additional details can be updated with null`() {
    val order = createOrder()
    addCurfewDataToOrder(order.id)

    webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-additional-details")
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

  @Test
  fun `Curfew additional details can be updated with an empty string`() {
    val order = createOrder()
    addCurfewDataToOrder(order.id)

    webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-additional-details")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "curfewAdditionalDetails": ""
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isOk
  }

  @Test
  fun `Curfew additional details can be updated with a string`() {
    val order = createOrder()
    addCurfewDataToOrder(order.id)

    webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-additional-details")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "curfewAdditionalDetails": "some curfew details"
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isOk
  }

  @Test
  fun `Curfew additional details cannot be updated if a curfew does not exist`() {
    val order = createOrder()

    webTestClient.put()
      .uri("/api/orders/${order.id}/monitoring-conditions-curfew-additional-details")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "curfewAdditionalDetails": "some curfew details"
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus().isNotFound()
      .expectBody().jsonPath("$.developerMessage").isEqualTo("Curfew conditions for ${order.id} not found")
  }

  fun addCurfewDataToOrder(orderId: UUID) {
    webTestClient.put()
      .uri("/api/orders/$orderId/monitoring-conditions-curfew-conditions")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          mockRequestBody(orderId, mockStartDate, mockEndDate, "PRIMARY,SECONDARY"),
        ),
      )
      .headers(setAuthorisation())
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
