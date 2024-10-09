package uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.times
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.SercoAuthMockServerExtension.Companion.sercoAuthApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.integration.wiremock.SercoMockApiExtension.Companion.sercoApi
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearerAddress
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.DeviceWearerContactDetails
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.InstallationAndRisk
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.MonitoringConditions
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.OrderForm
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ResponsibleAdult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.DeviceWearerAddressType
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.enums.FormStatus
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.SercoResponse
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.models.fms.SercoResult
import uk.gov.justice.digital.hmpps.hmppselectronicmonitoringcreateanorderapi.repository.OrderFormRepository
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class OrderFormControllerTest : IntegrationTestBase() {
  @Autowired
  lateinit var repo: OrderFormRepository

  @BeforeEach
  fun setup() {
    repo.deleteAll()
  }

  @Test
  fun `Form created and saved in database`() {
    webTestClient.get()
      .uri("/api/CreateForm")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(OrderForm::class.java)

    val forms = repo.findAll()
    assertThat(forms).hasSize(1)
    assertThat(forms[0].username).isEqualTo("AUTH_ADM")
    assertThat(forms[0].status).isEqualTo(FormStatus.IN_PROGRESS)
    assertThat(forms[0].id).isNotNull()
    assertThat(UUID.fromString(forms[0].id.toString())).isEqualTo(forms[0].id)
  }

  @Test
  fun `Only forms belonging to user returned from database`() {
    createOrder("AUTH_ADM")
    createOrder("AUTH_ADM")
    createOrder("AUTH_ADM_2")

    // Verify the database is set up correctly
    val allForms = repo.findAll()
    assertThat(allForms).hasSize(3)

    webTestClient.get()
      .uri("/api/ListForms")
      .headers(setAuthorisation("AUTH_ADM"))
      .exchange()
      .expectStatus()
      .isOk
      .expectBodyList(OrderForm::class.java)
      .hasSize(2)
  }

  @Test
  fun `Should return order if owned by the user`() {
    val order = createOrder()

    webTestClient.get()
      .uri("/api/GetForm?id=${order.id}")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(OrderForm::class.java)
      .isEqualTo(order)
  }

  @Test
  fun `Should return not found if order does not exist`() {
    webTestClient.get()
      .uri("/api/GetForm?id=${UUID.randomUUID()}")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isNotFound()
  }

  @Test
  fun `Should return not found if order belongs to another user`() {
    val order = createOrder("AUTH_ADM")

    webTestClient.get()
      .uri("/api/GetForm?id=${order.id}")
      .headers(setAuthorisation("AUTH_ADM_2"))
      .exchange()
      .expectStatus()
      .isNotFound()
  }

  @Test
  fun `Should return not found if order does not exist when submitting order`() {
    webTestClient.post()
      .uri("/api/SubmitForm/?id=${UUID.randomUUID()}")
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
      .uri("/api/SubmitForm/${order.id}")
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
      .uri("/api/SubmitForm/${order.id}")
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
      .uri("/api/SubmitForm/${order.id}")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isOk

    val updatedOrder = repo.findById(order.id).get()
    assertThat(updatedOrder.fmsDeviceWearerId).isEqualTo("MockDeviceWearerId")
  }

  fun createOrderWithDeviceWearer(): OrderForm {
    val orderForm = OrderForm(
      username = "AUTH_ADM",
      status = FormStatus.IN_PROGRESS,
    )
    orderForm.deviceWearer = DeviceWearer(
      orderId = orderForm.id,
      firstName = "John",
      lastName = "Smith",
      alias = "Johny",
      dateOfBirth = ZonedDateTime.of(1990, 1, 1, 1, 1, 1, 1, ZoneId.systemDefault()),
      adultAtTimeOfInstallation = true,
      sex = "Male",
      gender = "Male",
      disabilities = "Vision,Hearing",
    )

    orderForm.deviceWearerResponsibleAdult = ResponsibleAdult(
      orderId = orderForm.id,
      fullName = "Mark Smith",
      contactNumber = "07401111111",
    )
    orderForm.deviceWearerAddresses = mutableListOf(
      DeviceWearerAddress(
        orderId = orderForm.id,
        addressLine1 = "20 Somewhere Street",
        addressLine2 = "Nowhere City",
        addressLine3 = "Random County",
        addressLine4 = "United Kingdom",
        postcode = "SW11 1NC",
        addressType = DeviceWearerAddressType.PRIMARY,
      ),
      DeviceWearerAddress(
        orderId = orderForm.id,
        addressLine1 = "22 Somewhere Street",
        addressLine2 = "Nowhere City",
        addressLine3 = "Random County",
        addressLine4 = "United Kingdom",
        postcode = "SW11 1NC",
        addressType = DeviceWearerAddressType.PRIMARY,
      ),
    )
    orderForm.installationAndRisk = InstallationAndRisk(
      orderId = orderForm.id,
      riskOfSeriousHarm = "High",
      riskOfSelfHarm = "Low",
      riskDetails = "Danger",
      mappaLevel = "MAAPA 1",
      mappaCaseType = "CPPC (Critical Public Protection Case)",
    )

    orderForm.deviceWearerContactDetails = DeviceWearerContactDetails(
      orderId = orderForm.id,
      contactNumber = "07401111111",
    )
    orderForm.monitoringConditions = MonitoringConditions(orderId = orderForm.id)
    orderForm.responsibleOfficer = ResponsibleOfficer(orderId = orderForm.id)
    orderForm.additionalDocuments = mutableListOf()
    repo.save(orderForm)
    return orderForm
  }
}
