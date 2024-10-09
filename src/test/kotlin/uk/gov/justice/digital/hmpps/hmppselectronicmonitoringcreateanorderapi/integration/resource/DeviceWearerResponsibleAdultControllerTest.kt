package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ResponsibleAdult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FormStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.DeviceWearerResponsibleAdultRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderFormRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidationError
import java.util.*

class DeviceWearerResponsibleAdultControllerTest : IntegrationTestBase() {
  @Autowired
  lateinit var responsibleAdultRepo: DeviceWearerResponsibleAdultRepository

  @Autowired
  lateinit var orderFormRepo: OrderFormRepository

  private val mockFullName: String = "mockFullName"
  private val mockRelationship: String = "mockRelationship"
  private val mockContactNumber: String = "mockcontactNumber"

  @BeforeEach
  fun setup() {
    responsibleAdultRepo.deleteAll()
    orderFormRepo.deleteAll()
  }

  @Test
  fun `ResponsibleAdult details for an order created by a different user are not update-able`() {
    val order = createOrder()

    webTestClient.post()
      .uri("/api/order/${order.id}/device-wearer-responsible-adult")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "fullName": "$mockFullName",
              "relationship": "$mockRelationship",
              "contactNumber": "$mockContactNumber"
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
  fun `ResponsibleAdult details for an non-existent order are not update-able`() {
    webTestClient.post()
      .uri("/api/order/${UUID.randomUUID()}/ResponsibleAdult")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "fullName": "$mockFullName",
              "relationship": "$mockRelationship",
              "contactNumber": "$mockContactNumber"
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
  fun `ResponsibleAdult details for a submitted order are not update-able`() {
    val order = createOrder()

    order.status = FormStatus.SUBMITTED
    orderFormRepo.save(order)

    webTestClient.post()
      .uri("/api/order/${order.id}/device-wearer-responsible-adult")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "fullName": "$mockFullName",
              "relationship": "$mockRelationship",
              "contactNumber": "$mockContactNumber"
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
  fun `ResponsibleAdult details can be updated`() {
    val order = createOrder()

    val result = webTestClient.post()
      .uri("/api/order/${order.id}/device-wearer-responsible-adult")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "fullName": "$mockFullName",
              "relationship": "$mockRelationship",
              "contactNumber": "$mockContactNumber"
            }
          """.trimIndent(),
        ),
      )
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(ResponsibleAdult::class.java)
      .returnResult()

    val responsibleAdult = result.responseBody!!

    Assertions.assertThat(responsibleAdult.fullName).isEqualTo(mockFullName)
    Assertions.assertThat(responsibleAdult.relationship).isEqualTo(mockRelationship)
    Assertions.assertThat(responsibleAdult.contactNumber).isEqualTo(mockContactNumber)
  }

  @Test
  fun `Responsible Adult Details are mandatory`() {
    val order = createOrder()

    val result = webTestClient.post()
      .uri("/api/order/${order.id}/device-wearer-responsible-adult")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "fullName": "",
              "relationship": "",
              "contactNumber": ""
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
      ValidationError("fullName", "Full name is required"),
    )
    Assertions.assertThat(result.responseBody!!).contains(
      ValidationError("relationship", "Relationship is required"),
    )
    Assertions.assertThat(result.responseBody!!).contains(
      ValidationError("contactNumber", "Contact number is required"),
    )
  }

  @Nested
  inner class OtherRelationshipDetails {
    @Test
    fun `Other relationship details is not mandatory when a relationship type except 'Other' is selected`() {
      val order = createOrder()

      webTestClient.post()
        .uri("/api/order/${order.id}/device-wearer-responsible-adult")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
            {
              "fullName": "$mockFullName",
              "relationship": "Parent",
              "contactNumber": "$mockContactNumber"
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
    fun `Other relationship details is mandatory when relationship type 'Other' is selected`() {
      val order = createOrder()

      val result = webTestClient.post()
        .uri("/api/order/${order.id}/device-wearer-responsible-adult")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
            {
              "fullName": "$mockFullName",
              "relationship": "other",
              "contactNumber": "$mockContactNumber"
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
        ValidationError("otherRelationshipDetails", "You must provide details of the responsible adult to the device wearer"),
      )
    }
  }
}
