package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearerAddress
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DeviceWearerAddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DeviceWearerAddressUsage
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.DeviceWearerAddressRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidationError
import java.util.*

class DeviceWearerAddressControllerTest : IntegrationTestBase() {
  @Autowired
  lateinit var deviceWearerAddressRepo: DeviceWearerAddressRepository

  @Autowired
  lateinit var orderRepo: OrderRepository

  private val mockAddressLine1: String = "mockAddressLine1"
  private val mockAddressLine2: String = "mockAddressLine2"
  private val mockAddressLine3: String = "mockAddressLine3"
  private val mockAddressLine4: String = "mockAddressLine4"
  private val mockPostcode: String = "mockPostcode"

  @BeforeEach
  fun setup() {
    deviceWearerAddressRepo.deleteAll()
    orderRepo.deleteAll()
  }

  @Test
  fun `Address details for an order created by a different user are not update-able`() {
    val order = createOrder()

    webTestClient.put()
      .uri("/api/orders/${order.id}/address")
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
    webTestClient.put()
      .uri("/api/orders/${UUID.randomUUID()}/address")
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

    order.status = OrderStatus.SUBMITTED
    orderRepo.save(order)

    webTestClient.put()
      .uri("/api/orders/${order.id}/address")
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

  @ParameterizedTest(name = "Address details can be updated for {0} address type")
  @ValueSource(strings = ["PRIMARY", "SECONDARY", "TERTIARY", "INSTALLATION"])
  fun `Address details can be updated`(addressType: String) {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/address")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "addressType": "$addressType",
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

    Assertions.assertThat(address.addressType).isEqualTo(DeviceWearerAddressType.valueOf(addressType))
    Assertions.assertThat(address.address?.addressLine1).isEqualTo(mockAddressLine1)
    Assertions.assertThat(address.address?.addressLine2).isEqualTo(mockAddressLine2)
    Assertions.assertThat(address.address?.addressLine3).isEqualTo(mockAddressLine3)
    Assertions.assertThat(address.address?.addressLine4).isEqualTo(mockAddressLine4)
    Assertions.assertThat(address.address?.postcode).isEqualTo(mockPostcode)
    Assertions.assertThat(address.addressUsage).isEqualTo(DeviceWearerAddressUsage.NA)
  }

  @Test
  fun `Address details can be updated for NO_FIXED_ABODE address type`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/address")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "addressType": "NO_FIXED_ABODE",
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

    Assertions.assertThat(address.addressType).isEqualTo(DeviceWearerAddressType.NO_FIXED_ABODE)
    Assertions.assertThat(address.address).isEqualTo(null)
    Assertions.assertThat(address.addressUsage).isEqualTo(DeviceWearerAddressUsage.NA)
  }

  @ParameterizedTest(name = "Address details can be updated for {0} address type")
  @ValueSource(strings = ["PRIMARY", "SECONDARY", "TERTIARY", "INSTALLATION"])
  fun `Address details are mandatory`(addressType: String) {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/address")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "addressType": "$addressType",
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
}
