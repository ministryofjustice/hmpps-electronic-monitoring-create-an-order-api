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
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
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
  fun `Address details for a submitted order are not update-able`() {
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

  @ParameterizedTest(name = "Address details can be updated for {0} order type")
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
    Assertions.assertThat(address.noFixedAbode).isEqualTo(false)
    Assertions.assertThat(address.installationAddress).isEqualTo(false)
    Assertions.assertThat(address.address.addressLine1).isEqualTo(mockAddressLine1)
    Assertions.assertThat(address.address.addressLine2).isEqualTo(mockAddressLine2)
    Assertions.assertThat(address.address.addressLine3).isEqualTo(mockAddressLine3)
    Assertions.assertThat(address.address.addressLine4).isEqualTo(mockAddressLine4)
    Assertions.assertThat(address.address.postcode).isEqualTo(mockPostcode)
    Assertions.assertThat(address.addressUsage).isEqualTo(DeviceWearerAddressUsage.NA)
  }

  @Test
  fun `Primary address details are mandatory when noFixedAbode is false`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/address")
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
  fun `Primary address details are optional when noFixedAbode is true`() {
    val order = createOrder()

    webTestClient.put()
      .uri("/api/orders/${order.id}/address")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "addressType": "PRIMARY",
              "noFixedAbode": true,
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
  fun `Primary address details should not be saved when noFixedAbode is true`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/address")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "addressType": "PRIMARY",
              "noFixedAbode": true,
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
    Assertions.assertThat(address.noFixedAbode).isEqualTo(true)
    Assertions.assertThat(address.installationAddress).isEqualTo(false)
    Assertions.assertThat(address.address.addressLine1).isEqualTo("")
    Assertions.assertThat(address.address.addressLine2).isEqualTo("")
    Assertions.assertThat(address.address.addressLine3).isEqualTo("")
    Assertions.assertThat(address.address.addressLine4).isEqualTo("")
    Assertions.assertThat(address.address.postcode).isEqualTo("")
    Assertions.assertThat(address.addressUsage).isEqualTo(DeviceWearerAddressUsage.NA)
  }

  @Test
  fun `Secondary address details are mandatory`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/address")
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
  fun `Tertiary address details are mandatory`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/address")
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
  fun `noFixedAbode cannot be set to true for a secondary address`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/address")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "addressType": "SECONDARY",
              "noFixedAbode": true,
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
    Assertions.assertThat(result.responseBody).hasSize(1)
    Assertions.assertThat(result.responseBody!!).contains(
      ValidationError("noFixedAbode", "noFixedAbode can only be true for a primary address"),
    )
  }

  @Test
  fun `noFixedAbode cannot be set to true for a tertiary address`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/address")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "addressType": "TERTIARY",
              "noFixedAbode": true,
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
    Assertions.assertThat(result.responseBody).hasSize(1)
    Assertions.assertThat(result.responseBody!!).contains(
      ValidationError("noFixedAbode", "noFixedAbode can only be true for a primary address"),
    )
  }

  @Test
  fun `A primary address can be an installation address`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/address")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "addressType": "PRIMARY",
              "installationAddress": true,
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
    Assertions.assertThat(address.noFixedAbode).isEqualTo(false)
    Assertions.assertThat(address.installationAddress).isEqualTo(true)
    Assertions.assertThat(address.address.addressLine1).isEqualTo(mockAddressLine1)
    Assertions.assertThat(address.address.addressLine2).isEqualTo(mockAddressLine2)
    Assertions.assertThat(address.address.addressLine3).isEqualTo(mockAddressLine3)
    Assertions.assertThat(address.address.addressLine4).isEqualTo(mockAddressLine4)
    Assertions.assertThat(address.address.postcode).isEqualTo(mockPostcode)
    Assertions.assertThat(address.addressUsage).isEqualTo(DeviceWearerAddressUsage.NA)
  }

  @Test
  fun `A secondary address cannot be an installation address`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/address")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "addressType": "SECONDARY",
              "installationAddress": true,
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
      .isBadRequest
      .expectBodyList(ValidationError::class.java)
      .returnResult()

    Assertions.assertThat(result.responseBody).isNotNull
    Assertions.assertThat(result.responseBody).hasSize(1)
    Assertions.assertThat(result.responseBody!!).contains(
      ValidationError("installationAddress", "installationAddress can only be true for a primary address"),
    )
  }

  @Test
  fun `A tertiary address cannot be an installation address`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/address")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "addressType": "TERTIARY",
              "installationAddress": true,
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
      .isBadRequest
      .expectBodyList(ValidationError::class.java)
      .returnResult()

    Assertions.assertThat(result.responseBody).isNotNull
    Assertions.assertThat(result.responseBody).hasSize(1)
    Assertions.assertThat(result.responseBody!!).contains(
      ValidationError("installationAddress", "installationAddress can only be true for a primary address"),
    )
  }

  @Test
  fun `A primary address cannot be an installation address and noFixedAbode`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/address")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "addressType": "PRIMARY",
              "noFixedAbode": true,
              "installationAddress": true,
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
    Assertions.assertThat(result.responseBody).hasSize(1)
    Assertions.assertThat(result.responseBody!!).contains(
      ValidationError("installationAddress", "installationAddress can only be true for a primary address"),
    )
  }

  @Test
  fun `A installation address cannot be added if the primary address is an installation address`() {
    val order = createOrder()

    // Create a primary address that is also the installation address
    webTestClient.put()
      .uri("/api/orders/${order.id}/address")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "addressType": "PRIMARY",
              "installationAddress": true,
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

    // Try to create a installation address
    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/address")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "addressType": "INSTALLATION",
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
      .isBadRequest
      .expectBodyList(ErrorResponse::class.java)
      .returnResult()

    val error = result.responseBody!!.first()

    Assertions.assertThat(error.userMessage).isEqualTo(
      "Validation failure: An installation address already exists for Order: ${order.id}",
    )
  }
}
