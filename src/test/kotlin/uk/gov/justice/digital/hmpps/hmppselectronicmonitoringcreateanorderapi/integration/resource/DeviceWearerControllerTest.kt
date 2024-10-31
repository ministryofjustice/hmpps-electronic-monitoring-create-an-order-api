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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.DeviceWearerRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidationError
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class DeviceWearerControllerTest : IntegrationTestBase() {
  @Autowired
  lateinit var deviceWearerRepo: DeviceWearerRepository

  @Autowired
  lateinit var orderRepo: OrderRepository

  private val mockNomisId: String = "mockNomisId"
  private val mockPncId: String = "mockPncId"
  private val mockDeliusId: String = "mockDeliusId"
  private val mockPrisonNumber: String = "mockPrisonerNumber"
  private val mockFirstName: String = "mockFirstName"
  private val mockLastName: String = "mockLastName"
  private val mockAlias: String = "mockAlias"
  private val mockSex: String = "mockSex"
  private val mockGender: String = "mockGender"
  private val mockDisabilities: String = "mockDisabilities"
  private val mockDateOfBirth: ZonedDateTime = ZonedDateTime.of(
    LocalDate.of(1970, 1, 1),
    LocalTime.NOON,
    ZoneId.of("UTC"),
  )

  @BeforeEach
  fun setup() {
    deviceWearerRepo.deleteAll()
    orderRepo.deleteAll()
  }

  @Test
  fun `Update device wearer`() {
    val order = createOrder()
    val updateDeviceWearer = webTestClient.put()
      .uri("/api/orders/${order.id}/device-wearer")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "nomisId": "$mockNomisId",
              "pncId": "$mockPncId",
              "deliusId": "$mockDeliusId",
              "prisonNumber": "$mockPrisonNumber",
              "firstName": "$mockFirstName",
              "lastName": "$mockLastName",
              "alias": "$mockAlias",
              "adultAtTimeOfInstallation": "false",
              "sex": "$mockSex",
              "gender": "$mockGender",
              "dateOfBirth": "$mockDateOfBirth",
              "disabilities": "$mockDisabilities"
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(DeviceWearer::class.java)
      .returnResult()

    Assertions.assertThat(updateDeviceWearer.responseBody?.orderId).isEqualTo(order.id)
    Assertions.assertThat(updateDeviceWearer.responseBody?.nomisId).isEqualTo(mockNomisId)
    Assertions.assertThat(updateDeviceWearer.responseBody?.pncId).isEqualTo(mockPncId)
    Assertions.assertThat(updateDeviceWearer.responseBody?.deliusId).isEqualTo(mockDeliusId)
    Assertions.assertThat(updateDeviceWearer.responseBody?.prisonNumber).isEqualTo(mockPrisonNumber)
    Assertions.assertThat(updateDeviceWearer.responseBody?.firstName).isEqualTo(mockFirstName)
    Assertions.assertThat(updateDeviceWearer.responseBody?.lastName).isEqualTo(mockLastName)
    Assertions.assertThat(updateDeviceWearer.responseBody?.alias).isEqualTo(mockAlias)
    Assertions.assertThat(updateDeviceWearer.responseBody?.adultAtTimeOfInstallation).isEqualTo(false)
    Assertions.assertThat(updateDeviceWearer.responseBody?.sex).isEqualTo(mockSex)
    Assertions.assertThat(updateDeviceWearer.responseBody?.gender).isEqualTo(mockGender)
    Assertions.assertThat(updateDeviceWearer.responseBody?.dateOfBirth).isEqualTo(mockDateOfBirth)
    Assertions.assertThat(updateDeviceWearer.responseBody?.disabilities).isEqualTo(mockDisabilities)
  }

  @Test
  fun `Update device wearer returns 404 status if a device wearer can't be found`() {
    webTestClient.put()
      .uri("/api/orders/${UUID.randomUUID()}/device-wearer")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "firstName": "$mockFirstName",
              "lastName": "$mockLastName",
              "alias": "$mockAlias",
              "adultAtTimeOfInstallation": "false",
              "sex": "$mockSex",
              "gender": "$mockGender",
              "dateOfBirth": "$mockDateOfBirth"
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isNotFound()
  }

  @Test
  fun `Update device wearer returns 404 status if the order belongs to another user`() {
    val order = createOrder()
    webTestClient.put()
      .uri("/api/orders/${order.id}/device-wearer")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "firstName": "$mockFirstName",
              "lastName": "$mockLastName",
              "alias": "$mockAlias",
              "adultAtTimeOfInstallation": "false",
              "sex": "$mockSex",
              "gender": "$mockGender",
              "dateOfBirth": "$mockDateOfBirth"
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM_2"))
      .exchange()
      .expectStatus()
      .isNotFound()
  }

  @Test
  fun `Update device wearer returns 400 if invalid data`() {
    val order = createOrder()
    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/device-wearer")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "firstName": "",
              "lastName": "",
              "alias": "",
              "adultAtTimeOfInstallation": "",
              "sex": "",
              "gender": "",
              "dateOfBirth": ""
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM_2"))
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBodyList(ValidationError::class.java)
      .returnResult()

    Assertions.assertThat(result.responseBody).isNotNull
    Assertions.assertThat(result.responseBody).hasSize(6)
    Assertions.assertThat(result.responseBody!!).contains(
      ValidationError("firstName", "First name is required"),
    )
    Assertions.assertThat(result.responseBody!!).contains(
      ValidationError("lastName", "Last name is required"),
    )
    Assertions.assertThat(result.responseBody!!).contains(
      ValidationError(
        "adultAtTimeOfInstallation",
        "You must indicate whether the device wearer will be an adult at installation",
      ),
    )
    Assertions.assertThat(result.responseBody!!).contains(
      ValidationError("sex", "Sex is required"),
    )
    Assertions.assertThat(result.responseBody!!).contains(
      ValidationError("gender", "Gender is required"),
    )
    Assertions.assertThat(result.responseBody!!).contains(
      ValidationError("dateOfBirth", "Date of birth is required"),
    )
  }

  @Test
  fun `Update device wearer returns 400 if dateOfBirth is in the future`() {
    val order = createOrder()
    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/device-wearer")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "firstName": "$mockFirstName",
              "lastName": "$mockLastName",
              "alias": "$mockAlias",
              "adultAtTimeOfInstallation": "false",
              "sex": "$mockSex",
              "gender": "$mockGender",
              "dateOfBirth": "${ZonedDateTime.parse("2222-01-01T00:00:00.000Z")}"
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM_2"))
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBodyList(ValidationError::class.java)
      .returnResult()

    Assertions.assertThat(result.responseBody).isNotNull
    Assertions.assertThat(result.responseBody).hasSize(1)
    Assertions.assertThat(result.responseBody!!).contains(
      ValidationError("dateOfBirth", "Date of birth must be in the past"),
    )
  }

  @Test
  fun `NoFixedAbode cannot be updated if the order doesn't exist`() {
    webTestClient.put()
      .uri("/api/orders/${UUID.randomUUID()}/device-wearer/no-fixed-abode")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "noFixedAbode": true
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isNotFound()
  }

  @Test
  fun `NoFixedAbode cannot be updated if the order is submitted`() {
    val order = createOrder()

    order.status = OrderStatus.SUBMITTED
    orderRepo.save(order)

    webTestClient.put()
      .uri("/api/orders/${order.id}/device-wearer/no-fixed-abode")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "noFixedAbode": true
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isNotFound()
  }

  @Test
  fun `NoFixedAbode cannot be updated if the order belongs to another user`() {
    val order = createOrder()
    webTestClient.put()
      .uri("/api/orders/${order.id}/device-wearer/no-fixed-abode")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "noFixedAbode": true
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM_2"))
      .exchange()
      .expectStatus()
      .isNotFound()
  }

  @ParameterizedTest(name = "NoFixedAbode can be updated with valid string - {0}")
  @ValueSource(strings = ["true", "false"])
  fun `NoFixedAbode can be updated with string values`(value: String) {
    val order = createOrder()
    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/device-wearer/no-fixed-abode")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "noFixedAbode": "$value"
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(DeviceWearer::class.java)
      .returnResult()

    val deviceWearer = result.responseBody!!
    Assertions.assertThat(deviceWearer.noFixedAbode).isEqualTo(value.toBoolean())
  }

  @ParameterizedTest(name = "NoFixedAbode can be updated with valid boolean - {0}")
  @ValueSource(booleans = [true, false])
  fun `NoFixedAbode can be updated with boolean values`(value: Boolean) {
    val order = createOrder()
    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/device-wearer/no-fixed-abode")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "noFixedAbode": $value
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(DeviceWearer::class.java)
      .returnResult()

    val deviceWearer = result.responseBody!!
    Assertions.assertThat(deviceWearer.noFixedAbode).isEqualTo(value)
  }

  @Test
  fun `NoFixedAbode is mandatory`() {
    val order = createOrder()
    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/device-wearer/no-fixed-abode")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "noFixedAbode": null
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
      ValidationError("noFixedAbode", "You must indicate whether the device wearer has a fixed abode"),
    )
  }

  @Test
  fun `isValid is false when mandatory fields are not populated`() {
    val order = createOrder()

    Assertions.assertThat(order.deviceWearer?.isValid).isFalse()
  }

  @Test
  fun `isValid is true when mandatory deviceWearer & noFixedAbode fields are populated`() {
    val order = createOrder()
    webTestClient.put()
      .uri("/api/orders/${order.id}/device-wearer")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "firstName": "$mockFirstName",
              "lastName": "$mockLastName",
              "adultAtTimeOfInstallation": "false",
              "sex": "$mockSex",
              "gender": "$mockGender",
              "dateOfBirth": "$mockDateOfBirth"
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isOk

    val updatedOrder = webTestClient.put()
      .uri("/api/orders/${order.id}/device-wearer/no-fixed-abode")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "noFixedAbode": true
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(DeviceWearer::class.java)
      .returnResult()

    Assertions.assertThat(updatedOrder.responseBody?.isValid).isTrue()
  }
}
