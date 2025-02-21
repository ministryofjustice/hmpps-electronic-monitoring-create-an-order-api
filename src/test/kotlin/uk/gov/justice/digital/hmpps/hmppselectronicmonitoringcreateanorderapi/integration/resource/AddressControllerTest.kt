package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DeviceWearerAddressUsage
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidationError
import java.util.*

class AddressControllerTest : IntegrationTestBase() {

  private val mockAddressLine1: String = "mockAddressLine1"
  private val mockAddressLine2: String = "mockAddressLine2"
  private val mockAddressLine3: String = "mockAddressLine3"
  private val mockAddressLine4: String = "mockAddressLine4"
  private val mockPostcode: String = "mockPostcode"

  private object ErrorMessages {
    const val ADDRESS_1_REQUIRED: String = "Enter address line 1, typically the building and street"
    const val ADDRESS_3_REQUIRED: String = "Enter town or city"
    const val POSTCODE_REQUIRED: String = "Enter postcode"
  }

  @BeforeEach
  fun setup() {
    repo.deleteAll()
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
  fun `Address details for a non-existent order are not update-able`() {
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
  fun `Address details for a submitted order are not update-able`() {
    val order = createSubmittedOrder()

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
      .expectBody(Address::class.java)
      .returnResult()

    val address = result.responseBody!!

    Assertions.assertThat(address.addressType).isEqualTo(AddressType.valueOf(addressType))
    Assertions.assertThat(address.addressLine1).isEqualTo(mockAddressLine1)
    Assertions.assertThat(address.addressLine2).isEqualTo(mockAddressLine2)
    Assertions.assertThat(address.addressLine3).isEqualTo(mockAddressLine3)
    Assertions.assertThat(address.addressLine4).isEqualTo(mockAddressLine4)
    Assertions.assertThat(address.postcode).isEqualTo(mockPostcode)
    Assertions.assertThat(address.addressUsage).isEqualTo(DeviceWearerAddressUsage.NA)
  }

  @ParameterizedTest(name = "Address details are mandatory for {0} address type")
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
      ValidationError("addressLine1", ErrorMessages.ADDRESS_1_REQUIRED),
    )
    Assertions.assertThat(result.responseBody!!).contains(
      ValidationError("addressLine3", ErrorMessages.ADDRESS_3_REQUIRED),
    )
    Assertions.assertThat(result.responseBody!!).contains(
      ValidationError("postcode", ErrorMessages.POSTCODE_REQUIRED),
    )
  }

  @Test
  fun `Address details can be updated multiple times`() {
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
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isOk

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
      .isOk
  }
}
