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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.ResponsibleAdultRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.resource.validator.ValidationError
import java.util.*

class ResponsibleAdultControllerTest : IntegrationTestBase() {
  @Autowired
  lateinit var responsibleAdultRepo: ResponsibleAdultRepository

  @Autowired
  lateinit var orderRepo: OrderRepository

  private val mockFullName: String = "mockFullName"
  private val mockRelationship: String = "mockRelationship"
  private val mockContactNumber: String = "01234567890"

  @BeforeEach
  fun setup() {
    responsibleAdultRepo.deleteAll()
    orderRepo.deleteAll()
  }

  @Test
  fun `ResponsibleAdult details for an order created by a different user are not update-able`() {
    val order = createOrder()

    webTestClient.put()
      .uri("/api/orders/${order.id}/device-wearer-responsible-adult")
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
    webTestClient.put()
      .uri("/api/orders/${UUID.randomUUID()}/ResponsibleAdult")
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

    order.status = OrderStatus.SUBMITTED
    orderRepo.save(order)

    webTestClient.put()
      .uri("/api/orders/${order.id}/device-wearer-responsible-adult")
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

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/device-wearer-responsible-adult")
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
  fun `Reponsible adult cannot be updated with an invalid contact number`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/device-wearer-responsible-adult")
      .contentType(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
            {
              "fullName": "$mockFullName",
              "relationship": "$mockRelationship",
              "contactNumber": "mock-invalid-phone-number"
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

    val validationError = result.responseBody!!.first()

    Assertions.assertThat(result.responseBody!!).contains(
      ValidationError("contactNumber", "Phone number is in an incorrect format"),
    )
  }

  @Test
  fun `Responsible Adult Details are mandatory`() {
    val order = createOrder()

    val result = webTestClient.put()
      .uri("/api/orders/${order.id}/device-wearer-responsible-adult")
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
      ValidationError("contactNumber", "Phone number is in an incorrect format"),
    )
  }

  @Nested
  inner class OtherRelationshipDetails {
    @Test
    fun `Other relationship details is not mandatory when a relationship type except 'Other' is selected`() {
      val order = createOrder()

      webTestClient.put()
        .uri("/api/orders/${order.id}/device-wearer-responsible-adult")
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

      val result = webTestClient.put()
        .uri("/api/orders/${order.id}/device-wearer-responsible-adult")
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
        ValidationError(
          "otherRelationshipDetails",
          "You must provide details of the responsible adult to the device wearer",
        ),
      )
    }
  }
}
