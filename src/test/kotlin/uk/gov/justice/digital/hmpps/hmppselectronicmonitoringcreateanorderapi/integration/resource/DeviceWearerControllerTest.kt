package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidationError
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class DeviceWearerControllerTest : IntegrationTestBase() {

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
  private val mockLanguage: String = "mockLanguage"
  private val mockHomeOfficeReferenceNumber: String = "mockHomeOfficeReferenceNumber"
  private val mockDateOfBirth: ZonedDateTime = ZonedDateTime.of(
    LocalDate.of(1970, 1, 1),
    LocalTime.NOON,
    ZoneId.of("UTC"),
  )

  private object ErrorMessages {
    const val FIRST_NAME_REQUIRED: String = "Enter device wearer's first name"
    const val LAST_NAME_REQUIRED: String = "Enter device wearer's last name"
    const val IS_ADULT_REQUIRED: String = "Select yes if a responsible adult is required"
    const val SEX_REQUIRED: String = "Select the device wearer's sex, or select 'Not able to provide this information'"
    const val GENDER_REQUIRED: String =
      "Select the device wearer's gender, or select 'Not able to provide this information'"
    const val DOB_REQUIRED: String = "Enter date of birth"
    const val DOB_MUST_BE_IN_PAST: String = "Date of birth must be in the past"
    const val INTERPRETER_REQUIRED: String = "Select yes if the device wearer requires an interpreter"
    const val LANGUAGE_REQUIRED: String = "Select the language required"
    const val NO_FIXED_ABODE_REQUIRED: String = "Select yes if the device wearer has a fixed address"
  }

  @BeforeEach
  fun setup() {
    repo.deleteAll()
  }

  @Nested
  @DisplayName("PUT /api/orders/{orderId}/device-wearer")
  inner class UpdateDeviceWearer {
    @Test
    fun `it should update the device wearer's personal details`() {
      val order = createOrder()
      val updateDeviceWearer = webTestClient.put()
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
              "dateOfBirth": "$mockDateOfBirth",
              "disabilities": "$mockDisabilities",
              "interpreterRequired": true,
              "language": "$mockLanguage"
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

      Assertions.assertThat(updateDeviceWearer.responseBody?.firstName).isEqualTo(mockFirstName)
      Assertions.assertThat(updateDeviceWearer.responseBody?.lastName).isEqualTo(mockLastName)
      Assertions.assertThat(updateDeviceWearer.responseBody?.alias).isEqualTo(mockAlias)
      Assertions.assertThat(
        updateDeviceWearer.responseBody?.adultAtTimeOfInstallation,
      ).isEqualTo(false)
      Assertions.assertThat(updateDeviceWearer.responseBody?.sex).isEqualTo(mockSex)
      Assertions.assertThat(updateDeviceWearer.responseBody?.gender).isEqualTo(mockGender)
      Assertions.assertThat(updateDeviceWearer.responseBody?.dateOfBirth).isEqualTo(mockDateOfBirth)
      Assertions.assertThat(updateDeviceWearer.responseBody?.disabilities).isEqualTo(mockDisabilities)
      Assertions.assertThat(updateDeviceWearer.responseBody?.interpreterRequired).isEqualTo(true)
      Assertions.assertThat(updateDeviceWearer.responseBody?.language).isEqualTo(mockLanguage)
    }

    @Test
    fun `it should return not found if the order does not exist`() {
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
              "dateOfBirth": "$mockDateOfBirth",
              "interpreterRequired": true,
              "language": "$mockLanguage"
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
    fun `it should return an error if the order was not created by the user`() {
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
              "dateOfBirth": "$mockDateOfBirth",
              "interpreterRequired": true,
              "language": "$mockLanguage"
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
    fun `it should return an error if an invalid data is submitted`() {
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
              "dateOfBirth": "",
              "interpreterRequired": ""
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
      Assertions.assertThat(result.responseBody).hasSize(7)
      Assertions.assertThat(result.responseBody!!).contains(
        ValidationError("firstName", ErrorMessages.FIRST_NAME_REQUIRED),
      )
      Assertions.assertThat(result.responseBody!!).contains(
        ValidationError("lastName", ErrorMessages.LAST_NAME_REQUIRED),
      )
      Assertions.assertThat(result.responseBody!!).contains(
        ValidationError(
          "adultAtTimeOfInstallation",
          ErrorMessages.IS_ADULT_REQUIRED,
        ),
      )
      Assertions.assertThat(result.responseBody!!).contains(
        ValidationError("sex", ErrorMessages.SEX_REQUIRED),
      )
      Assertions.assertThat(result.responseBody!!).contains(
        ValidationError("gender", ErrorMessages.GENDER_REQUIRED),
      )
      Assertions.assertThat(result.responseBody!!).contains(
        ValidationError("dateOfBirth", ErrorMessages.DOB_REQUIRED),
      )

      Assertions.assertThat(result.responseBody!!).contains(
        ValidationError(
          "interpreterRequired",
          ErrorMessages.INTERPRETER_REQUIRED,
        ),
      )
    }

    @Test
    fun `it should return an error if dateOfBirth is in the future`() {
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
              "dateOfBirth": "${ZonedDateTime.parse("2222-01-01T00:00:00.000Z")}",
              "interpreterRequired": true,
              "language": "$mockLanguage"
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
        ValidationError("dateOfBirth", ErrorMessages.DOB_MUST_BE_IN_PAST),
      )
    }

    @Test
    fun `it should return an error if language is not set when interpreterRequired is true`() {
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
              "dateOfBirth": "$mockDateOfBirth",
              "interpreterRequired": true,
              "language": ""
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
        ValidationError("language", ErrorMessages.LANGUAGE_REQUIRED),
      )
    }
  }

  @Nested
  @DisplayName("PUT /api/orders/{orderId}/device-wearer/no-fixed-abode")
  inner class UpdateNoFixedAbode {
    @Test
    fun `it should return not found if the order does not exist`() {
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
    fun `it should return an error if the order is in a submitted state`() {
      val order = createSubmittedOrder()

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
    fun `it should return an error if the order was not created by the user`() {
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

    @ParameterizedTest(name = "it should update noFixedAbode with valid string - {0}")
    @ValueSource(strings = ["true", "false"])
    fun `it should update noFixedAbode with string values`(value: String) {
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

    @ParameterizedTest(name = "it should update noFixedAbode with valid boolean - {0}")
    @ValueSource(booleans = [true, false])
    fun `it should update noFixedAbode with boolean values`(value: Boolean) {
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
    fun `it should return an error if an invalid data is submitted`() {
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
        ValidationError(
          "noFixedAbode",
          ErrorMessages.NO_FIXED_ABODE_REQUIRED,
        ),
      )
    }
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
              "dateOfBirth": "$mockDateOfBirth",
              "interpreterRequired": false
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

  @Nested
  @DisplayName("PUT /api/orders/{orderId}/device-wearer/identity-numbers")
  inner class UpdateIdentityNumbers {
    @Test
    fun `it should return not found if the order does not exist`() {
      webTestClient.put()
        .uri("/api/orders/${UUID.randomUUID()}/device-wearer/identity-numbers")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
            {
              "nomisId": "$mockNomisId",
              "pncId": "$mockPncId",
              "deliusId": "$mockDeliusId",
              "prisonNumber": "$mockPrisonNumber",
              "homeOfficeReferenceNumber": "$mockHomeOfficeReferenceNumber"
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
    fun `it should return an error if the order is in a submitted state`() {
      val order = createSubmittedOrder()

      webTestClient.put()
        .uri("/api/orders/${order.id}/device-wearer/identity-numbers")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
            {
              "nomisId": "$mockNomisId",
              "pncId": "$mockPncId",
              "deliusId": "$mockDeliusId",
              "prisonNumber": "$mockPrisonNumber",
              "homeOfficeReferenceNumber": "$mockHomeOfficeReferenceNumber"
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
    fun `it should return an error if the order was not created by the user`() {
      val order = createOrder()
      webTestClient.put()
        .uri("/api/orders/${order.id}/device-wearer/identity-numbers")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
            {
              "nomisId": "$mockNomisId",
              "pncId": "$mockPncId",
              "deliusId": "$mockDeliusId",
              "prisonNumber": "$mockPrisonNumber",
              "homeOfficeReferenceNumber": "$mockHomeOfficeReferenceNumber"
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
    fun `it should update the identity numbers`() {
      val order = createOrder()
      val result = webTestClient.put()
        .uri("/api/orders/${order.id}/device-wearer/identity-numbers")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
            {
              "nomisId": "$mockNomisId",
              "pncId": "$mockPncId",
              "deliusId": "$mockDeliusId",
              "prisonNumber": "$mockPrisonNumber",
              "homeOfficeReferenceNumber": "$mockHomeOfficeReferenceNumber"
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
      Assertions.assertThat(deviceWearer.nomisId).isEqualTo(mockNomisId)
      Assertions.assertThat(deviceWearer.deliusId).isEqualTo(mockDeliusId)
      Assertions.assertThat(deviceWearer.pncId).isEqualTo(mockPncId)
      Assertions.assertThat(deviceWearer.prisonNumber).isEqualTo(mockPrisonNumber)
      Assertions.assertThat(deviceWearer.homeOfficeReferenceNumber).isEqualTo(mockHomeOfficeReferenceNumber)
    }

    @Test
    fun `it should retain the identity numbers when updating the device wearer's personal details`() {
      // Set up a new order
      val order = createOrder()

      // Save some identity numbers
      webTestClient.put()
        .uri("/api/orders/${order.id}/device-wearer/identity-numbers")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
            {
              "nomisId": "$mockNomisId",
              "pncId": "$mockPncId",
              "deliusId": "$mockDeliusId",
              "prisonNumber": "$mockPrisonNumber",
              "homeOfficeReferenceNumber": "$mockHomeOfficeReferenceNumber"
            }
            """.trimIndent(),
          ),
        )
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isOk

      // Update the device wearer's personal details
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
              "dateOfBirth": "$mockDateOfBirth",
              "disabilities": "$mockDisabilities",
              "interpreterRequired": true,
              "language": "$mockLanguage"
            }
            """.trimIndent(),
          ),
        )
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isOk

      // Ensure that the identity numbers have not been overwritten
      val updatedOrder = getOrder(order.id)

      Assertions.assertThat(updatedOrder.deviceWearer!!.nomisId).isEqualTo(mockNomisId)
      Assertions.assertThat(updatedOrder.deviceWearer!!.deliusId).isEqualTo(mockDeliusId)
      Assertions.assertThat(updatedOrder.deviceWearer!!.pncId).isEqualTo(mockPncId)
      Assertions.assertThat(updatedOrder.deviceWearer!!.prisonNumber).isEqualTo(mockPrisonNumber)
      Assertions.assertThat(
        updatedOrder.deviceWearer!!.homeOfficeReferenceNumber,
      ).isEqualTo(mockHomeOfficeReferenceNumber)
    }
  }
}
