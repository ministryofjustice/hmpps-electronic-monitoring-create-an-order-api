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
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearerContactDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationAndRisk
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.Order
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ResponsibleAdult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.AddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.OrderStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.SercoResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.SercoResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderRepository
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class OrderControllerTest : IntegrationTestBase() {
  @Autowired
  lateinit var repo: OrderRepository

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
    val order = createOrderWithDeviceWearer()

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
    val order = createOrderWithDeviceWearer()
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
    "prison_number": ""
}
      """
    sercoAuthApi.stubGrantToken()

    sercoApi.stupCreateDeviceWearer(mockDeviceWearerJson, HttpStatus.INTERNAL_SERVER_ERROR, SercoResponse())
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
      .isEqualTo("Error creating Serco Device Wearer for order: ${order.id}")
  }

  @Test
  fun `Should update order with serco device wearer id and return 200`() {
    val order = createOrderWithDeviceWearer()
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
    "prison_number": ""
}
      """
    sercoAuthApi.stubGrantToken()

    sercoApi.stupCreateDeviceWearer(mockDeviceWearerJson, HttpStatus.OK, SercoResponse(result = listOf(SercoResult(message = "", id = "MockDeviceWearerId"))))
    webTestClient.post()
      .uri("/api/orders/${order.id}/submit")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isOk

    val updatedOrder = repo.findById(order.id).get()
    assertThat(updatedOrder.fmsDeviceWearerId).isEqualTo("MockDeviceWearerId")
  }

  fun createOrderWithDeviceWearer(): Order {
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
    repo.save(order)
    return order
  }
}
