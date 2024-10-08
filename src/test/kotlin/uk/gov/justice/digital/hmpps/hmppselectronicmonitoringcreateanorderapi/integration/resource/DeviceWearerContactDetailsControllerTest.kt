package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearerContactDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FormStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.DeviceWearerContactDetailsRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderFormRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidationError
import java.util.*

class DeviceWearerContactDetailsControllerTest : IntegrationTestBase() {
  @Autowired
  lateinit var contactDetailRepo: DeviceWearerContactDetailsRepository

  @Autowired
  lateinit var orderFormRepo: OrderFormRepository

  @BeforeEach
  fun setup() {
    contactDetailRepo.deleteAll()
    orderFormRepo.deleteAll()
  }

  @Test
  fun `Contact details are created and retrievable after order creation`() {
    val order = createOrder()
    val contactDetails = webTestClient.get()
      .uri("/api/order/${order.id}/contact-details")
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(DeviceWearerContactDetails::class.java)
  }

  @Test
  fun `Contact details created by a different user are not accessible`() {
    val order = createOrder()
    val contactDetails = webTestClient.get()
      .uri("/api/order/${order.id}/contact-details")
      .headers(setAuthorisation("AUTH_ADM_2"))
      .exchange()
      .expectStatus()
      .isNotFound
  }

  @Test
  fun `Contact details for a non-existent order are not accessible`() {
    val contactDetails = webTestClient.get()
      .uri("/api/order/${UUID.randomUUID()}/contact-details")
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isNotFound
  }

  @Test
  fun `Contact details can be updated with a null contactNumber`() {
    val order = createOrder()

    webTestClient.post()
      .uri("/api/order/${order.id}/contact-details")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "contactNumber": null
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
  fun `Contact details can be updated with a valid contact number`() {
    val order = createOrder()

    webTestClient.post()
      .uri("/api/order/${order.id}/contact-details")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "contactNumber": "01234567890"
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
  fun `Contact details cannot be updated with an invalid contact number`() {
    val order = createOrder()
    val result = webTestClient.post()
      .uri("/api/order/${order.id}/contact-details")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "contactNumber": "abc"
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBodyList(ValidationError::class.java)
      .returnResult()

    Assertions.assertThat(result.responseBody).isNotNull
    Assertions.assertThat(result.responseBody).hasSize(1)
    Assertions.assertThat(result.responseBody).first().isNotNull

    val validationError = result.responseBody!!.first()

    Assertions.assertThat(validationError.field).isEqualTo("contactNumber")
    Assertions.assertThat(validationError.error).isEqualTo("Phone number is in an incorrect format")
  }

  @Test
  fun `Contact details cannot be updated by a different user`() {
    val order = createOrder()
    val contactDetails = webTestClient.post()
      .uri("/api/order/${order.id}/contact-details")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "contactNumber": "01234567890"
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM_2"))
      .exchange()
      .expectStatus()
      .isNotFound
  }

  @Test
  fun `Contact details cannot be updated for a submitted order`() {
    val order = createOrder()

    order.status = FormStatus.SUBMITTED
    orderFormRepo.save(order)

    val contactDetails = webTestClient.post()
      .uri("/api/order/${order.id}/contact-details")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "contactNumber": "01234567890"
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isNotFound
  }
}
