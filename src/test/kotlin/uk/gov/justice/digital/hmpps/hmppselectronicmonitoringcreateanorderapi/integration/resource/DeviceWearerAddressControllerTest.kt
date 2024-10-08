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
  private val mockPostCode: String = "mockPostCode"
  private val mockAddressUsage: String = "WORK"

  @BeforeEach
  fun setup() {
    deviceWearerAddressRepo.deleteAll()
    orderFormRepo.deleteAll()
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
              "postCode": "$mockPostCode"
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
    Assertions.assertThat(address.addressLine1).isEqualTo(mockAddressLine1)
    Assertions.assertThat(address.addressLine2).isEqualTo(mockAddressLine2)
    Assertions.assertThat(address.addressLine3).isEqualTo(mockAddressLine3)
    Assertions.assertThat(address.addressLine4).isEqualTo(mockAddressLine4)
    Assertions.assertThat(address.postcode).isEqualTo(mockPostCode)
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
              "postCode": ""
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
      ValidationError("postCode", "Post code is required"),
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
              "postCode": ""
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
              "postCode": ""
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
