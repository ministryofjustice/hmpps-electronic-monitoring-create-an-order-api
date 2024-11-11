package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.SercoAuthMockServerExtension.Companion.sercoAuthApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.SercoMockApiExtension.Companion.sercoApi
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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringInstallationLocationType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AlcoholMonitoringType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.EnforcementZoneType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.MonitoringConditionType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderTypeDescription
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

  @BeforeEach
  fun setup() {
    repo.deleteAll()
    fmsResultRepository.deleteAll()
  }

  @Test
  fun `Order created and saved in database`() {
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
    assertThat(orders[0].id).isNotNull()
    assertThat(UUID.fromString(orders[0].id.toString())).isEqualTo(orders[0].id)
  }

  @Test
  fun `Only orders belonging to user returned from database`() {
    createOrder("AUTH_ADM")
    createOrder("AUTH_ADM")
    createOrder("AUTH_ADM_2")

    // Verify the database is set up correctly
    val allOrders = repo.findAll()
    assertThat(allOrders).hasSize(3)

    webTestClient.get()
      .uri("/api/orders")
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isOk
      .expectBodyList(Order::class.java)
      .hasSize(2)
  }

  @Test
  fun `Should return order if owned by the user`() {
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
  fun `Should return not found if order does not exist`() {
    webTestClient.get()
      .uri("/api/orders/${UUID.randomUUID()}")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isNotFound()
  }

  @Test
  fun `Should return not found if order belongs to another user`() {
    val order = createOrder("AUTH_ADM")

    webTestClient.get()
      .uri("/api/orders/${order.id}")
      .headers(setAuthorisation("AUTH_ADM_2"))
      .exchange()
      .expectStatus()
      .isNotFound()
  }

  @Test
  fun `Should return not found if order does not exist when submitting order`() {
    webTestClient.post()
      .uri("/api/orders/${UUID.randomUUID()}/submit")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isNotFound()
  }

  @Test
  fun `Should throw an error if an attempt is made to re-submit a submitted order`() {
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
  fun `Should throw an error if an attempt is made to submit an order with error status`() {
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
  fun `Should throw an error if an incomplete order is submitted`() {
    val order = createOrder()
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
      .isEqualTo("Error submitting order: Please complete all mandatory fields before submitting this form")
  }

  @Test
  fun `Should return an error if serco auth service returned error`() {
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
  fun `Should return an error if serco create device wearer service returned error`() {
    val order = createAndPersistReadyToSubmitOrder()

    sercoAuthApi.stubGrantToken()
    sercoApi.stupCreateDeviceWearer(
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
  fun `Should return an error if serco create monitoring order service returned error`() {
    val order = createAndPersistReadyToSubmitOrder()
    sercoAuthApi.stubGrantToken()

    sercoApi.stupCreateDeviceWearer(
      HttpStatus.OK,
      FmsResponse(result = listOf(FmsResult(message = "", id = "MockDeviceWearerId"))),
    )
    sercoApi.stupMonitoringOrder(
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
  fun `Should update order with serco device wearer id, monitoring id & order status, and return 200`() {
    val order = createAndPersistReadyToSubmitOrder()
    sercoAuthApi.stubGrantToken()

    sercoApi.stupCreateDeviceWearer(
      HttpStatus.OK,
      FmsResponse(result = listOf(FmsResult(message = "", id = "MockDeviceWearerId"))),
    )
    sercoApi.stupMonitoringOrder(
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
      	"alias": "Johny",
      	"date_of_birth": "1990-01-01",
      	"adult_child": "adult",
      	"sex": "Male",
      	"gender_identity": "Male",
      	"disability": [
      		{
      			"disability": "Vision"
      		},
      		{
      			"disability": "Hearing"
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
      	"describe_exclusion": "Mock Exclusion Zone",
      	"device_type": ",, ,",
      	"device_wearer": "John Smith",
      	"enforceable_condition": [
      		{
      			"condition": "Curfew with EM"
      		},
      		{
      			"condition": "Location Monitoring (Fitted Device)"
      		},
      		{
      			"condition": "EM Exclusion / Inclusion Zone"
      		},
      		{
      			"condition": "AAMR"
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
      	"order_end": "${mockEndDate.format(formatter)}",
      	"order_id": "${order.id}",
      	"order_request_type": "",
      	"order_start": "${mockStartDate.format(formatter)}",
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
      	"ro_address_1": "",
      	"ro_address_2": "",
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
      	"curfew_start": "${mockStartDate.format(formatter)}",
      	"curfew_end": null,
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
      	"trail_monitoring": "Yes",
      	"exclusion_zones": "true",
      	"exclusion_zones_duration": "Mock Exclusion Duration",
      	"inclusion_zones": "",
      	"inclusion_zones_duration": "",
      	"abstinence": "Yes",
      	"schedule": "",
      	"checkin_schedule": "",
      	"revocation_date": "",
      	"revocation_type": "",
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
  fun `Should default address to No Fixed Address if device wearer no fixed Abode is true`() {
    val order = createAndPersistReadyToSubmitOrder(true)
    sercoAuthApi.stubGrantToken()

    sercoApi.stupCreateDeviceWearer(
      HttpStatus.OK,
      FmsResponse(result = listOf(FmsResult(message = "", id = "MockDeviceWearerId"))),
    )
    sercoApi.stupMonitoringOrder(
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
      	"alias": "Johny",
      	"date_of_birth": "1990-01-01",
      	"adult_child": "adult",
      	"sex": "Male",
      	"gender_identity": "Male",
      	"disability": [
      		{
      			"disability": "Vision"
      		},
      		{
      			"disability": "Hearing"
      		}
      	],
      	"address_1": "No Fixed Address",
      	"address_2": "No Fixed Address",
      	"address_3": "No Fixed Address",
      	"address_4": "No Fixed Address",
      	"address_post_code": "No Fixed Address",
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

    val updatedOrder = repo.findById(order.id).get()
    assertThat(updatedOrder.fmsResultId).isEqualTo(submitResult.id)
  }

  fun createReadyToSubmitOrder(noFixedAddress: Boolean = false): Order {
    val order = Order(
      username = "AUTH_ADM",
      status = OrderStatus.IN_PROGRESS,
    )
    order.deviceWearer = DeviceWearer(
      orderId = order.id,
      firstName = "John",
      lastName = "Smith",
      alias = "Johny",
      dateOfBirth = ZonedDateTime.of(1990, 1, 1, 1, 1, 1, 1, ZoneId.systemDefault()),
      adultAtTimeOfInstallation = true,
      sex = "Male",
      gender = "Male",
      disabilities = "Vision,Hearing",
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

    order.deviceWearerContactDetails = ContactDetails(
      orderId = order.id,
      contactNumber = "07401111111",
    )
    order.monitoringConditions = MonitoringConditions(
      orderId = order.id,
      orderType = "community",
      orderTypeDescription = OrderTypeDescription.DAPOL,
      devicesRequired = arrayOf("Location - fitted,Alcohol (Remote Breath)"),
      startDate = mockStartDate,
      endDate = mockEndDate,
      curfew = true,
      trail = true,
      exclusionZone = true,
      alcohol = true,
      caseId = "d8ea62e61bb8d610a10c20e0b24bcb85",
      conditionType = MonitoringConditionType.REQUIREMENT_OF_A_COMMUNITY_ORDER,
    )
    order.additionalDocuments = mutableListOf()
    val curfewConditions = CurfewConditions(
      orderId = order.id,
      startDate = mockStartDate,
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

    order.monitoringConditionsAlcohol = AlcoholMonitoringConditions(
      orderId = order.id,
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
    return order
  }

  fun createAndPersistReadyToSubmitOrder(noFixedAddress: Boolean = false): Order {
    val order = createReadyToSubmitOrder(noFixedAddress)
    repo.save(order)
    return order
  }
}
