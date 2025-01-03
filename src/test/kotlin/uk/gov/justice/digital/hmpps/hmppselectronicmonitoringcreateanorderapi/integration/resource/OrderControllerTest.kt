package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Address
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.AlcoholMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ContactDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewReleaseDateConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewTimeTable
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.EnforcementZoneConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationAndRisk
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InterestedParties
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ResponsibleAdult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.TrailMonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.VariationDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringInstallationLocationType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DocumentType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.EnforcementZoneType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MonitoringConditionType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderTypeDescription
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.VariationType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsAttachmentResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsAttachmentResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsErrorResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.FmsResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.SubmitFmsOrderResultRepository
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.ErrorResponse as FmsErrorResponseDetails

class OrderControllerTest : IntegrationTestBase() {
  @Autowired
  lateinit var repo: OrderRepository

  @Autowired
  lateinit var fmsResultRepository: SubmitFmsOrderResultRepository

  val mockStartDate: ZonedDateTime = ZonedDateTime.now().plusMonths(1)
  val mockEndDate: ZonedDateTime = ZonedDateTime.now().plusMonths(2)
  private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

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
      webTestClient.post()
        .uri("/api/orders")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isOk
        .expectBody(Order::class.java)

      val orders = repo.findAll()
      assertThat(orders).hasSize(1)
      assertThat(orders[0].username).isEqualTo("AUTH_ADM")
      assertThat(orders[0].status).isEqualTo(OrderStatus.IN_PROGRESS)
      assertThat(orders[0].type).isEqualTo(OrderType.REQUEST)
      assertThat(orders[0].id).isNotNull()
      assertThat(UUID.fromString(orders[0].id.toString())).isEqualTo(orders[0].id)
    }

    @Test
    fun `It should create an order with type VARIATION`() {
      webTestClient.post()
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
        .expectBody(Order::class.java)

      val orders = repo.findAll()
      assertThat(orders).hasSize(1)
      assertThat(orders[0].username).isEqualTo("AUTH_ADM")
      assertThat(orders[0].status).isEqualTo(OrderStatus.IN_PROGRESS)
      assertThat(orders[0].type).isEqualTo(OrderType.VARIATION)
      assertThat(orders[0].id).isNotNull()
      assertThat(UUID.fromString(orders[0].id.toString())).isEqualTo(orders[0].id)
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
        .expectBodyList(Order::class.java)
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
        .expectBodyList(Order::class.java)
        .hasSize(1)
    }

    @Test
    fun `It should return orders where the firstName matches the searchTerm`() {
      val order = createOrder("AUTH_ADM")

      order.deviceWearer = DeviceWearer(
        orderId = order.id,
        firstName = "John",
      )
      repo.save(order)

      webTestClient.get()
        .uri("/api/orders?searchTerm=John")
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isOk
        .expectBodyList(Order::class.java)
        .hasSize(1)
    }

    @Test
    fun `It should return orders where the lastName matches the searchTerm`() {
      val order = createOrder("AUTH_ADM")

      order.deviceWearer = DeviceWearer(
        orderId = order.id,
        lastName = "Smith",
      )
      repo.save(order)

      webTestClient.get()
        .uri("/api/orders?searchTerm=Smith")
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isOk
        .expectBodyList(Order::class.java)
        .hasSize(1)
    }

    @Test
    fun `It should return orders where the firstName matches the searchTerm with different casing`() {
      val order = createOrder("AUTH_ADM")

      order.deviceWearer = DeviceWearer(
        orderId = order.id,
        firstName = "John",
      )
      repo.save(order)

      webTestClient.get()
        .uri("/api/orders?searchTerm=john")
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isOk
        .expectBodyList(Order::class.java)
        .hasSize(1)
    }

    @Test
    fun `It should return orders where the lastName matches the searchTerm with different casing`() {
      val order = createOrder("AUTH_ADM")

      order.deviceWearer = DeviceWearer(
        orderId = order.id,
        lastName = "Smith",
      )
      repo.save(order)

      webTestClient.get()
        .uri("/api/orders?searchTerm=smith")
        .headers(setAuthorisation("AUTH_ADM"))
        .exchange()
        .expectStatus()
        .isOk
        .expectBodyList(Order::class.java)
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
        .expectBodyList(Order::class.java)
        .hasSize(2)
    }
  }

  @Nested
  @DisplayName("GET /api/orders/{orderId}")
  inner class GetOrder {
    @Test
    fun `It should return the order if owned by the user`() {
      val order = createOrder()

      webTestClient.get()
        .uri("/api/orders/${order.id}")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isOk
        .expectBody(Order::class.java)
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
  }

  @Nested
  @DisplayName("DELETE /api/orders/{orderId}")
  inner class DeleteOrder {
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
      ).isEqualTo("An order with id $id does not exist")
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
      ).isEqualTo("An order with id ${order.id} does not exist")
    }

    @Test
    fun `It should return an error if the order is in a submitted state`() {
      val order = createOrder()

      order.status = OrderStatus.SUBMITTED
      repo.save(order)

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
      val order = createReadyToSubmitOrder()
      order.status = OrderStatus.SUBMITTED
      repo.save(order)

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
      val order = createReadyToSubmitOrder()
      order.status = OrderStatus.ERROR
      repo.save(order)

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
      val order = createAndPersistReadyToSubmitOrder()

      sercoAuthApi.stubError()

      val result = webTestClient.post()
        .uri("/api/orders/${order.id}/submit")
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .is4xxClientError()
        .expectBody(ErrorResponse::class.java)
        .returnResult()

      val error = result.responseBody!!
      assertThat(error.userMessage)
        .isEqualTo("Error submitting order: The order could not be submitted to Serco")
    }

    @Test
    fun `It should return an error if serco create device wearer service returned error`() {
      val order = createAndPersistReadyToSubmitOrder()

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
    }

    @Test
    fun `It should return an error if serco create monitoring order service returned error`() {
      val order = createAndPersistReadyToSubmitOrder()
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
      var result = webTestClient.post()
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

      val updatedOrder = repo.findById(order.id).get()
      assertThat(updatedOrder.fmsResultId).isEqualTo(submitResult!!.id)
      assertThat(updatedOrder.status).isEqualTo(OrderStatus.ERROR)
    }

    fun String.removeWhitespaceAndNewlines(): String = this.replace("(\"[^\"]*\")|\\s".toRegex(), "\$1")

    @Test
    fun `It updates order with serco device wearer id, monitoring id, order status & attachments, and return 200`() {
      val order = createAndPersistReadyToSubmitOrder()

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
            sizeBytes = "",
            fileName = order.additionalDocuments.first().fileName!!,
            sysModCount = "",
            averageImageColor = "",
            imageWidth = "",
            sysUpdatedOn = "",
            sysTags = "",
            createdByName = "",
            tableName = "x_serg2_ems_csm_sr_mo_new",
            sysId = "MockSysId",
            updatedByName = "",
            imageHeight = "",
            sysUpdatedBy = "",
            downloadLink = "",
            contentType = "",
            sysCreatedOn = "",
            sizeCompressed = "",
            compressed = "",
            state = "",
            tableSysId = "MockDeviceWearerId",
            chunkSizeBytes = "",
            hash = "",
            sysCreatedBy = "",
          ),
        ),
      )

      documentApi.stubGetDocument(order.additionalDocuments.first().id.toString())

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
      	"secondary_address_1": "",
      	"secondary_address_2": "",
      	"secondary_address_3": "",
      	"secondary_address_4": "",
      	"secondary_address_post_code": "",
      	"phone_number": "07401111111",
      	"risk_serious_harm": "",
      	"risk_self_harm": "",
      	"risk_details": "Danger",
      	"mappa": "MAAPA 1",
      	"mappa_case_type": "CPPC (Critical Public Protection Case)",
      	"risk_categories": [],
      	"responsible_adult_required": "true",
      	"parent": "Mark Smith",
      	"guardian": "",
      	"parent_address_1": "",
      	"parent_address_2": "",
      	"parent_address_3": "",
      	"parent_address_4": "",
      	"parent_address_post_code": "",
      	"parent_phone_number": "07401111111",
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
            "start_date": "${mockStartDate.format(dateTimeFormatter)}",
            "end_date": "${mockEndDate.format(dateTimeFormatter)}"
      		},
      		{
      			"condition": "Location Monitoring (Fitted Device)",
            "start_date": "${mockStartDate.format(dateTimeFormatter)}",
            "end_date": "${mockEndDate.format(dateTimeFormatter)}"
      		},
      		{
      			"condition": "EM Exclusion / Inclusion Zone",
            "start_date": "${mockStartDate.format(dateTimeFormatter)}",
            "end_date": "${mockEndDate.format(dateTimeFormatter)}"
      		},          
      		{
      			"condition": "AAMR",
            "start_date": "${mockStartDate.format(dateTimeFormatter)}",
            "end_date": "${mockEndDate.format(dateTimeFormatter)}"
      		}
      	],
      	"exclusion_allday": "",
      	"interim_court_date": "",
      	"issuing_organisation": "",
      	"media_interest": "",
      	"new_order_received": "",
      	"notifying_officer_email": "",
      	"notifying_officer_name": "",
      	"notifying_organization": "Mock Organisation",
      	"no_post_code": "",
      	"no_address_1": "",
      	"no_address_2": "",
      	"no_address_3": "",
      	"no_address_4": "",
      	"no_email": "",
      	"no_name": "",
      	"no_phone_number": "",
      	"offence": "Fraud Offences",
      	"offence_date": "",
      	"order_end": "${mockEndDate.format(dateTimeFormatter)}",
      	"order_id": "${order.id}",
      	"order_request_type": "New Order",
      	"order_start": "${mockStartDate.format(dateTimeFormatter)}",
      	"order_type": "community",
      	"order_type_description": "DAPOL",
      	"order_type_detail": "",
      	"order_variation_date": "",
      	"order_variation_details": "",
      	"order_variation_req_received_date": "",
      	"order_variation_type": "",
      	"pdu_responsible": "",
      	"pdu_responsible_email": "",
      	"planned_order_end_date": "",
      	"responsible_officer_details_received": "",
      	"responsible_officer_email": "",
      	"responsible_officer_phone": "07401111111",
      	"responsible_officer_name": "John Smith",
      	"responsible_organization": "Avon and Somerset Constabulary",
      	"ro_post_code": "AB11 1CD",
      	"ro_address_1": "Line 1",
      	"ro_address_2": "Line 2",
      	"ro_address_3": "",
      	"ro_address_4": "",
      	"ro_email": "abc@def.com",
      	"ro_phone": "07401111111",
      	"ro_region": "Mock Region",
      	"sentence_date": "",
      	"sentence_expiry": "",
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
      	"curfew_start": "${mockStartDate.format(dateTimeFormatter)}",
      	"curfew_end": "${mockEndDate.format(dateTimeFormatter)}",
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
      	"order_status": "Not Started"
      }
      """.trimIndent()
      val expectedAttachmentJson = """
      {
        "fmsSysId": "MockSysId",
        "fileType": "${order.additionalDocuments.first().fileType}",
        "cemoAttachmentId": "${order.additionalDocuments.first().id}"
        
    }
      """.trimIndent()

      assertThat(submitResult!!.fmsDeviceWearerRequest).isEqualTo(expectedDWJson.removeWhitespaceAndNewlines())
      assertThat(submitResult.fmsOrderRequest).isEqualTo(expectedOrderJson.removeWhitespaceAndNewlines())
      assertThat(submitResult.fmsAdditionalDocument).isEqualTo(expectedAttachmentJson.removeWhitespaceAndNewlines())
      val updatedOrder = repo.findById(order.id).get()
      assertThat(updatedOrder.fmsResultId).isEqualTo(submitResult.id)
      assertThat(updatedOrder.status).isEqualTo(OrderStatus.SUBMITTED)
    }

    @Test
    fun `It should update order with device wearer id, monitoring id & order status, and return 200 for a variation`() {
      val order = createAndPersistReadyToSubmitOrder(false, OrderType.VARIATION)
      sercoAuthApi.stubGrantToken()

      sercoApi.stubCreateDeviceWearer(
        HttpStatus.OK,
        FmsResponse(result = listOf(FmsResult(message = "", id = "MockDeviceWearerId"))),
      )
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
      	"secondary_address_1": "",
      	"secondary_address_2": "",
      	"secondary_address_3": "",
      	"secondary_address_4": "",
      	"secondary_address_post_code": "",
      	"phone_number": "07401111111",
      	"risk_serious_harm": "",
      	"risk_self_harm": "",
      	"risk_details": "Danger",
      	"mappa": "MAAPA 1",
      	"mappa_case_type": "CPPC (Critical Public Protection Case)",
      	"risk_categories": [],
      	"responsible_adult_required": "true",
      	"parent": "Mark Smith",
      	"guardian": "",
      	"parent_address_1": "",
      	"parent_address_2": "",
      	"parent_address_3": "",
      	"parent_address_4": "",
      	"parent_address_post_code": "",
      	"parent_phone_number": "07401111111",
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
            "start_date": "${mockStartDate.format(dateTimeFormatter)}",
            "end_date": "${mockEndDate.format(dateTimeFormatter)}"
      		},
      		{
      			"condition": "Location Monitoring (Fitted Device)",
            "start_date": "${mockStartDate.format(dateTimeFormatter)}",
            "end_date": "${mockEndDate.format(dateTimeFormatter)}"
      		},
      		{
      			"condition": "EM Exclusion / Inclusion Zone",
            "start_date": "${mockStartDate.format(dateTimeFormatter)}",
            "end_date": "${mockEndDate.format(dateTimeFormatter)}"
      		},          
      		{
      			"condition": "AAMR",
            "start_date": "${mockStartDate.format(dateTimeFormatter)}",
            "end_date": "${mockEndDate.format(dateTimeFormatter)}"
      		}
      	],
      	"exclusion_allday": "",
      	"interim_court_date": "",
      	"issuing_organisation": "",
      	"media_interest": "",
      	"new_order_received": "",
      	"notifying_officer_email": "",
      	"notifying_officer_name": "",
      	"notifying_organization": "Mock Organisation",
      	"no_post_code": "",
      	"no_address_1": "",
      	"no_address_2": "",
      	"no_address_3": "",
      	"no_address_4": "",
      	"no_email": "",
      	"no_name": "",
      	"no_phone_number": "",
      	"offence": "Fraud Offences",
      	"offence_date": "",
      	"order_end": "${mockEndDate.format(dateTimeFormatter)}",
      	"order_id": "${order.id}",
      	"order_request_type": "Variation",
      	"order_start": "${mockStartDate.format(dateTimeFormatter)}",
      	"order_type": "community",
      	"order_type_description": "DAPOL",
      	"order_type_detail": "",
      	"order_variation_date": "${mockStartDate.format(dateTimeFormatter)}",
      	"order_variation_details": "",
      	"order_variation_req_received_date": "",
      	"order_variation_type": "Change of address",
      	"pdu_responsible": "",
      	"pdu_responsible_email": "",
      	"planned_order_end_date": "",
      	"responsible_officer_details_received": "",
      	"responsible_officer_email": "",
      	"responsible_officer_phone": "07401111111",
      	"responsible_officer_name": "John Smith",
      	"responsible_organization": "Avon and Somerset Constabulary",
      	"ro_post_code": "AB11 1CD",
      	"ro_address_1": "Line 1",
      	"ro_address_2": "Line 2",
      	"ro_address_3": "",
      	"ro_address_4": "",
      	"ro_email": "abc@def.com",
      	"ro_phone": "07401111111",
      	"ro_region": "Mock Region",
      	"sentence_date": "",
      	"sentence_expiry": "",
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
      	"curfew_start": "${mockStartDate.format(dateTimeFormatter)}",
      	"curfew_end": "${mockEndDate.format(dateTimeFormatter)}",
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
      	"order_status": "Not Started"
      }
      """.trimIndent()

      assertThat(submitResult!!.fmsDeviceWearerRequest).isEqualTo(expectedDWJson.removeWhitespaceAndNewlines())
      assertThat(submitResult.fmsOrderRequest).isEqualTo(expectedOrderJson.removeWhitespaceAndNewlines())
      val updatedOrder = repo.findById(order.id).get()
      assertThat(updatedOrder.fmsResultId).isEqualTo(submitResult.id)
      assertThat(updatedOrder.status).isEqualTo(OrderStatus.SUBMITTED)
    }

    @Test
    fun `It should map installation address if device wearer no fixed Abode is true`() {
      val order = createAndPersistReadyToSubmitOrder(true)
      sercoAuthApi.stubGrantToken()

      sercoApi.stubCreateDeviceWearer(
        HttpStatus.OK,
        FmsResponse(result = listOf(FmsResult(message = "", id = "MockDeviceWearerId"))),
      )
      sercoApi.stubCreateMonitoringOrder(
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
      	"phone_number": "07401111111",
      	"risk_serious_harm": "",
      	"risk_self_harm": "",
      	"risk_details": "Danger",
      	"mappa": "MAAPA 1",
      	"mappa_case_type": "CPPC (Critical Public Protection Case)",
      	"risk_categories": [],
      	"responsible_adult_required": "true",
      	"parent": "Mark Smith",
      	"guardian": "",
      	"parent_address_1": "",
      	"parent_address_2": "",
      	"parent_address_3": "",
      	"parent_address_4": "",
      	"parent_address_post_code": "",
      	"parent_phone_number": "07401111111",
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

      assertThat(submitResult!!.fmsDeviceWearerRequest).isEqualTo(expectedDWJson.removeWhitespaceAndNewlines())
      val fmsOrderRequest = submitResult.fmsOrderRequest!!

      JsonPathExpectationsHelper("installation_address_1").assertValue(fmsOrderRequest, "24 Somewhere Street")
      JsonPathExpectationsHelper("installation_address_2").assertValue(fmsOrderRequest, "Nowhere City")
      JsonPathExpectationsHelper("installation_address_3").assertValue(fmsOrderRequest, "Random County")
      JsonPathExpectationsHelper("installation_address_4").assertValue(fmsOrderRequest, "United Kingdom")
      JsonPathExpectationsHelper("installation_address_post_code").assertValue(fmsOrderRequest, "SW11 1NC")
      val updatedOrder = repo.findById(order.id).get()
      assertThat(updatedOrder.fmsResultId).isEqualTo(submitResult.id)
    }
  }

  fun createReadyToSubmitOrder(noFixedAddress: Boolean = false, orderType: OrderType = OrderType.REQUEST): Order {
    val order = Order(
      username = "AUTH_ADM",
      status = OrderStatus.IN_PROGRESS,
      type = orderType,
    )

    order.deviceWearer = DeviceWearer(
      orderId = order.id,
      firstName = "John",
      lastName = "Smith",
      alias = "Johnny",
      dateOfBirth = ZonedDateTime.of(1990, 1, 1, 1, 1, 1, 1, ZoneId.systemDefault()),
      adultAtTimeOfInstallation = true,
      sex = "Male",
      gender = "Male",
      disabilities = "VISION,LEARNING_UNDERSTANDING_CONCENTRATING",
      interpreterRequired = true,
      language = "British Sign",
      pncId = "pncId",
      deliusId = "deliusId",
      nomisId = "nomisId",
      prisonNumber = "prisonNumber",
      homeOfficeReferenceNumber = "homeOfficeReferenceNumber",
      noFixedAbode = noFixedAddress,
    )

    order.deviceWearerResponsibleAdult = ResponsibleAdult(
      orderId = order.id,
      fullName = "Mark Smith",
      contactNumber = "07401111111",
    )

    val responsibleOrganisationAddress = Address(
      orderId = order.id,
      addressLine1 = "Line 1",
      addressLine2 = "Line 2",
      addressLine3 = "",
      addressLine4 = "",
      postcode = "AB11 1CD",
      addressType = AddressType.RESPONSIBLE_ORGANISATION,
    )

    val installationAddress = Address(
      orderId = order.id,
      addressLine1 = "24 Somewhere Street",
      addressLine2 = "Nowhere City",
      addressLine3 = "Random County",
      addressLine4 = "United Kingdom",
      postcode = "SW11 1NC",
      addressType = AddressType.INSTALLATION,
    )

    if (!noFixedAddress) {
      order.addresses = mutableListOf(
        Address(
          orderId = order.id,
          addressLine1 = "20 Somewhere Street",
          addressLine2 = "Nowhere City",
          addressLine3 = "Random County",
          addressLine4 = "United Kingdom",
          postcode = "SW11 1NC",
          addressType = AddressType.PRIMARY,
        ),
        Address(
          orderId = order.id,
          addressLine1 = "22 Somewhere Street",
          addressLine2 = "Nowhere City",
          addressLine3 = "Random County",
          addressLine4 = "United Kingdom",
          postcode = "SW11 1NC",
          addressType = AddressType.SECONDARY,
        ),
        responsibleOrganisationAddress,
        installationAddress,
      )
    } else {
      order.addresses = mutableListOf(
        responsibleOrganisationAddress,
        installationAddress,
      )
    }

    order.installationAndRisk = InstallationAndRisk(
      orderId = order.id,
      offence = "Fraud Offences",
      riskDetails = "Danger",
      mappaLevel = "MAAPA 1",
      mappaCaseType = "CPPC (Critical Public Protection Case)",
    )

    order.contactDetails = ContactDetails(
      orderId = order.id,
      contactNumber = "07401111111",
    )

    order.monitoringConditions = MonitoringConditions(
      orderId = order.id,
      orderType = "community",
      orderTypeDescription = OrderTypeDescription.DAPOL,
      startDate = mockStartDate,
      endDate = mockEndDate,
      curfew = true,
      trail = true,
      exclusionZone = true,
      alcohol = true,
      caseId = "d8ea62e61bb8d610a10c20e0b24bcb85",
      conditionType = MonitoringConditionType.REQUIREMENT_OF_A_COMMUNITY_ORDER,
    )
    order.additionalDocuments = mutableListOf(
      AdditionalDocument(
        id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
        orderId = order.id,
        fileType = DocumentType.LICENCE,
        fileName = "mockFile",
      ),
    )

    val curfewConditions = CurfewConditions(
      orderId = order.id,
      startDate = mockStartDate,
      endDate = mockEndDate,
      curfewAddress = "PRIMARY,SECONDARY",
    )

    val curfewTimeTables = DayOfWeek.entries.map {
      CurfewTimeTable(
        orderId = order.id,
        dayOfWeek = it,
        startTime = "17:00",
        endTime = "09:00",
        curfewAddress = "PRIMARY_ADDRESS",
      )
    }
    order.curfewTimeTable.addAll(curfewTimeTables)
    val secondTimeTable = DayOfWeek.entries.map {
      CurfewTimeTable(
        orderId = order.id,
        dayOfWeek = it,
        startTime = "17:00",
        endTime = "09:00",
        curfewAddress = "SECONDARY_ADDRESS",
      )
    }
    order.curfewTimeTable.addAll(secondTimeTable)
    order.curfewConditions = curfewConditions

    order.curfewReleaseDateConditions = CurfewReleaseDateConditions(
      orderId = order.id,
      releaseDate = mockStartDate,
      startTime = "19:00",
      endTime = "23:00",
      curfewAddress = AddressType.PRIMARY,
    )

    order.enforcementZoneConditions.add(
      EnforcementZoneConditions(
        orderId = order.id,
        description = "Mock Exclusion Zone",
        duration = "Mock Exclusion Duration",
        startDate = mockStartDate,
        endDate = mockEndDate,
        zoneType = EnforcementZoneType.EXCLUSION,
      ),
    )

    order.enforcementZoneConditions.add(
      EnforcementZoneConditions(
        orderId = order.id,
        description = "Mock Inclusion Zone",
        duration = "Mock Inclusion Duration",
        startDate = mockStartDate,
        endDate = mockEndDate,
        zoneType = EnforcementZoneType.INCLUSION,
      ),
    )

    order.monitoringConditionsAlcohol = AlcoholMonitoringConditions(
      orderId = order.id,
      startDate = mockStartDate,
      endDate = mockEndDate,
      monitoringType = AlcoholMonitoringType.ALCOHOL_ABSTINENCE,
      installationLocation = AlcoholMonitoringInstallationLocationType.PRIMARY,
    )

    order.monitoringConditionsTrail = TrailMonitoringConditions(
      orderId = order.id,
      startDate = mockStartDate,
      endDate = mockEndDate,
    )

    order.interestedParties = InterestedParties(
      orderId = order.id,
      responsibleOfficerName = "John Smith",
      responsibleOfficerPhoneNumber = "07401111111",
      responsibleOrganisation = "Avon and Somerset Constabulary",
      responsibleOrganisationRegion = "Mock Region",
      responsibleOrganisationAddress = responsibleOrganisationAddress,
      responsibleOrganisationPhoneNumber = "07401111111",
      responsibleOrganisationEmail = "abc@def.com",
      notifyingOrganisation = "Mock Organisation",
      notifyingOrganisationEmail = "",
    )

    if (order.type === OrderType.VARIATION) {
      order.variationDetails = VariationDetails(
        orderId = order.id,
        variationType = VariationType.ADDRESS,
        variationDate = mockStartDate,
      )
    }

    return order
  }

  fun createAndPersistReadyToSubmitOrder(
    noFixedAddress: Boolean = false,
    orderType: OrderType = OrderType.REQUEST,
  ): Order {
    val order = createReadyToSubmitOrder(noFixedAddress, orderType)
    repo.save(order)
    return order
  }
}
