package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearerAddress
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DeviceWearerAddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DeviceWearerAddressUsage
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FormStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.DeviceWearerAddressRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderFormRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidationError
import java.util.*

class DeviceWearerAddressControllerTest : IntegrationTestBase() {
  @Autowired
  lateinit var deviceWearerAddressRepo: DeviceWearerAddressRepository

  @Autowired
  lateinit var orderFormRepo: OrderFormRepository

  private val mockAddressLine1: String = "mockAddressLine1"
  private val mockAddressLine2: String = "mockAddressLine2"
  private val mockAddressLine3: String = "mockAddressLine3"
  private val mockAddressLine4: String = "mockAddressLine4"
  private val mockPostcode: String = "mockPostcode"

  @BeforeEach
  fun setup() {
    deviceWearerAddressRepo.deleteAll()
    orderFormRepo.deleteAll()
  }

  @Test
  fun `Address details for an order created by a different user are not update-able`() {
    val order = createOrder()

    webTestClient.post()
      .uri("/api/order/${order.id}/address")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "addressType": "PRIMARY",
              "addressLine1": "$mockAddressLine1",
              "addressLine2": "$mockAddressLine2",
              "addressLine3": "$mockAddressLine3",
              "addressLine4": "$mockAddressLine4",
              "postcode": "$mockPostcode"
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
  fun `Address details for an non-existent order are not update-able`() {
    webTestClient.post()
      .uri("/api/order/${UUID.randomUUID()}/address")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "addressType": "PRIMARY",
              "addressLine1": "$mockAddressLine1",
              "addressLine2": "$mockAddressLine2",
              "addressLine3": "$mockAddressLine3",
              "addressLine4": "$mockAddressLine4",
              "postcode": "$mockPostcode"
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isNotFound
  }

  @Test
  fun `Address details for an submitted order are not update-able`() {
    val order = createOrder()

    order.status = FormStatus.SUBMITTED
    orderFormRepo.save(order)

    webTestClient.post()
      .uri("/api/order/${order.id}/address")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "addressType": "PRIMARY",
              "addressLine1": "$mockAddressLine1",
              "addressLine2": "$mockAddressLine2",
              "addressLine3": "$mockAddressLine3",
              "addressLine4": "$mockAddressLine4",
              "postcode": "$mockPostcode"
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isNotFound
  }

  @Test
  fun `Address details can be updated`() {
    val order = createOrder()

    val result = webTestClient.post()
      .uri("/api/order/${order.id}/address")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "addressType": "PRIMARY",
              "addressLine1": "$mockAddressLine1",
              "addressLine2": "$mockAddressLine2",
              "addressLine3": "$mockAddressLine3",
              "addressLine4": "$mockAddressLine4",
              "postcode": "$mockPostcode"
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(DeviceWearerAddress::class.java)
      .returnResult()

    val address = result.responseBody!!

    Assertions.assertThat(address.addressType).isEqualTo(DeviceWearerAddressType.PRIMARY)
    Assertions.assertThat(address.address.addressLine1).isEqualTo(mockAddressLine1)
    Assertions.assertThat(address.address.addressLine2).isEqualTo(mockAddressLine2)
    Assertions.assertThat(address.address.addressLine3).isEqualTo(mockAddressLine3)
    Assertions.assertThat(address.address.addressLine4).isEqualTo(mockAddressLine4)
    Assertions.assertThat(address.address.postcode).isEqualTo(mockPostcode)
    Assertions.assertThat(address.addressUsage).isEqualTo(DeviceWearerAddressUsage.NA)
  }

  @Test
  fun `Primary address details are mandatory`() {
    val order = createOrder()

    val result = webTestClient.post()
      .uri("/api/order/${order.id}/address")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "addressType": "PRIMARY",
              "addressLine1": "",
              "addressLine2": "",
              "addressLine3": "",
              "addressLine4": "",
              "postcode": ""
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
    Assertions.assertThat(result.responseBody).hasSize(3)
    Assertions.assertThat(result.responseBody!!).contains(
      ValidationError("addressLine1", "Address line 1 is required"),
    )
    Assertions.assertThat(result.responseBody!!).contains(
      ValidationError("addressLine2", "Address line 2 is required"),
    )
    Assertions.assertThat(result.responseBody!!).contains(
      ValidationError("postcode", "Postcode is required"),
    )
  }

  @Test
  fun `Secondary address details are not mandatory`() {
    val order = createOrder()

    val result = webTestClient.post()
      .uri("/api/order/${order.id}/address")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "addressType": "SECONDARY",
              "addressLine1": "",
              "addressLine2": "",
              "addressLine3": "",
              "addressLine4": "",
              "postcode": ""
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
  fun `Tertiary address details are not mandatory`() {
    val order = createOrder()

    val result = webTestClient.post()
      .uri("/api/order/${order.id}/address")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "addressType": "TERTIARY",
              "addressLine1": "",
              "addressLine2": "",
              "addressLine3": "",
              "addressLine4": "",
              "postcode": ""
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isOk
  }
}
