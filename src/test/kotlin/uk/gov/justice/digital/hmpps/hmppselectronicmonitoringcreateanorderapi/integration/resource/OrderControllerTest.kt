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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewReleaseDateConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.CurfewTimeTable
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearerContactDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.EnforcementZoneConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationAndRisk
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ResponsibleAdult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ResponsibleOfficer
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

  val mockStartDate: ZonedDateTime = ZonedDateTime.now().plusMonths(1)
  val mockEndDate: ZonedDateTime = ZonedDateTime.now().plusMonths(2)
  private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  @BeforeEach
  fun setup() {
    repo.deleteAll()
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
  fun `Should return 500 error if serco auth service returned error`() {
    val order = createReadyToSubmitOrder()

    sercoAuthApi.stubError()

    val result = webTestClient.post()
      .uri("/api/orders/${order.id}/submit")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .is5xxServerError
      .expectBody(ErrorResponse::class.java)
      .returnResult()

    val error = result.responseBody!!
    assertThat(error.userMessage)
      .isEqualTo("Error with Serco service Now: Invalid credentials used.")
  }

  @Test
  fun `Should return 500 error if serco create device wearer service returned error`() {
    val order = createReadyToSubmitOrder()
    val mockDeviceWearerJson = """
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
    "risk_serious_harm": "High",
    "risk_self_harm": "Low",
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
    "pnc_id": "",
    "nomis_id": "",
    "delius_id": "",
    "prison_number": "",
    "interpreter_required": "true",
    "language": "British Sign"
}
      """
    sercoAuthApi.stubGrantToken()

    sercoApi.stupCreateDeviceWearer(mockDeviceWearerJson, HttpStatus.INTERNAL_SERVER_ERROR, FmsResponse(), FmsErrorResponse(error = FmsErrorResponseDetails("", "Mock Create DW Error")))
    val result = webTestClient.post()
      .uri("/api/orders/${order.id}/submit")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .is5xxServerError
      .expectBody(ErrorResponse::class.java)
      .returnResult()

    val error = result.responseBody!!
    assertThat(error.userMessage)
      .isEqualTo("Error creating FMS Device Wearer for order: ${order.id} with error: Mock Create DW Error")
  }

  @Test
  fun `Should return 500 error if serco create monitoring order service returned error`() {
    val order = createReadyToSubmitOrder()
    val mockDeviceWearerJson = """
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
    "risk_serious_harm": "High",
    "risk_self_harm": "Low",
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
    "pnc_id": "",
    "nomis_id": "",
    "delius_id": "",
    "prison_number": "",
    "interpreter_required": "true",
    "language": "British Sign"
}
      """

    val mockOrderJson = """
      {
      	"case_id": "d8ea62e61bb8d610a10c20e0b24bcb85",
      	"allday_lockdown": "",
      	"atv_allowance": "",
      	"condition_type": "Requirement of Community Order",
      	"court": "",
      	"court_order_email": "",
      	"describe_exclusion": "Mock Exclusion Zone",
      	"device_type": "Location - fitted,Alcohol (Remote Breath)",
      	"device_wearer": "ebb5c29d1b115250a10c20e0b24bcb88",
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
      	"offence": "",
      	"offence_date": "",
      	"order_end": "${mockEndDate.format(formatter)}",
      	"order_id": "995069e1-a311-4c19-b9f8-cab259b4dafd",
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
      	"tag_at_source_details": "PRIMARY_ADDRESS",
      	"technical_bail": "",
      	"trial_date": "",
      	"trial_outcome": "",
      	"conditional_release_date": "${mockStartDate.format(formatter)}",
      	"reason_for_order_ending_early": "",
      	"business_unit": "",
      	"service_end_date": "${mockEndDate.format(formatter)}",
      	"curfew_start": "${mockStartDate.format(formatter)}",
      	"curfew_end": "${mockEndDate.format(formatter)}",
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
      		}
      	],
      	"trail_monitoring": "true",
      	"exclusion_zones": "true",
      	"exclusion_zones_duration": "",
      	"inclusion_zones": "",
      	"inclusion_zones_duration": "Mock Exclusion Duration",
      	"abstinence": "true",
      	"schedule": "",
      	"checkin_schedule": "",
      	"revocation_date": "",
      	"revocation_type": "",
      	"order_status": "Not Started"
      }
    """.trimIndent()
    sercoAuthApi.stubGrantToken()

    sercoApi.stupCreateDeviceWearer(mockDeviceWearerJson, HttpStatus.OK, FmsResponse(result = listOf(FmsResult(message = "", id = "MockDeviceWearerId"))))
    sercoApi.stupMonitoringOrder(mockOrderJson, HttpStatus.INTERNAL_SERVER_ERROR, FmsResponse(), FmsErrorResponse(error = FmsErrorResponseDetails("", "Mock Create MO Error")))
    var result = webTestClient.post()
      .uri("/api/orders/${order.id}/submit")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .is5xxServerError
      .expectBody(ErrorResponse::class.java)
      .returnResult()

    val error = result.responseBody!!
    assertThat(error.userMessage)
      .isEqualTo("Error creating FMS Monitoring Order for order: ${order.id} with error: Mock Create MO Error")
  }

  @Test
  fun `Should update order with serco device wearer id and monitoring Id and return 200`() {
    val order = createReadyToSubmitOrder()
    val mockDeviceWearerJson = """
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
    "risk_serious_harm": "High",
    "risk_self_harm": "Low",
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
    "pnc_id": "",
    "nomis_id": "",
    "delius_id": "",
    "prison_number": "",
    "interpreter_required": "true",
    "language": "British Sign"
}
      """

    val mockOrderJson = """
      {
      	"case_id": "d8ea62e61bb8d610a10c20e0b24bcb85",
      	"allday_lockdown": "",
      	"atv_allowance": "",
      	"condition_type": "Requirement of Community Order",
      	"court": "",
      	"court_order_email": "",
      	"describe_exclusion": "Mock Exclusion Zone",
      	"device_type": "Location - fitted,Alcohol (Remote Breath)",
      	"device_wearer": "ebb5c29d1b115250a10c20e0b24bcb88",
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
      	"offence": "",
      	"offence_date": "",
      	"order_end": "${mockEndDate.format(formatter)}",
      	"order_id": "995069e1-a311-4c19-b9f8-cab259b4dafd",
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
      	"tag_at_source_details": "PRIMARY_ADDRESS",
      	"technical_bail": "",
      	"trial_date": "",
      	"trial_outcome": "",
      	"conditional_release_date": "${mockStartDate.format(formatter)}",
      	"reason_for_order_ending_early": "",
      	"business_unit": "",
      	"service_end_date": "${mockEndDate.format(formatter)}",
      	"curfew_start": "${mockStartDate.format(formatter)}",
      	"curfew_end": "${mockEndDate.format(formatter)}",
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
      		}
      	],
      	"trail_monitoring": "true",
      	"exclusion_zones": "true",
      	"exclusion_zones_duration": "",
      	"inclusion_zones": "",
      	"inclusion_zones_duration": "Mock Exclusion Duration",
      	"abstinence": "true",
      	"schedule": "",
      	"checkin_schedule": "",
      	"revocation_date": "",
      	"revocation_type": "",
      	"order_status": "Not Started"
      }
    """.trimIndent()
    sercoAuthApi.stubGrantToken()

    sercoApi.stupCreateDeviceWearer(mockDeviceWearerJson, HttpStatus.OK, FmsResponse(result = listOf(FmsResult(message = "", id = "MockDeviceWearerId"))))
    sercoApi.stupMonitoringOrder(mockOrderJson, HttpStatus.OK, FmsResponse(result = listOf(FmsResult(message = "", id = "MockMonitoringOrderId"))))
    webTestClient.post()
      .uri("/api/orders/${order.id}/submit")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isOk

    val updatedOrder = repo.findById(order.id).get()
    assertThat(updatedOrder.fmsDeviceWearerId).isEqualTo("MockDeviceWearerId")
    assertThat(updatedOrder.fmsMonitoringOrderId).isEqualTo("MockMonitoringOrderId")
  }

  fun createReadyToSubmitOrder(): Order {
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
    )

    order.deviceWearerResponsibleAdult = ResponsibleAdult(
      orderId = order.id,
      fullName = "Mark Smith",
      contactNumber = "07401111111",
    )
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
    )
    order.installationAndRisk = InstallationAndRisk(
      orderId = order.id,
      riskOfSeriousHarm = "High",
      riskOfSelfHarm = "Low",
      riskDetails = "Danger",
      mappaLevel = "MAAPA 1",
      mappaCaseType = "CPPC (Critical Public Protection Case)",
    )

    order.deviceWearerContactDetails = DeviceWearerContactDetails(
      orderId = order.id,
      contactNumber = "07401111111",
    )
    order.monitoringConditions = MonitoringConditions(orderId = order.id)
    order.responsibleOfficer = ResponsibleOfficer(orderId = order.id)
    order.additionalDocuments = mutableListOf()
    val conditions = MonitoringConditions(
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

    val releaseDay = CurfewReleaseDateConditions(
      orderId = order.id,
      releaseDate = mockStartDate,
      startTime = "19:00",
      endTime = "23:00",
      curfewAddress = AddressType.PRIMARY,
    )
    order.curfewReleaseDateConditions = releaseDay

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

    val alcohol = AlcoholMonitoringConditions(
      orderId = order.id,
      monitoringType = AlcoholMonitoringType.ALCOHOL_ABSTINENCE,
      installationLocation = AlcoholMonitoringInstallationLocationType.PRIMARY,
    )
    order.monitoringConditionsAlcohol = alcohol

    val responsibleOfficer = ResponsibleOfficer(
      orderId = order.id,
      name = "John Smith",
      phoneNumber = "07401111111",
      organisation = "Avon and Somerset Constabulary",
      organisationRegion = "Mock Region",
      organisationPostCode = "AB11 1CD",
      organisationPhoneNumber = "07401111111",
      organisationEmail = "abc@def.com",
      notifyingOrganisation = "Mock Organisation",
    )

    order.responsibleOfficer = responsibleOfficer

    order.monitoringConditions = conditions
    repo.save(order)
    return order
  }
}
