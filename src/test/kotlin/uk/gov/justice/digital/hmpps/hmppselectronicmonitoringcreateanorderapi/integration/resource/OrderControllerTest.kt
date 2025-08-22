package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.util.JsonPathExpectationsHelper
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.HmppsDocumentManagementApiExtension.Companion.documentApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.SercoAuthMockServerExtension.Companion.sercoAuthApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.SercoMockApiExtension.Companion.sercoApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AdditionalDocument
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.dto.OrderDto
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DataDictionaryVersion
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DocumentType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.RequestType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.SubmissionStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsAttachmentResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsAttachmentResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsAttachmentSubmissionResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsErrorResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.MonitoringOrder
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.FmsSubmissionResultRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.utilities.TestUtilities
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.DeviceWearer as FmsDeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.ErrorResponse as FmsErrorResponseDetails

class OrderControllerTest : IntegrationTestBase() {

  @Autowired
  lateinit var fmsResultRepository: FmsSubmissionResultRepository
  private val objectMapper: ObjectMapper = jacksonObjectMapper()
  val mockStartDate: ZonedDateTime = ZonedDateTime.now().plusMonths(1)
  val mockEndDate: ZonedDateTime = ZonedDateTime.now().plusMonths(2)

  private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
  val mockStartDateInBritishTime = mockStartDate.toInstant().atZone(
    ZoneId.of("Europe/London"),
  ).format(dateTimeFormatter)
  val mockEndDateInBritishTime = mockEndDate.toInstant().atZone(ZoneId.of("Europe/London")).format(dateTimeFormatter)

  @BeforeEach
  fun setup() {
    repo.deleteAll()
    fmsResultRepository.deleteAll()
  }

  @Nested
  @DisplayName("POST /api/orders")
  inner class PostOrders {
    @Test
    fun `It should should create an order with type REQUEST if no type provided`() {
      val order = webTestClient.post()
        .uri("/api/orders")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isOk
        .expectBody(OrderDto::class.java)
        .returnResult()
        .responseBody!!

      assertThat(order.id).isNotNull()
      assertThat(order.status).isEqualTo(OrderStatus.IN_PROGRESS)
      assertThat(order.type).isEqualTo(RequestType.REQUEST)
      assertThat(order.username).isEqualTo(testUser)
    }

    @Test
    fun `It should create an order with type VARIATION`() {
      val order = webTestClient.post()
        .uri("/api/orders")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
            {
              "type": "VARIATION"
            }
            """.trimIndent(),
          ),
        )
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isOk
        .expectBody(OrderDto::class.java)
        .returnResult()
        .responseBody!!

      assertThat(order.id).isNotNull()
      assertThat(order.status).isEqualTo(OrderStatus.IN_PROGRESS)
      assertThat(order.type).isEqualTo(RequestType.VARIATION)
      assertThat(order.username).isEqualTo(testUser)
    }
  }

  @Nested
  @DisplayName("POST /api/orders/copy-as-variation")
  inner class PostVariation {
    @Test
    fun `It should should create an order version with type VARIATION`() {
      val order = createAndPersistPopulatedOrder(status = OrderStatus.SUBMITTED)

      val variationOrder = webTestClient.post()
        .uri("/api/orders/${order.id}/copy-as-variation")
        .headers(setAuthorisation(username = "AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody(OrderDto::class.java)
        .returnResult()
        .responseBody!!

      assertThat(variationOrder.id).isNotNull()
      assertThat(variationOrder.id).isEqualTo(order.id)
      assertThat(variationOrder.status).isEqualTo(OrderStatus.IN_PROGRESS)
      assertThat(variationOrder.type).isEqualTo(RequestType.VARIATION)
      assertThat(variationOrder.username).isEqualTo(testUser)
    }

    @Test
    fun `A new id and variationId should be assigned to the new variation order`() {
      val order = createAndPersistPopulatedOrder(status = OrderStatus.SUBMITTED)

      val variationOrder = webTestClient.post()
        .uri("/api/orders/${order.id}/copy-as-variation")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isOk
        .expectBody(OrderDto::class.java)
        .returnResult()
        .responseBody!!

      assertThat(variationOrder.deviceWearer!!.id).isNotEqualTo(order.deviceWearer!!.id)
      assertThat(variationOrder.deviceWearer.versionId).isNotEqualTo(order.deviceWearer!!.versionId)
    }

    @Test
    fun `Details about the device wearer and order should be copied from the original order`() {
      val order = createAndPersistPopulatedOrder(status = OrderStatus.SUBMITTED)

      val variationOrder = webTestClient.post()
        .uri("/api/orders/${order.id}/copy-as-variation")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isOk
        .expectBody(OrderDto::class.java)
        .returnResult()
        .responseBody!!

      assertThat(variationOrder.deviceWearer)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .ignoringFields(
          "id",
          "versionId",
          "dateOfBirth",
          "version",
        )
        .isEqualTo(order.deviceWearer)

      assertThat(variationOrder.contactDetails)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .ignoringFields(
          "id",
          "versionId",
          "version",
        )
        .isEqualTo(order.contactDetails)

      assertThat(variationOrder.interestedParties)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .ignoringFields(
          "id",
          "versionId",
          "version",
        )
        .isEqualTo(order.interestedParties)

      assertThat(variationOrder.addresses)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .ignoringFields(
          "id",
          "versionId",
          "version",
        )
        .isEqualTo(order.addresses)

      assertThat(variationOrder.monitoringConditions)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .ignoringFields(
          "id",
          "versionId",
          "startDate",
          "endDate",
          "version",
        )
        .isEqualTo(order.monitoringConditions)
    }

    @Test
    fun `It return bad request when latest version is not in SUBMITTED state`() {
      val order = createAndPersistPopulatedOrder(status = OrderStatus.IN_PROGRESS)

      val result = webTestClient.post()
        .uri("/api/orders/${order.id}/copy-as-variation")
        .headers(setAuthorisation(username = "AUTH_ADM"))
        .exchange()
        .expectStatus()
        .is4xxClientError
        .expectBody(ErrorResponse::class.java)
        .returnResult()

      val error = result.responseBody!!
      assertThat(error.userMessage)
        .isEqualTo("Bad Request: Order latest version not submitted")
    }
  }

  @Nested
  @DisplayName("GET /api/orders")
  inner class GetOrders {
    @Test
    fun `It should return orders when no searchTerm is provided`() {
      createOrder("AUTH_ADM")

      webTestClient.get()
        .uri("/api/orders")
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isOk
        .expectBodyList(OrderDto::class.java)
        .hasSize(1)
    }

    @Test
    fun `It should return orders when an empty searchTerm is provided`() {
      createOrder("AUTH_ADM")

      webTestClient.get()
        .uri("/api/orders?searchTerm=")
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isOk
        .expectBodyList(OrderDto::class.java)
        .hasSize(1)
    }

    @Test
    fun `It should return orders where the firstName matches the searchTerm`() {
      val order = createOrder("AUTH_ADM")

      // Create the device wearer
      webTestClient.put()
        .uri("/api/orders/${order.id}/device-wearer")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
            {
              "firstName": "John",
              "lastName": "Smith",
              "alias": "",
              "adultAtTimeOfInstallation": "false",
              "sex": "MALE",
              "gender": "MALE",
              "dateOfBirth": "2024-01-01T00:00:00.000Z",
              "disabilities": "",
              "interpreterRequired": true,
              "language": "French"
            }
            """.trimIndent(),
          ),
        )
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isOk

      webTestClient.get()
        .uri("/api/orders?searchTerm=John")
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isOk
        .expectBodyList(OrderDto::class.java)
        .hasSize(1)
    }

    @Test
    fun `It should return orders where the lastName matches the searchTerm`() {
      val order = createOrder("AUTH_ADM")

      // Create the device wearer
      webTestClient.put()
        .uri("/api/orders/${order.id}/device-wearer")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
            {
              "firstName": "John",
              "lastName": "Smith",
              "alias": "",
              "adultAtTimeOfInstallation": "false",
              "sex": "MALE",
              "gender": "MALE",
              "dateOfBirth": "2024-01-01T00:00:00.000Z",
              "disabilities": "",
              "interpreterRequired": true,
              "language": "French"
            }
            """.trimIndent(),
          ),
        )
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isOk

      webTestClient.get()
        .uri("/api/orders?searchTerm=Smith")
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isOk
        .expectBodyList(OrderDto::class.java)
        .hasSize(1)
    }

    @Test
    fun `It should return orders where the firstName matches the searchTerm with different casing`() {
      val order = createOrder("AUTH_ADM")

      // Create the device wearer
      webTestClient.put()
        .uri("/api/orders/${order.id}/device-wearer")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
            {
              "firstName": "John",
              "lastName": "Smith",
              "alias": "",
              "adultAtTimeOfInstallation": "false",
              "sex": "MALE",
              "gender": "MALE",
              "dateOfBirth": "2024-01-01T00:00:00.000Z",
              "disabilities": "",
              "interpreterRequired": true,
              "language": "French"
            }
            """.trimIndent(),
          ),
        )
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isOk

      webTestClient.get()
        .uri("/api/orders?searchTerm=john")
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isOk
        .expectBodyList(OrderDto::class.java)
        .hasSize(1)
    }

    @Test
    fun `It should return orders where the lastName matches the searchTerm with different casing`() {
      val order = createOrder("AUTH_ADM")

      // Create the device wearer
      webTestClient.put()
        .uri("/api/orders/${order.id}/device-wearer")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
            {
              "firstName": "John",
              "lastName": "Smith",
              "alias": "",
              "adultAtTimeOfInstallation": "false",
              "sex": "MALE",
              "gender": "MALE",
              "dateOfBirth": "2024-01-01T00:00:00.000Z",
              "disabilities": "",
              "interpreterRequired": true,
              "language": "French"
            }
            """.trimIndent(),
          ),
        )
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isOk

      webTestClient.get()
        .uri("/api/orders?searchTerm=smith")
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isOk
        .expectBodyList(OrderDto::class.java)
        .hasSize(1)
    }

    @Test
    fun `It should return orders where the first and last names match`() {
      val order = createOrder("AUTH_ADM")

      // Create the device wearer
      webTestClient.put()
        .uri("/api/orders/${order.id}/device-wearer")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
            {
              "firstName": "John",
              "lastName": "Smith",
              "alias": "",
              "adultAtTimeOfInstallation": "false",
              "sex": "MALE",
              "gender": "MALE",
              "dateOfBirth": "2024-01-01T00:00:00.000Z",
              "disabilities": "",
              "interpreterRequired": true,
              "language": "French"
            }
            """.trimIndent(),
          ),
        )
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isOk

      webTestClient.get()
        .uri("/api/orders?searchTerm=john smith")
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isOk
        .expectBodyList(OrderDto::class.java)
        .hasSize(1)
    }

    @Test
    fun `It should only return orders belonging to user`() {
      createOrder("AUTH_ADM")
      createOrder("AUTH_ADM")
      createOrder("AUTH_ADM_2")

      webTestClient.get()
        .uri("/api/orders")
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isOk
        .expectBodyList(OrderDto::class.java)
        .hasSize(2)
    }
  }

  @Nested
  @DisplayName("GET /api/orders/search")
  inner class SearchOrders {
    @Test
    fun `It should return orders where the first and last names match`() {
      createAndPersistPopulatedOrder(status = OrderStatus.SUBMITTED)

      webTestClient.get()
        .uri("/api/orders/search?searchTerm=john smith")
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isOk
        .expectBodyList(OrderDto::class.java)
        .hasSize(1)
    }

    @Test
    fun `It should return orders created by a different user`() {
      createAndPersistPopulatedOrder(status = OrderStatus.SUBMITTED)

      webTestClient.get()
        .uri("/api/orders/search?searchTerm=john smith")
        .headers(setAuthorisation("SOME_OTHER_USER"))
        .exchange()
        .expectStatus()
        .isOk
        .expectBodyList(OrderDto::class.java)
        .hasSize(1)
    }

    @Test
    fun `It should only return submitted orders`() {
      createAndPersistPopulatedOrder(status = OrderStatus.IN_PROGRESS)

      webTestClient.get()
        .uri("/api/orders/search?searchTerm=john smith")
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isOk
        .expectBodyList(OrderDto::class.java)
        .hasSize(0)
    }

    @Test
    fun `It should only return orders that match full name`() {
      createAndPersistPopulatedOrder(status = OrderStatus.SUBMITTED)

      webTestClient.get()
        .uri("/api/orders/search?searchTerm=john")
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isOk
        .expectBodyList(OrderDto::class.java)
        .hasSize(0)
    }

    @Test
    fun `Should only return the most recent order version`() {
      val order = TestUtilities.createReadyToSubmitOrder(

        startDate = mockStartDate,
        endDate = mockEndDate,
      )
      val versionId1 = UUID.randomUUID()
      val versionId2 = UUID.randomUUID()
      order.versions.add(
        OrderVersion(
          id = versionId1,
          username = "AUTH_ADM",
          status = OrderStatus.SUBMITTED,
          type = RequestType.REQUEST,
          orderId = order.id,
          versionId = 2,
          deviceWearer = DeviceWearer(versionId = versionId1, firstName = "John", lastName = "Smith"),
          dataDictionaryVersion = DataDictionaryVersion.DDV4,
        ),
      )
      order.versions.add(
        OrderVersion(
          id = versionId2,
          username = "AUTH_ADM",
          status = OrderStatus.SUBMITTED,
          type = RequestType.REQUEST,
          orderId = order.id,
          versionId = 3,
          deviceWearer = DeviceWearer(versionId = versionId2, firstName = "John", lastName = "Smith"),
          dataDictionaryVersion = DataDictionaryVersion.DDV4,
        ),
      )

      repo.save(order)

      val result = webTestClient.get()
        .uri("/api/orders?searchTerm=john smith")
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isOk
        .expectBodyList(OrderDto::class.java)
        .hasSize(1).returnResult().responseBody

      assertThat(result.first().deviceWearer?.versionId).isEqualTo(versionId2)
    }
  }

  @Nested
  @DisplayName("GET /api/orders/{orderId}")
  inner class GetOrderVersion {
    @Test
    fun `It should return the order if owned by the user`() {
      val order = createOrder()

      webTestClient.get()
        .uri("/api/orders/${order.id}")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isOk
        .expectBody(OrderDto::class.java)
        .isEqualTo(order)
    }

    @Test
    fun `It should return not found if order does not exist`() {
      webTestClient.get()
        .uri("/api/orders/${UUID.randomUUID()}")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isNotFound()
    }

    @Test
    fun `It should return not found if order belongs to another user`() {
      val order = createOrder("AUTH_ADM")

      webTestClient.get()
        .uri("/api/orders/${order.id}")
        .headers(setAuthorisation("AUTH_ADM_2"))
        .exchange()
        .expectStatus()
        .isNotFound()
    }

    @Test
    fun `It should return if order belongs to another user but the order is submitted`() {
      val order = createSubmittedOrder()

      webTestClient.get()
        .uri("/api/orders/${order.id}")
        .headers(setAuthorisation("AUTH_ADM_2"))
        .exchange()
        .expectStatus()
        .isOk
    }
  }

  @Nested
  @DisplayName("DELETE /api/orders/{orderId}")
  inner class DeleteOrderVersion {
    @Test
    fun `It should return an error if the order does not exist`() {
      val id = UUID.randomUUID()
      val result = webTestClient.delete()
        .uri("/api/orders/$id")
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectBodyList(ErrorResponse::class.java)
        .returnResult()

      val error = result.responseBody!!.first()

      assertThat(
        error.developerMessage,
      ).isEqualTo("Order with id $id does not exist")
    }

    @Test
    fun `It should return an error if the order belongs to another user`() {
      val order = createOrder()
      val result = webTestClient.delete()
        .uri("/api/orders/${order.id}")
        .headers(setAuthorisation("AUTH_ADM_2"))
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectBodyList(ErrorResponse::class.java)
        .returnResult()

      val error = result.responseBody!!.first()

      assertThat(
        error.developerMessage,
      ).isEqualTo("Order (${order.id}) for AUTH_ADM_2 not found")
    }

    @Test
    fun `It should return an error if the order is in a submitted state`() {
      val order = createSubmittedOrder()

      val result = webTestClient.delete()
        .uri("/api/orders/${order.id}")
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .expectBodyList(ErrorResponse::class.java)
        .returnResult()

      val error = result.responseBody!!.first()

      assertThat(
        error.developerMessage,
      ).isEqualTo("Order with id ${order.id} cannot be deleted because it has already been submitted")
    }

    @Test
    fun `It should delete an in progress order`() {
      val order = createOrder()

      // Delete the order
      webTestClient.delete()
        .uri("/api/orders/${order.id}")
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isNoContent

      // Getting the order should return a not found
      webTestClient.get()
        .uri("/api/orders/${order.id}")
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isNotFound
    }
  }

  @Nested
  @DisplayName("POST /api/orders/{orderId}/submit")
  inner class SubmitOrder {
    @Test
    fun `It should return not found if order does not exist when submitting order`() {
      webTestClient.post()
        .uri("/api/orders/${UUID.randomUUID()}/submit")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isNotFound()
    }

    @Test
    fun `It should throw an error if an attempt is made to re-submit a submitted order`() {
      val order = createAndPersistPopulatedOrder(status = OrderStatus.SUBMITTED)

      val result = webTestClient.post()
        .uri("/api/orders/${order.id}/submit")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .is4xxClientError
        .expectBody(ErrorResponse::class.java)
        .returnResult()

      val error = result.responseBody!!
      assertThat(error.userMessage)
        .isEqualTo("Error submitting order: This order has already been submitted")
    }

    @Test
    fun `It should throw an error if an attempt is made to submit an order with error status`() {
      val order = createAndPersistPopulatedOrder(status = OrderStatus.ERROR)

      val result = webTestClient.post()
        .uri("/api/orders/${order.id}/submit")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .is4xxClientError
        .expectBody(ErrorResponse::class.java)
        .returnResult()

      val error = result.responseBody!!
      assertThat(error.userMessage)
        .isEqualTo("Error submitting order: This order has encountered an error and cannot be submitted")
    }

    @Test
    fun `It should throw an error if an incomplete order is submitted`() {
      val order = createOrder()

      val result = webTestClient.post()
        .uri("/api/orders/${order.id}/submit")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .is4xxClientError
        .expectBody(ErrorResponse::class.java)
        .returnResult()

      val error = result.responseBody!!
      assertThat(error.userMessage)
        .isEqualTo("Error submitting order: Please complete all mandatory fields before submitting this form")
    }

    @Test
    fun `It should throw an error if an incomplete variation is submitted`() {
      val order = createVariation()

      val result = webTestClient.post()
        .uri("/api/orders/${order.id}/submit")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .is4xxClientError
        .expectBody(ErrorResponse::class.java)
        .returnResult()

      val error = result.responseBody!!
      assertThat(error.userMessage)
        .isEqualTo("Error submitting order: Please complete all mandatory fields before submitting this form")
    }

    @Test
    fun `It should return an error if serco auth service returned error`() {
      val order = createAndPersistPopulatedOrder()

      sercoAuthApi.stubError()

      val result = webTestClient.post()
        .uri("/api/orders/${order.id}/submit")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .is4xxClientError
        .expectBody(ErrorResponse::class.java)
        .returnResult()

      val error = result.responseBody!!
      assertThat(error.userMessage)
        .isEqualTo("Error submitting order: The order could not be submitted to Serco")
    }

    @Test
    fun `It should return an error if serco create device wearer service returned error`() {
      val order = createAndPersistPopulatedOrder()

      sercoAuthApi.stubGrantToken()
      sercoApi.stubCreateDeviceWearer(
        HttpStatus.INTERNAL_SERVER_ERROR,
        FmsResponse(),
        FmsErrorResponse(error = FmsErrorResponseDetails("", "Mock Create DW Error")),
      )

      val result = webTestClient.post()
        .uri("/api/orders/${order.id}/submit")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .is4xxClientError
        .expectBody(ErrorResponse::class.java)
        .returnResult()

      val error = result.responseBody!!
      assertThat(error.userMessage)
        .isEqualTo("Error submitting order: The order could not be submitted to Serco")

      val submitResult = fmsResultRepository.findAll().firstOrNull()
      assertThat(submitResult).isNotNull
      assertThat(
        submitResult!!.deviceWearerResult.error,
      ).isEqualTo("Error creating FMS Device Wearer for order: ${order.id} with error: Mock Create DW Error")
    }

    @Test
    fun `It should return an error if serco create monitoring order service returned error`() {
      val order = createAndPersistPopulatedOrder()
      sercoAuthApi.stubGrantToken()

      sercoApi.stubCreateDeviceWearer(
        HttpStatus.OK,
        FmsResponse(result = listOf(FmsResult(message = "", id = "MockDeviceWearerId"))),
      )
      sercoApi.stubCreateMonitoringOrder(
        HttpStatus.INTERNAL_SERVER_ERROR,
        FmsResponse(),
        FmsErrorResponse(error = FmsErrorResponseDetails("", "Mock Create MO Error")),
      )
      val result = webTestClient.post()
        .uri("/api/orders/${order.id}/submit")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .is4xxClientError
        .expectBody(ErrorResponse::class.java)
        .returnResult()

      val error = result.responseBody!!
      assertThat(error.userMessage)
        .isEqualTo("Error submitting order: The order could not be submitted to Serco")

      val submitResult = fmsResultRepository.findAll().firstOrNull()
      assertThat(submitResult).isNotNull
      assertThat(
        submitResult!!.monitoringOrderResult.error,
      ).isEqualTo("Error creating FMS Monitoring Order for order: ${order.id} with error: Mock Create MO Error")

      // Get updated order
      val updatedOrder = getOrder(order.id)

      assertThat(updatedOrder.fmsResultId).isEqualTo(submitResult.id)
      assertThat(updatedOrder.status).isEqualTo(OrderStatus.ERROR)
    }

    @Test
    fun `It should return an error if submit attachment to serco returned error`() {
      val orderId = UUID.randomUUID()
      val versionId = UUID.randomUUID()
      val order = createAndPersistPopulatedOrder(
        id = orderId,
        versionId = versionId,
        documents = mutableListOf(
          AdditionalDocument(
            id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
            versionId = versionId,
            fileType = DocumentType.LICENCE,
            fileName = "mockLicense.jpg",
          ),
        ),
      )
      sercoAuthApi.stubGrantToken()
      sercoApi.stubCreateDeviceWearer(
        HttpStatus.OK,
        FmsResponse(result = listOf(FmsResult(message = "", id = "MockDeviceWearerId"))),
      )
      sercoApi.stubCreateMonitoringOrder(
        HttpStatus.OK,
        FmsResponse(result = listOf(FmsResult(message = "", id = "MockMonitoringOrderId"))),
      )

      sercoApi.stubSubmitAttachment(
        HttpStatus.INTERNAL_SERVER_ERROR,
        FmsAttachmentResponse(
          result = FmsAttachmentResult(),
        ),
        FmsErrorResponse(error = FmsErrorResponseDetails("", "Mock Create Attachment Error")),
      )

      documentApi.stubGetDocument(order.additionalDocuments[0].id.toString())
      val result = webTestClient.post()
        .uri("/api/orders/${order.id}/submit")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .is4xxClientError
        .expectBody(ErrorResponse::class.java)
        .returnResult()

      val error = result.responseBody!!
      assertThat(error.developerMessage)
        .isEqualTo("Error submit attachments to Serco")

      val submitResult = fmsResultRepository.findAll().firstOrNull()
      assertThat(submitResult).isNotNull

      // Get updated order
      val updatedOrder = getOrder(order.id)

      assertThat(updatedOrder.fmsResultId).isEqualTo(submitResult!!.id)
      assertThat(updatedOrder.status).isEqualTo(OrderStatus.ERROR)
    }

    fun String.removeWhitespaceAndNewlines(): String = this.replace("(\"[^\"]*\")|\\s".toRegex(), "\$1")

    @Test
    fun `It updates order with serco device wearer id, monitoring id, order status & attachments, and return 200`() {
      val orderId = UUID.randomUUID()
      val versionId = UUID.randomUUID()
      val order = createAndPersistPopulatedOrder(
        id = orderId,
        versionId = versionId,
        documents = mutableListOf(
          AdditionalDocument(
            id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
            versionId = versionId,
            fileType = DocumentType.LICENCE,
            fileName = "mockFile",
          ),
        ),
      )

      sercoAuthApi.stubGrantToken()

      sercoApi.stubCreateDeviceWearer(
        HttpStatus.OK,
        FmsResponse(result = listOf(FmsResult(message = "", id = "MockDeviceWearerId"))),
      )

      sercoApi.stubCreateMonitoringOrder(
        HttpStatus.OK,
        FmsResponse(result = listOf(FmsResult(message = "", id = "MockMonitoringOrderId"))),
      )

      sercoApi.stubSubmitAttachment(
        HttpStatus.OK,
        FmsAttachmentResponse(
          result = FmsAttachmentResult(
            fileName = order.additionalDocuments[0].fileName!!,
            tableName = "x_serg2_ems_csm_sr_mo_new",
            sysId = "MockSysId",
            tableSysId = "MockDeviceWearerId",
          ),
        ),
      )

      sercoApi.stubSubmitAttachment(
        HttpStatus.OK,
        FmsAttachmentResponse(
          result = FmsAttachmentResult(
            fileName = order.enforcementZoneConditions[0].fileName!!,
            tableName = "x_serg2_ems_csm_sr_mo_new",
            sysId = "MockSysId",
            tableSysId = "MockDeviceWearerId",
          ),
        ),
      )

      documentApi.stubGetDocument(order.additionalDocuments.first().id.toString())
      documentApi.stubGetDocument(order.enforcementZoneConditions[0].fileId.toString())

      webTestClient.post()
        .uri("/api/orders/${order.id}/submit")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isOk

      val submitResult = fmsResultRepository.findAll().firstOrNull()
      assertThat(submitResult).isNotNull

      val expectedDWJson = """
      {
      	"title": "",
      	"first_name": "John",
      	"middle_name": "",
      	"last_name": "Smith",
      	"alias": "Johnny",
      	"date_of_birth": "1990-01-01",
      	"adult_child": "adult",
      	"sex": "Male",
      	"gender_identity": "Male",
      	"disability": [
      		{
      			"disability": "Vision"
      		},
      		{
      			"disability": "Learning, understanding or concentrating"
      		}
      	],
      	"address_1": "20 Somewhere Street",
      	"address_2": "Nowhere City",
      	"address_3": "Random County",
      	"address_4": "United Kingdom",
      	"address_post_code": "SW11 1NC",
      	"secondary_address_1": "22 Somewhere Street",
        "secondary_address_2": "Nowhere City",
        "secondary_address_3": "Random County",
        "secondary_address_4": "United Kingdom",
        "secondary_address_post_code": "SW11 1NC",
      	"phone_number": "00447401111111",
      	"risk_serious_harm": "",
      	"risk_self_harm": "",
      	"risk_details": "Danger",
      	"mappa": "MAAPA 1",
      	"mappa_case_type": "CPPC (Critical Public Protection Case)",
      	"risk_categories": [
          {
            "category": "Sexual Offences"
          },
          {
            "category": "Risk to Specific Gender"
          }
        ],
      	"responsible_adult_required": "true",
      	"parent": "Mark Smith",
      	"guardian": "",
      	"parent_address_1": "",
      	"parent_address_2": "",
      	"parent_address_3": "",
      	"parent_address_4": "",
      	"parent_address_post_code": "",
      	"parent_phone_number": "00447401111111",
      	"parent_dob": "",
      	"pnc_id": "pncId",
      	"nomis_id": "nomisId",
      	"delius_id": "deliusId",
      	"prison_number": "prisonNumber",
      	"home_office_case_reference_number": "homeOfficeReferenceNumber",
      	"interpreter_required": "true",
      	"language": "British Sign"
      }
      """.trimIndent()

      val expectedOrderJson = """
      {
      	"case_id": "MockDeviceWearerId",
      	"allday_lockdown": "",
      	"atv_allowance": "",
      	"condition_type": "Requirement of a Community Order",
      	"court": "",
      	"court_order_email": "",      	
      	"device_type": "",
      	"device_wearer": "John Smith",
      	"enforceable_condition": [
      		{
      			"condition": "Curfew with EM",
            "start_date": "$mockStartDateInBritishTime",
            "end_date": "$mockEndDateInBritishTime"
      		},
      		{
      			"condition": "Location Monitoring (Fitted Device)",
            "start_date": "$mockStartDateInBritishTime",
            "end_date": "$mockEndDateInBritishTime"
      		},
      		{
      			"condition": "EM Exclusion / Inclusion Zone",
            "start_date": "$mockStartDateInBritishTime",
            "end_date": "$mockEndDateInBritishTime"
      		},          
      		{
      			"condition": "AAMR",
            "start_date": "$mockStartDateInBritishTime",
            "end_date": "$mockEndDateInBritishTime"
      		}
      	],
      	"exclusion_allday": "",
      	"interim_court_date": "",
      	"issuing_organisation": "",
      	"media_interest": "",
      	"new_order_received": "",
      	"notifying_officer_email": "",
      	"notifying_officer_name": "",
      	"notifying_organization": "Prison",
      	"no_post_code": "",
      	"no_address_1": "",
      	"no_address_2": "",
      	"no_address_3": "",
      	"no_address_4": "",
      	"no_email": "",
      	"no_name": "Wayland Prison",
      	"no_phone_number": "",
      	"offence": "Fraud Offences",
      	"offence_date": "",
      	"order_end": "$mockEndDateInBritishTime",
      	"order_id": "$orderId",
      	"order_request_type": "New Order",
      	"order_start": "$mockStartDateInBritishTime",
      	"order_type": "Community",
      	"order_type_description": "DAPOL",
      	"order_type_detail": "",
      	"order_variation_date": "",
      	"order_variation_details": "",
      	"order_variation_req_received_date": "",
      	"order_variation_type": "",
      	"pdu_responsible": "Camden and Islington",
      	"pdu_responsible_email": "",
      	"planned_order_end_date": "",
      	"responsible_officer_details_received": "",
      	"responsible_officer_email": "",
      	"responsible_officer_phone": "00447401111111",
      	"responsible_officer_name": "John Smith",
      	"responsible_organization": "Probation",
      	"ro_post_code": "",
      	"ro_address_1": "",
      	"ro_address_2": "",
      	"ro_address_3": "",
      	"ro_address_4": "",
      	"ro_email": "abc@def.com",
      	"ro_phone": "",
      	"ro_region": "London",
      	"sentence_date": "",
      	"sentence_expiry": "",
        "sentence_type": "Life Sentence",
      	"tag_at_source": "",
      	"tag_at_source_details": "",
      	"technical_bail": "",
      	"trial_date": "",
      	"trial_outcome": "",
      	"conditional_release_date": "${mockStartDate.format(formatter)}",
      	"reason_for_order_ending_early": "",
      	"business_unit": "",
        "service_end_date": "${mockEndDate.format(formatter)}",
        "curfew_description": "",
      	"curfew_start": "$mockStartDateInBritishTime",
      	"curfew_end": "$mockEndDateInBritishTime",
      	"curfew_duration": [
      		{
      			"location": "primary",
      			"allday": "",
      			"schedule": [
      				{
      					"day": "Mo",
      					"start": "17:00",
      					"end": "09:00"
      				},
      				{
      					"day": "Tu",
      					"start": "17:00",
      					"end": "09:00"
      				},
      				{
      					"day": "Wed",
      					"start": "17:00",
      					"end": "09:00"
      				},
      				{
      					"day": "Th",
      					"start": "17:00",
      					"end": "09:00"
      				},
      				{
      					"day": "Fr",
      					"start": "17:00",
      					"end": "09:00"
      				},
      				{
      					"day": "Sa",
      					"start": "17:00",
      					"end": "09:00"
      				},
      				{
      					"day": "Su",
      					"start": "17:00",
      					"end": "09:00"
      				}
      			]
      		},
      		{
      			"location": "secondary",
      			"allday": "",
      			"schedule": [
      				{
      					"day": "Mo",
      					"start": "17:00",
      					"end": "09:00"
      				},
      				{
      					"day": "Tu",
      					"start": "17:00",
      					"end": "09:00"
      				},
      				{
      					"day": "Wed",
      					"start": "17:00",
      					"end": "09:00"
      				},
      				{
      					"day": "Th",
      					"start": "17:00",
      					"end": "09:00"
      				},
      				{
      					"day": "Fr",
      					"start": "17:00",
      					"end": "09:00"
      				},
      				{
      					"day": "Sa",
      					"start": "17:00",
      					"end": "09:00"
      				},
      				{
      					"day": "Su",
      					"start": "17:00",
      					"end": "09:00"
      				}
      			]
      		}
      	],
      	"trail_monitoring": "No",
      	"exclusion_zones": [
          {
            "description": "Mock Exclusion Zone",
            "duration": "Mock Exclusion Duration",
            "start": "${mockStartDate.format(formatter)}",
            "end": "${mockEndDate.format(formatter)}"
          }
        ],      	
      	"inclusion_zones": [
          {
            "description": "Mock Inclusion Zone",
            "duration": "Mock Inclusion Duration",
            "start": "${mockStartDate.format(formatter)}",
            "end": "${mockEndDate.format(formatter)}"
          }
        ],
      	"abstinence": "Yes",
      	"schedule": "",
      	"checkin_schedule": [],
      	"revocation_date": "",
      	"revocation_type": "",
        "installation_address_1": "24 Somewhere Street",
        "installation_address_2": "Nowhere City",
        "installation_address_3": "Random County",
        "installation_address_4": "United Kingdom",
        "installation_address_post_code": "SW11 1NC",
        "crown_court_case_reference_number": "",
        "magistrate_court_case_reference_number": "",
        "issp": "Yes",
        "hdc": "No",
      	"order_status": "Not Started"
      }
      """.trimIndent()

      val expectedDeviceWearer = objectMapper.readValue<FmsDeviceWearer>(expectedDWJson)
      val storedDeviceWearer = objectMapper.readValue<FmsDeviceWearer>(submitResult!!.deviceWearerResult.payload)
      assertThat(expectedDeviceWearer).isEqualTo(storedDeviceWearer)

      val expectedMonitoringOrder = objectMapper.readValue<MonitoringOrder>(expectedOrderJson)
      val storedMonitoringOrder = objectMapper.readValue<MonitoringOrder>(submitResult.monitoringOrderResult.payload)
      assertThat(expectedMonitoringOrder).isEqualTo(storedMonitoringOrder)

      assertThat(submitResult.attachmentResults[0].sysId).isEqualTo("MockSysId")
      assertThat(
        submitResult.attachmentResults[0].fileType,
      ).isEqualTo(order.additionalDocuments.first().fileType.toString())
      assertThat(
        submitResult.attachmentResults[0].attachmentId,
      ).isEqualTo(order.additionalDocuments.first().id.toString())
      val updatedOrder = getOrder(order.id)
      assertThat(updatedOrder.fmsResultId).isEqualTo(submitResult.id)
      assertThat(updatedOrder.status).isEqualTo(OrderStatus.SUBMITTED)
    }

    @Test
    fun `It should be possible to submit multiple attachments`() {
      val orderId = UUID.randomUUID()
      val versionId = UUID.randomUUID()
      val order = createAndPersistPopulatedOrder(
        id = orderId,
        versionId = versionId,
        documents = mutableListOf(
          AdditionalDocument(
            id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
            versionId = versionId,
            fileType = DocumentType.LICENCE,
            fileName = "mockLicense.jpg",
          ),
          AdditionalDocument(
            id = UUID.fromString("550e8400-e29b-41d4-a716-446655440001"),
            versionId = versionId,
            fileType = DocumentType.PHOTO_ID,
            fileName = "mockPhotoId.jpg",
          ),
        ),
      )

      sercoAuthApi.stubGrantToken()

      sercoApi.stubCreateDeviceWearer(
        HttpStatus.OK,
        FmsResponse(result = listOf(FmsResult(message = "", id = "MockDeviceWearerId"))),
      )

      sercoApi.stubCreateMonitoringOrder(
        HttpStatus.OK,
        FmsResponse(result = listOf(FmsResult(message = "", id = "MockMonitoringOrderId"))),
      )

      sercoApi.stubSubmitAttachment(
        HttpStatus.OK,
        FmsAttachmentResponse(
          result = FmsAttachmentResult(
            fileName = order.additionalDocuments[0].fileName!!,
            tableName = "x_serg2_ems_csm_sr_mo_new",
            sysId = "MockSysId",
            tableSysId = "MockDeviceWearerId",
          ),
        ),
      )

      sercoApi.stubSubmitAttachment(
        HttpStatus.OK,
        FmsAttachmentResponse(
          result = FmsAttachmentResult(
            fileName = order.additionalDocuments[1].fileName!!,
            tableName = "x_serg2_ems_csm_sr_mo_new",
            sysId = "MockSysId",
            tableSysId = "MockDeviceWearerId",
          ),
        ),
      )

      sercoApi.stubSubmitAttachment(
        HttpStatus.OK,
        FmsAttachmentResponse(
          result = FmsAttachmentResult(
            fileName = order.enforcementZoneConditions[0].fileName!!,
            tableName = "x_serg2_ems_csm_sr_mo_new",
            sysId = "MockSysId",
            tableSysId = "MockDeviceWearerId",
          ),
        ),
      )

      documentApi.stubGetDocument(order.additionalDocuments[0].id.toString())
      documentApi.stubGetDocument(order.additionalDocuments[1].id.toString())
      documentApi.stubGetDocument(order.enforcementZoneConditions[0].fileId.toString())

      webTestClient.post()
        .uri("/api/orders/${order.id}/submit")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isOk

      val submitResult = fmsResultRepository.findAll().firstOrNull()
      assertThat(submitResult).isNotNull

      val expectedDWJson = """
      {
      	"title": "",
      	"first_name": "John",
      	"middle_name": "",
      	"last_name": "Smith",
      	"alias": "Johnny",
      	"date_of_birth": "1990-01-01",
      	"adult_child": "adult",
      	"sex": "Male",
      	"gender_identity": "Male",
      	"disability": [
      		{
      			"disability": "Vision"
      		},
      		{
      			"disability": "Learning, understanding or concentrating"
      		}
      	],
      	"address_1": "20 Somewhere Street",
      	"address_2": "Nowhere City",
      	"address_3": "Random County",
      	"address_4": "United Kingdom",
      	"address_post_code": "SW11 1NC",
      	"secondary_address_1": "22 Somewhere Street",
        "secondary_address_2": "Nowhere City",
        "secondary_address_3": "Random County",
        "secondary_address_4": "United Kingdom",
        "secondary_address_post_code": "SW11 1NC",
      	"phone_number": "00447401111111",
      	"risk_serious_harm": "",
      	"risk_self_harm": "",
      	"risk_details": "Danger",
      	"mappa": "MAAPA 1",
      	"mappa_case_type": "CPPC (Critical Public Protection Case)",
      	"risk_categories": [
          {
            "category": "Sexual Offences"
          },
          {
            "category": "Risk to Specific Gender"
          }
        ],
      	"responsible_adult_required": "true",
      	"parent": "Mark Smith",
      	"guardian": "",
      	"parent_address_1": "",
      	"parent_address_2": "",
      	"parent_address_3": "",
      	"parent_address_4": "",
      	"parent_address_post_code": "",
      	"parent_phone_number": "00447401111111",
      	"parent_dob": "",
      	"pnc_id": "pncId",
      	"nomis_id": "nomisId",
      	"delius_id": "deliusId",
      	"prison_number": "prisonNumber",
      	"home_office_case_reference_number": "homeOfficeReferenceNumber",
      	"interpreter_required": "true",
      	"language": "British Sign"
      }
      """.trimIndent()

      val expectedOrderJson = """
      {
      	"case_id": "MockDeviceWearerId",
      	"allday_lockdown": "",
      	"atv_allowance": "",
      	"condition_type": "Requirement of a Community Order",
      	"court": "",
      	"court_order_email": "",      	
      	"device_type": "",
      	"device_wearer": "John Smith",
      	"enforceable_condition": [
      		{
      			"condition": "Curfew with EM",
            "start_date": "$mockStartDateInBritishTime",
            "end_date": "$mockEndDateInBritishTime"
      		},
      		{
      			"condition": "Location Monitoring (Fitted Device)",
            "start_date": "$mockStartDateInBritishTime",
            "end_date": "$mockEndDateInBritishTime"
      		},
      		{
      			"condition": "EM Exclusion / Inclusion Zone",
            "start_date": "$mockStartDateInBritishTime",
            "end_date": "$mockEndDateInBritishTime"
      		},          
      		{
      			"condition": "AAMR",
            "start_date": "$mockStartDateInBritishTime",
           "end_date": "$mockEndDateInBritishTime"
      		}
      	],
      	"exclusion_allday": "",
      	"interim_court_date": "",
      	"issuing_organisation": "",
      	"media_interest": "",
      	"new_order_received": "",
      	"notifying_officer_email": "",
      	"notifying_officer_name": "",
      	"notifying_organization": "Prison",
      	"no_post_code": "",
      	"no_address_1": "",
      	"no_address_2": "",
      	"no_address_3": "",
      	"no_address_4": "",
      	"no_email": "",
      	"no_name": "Wayland Prison",
      	"no_phone_number": "",
      	"offence": "Fraud Offences",
      	"offence_date": "",
      	"order_end": "$mockEndDateInBritishTime",
      	"order_id": "$orderId",
      	"order_request_type": "New Order",
      	"order_start": "$mockStartDateInBritishTime",
      	"order_type": "Community",
      	"order_type_description": "DAPOL",
      	"order_type_detail": "",
      	"order_variation_date": "",
      	"order_variation_details": "",
      	"order_variation_req_received_date": "",
      	"order_variation_type": "",
      	"pdu_responsible": "Camden and Islington",
      	"pdu_responsible_email": "",
      	"planned_order_end_date": "",
      	"responsible_officer_details_received": "",
      	"responsible_officer_email": "",
      	"responsible_officer_phone": "00447401111111",
      	"responsible_officer_name": "John Smith",
      	"responsible_organization": "Probation",
      	"ro_post_code": "",
      	"ro_address_1": "",
      	"ro_address_2": "",
      	"ro_address_3": "",
      	"ro_address_4": "",
      	"ro_email": "abc@def.com",
      	"ro_phone": "",
      	"ro_region": "London",
      	"sentence_date": "",
      	"sentence_expiry": "",
        "sentence_type": "Life Sentence",
      	"tag_at_source": "",
      	"tag_at_source_details": "",
      	"technical_bail": "",
      	"trial_date": "",
      	"trial_outcome": "",
      	"conditional_release_date": "${mockStartDate.format(formatter)}",
      	"reason_for_order_ending_early": "",
      	"business_unit": "",
        "service_end_date": "${mockEndDate.format(formatter)}",
        "curfew_description": "",
      	"curfew_start": "$mockStartDateInBritishTime",
      	"curfew_end": "$mockEndDateInBritishTime",
      	"curfew_duration": [
      		{
      			"location": "primary",
      			"allday": "",
      			"schedule": [
      				{
      					"day": "Mo",
      					"start": "17:00",
      					"end": "09:00"
      				},
      				{
      					"day": "Tu",
      					"start": "17:00",
      					"end": "09:00"
      				},
      				{
      					"day": "Wed",
      					"start": "17:00",
      					"end": "09:00"
      				},
      				{
      					"day": "Th",
      					"start": "17:00",
      					"end": "09:00"
      				},
      				{
      					"day": "Fr",
      					"start": "17:00",
      					"end": "09:00"
      				},
      				{
      					"day": "Sa",
      					"start": "17:00",
      					"end": "09:00"
      				},
      				{
      					"day": "Su",
      					"start": "17:00",
      					"end": "09:00"
      				}
      			]
      		},
      		{
      			"location": "secondary",
      			"allday": "",
      			"schedule": [
      				{
      					"day": "Mo",
      					"start": "17:00",
      					"end": "09:00"
      				},
      				{
      					"day": "Tu",
      					"start": "17:00",
      					"end": "09:00"
      				},
      				{
      					"day": "Wed",
      					"start": "17:00",
      					"end": "09:00"
      				},
      				{
      					"day": "Th",
      					"start": "17:00",
      					"end": "09:00"
      				},
      				{
      					"day": "Fr",
      					"start": "17:00",
      					"end": "09:00"
      				},
      				{
      					"day": "Sa",
      					"start": "17:00",
      					"end": "09:00"
      				},
      				{
      					"day": "Su",
      					"start": "17:00",
      					"end": "09:00"
      				}
      			]
      		}
      	],
      	"trail_monitoring": "No",
      	"exclusion_zones": [
          {
            "description": "Mock Exclusion Zone",
            "duration": "Mock Exclusion Duration",
            "start": "${mockStartDate.format(formatter)}",
            "end": "${mockEndDate.format(formatter)}"
          }
        ],      	
      	"inclusion_zones": [
          {
            "description": "Mock Inclusion Zone",
            "duration": "Mock Inclusion Duration",
            "start": "${mockStartDate.format(formatter)}",
            "end": "${mockEndDate.format(formatter)}"
          }
        ],
      	"abstinence": "Yes",
      	"schedule": "",
      	"checkin_schedule": [],
      	"revocation_date": "",
      	"revocation_type": "",
        "installation_address_1": "24 Somewhere Street",
        "installation_address_2": "Nowhere City",
        "installation_address_3": "Random County",
        "installation_address_4": "United Kingdom",
        "installation_address_post_code": "SW11 1NC",
        "crown_court_case_reference_number": "",
        "magistrate_court_case_reference_number": "",
        "issp": "Yes",
        "hdc": "No",
      	"order_status": "Not Started"
      }
      """.trimIndent()

      assertThat(submitResult!!.success).isEqualTo(true)
      assertThat(submitResult.error).isEqualTo("")

      val expectedDeviceWearer = objectMapper.readValue<FmsDeviceWearer>(expectedDWJson)
      val storedDeviceWearer = objectMapper.readValue<FmsDeviceWearer>(submitResult.deviceWearerResult.payload)
      assertThat(expectedDeviceWearer).isEqualTo(storedDeviceWearer)

      val expectedMonitoringOrder = objectMapper.readValue<MonitoringOrder>(expectedOrderJson)
      val storedMonitoringOrder = objectMapper.readValue<MonitoringOrder>(submitResult.monitoringOrderResult.payload)
      assertThat(expectedMonitoringOrder).isEqualTo(storedMonitoringOrder)

      assertThat(submitResult.attachmentResults[0])
        .usingRecursiveComparison()
        .ignoringFields("id")
        .isEqualTo(
          FmsAttachmentSubmissionResult(
            status = SubmissionStatus.SUCCESS,
            sysId = "MockSysId",
            fileType = order.additionalDocuments[0].fileType.toString(),
            attachmentId = order.additionalDocuments[0].id.toString(),
          ),
        )

      assertThat(submitResult.attachmentResults[1])
        .usingRecursiveComparison()
        .ignoringFields("id")
        .isEqualTo(
          FmsAttachmentSubmissionResult(
            status = SubmissionStatus.SUCCESS,
            sysId = "MockSysId",
            fileType = order.additionalDocuments[1].fileType.toString(),
            attachmentId = order.additionalDocuments[1].id.toString(),
          ),
        )

      assertThat(submitResult.attachmentResults[2])
        .usingRecursiveComparison()
        .ignoringFields("id")
        .isEqualTo(
          FmsAttachmentSubmissionResult(
            status = SubmissionStatus.SUCCESS,
            sysId = "MockSysId",
            fileType = DocumentType.ENFORCEMENT_ZONE_MAP.toString(),
            attachmentId = order.enforcementZoneConditions[0].fileId.toString(),
          ),
        )

      val updatedOrder = getOrder(order.id)
      assertThat(updatedOrder.fmsResultId).isEqualTo(submitResult.id)
      assertThat(updatedOrder.status).isEqualTo(OrderStatus.SUBMITTED)
    }

    @Test
    fun `It should update order with device wearer id, monitoring id & order status, and return 200 for a variation`() {
      val orderId = UUID.randomUUID()
      val versionId = UUID.randomUUID()
      val order = createAndPersistPopulatedOrder(
        id = orderId,
        versionId = versionId,
        noFixedAddress = false,
        requestType = RequestType.VARIATION,
        documents = mutableListOf(
          AdditionalDocument(
            id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
            versionId = versionId,
            fileType = DocumentType.LICENCE,
            fileName = "mockLicense.jpg",
          ),
        ),
      )
      sercoAuthApi.stubGrantToken()

      sercoApi.stubUpdateDeviceWearer(
        HttpStatus.OK,
        FmsResponse(result = listOf(FmsResult(message = "", id = "MockDeviceWearerId"))),
      )

      sercoApi.stubSubmitAttachment(
        HttpStatus.OK,
        FmsAttachmentResponse(
          result = FmsAttachmentResult(
            fileName = order.additionalDocuments[0].fileName!!,
            tableName = "x_serg2_ems_csm_sr_mo_existing",
            sysId = "MockSysId",
            tableSysId = "MockDeviceWearerId",
          ),
        ),
      )
      sercoApi.stubSubmitAttachment(
        HttpStatus.OK,
        FmsAttachmentResponse(
          result = FmsAttachmentResult(
            fileName = order.enforcementZoneConditions[0].fileName!!,
            tableName = "x_serg2_ems_csm_sr_mo_existing",
            sysId = "MockSysId",
            tableSysId = "MockDeviceWearerId",
          ),
        ),
      )
      documentApi.stubGetDocument(order.additionalDocuments[0].id.toString())
      documentApi.stubGetDocument(order.enforcementZoneConditions[0].fileId.toString())
      sercoApi.stubUpdateMonitoringOrder(
        HttpStatus.OK,
        FmsResponse(result = listOf(FmsResult(message = "", id = "MockMonitoringOrderId"))),
      )
      webTestClient.post()
        .uri("/api/orders/${order.id}/submit")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isOk

      val submitResult = fmsResultRepository.findAll().firstOrNull()
      assertThat(submitResult).isNotNull

      val expectedDWJson = """
      {
      	"title": "",
      	"first_name": "John",
      	"middle_name": "",
      	"last_name": "Smith",
      	"alias": "Johnny",
      	"date_of_birth": "1990-01-01",
      	"adult_child": "adult",
      	"sex": "Male",
      	"gender_identity": "Male",
      	"disability": [
      		{
      			"disability": "Vision"
      		},
      		{
      			"disability": "Learning, understanding or concentrating"
      		}
      	],
      	"address_1": "20 Somewhere Street",
      	"address_2": "Nowhere City",
      	"address_3": "Random County",
      	"address_4": "United Kingdom",
      	"address_post_code": "SW11 1NC",
      	"secondary_address_1": "22 Somewhere Street",
        "secondary_address_2": "Nowhere City",
        "secondary_address_3": "Random County",
        "secondary_address_4": "United Kingdom",
        "secondary_address_post_code": "SW11 1NC",
      	"phone_number": "00447401111111",
      	"risk_serious_harm": "",
      	"risk_self_harm": "",
      	"risk_details": "Danger",
      	"mappa": "MAAPA 1",
      	"mappa_case_type": "CPPC (Critical Public Protection Case)",
      	"risk_categories": [
          {
            "category": "Sexual Offences"
          },
          {
            "category": "Risk to Specific Gender"
          }
        ],
      	"responsible_adult_required": "true",
      	"parent": "Mark Smith",
      	"guardian": "",
      	"parent_address_1": "",
      	"parent_address_2": "",
      	"parent_address_3": "",
      	"parent_address_4": "",
      	"parent_address_post_code": "",
      	"parent_phone_number": "00447401111111",
      	"parent_dob": "",
      	"pnc_id": "pncId",
      	"nomis_id": "nomisId",
      	"delius_id": "deliusId",
      	"prison_number": "prisonNumber",
      	"home_office_case_reference_number": "homeOfficeReferenceNumber",
      	"interpreter_required": "true",
      	"language": "British Sign"
      }
      """.trimIndent()
      val expectedOrderJson = """
      {
      	"case_id": "MockDeviceWearerId",
      	"allday_lockdown": "",
      	"atv_allowance": "",
      	"condition_type": "Requirement of a Community Order",
      	"court": "",
      	"court_order_email": "",      	
      	"device_type": "",
      	"device_wearer": "John Smith",
      	"enforceable_condition": [
      		{
      			"condition": "Curfew with EM",
            "start_date": "$mockStartDateInBritishTime",
            "end_date": "$mockEndDateInBritishTime"
      		},
      		{
      			"condition": "Location Monitoring (Fitted Device)",
            "start_date": "$mockStartDateInBritishTime",
            "end_date": "$mockEndDateInBritishTime"
      		},
      		{
      			"condition": "EM Exclusion / Inclusion Zone",
            "start_date": "$mockStartDateInBritishTime",
            "end_date": "$mockEndDateInBritishTime"
      		},          
      		{
      			"condition": "AAMR",
            "start_date": "$mockStartDateInBritishTime",
            "end_date": "$mockEndDateInBritishTime"
      		}
      	],
      	"exclusion_allday": "",
      	"interim_court_date": "",
      	"issuing_organisation": "",
      	"media_interest": "",
      	"new_order_received": "",
      	"notifying_officer_email": "",
      	"notifying_officer_name": "",
      	"notifying_organization": "Prison",
      	"no_post_code": "",
      	"no_address_1": "",
      	"no_address_2": "",
      	"no_address_3": "",
      	"no_address_4": "",
      	"no_email": "",
      	"no_name": "Wayland Prison",
      	"no_phone_number": "",
      	"offence": "Fraud Offences",
      	"offence_date": "",
      	"order_end": "$mockEndDateInBritishTime",
      	"order_id": "${order.id}",
      	"order_request_type": "Variation",
      	"order_start": "$mockStartDateInBritishTime",
      	"order_type": "Community",
      	"order_type_description": "DAPOL",
      	"order_type_detail": "",
      	"order_variation_date": "${mockStartDate.format(dateTimeFormatter)}",
      	"order_variation_details": "Change to address",
      	"order_variation_req_received_date": "",
      	"order_variation_type": "Change of address",
      	"pdu_responsible": "Camden and Islington",
      	"pdu_responsible_email": "",
      	"planned_order_end_date": "",
      	"responsible_officer_details_received": "",
      	"responsible_officer_email": "",
      	"responsible_officer_phone": "00447401111111",
      	"responsible_officer_name": "John Smith",
      	"responsible_organization": "Probation",
      	"ro_post_code": "",
      	"ro_address_1": "",
      	"ro_address_2": "",
      	"ro_address_3": "",
      	"ro_address_4": "",
      	"ro_email": "abc@def.com",
      	"ro_phone": "",
      	"ro_region": "London",
      	"sentence_date": "",
      	"sentence_expiry": "",
        "sentence_type": "Life Sentence",
      	"tag_at_source": "",
      	"tag_at_source_details": "",
      	"technical_bail": "",
      	"trial_date": "",
      	"trial_outcome": "",
      	"conditional_release_date": "${mockStartDate.format(formatter)}",
      	"reason_for_order_ending_early": "",
      	"business_unit": "",
        "service_end_date": "${mockEndDate.format(formatter)}",
        "curfew_description": "",
      	"curfew_start": "$mockStartDateInBritishTime",
      	"curfew_end": "$mockEndDateInBritishTime",
      	"curfew_duration": [
      		{
      			"location": "primary",
      			"allday": "",
      			"schedule": [
      				{
      					"day": "Mo",
      					"start": "17:00",
      					"end": "09:00"
      				},
      				{
      					"day": "Tu",
      					"start": "17:00",
      					"end": "09:00"
      				},
      				{
      					"day": "Wed",
      					"start": "17:00",
      					"end": "09:00"
      				},
      				{
      					"day": "Th",
      					"start": "17:00",
      					"end": "09:00"
      				},
      				{
      					"day": "Fr",
      					"start": "17:00",
      					"end": "09:00"
      				},
      				{
      					"day": "Sa",
      					"start": "17:00",
      					"end": "09:00"
      				},
      				{
      					"day": "Su",
      					"start": "17:00",
      					"end": "09:00"
      				}
      			]
      		},
      		{
      			"location": "secondary",
      			"allday": "",
      			"schedule": [
      				{
      					"day": "Mo",
      					"start": "17:00",
      					"end": "09:00"
      				},
      				{
      					"day": "Tu",
      					"start": "17:00",
      					"end": "09:00"
      				},
      				{
      					"day": "Wed",
      					"start": "17:00",
      					"end": "09:00"
      				},
      				{
      					"day": "Th",
      					"start": "17:00",
      					"end": "09:00"
      				},
      				{
      					"day": "Fr",
      					"start": "17:00",
      					"end": "09:00"
      				},
      				{
      					"day": "Sa",
      					"start": "17:00",
      					"end": "09:00"
      				},
      				{
      					"day": "Su",
      					"start": "17:00",
      					"end": "09:00"
      				}
      			]
      		}
      	],
      	"trail_monitoring": "No",
      	"exclusion_zones": [
          {
            "description": "Mock Exclusion Zone",
            "duration": "Mock Exclusion Duration",
            "start": "${mockStartDate.format(formatter)}",
            "end": "${mockEndDate.format(formatter)}"
          }
        ],      	
      	"inclusion_zones": [
          {
            "description": "Mock Inclusion Zone",
            "duration": "Mock Inclusion Duration",
            "start": "${mockStartDate.format(formatter)}",
            "end": "${mockEndDate.format(formatter)}"
          }
        ],
      	"abstinence": "Yes",
      	"schedule": "",
      	"checkin_schedule": [],
      	"revocation_date": "",
      	"revocation_type": "",
        "installation_address_1": "24 Somewhere Street",
        "installation_address_2": "Nowhere City",
        "installation_address_3": "Random County",
        "installation_address_4": "United Kingdom",
        "installation_address_post_code": "SW11 1NC",
        "crown_court_case_reference_number": "",
        "magistrate_court_case_reference_number": "",
        "issp": "Yes",
        "hdc": "No",
      	"order_status": "Not Started"
      }
      """.trimIndent()

      val expectedDeviceWearer = objectMapper.readValue<FmsDeviceWearer>(expectedDWJson)
      val storedDeviceWearer = objectMapper.readValue<FmsDeviceWearer>(submitResult!!.deviceWearerResult.payload)
      assertThat(expectedDeviceWearer).isEqualTo(storedDeviceWearer)

      val expectedMonitoringOrder = objectMapper.readValue<MonitoringOrder>(expectedOrderJson)
      val storedMonitoringOrder = objectMapper.readValue<MonitoringOrder>(submitResult.monitoringOrderResult.payload)
      assertThat(expectedMonitoringOrder).isEqualTo(storedMonitoringOrder)
      val updatedOrder = getOrder(order.id)
      assertThat(updatedOrder.fmsResultId).isEqualTo(submitResult.id)
      assertThat(updatedOrder.status).isEqualTo(OrderStatus.SUBMITTED)

      assertThat(submitResult.attachmentResults[0])
        .usingRecursiveComparison()
        .ignoringFields("id")
        .isEqualTo(
          FmsAttachmentSubmissionResult(
            status = SubmissionStatus.SUCCESS,
            sysId = "MockSysId",
            fileType = order.additionalDocuments[0].fileType.toString(),
            attachmentId = order.additionalDocuments[0].id.toString(),
          ),
        )
      assertThat(submitResult.attachmentResults[1])
        .usingRecursiveComparison()
        .ignoringFields("id")
        .isEqualTo(
          FmsAttachmentSubmissionResult(
            status = SubmissionStatus.SUCCESS,
            sysId = "MockSysId",
            fileType = DocumentType.ENFORCEMENT_ZONE_MAP.toString(),
            attachmentId = order.enforcementZoneConditions[0].fileId.toString(),
          ),
        )
    }

    @Test
    fun `It should map installation address if device wearer no fixed Abode is true`() {
      val order = createAndPersistPopulatedOrder(noFixedAddress = true)
      sercoAuthApi.stubGrantToken()

      sercoApi.stubCreateDeviceWearer(
        HttpStatus.OK,
        FmsResponse(result = listOf(FmsResult(message = "", id = "MockDeviceWearerId"))),
      )

      sercoApi.stubCreateMonitoringOrder(
        HttpStatus.OK,
        FmsResponse(result = listOf(FmsResult(message = "", id = "MockMonitoringOrderId"))),
      )

      sercoApi.stubSubmitAttachment(
        HttpStatus.OK,
        FmsAttachmentResponse(
          result = FmsAttachmentResult(
            fileName = order.enforcementZoneConditions[0].fileName!!,
            tableName = "x_serg2_ems_csm_sr_mo_new",
            sysId = "MockSysId",
            tableSysId = "MockDeviceWearerId",
          ),
        ),
      )

      sercoApi.stubSubmitAttachment(
        HttpStatus.OK,
        FmsAttachmentResponse(
          result = FmsAttachmentResult(
            fileName = order.additionalDocuments[0].fileName!!,
            tableName = "x_serg2_ems_csm_sr_mo_new",
            sysId = "MockSysId",
            tableSysId = "MockDeviceWearerId",
          ),
        ),
      )

      documentApi.stubGetDocument(order.enforcementZoneConditions[0].fileId.toString())
      documentApi.stubGetDocument(order.additionalDocuments[0].id.toString())

      webTestClient.post()
        .uri("/api/orders/${order.id}/submit")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isOk

      val submitResult = fmsResultRepository.findAll().firstOrNull()
      assertThat(submitResult).isNotNull

      val expectedDWJson = """
      {
      	"title": "",
      	"first_name": "John",
      	"middle_name": "",
      	"last_name": "Smith",
      	"alias": "Johnny",
      	"date_of_birth": "1990-01-01",
      	"adult_child": "adult",
      	"sex": "Male",
      	"gender_identity": "Male",
      	"disability": [
      		{
      			"disability": "Vision"
      		},
      		{
      			"disability": "Learning, understanding or concentrating"
      		}
      	],
      	"address_1": "",
      	"address_2": "",
      	"address_3": "",
      	"address_4": "",
      	"address_post_code": "",
      	"secondary_address_1": "",
      	"secondary_address_2": "",
      	"secondary_address_3": "",
      	"secondary_address_4": "",
      	"secondary_address_post_code": "",
      	"phone_number": "00447401111111",
      	"risk_serious_harm": "",
      	"risk_self_harm": "",
      	"risk_details": "Danger",
      	"mappa": "MAAPA 1",
      	"mappa_case_type": "CPPC (Critical Public Protection Case)",
      	"risk_categories": [
          {
            "category": "Sexual Offences"
          },
          {
            "category": "Risk to Specific Gender"
          }
        ],
      	"responsible_adult_required": "true",
      	"parent": "Mark Smith",
      	"guardian": "",
      	"parent_address_1": "",
      	"parent_address_2": "",
      	"parent_address_3": "",
      	"parent_address_4": "",
      	"parent_address_post_code": "",
      	"parent_phone_number": "00447401111111",
      	"parent_dob": "",
      	"pnc_id": "pncId",
      	"nomis_id": "nomisId",
      	"delius_id": "deliusId",
      	"prison_number": "prisonNumber",
      	"home_office_case_reference_number": "homeOfficeReferenceNumber",
      	"interpreter_required": "true",
      	"language": "British Sign"
      }
      """.trimIndent()

      assertThat(submitResult!!.deviceWearerResult.payload).isEqualTo(expectedDWJson.removeWhitespaceAndNewlines())
      val fmsOrderRequest = submitResult.monitoringOrderResult.payload

      JsonPathExpectationsHelper("installation_address_1").assertValue(fmsOrderRequest, "24 Somewhere Street")
      JsonPathExpectationsHelper("installation_address_2").assertValue(fmsOrderRequest, "Nowhere City")
      JsonPathExpectationsHelper("installation_address_3").assertValue(fmsOrderRequest, "Random County")
      JsonPathExpectationsHelper("installation_address_4").assertValue(fmsOrderRequest, "United Kingdom")
      JsonPathExpectationsHelper("installation_address_post_code").assertValue(fmsOrderRequest, "SW11 1NC")

      assertThat(submitResult.attachmentResults[1])
        .usingRecursiveComparison()
        .ignoringFields("id")
        .isEqualTo(
          FmsAttachmentSubmissionResult(
            status = SubmissionStatus.SUCCESS,
            sysId = "MockSysId",
            fileType = DocumentType.ENFORCEMENT_ZONE_MAP.toString(),
            attachmentId = order.enforcementZoneConditions[0].fileId.toString(),
          ),
        )

      val updatedOrder = getOrder(order.id)
      assertThat(updatedOrder.fmsResultId).isEqualTo(submitResult.id)
    }
  }

  fun createAndPersistPopulatedOrder(
    id: UUID = UUID.randomUUID(),
    versionId: UUID = UUID.randomUUID(),
    noFixedAddress: Boolean = false,
    requestType: RequestType = RequestType.REQUEST,
    status: OrderStatus = OrderStatus.IN_PROGRESS,
    documents: MutableList<AdditionalDocument> = mutableListOf(),
  ): Order {
    val order = TestUtilities.createReadyToSubmitOrder(
      id = id,
      versionId = versionId,
      noFixedAddress = noFixedAddress,
      requestType = requestType,
      status = status,
      documents = documents,
      mockStartDate,
      mockEndDate,
    )
    repo.save(order)
    return order
  }
}
